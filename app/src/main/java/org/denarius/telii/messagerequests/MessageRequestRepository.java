package org.denarius.telii.messagerequests;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import org.signal.storageservice.protos.groups.local.DecryptedGroup;
import org.denarius.telii.database.DatabaseFactory;
import org.denarius.telii.database.GroupDatabase;
import org.denarius.telii.database.MessagingDatabase;
import org.denarius.telii.database.RecipientDatabase;
import org.denarius.telii.database.ThreadDatabase;
import org.denarius.telii.dependencies.ApplicationDependencies;
import org.denarius.telii.groups.GroupChangeBusyException;
import org.denarius.telii.groups.GroupChangeFailedException;
import org.denarius.telii.groups.GroupInsufficientRightsException;
import org.denarius.telii.groups.GroupManager;
import org.denarius.telii.groups.GroupNotAMemberException;
import org.denarius.telii.groups.ui.GroupChangeErrorCallback;
import org.denarius.telii.groups.ui.GroupChangeFailureReason;
import org.denarius.telii.jobs.MultiDeviceMessageRequestResponseJob;
import org.denarius.telii.logging.Log;
import org.denarius.telii.notifications.MarkReadReceiver;
import org.denarius.telii.notifications.MessageNotifier;
import org.denarius.telii.recipients.LiveRecipient;
import org.denarius.telii.recipients.Recipient;
import org.denarius.telii.recipients.RecipientId;
import org.denarius.telii.recipients.RecipientUtil;
import org.denarius.telii.sms.MessageSender;
import org.denarius.telii.util.TextSecurePreferences;
import org.denarius.telii.util.Util;
import org.denarius.telii.util.concurrent.SignalExecutors;
import org.whispersystems.libsignal.util.guava.Optional;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Executor;

final class MessageRequestRepository {

  private static final String TAG = Log.tag(MessageRequestRepository.class);

  private final Context  context;
  private final Executor executor;

  MessageRequestRepository(@NonNull Context context) {
    this.context  = context.getApplicationContext();
    this.executor = SignalExecutors.BOUNDED;
  }

  void getGroups(@NonNull RecipientId recipientId, @NonNull Consumer<List<String>> onGroupsLoaded) {
    executor.execute(() -> {
      GroupDatabase groupDatabase = DatabaseFactory.getGroupDatabase(context);
      onGroupsLoaded.accept(groupDatabase.getPushGroupNamesContainingMember(recipientId));
    });
  }

  void getMemberCount(@NonNull RecipientId recipientId, @NonNull Consumer<GroupMemberCount> onMemberCountLoaded) {
    executor.execute(() -> {
      GroupDatabase groupDatabase = DatabaseFactory.getGroupDatabase(context);
      Optional<GroupDatabase.GroupRecord> groupRecord = groupDatabase.getGroup(recipientId);
      onMemberCountLoaded.accept(groupRecord.transform(record -> {
        if (record.isV2Group()) {
          DecryptedGroup decryptedGroup = record.requireV2GroupProperties().getDecryptedGroup();
          return new GroupMemberCount(decryptedGroup.getMembersCount(), decryptedGroup.getPendingMembersCount());
        } else {
          return new GroupMemberCount(record.getMembers().size(), 0);
        }
      }).or(GroupMemberCount.ZERO));
    });
  }

  void getMessageRequestState(@NonNull Recipient recipient, long threadId, @NonNull Consumer<MessageRequestState> state) {
    executor.execute(() -> {
      if (recipient.isPushV2Group()) {
        boolean pendingMember = DatabaseFactory.getGroupDatabase(context)
                                               .isPendingMember(recipient.requireGroupId().requireV2(), Recipient.self());
        state.accept(pendingMember ? MessageRequestState.UNACCEPTED
                                   : MessageRequestState.ACCEPTED);
      } else if (!RecipientUtil.isMessageRequestAccepted(context, threadId)) {
        state.accept(MessageRequestState.UNACCEPTED);
      } else if (RecipientUtil.isPreMessageRequestThread(context, threadId) && !RecipientUtil.isLegacyProfileSharingAccepted(recipient)) {
        state.accept(MessageRequestState.LEGACY);
      } else {
        state.accept(MessageRequestState.ACCEPTED);
      }
    });
  }

