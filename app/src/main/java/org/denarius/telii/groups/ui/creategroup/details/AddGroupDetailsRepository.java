package org.denarius.telii.groups.ui.creategroup.details;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import com.annimon.stream.Stream;

import org.denarius.telii.groups.GroupChangeBusyException;
import org.denarius.telii.groups.GroupChangeFailedException;
import org.denarius.telii.groups.GroupManager;
import org.denarius.telii.groups.ui.GroupMemberEntry;
import org.denarius.telii.recipients.Recipient;
import org.denarius.telii.recipients.RecipientId;
import org.denarius.telii.util.concurrent.SignalExecutors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class AddGroupDetailsRepository {

  private final Context context;

  AddGroupDetailsRepository(@NonNull Context context) {
    this.context = context;
  }

  void resolveMembers(@NonNull RecipientId[] recipientIds, Consumer<List<GroupMemberEntry.NewGroupCandidate>> consumer) {
    SignalExecutors.BOUNDED.execute(() -> {
      List<GroupMemberEntry.NewGroupCandidate> members = new ArrayList<>(recipientIds.length);

      for (RecipientId id : recipientIds) {
        members.add(new GroupMemberEntry.NewGroupCandidate(Recipient.resolved(id)));
      }

      consumer.accept(members);
    });
  }

  void createPushGroup(@NonNull  Set<RecipientId>  members,
                       @Nullable byte[]            avatar,
                       @Nullable String            name,
                       boolean                     mms,
                       Consumer<GroupCreateResult> resultConsumer)
  {
    SignalExecutors.BOUNDED.execute(() -> {
      Set<Recipient> recipients = new HashSet<>(Stream.of(members).map(Recipient::resolved).toList());

      try {
        GroupManager.GroupActionResult result = GroupManager.createGroup(context, recipients, avatar, name, mms);

        resultConsumer.accept(GroupCreateResult.success(result));
      } catch (GroupChangeBusyException e) {
        resultConsumer.accept(GroupCreateResult.error(GroupCreateResult.Error.Type.ERROR_BUSY));
      } catch (GroupChangeFailedException e) {
        resultConsumer.accept(GroupCreateResult.error(GroupCreateResult.Error.Type.ERROR_FAILED));
      } catch (IOException e) {
        resultConsumer.accept(GroupCreateResult.error(GroupCreateResult.Error.Type.ERROR_IO));
      }
    });
  }
}
