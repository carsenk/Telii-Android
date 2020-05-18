package org.denarius.telii.recipients.ui.bottomsheet;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import org.denarius.telii.database.DatabaseFactory;
import org.denarius.telii.database.IdentityDatabase;
import org.denarius.telii.groups.GroupChangeBusyException;
import org.denarius.telii.groups.GroupChangeFailedException;
import org.denarius.telii.groups.GroupId;
import org.denarius.telii.groups.GroupInsufficientRightsException;
import org.denarius.telii.groups.GroupManager;
import org.denarius.telii.groups.GroupNotAMemberException;
import org.denarius.telii.groups.ui.GroupChangeErrorCallback;
import org.denarius.telii.groups.ui.GroupChangeFailureReason;
import org.denarius.telii.logging.Log;
import org.denarius.telii.recipients.Recipient;
import org.denarius.telii.recipients.RecipientId;
import org.denarius.telii.util.concurrent.SignalExecutors;
import org.denarius.telii.util.concurrent.SimpleTask;

import java.io.IOException;
import java.util.Objects;

final class RecipientDialogRepository {

  private static final String TAG = Log.tag(RecipientDialogRepository.class);

  @NonNull  private final Context     context;
  @NonNull  private final RecipientId recipientId;
  @Nullable private final GroupId     groupId;

  RecipientDialogRepository(@NonNull Context context,
                            @NonNull RecipientId recipientId,
                            @Nullable GroupId groupId)
  {
    this.context     = context;
    this.recipientId = recipientId;
    this.groupId     = groupId;
  }

  @NonNull RecipientId getRecipientId() {
    return recipientId;
  }

  @Nullable GroupId getGroupId() {
    return groupId;
  }

  void getIdentity(@NonNull IdentityCallback callback) {
    SimpleTask.run(SignalExecutors.BOUNDED,
                   () -> DatabaseFactory.getIdentityDatabase(context)
                                        .getIdentity(recipientId)
                                        .orNull(),
                   callback::remoteIdentity);
  }

  void getRecipient(@NonNull RecipientCallback recipientCallback) {
    SimpleTask.run(SignalExecutors.BOUNDED,
                   () -> Recipient.resolved(recipientId),
                   recipientCallback::onRecipient);
  }

  void getGroupName(@NonNull Consumer<String> stringConsumer) {
    SimpleTask.run(SignalExecutors.BOUNDED,
                   () -> DatabaseFactory.getGroupDatabase(context).requireGroup(Objects.requireNonNull(groupId)).getTitle(),
                   stringConsumer::accept);
  }

  void removeMember(@NonNull Consumer<Boolean> onComplete, @NonNull GroupChangeErrorCallback error) {
    SimpleTask.run(SignalExecutors.UNBOUNDED,
                   () -> {
                     try {
                       GroupManager.ejectFromGroup(context, Objects.requireNonNull(groupId).requireV2(), Recipient.resolved(recipientId));
                       return true;
                     } catch (GroupInsufficientRightsException | GroupNotAMemberException e) {
                       Log.w(TAG, e);
                       error.onError(GroupChangeFailureReason.NO_RIGHTS);
                     } catch (GroupChangeFailedException | GroupChangeBusyException | IOException e) {
                       Log.w(TAG, e);
                       error.onError(GroupChangeFailureReason.OTHER);
                     }
                     return false;
                   },
                   onComplete::accept);
  }

  void setMemberAdmin(boolean admin, @NonNull Consumer<Boolean> onComplete, @NonNull GroupChangeErrorCallback error) {
    SimpleTask.run(SignalExecutors.UNBOUNDED,
                   () -> {
                     try {
                       GroupManager.setMemberAdmin(context, Objects.requireNonNull(groupId).requireV2(), recipientId, admin);
                       return true;
                     } catch (GroupInsufficientRightsException | GroupNotAMemberException e) {
                       Log.w(TAG, e);
                       error.onError(GroupChangeFailureReason.NO_RIGHTS);
                     } catch (GroupChangeFailedException | GroupChangeBusyException | IOException e) {
                       Log.w(TAG, e);
                       error.onError(GroupChangeFailureReason.OTHER);
                     }
                     return false;
                   },
                   onComplete::accept);
  }

  interface IdentityCallback {
    void remoteIdentity(@Nullable IdentityDatabase.IdentityRecord identityRecord);
  }

  interface RecipientCallback {
    void onRecipient(@NonNull Recipient recipient);
  }
}