  void acceptMessageRequest(@NonNull LiveRecipient liveRecipient,
                            long threadId,
                            @NonNull Runnable onMessageRequestAccepted,
                            @NonNull GroupChangeErrorCallback mainThreadError)
  {
    GroupChangeErrorCallback error = e -> Util.runOnMain(() -> mainThreadError.onError(e));
    executor.execute(()-> {
      if (liveRecipient.get().isPushV2Group()) {
        try {
          Log.i(TAG, "GV2 accepting invite");
          GroupManager.acceptInvite(context, liveRecipient.get().requireGroupId().requireV2());

          RecipientDatabase recipientDatabase = DatabaseFactory.getRecipientDatabase(context);
          recipientDatabase.setProfileSharing(liveRecipient.getId(), true);

          onMessageRequestAccepted.run();
        } catch (GroupInsufficientRightsException e) {
          Log.w(TAG, e);
          error.onError(GroupChangeFailureReason.NO_RIGHTS);
        } catch (GroupChangeBusyException | GroupChangeFailedException | GroupNotAMemberException | IOException e) {
          Log.w(TAG, e);
          error.onError(GroupChangeFailureReason.OTHER);
        }
      } else {
        RecipientDatabase recipientDatabase = DatabaseFactory.getRecipientDatabase(context);
        recipientDatabase.setProfileSharing(liveRecipient.getId(), true);

        MessageSender.sendProfileKey(context, threadId);

        List<MessagingDatabase.MarkedMessageInfo> messageIds = DatabaseFactory.getThreadDatabase(context)
                                                                              .setEntireThreadRead(threadId);
        MessageNotifier.updateNotification(context);
        MarkReadReceiver.process(context, messageIds);

        if (TextSecurePreferences.isMultiDevice(context)) {
          ApplicationDependencies.getJobManager().add(MultiDeviceMessageRequestResponseJob.forAccept(liveRecipient.getId()));
        }

        onMessageRequestAccepted.run();
      }
    });
  }

  void deleteMessageRequest(@NonNull LiveRecipient recipient, long threadId, @NonNull Runnable onMessageRequestDeleted) {
    executor.execute(() -> {
      ThreadDatabase threadDatabase = DatabaseFactory.getThreadDatabase(context);
      threadDatabase.deleteConversation(threadId);

      if (recipient.resolve().isGroup()) {
        RecipientUtil.leaveGroup(context, recipient.get());
      }

      if (TextSecurePreferences.isMultiDevice(context)) {
        ApplicationDependencies.getJobManager().add(MultiDeviceMessageRequestResponseJob.forDelete(recipient.getId()));
      }

      onMessageRequestDeleted.run();
    });
  }

  void blockMessageRequest(@NonNull LiveRecipient liveRecipient, @NonNull Runnable onMessageRequestBlocked) {
    executor.execute(() -> {
      Recipient recipient = liveRecipient.resolve();
      RecipientUtil.block(context, recipient);
      liveRecipient.refresh();

      if (TextSecurePreferences.isMultiDevice(context)) {
        ApplicationDependencies.getJobManager().add(MultiDeviceMessageRequestResponseJob.forBlock(liveRecipient.getId()));
      }

      onMessageRequestBlocked.run();
    });
  }

  void blockAndDeleteMessageRequest(@NonNull LiveRecipient liveRecipient, long threadId, @NonNull Runnable onMessageRequestBlocked) {
    executor.execute(() -> {
      Recipient recipient = liveRecipient.resolve();
      RecipientUtil.block(context, recipient);
      liveRecipient.refresh();

      DatabaseFactory.getThreadDatabase(context).deleteConversation(threadId);

      if (TextSecurePreferences.isMultiDevice(context)) {
        ApplicationDependencies.getJobManager().add(MultiDeviceMessageRequestResponseJob.forBlockAndDelete(liveRecipient.getId()));
      }

      onMessageRequestBlocked.run();
    });
  }

  void unblockAndAccept(@NonNull LiveRecipient liveRecipient, long threadId, @NonNull Runnable onMessageRequestUnblocked) {
    executor.execute(() -> {
      Recipient         recipient         = liveRecipient.resolve();
      RecipientDatabase recipientDatabase = DatabaseFactory.getRecipientDatabase(context);

      RecipientUtil.unblock(context, recipient);
      recipientDatabase.setProfileSharing(liveRecipient.getId(), true);
      liveRecipient.refresh();

      List<MessagingDatabase.MarkedMessageInfo> messageIds = DatabaseFactory.getThreadDatabase(context)
                                                                            .setEntireThreadRead(threadId);
      MessageNotifier.updateNotification(context);
      MarkReadReceiver.process(context, messageIds);

      if (TextSecurePreferences.isMultiDevice(context)) {
        ApplicationDependencies.getJobManager().add(MultiDeviceMessageRequestResponseJob.forAccept(liveRecipient.getId()));
      }

      onMessageRequestUnblocked.run();
    });
  }

  enum MessageRequestState {
    ACCEPTED, UNACCEPTED, LEGACY
  }
}
