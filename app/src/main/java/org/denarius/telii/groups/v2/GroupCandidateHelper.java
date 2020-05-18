package org.denarius.telii.groups.v2;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.signal.zkgroup.VerificationFailedException;
import org.signal.zkgroup.profiles.ProfileKey;
import org.signal.zkgroup.profiles.ProfileKeyCredential;
import org.denarius.telii.crypto.ProfileKeyUtil;
import org.denarius.telii.database.DatabaseFactory;
import org.denarius.telii.database.RecipientDatabase;
import org.denarius.telii.dependencies.ApplicationDependencies;
import org.denarius.telii.logging.Log;
import org.denarius.telii.recipients.Recipient;
import org.denarius.telii.recipients.RecipientId;
import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.SignalServiceAccountManager;
import org.whispersystems.signalservice.api.groupsv2.GroupCandidate;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class GroupCandidateHelper {
  private final SignalServiceAccountManager signalServiceAccountManager;
  private final RecipientDatabase           recipientDatabase;

  public GroupCandidateHelper(@NonNull Context context) {
    signalServiceAccountManager = ApplicationDependencies.getSignalServiceAccountManager();
    recipientDatabase           = DatabaseFactory.getRecipientDatabase(context);
  }

  private static final String TAG = Log.tag(GroupCandidateHelper.class);

  /**
   * Given a recipient will create a {@link GroupCandidate} which may or may not have a profile key credential.
   * <p>
   * It will try to find missing profile key credentials from the server and persist locally.
   */
  @WorkerThread
  public @NonNull GroupCandidate recipientIdToCandidate(@NonNull RecipientId recipientId)
      throws IOException
  {
    final Recipient recipient = Recipient.resolved(recipientId);

    UUID uuid = recipient.getUuid().orNull();
    if (uuid == null) {
      throw new AssertionError("Non UUID members should have need detected by now");
    }

    Optional<ProfileKeyCredential> profileKeyCredential = ProfileKeyUtil.profileKeyCredentialOptional(recipient.getProfileKeyCredential());
    GroupCandidate                 candidate            = new GroupCandidate(uuid, profileKeyCredential);

    if (!candidate.hasProfileKeyCredential()) {
      ProfileKey profileKey = ProfileKeyUtil.profileKeyOrNull(recipient.getProfileKey());

      if (profileKey != null) {
        try {
          Optional<ProfileKeyCredential> profileKeyCredentialOptional = signalServiceAccountManager.resolveProfileKeyCredential(uuid, profileKey);

          if (profileKeyCredentialOptional.isPresent()) {
            candidate = candidate.withProfileKeyCredential(profileKeyCredentialOptional.get());

            recipientDatabase.setProfileKeyCredential(recipient.getId(), profileKey, profileKeyCredentialOptional.get());
          }
        } catch (VerificationFailedException e) {
          Log.w(TAG, e);
          throw new IOException(e);
        }
      }
    }

    return candidate;
  }

  @WorkerThread
  public @NonNull Set<GroupCandidate> recipientIdsToCandidates(@NonNull Collection<RecipientId> recipientIds)
      throws IOException
  {
    Set<GroupCandidate> result = new HashSet<>(recipientIds.size());

    for (RecipientId recipientId : recipientIds) {
      result.add(recipientIdToCandidate(recipientId));
    }

    return result;
  }
}
