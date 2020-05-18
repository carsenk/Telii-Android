package org.denarius.telii.jobs;

import androidx.annotation.NonNull;

import org.denarius.telii.database.DatabaseFactory;
import org.denarius.telii.database.GroupDatabase;
import org.denarius.telii.groups.GroupChangeBusyException;
import org.denarius.telii.groups.GroupId;
import org.denarius.telii.groups.GroupManager;
import org.denarius.telii.groups.GroupNotAMemberException;
import org.denarius.telii.groups.v2.processing.GroupsV2StateProcessor;
import org.denarius.telii.jobmanager.Data;
import org.denarius.telii.jobmanager.Job;
import org.denarius.telii.jobmanager.impl.NetworkConstraint;
import org.denarius.telii.logging.Log;
import org.denarius.telii.util.FeatureFlags;
import org.whispersystems.libsignal.util.guava.Optional;
import org.whispersystems.signalservice.api.groupsv2.NoCredentialForRedemptionTimeException;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class RequestGroupV2InfoJob extends BaseJob {

  public static final String KEY = "RequestGroupV2InfoJob";

  @SuppressWarnings("unused")
  private static final String TAG = Log.tag(RequestGroupV2InfoJob.class);

  private static final String KEY_GROUP_ID    = "group_id";
  private static final String KEY_TO_REVISION = "to_revision";

  private final GroupId.V2 groupId;
  private final int        toRevision;

  public RequestGroupV2InfoJob(@NonNull GroupId.V2 groupId, int toRevision) {
    this(new Parameters.Builder()
                       .setQueue("RequestGroupV2InfoJob::" + groupId)
                       .addConstraint(NetworkConstraint.KEY)
                       .setLifespan(TimeUnit.DAYS.toMillis(1))
                       .setMaxAttempts(Parameters.UNLIMITED)
                       .build(),
         groupId,
         toRevision);
  }

  /**
   * Get latest group state for group.
   */
  public RequestGroupV2InfoJob(@NonNull GroupId.V2 groupId) {
    this(groupId, GroupsV2StateProcessor.LATEST);
  }

  private RequestGroupV2InfoJob(@NonNull Parameters parameters, @NonNull GroupId.V2 groupId, int toRevision) {
    super(parameters);

    this.groupId    = groupId;
    this.toRevision = toRevision;
  }

  @Override
  public @NonNull Data serialize() {
    return new Data.Builder().putString(KEY_GROUP_ID, groupId.toString())
                             .putInt(KEY_TO_REVISION, toRevision)
                             .build();
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void onRun() throws IOException, GroupNotAMemberException, GroupChangeBusyException {
    if (!FeatureFlags.groupsV2()) {
      Log.w(TAG, "Group update skipped due to feature flag " + groupId);
      return;
    }

    Log.i(TAG, "Updating group to revision " + toRevision);

    Optional<GroupDatabase.GroupRecord> group = DatabaseFactory.getGroupDatabase(context).getGroup(groupId);

    if (!group.isPresent()) {
      Log.w(TAG, "Group not found");
      return;
    }

    GroupManager.updateGroupFromServer(context, group.get().requireV2GroupProperties().getGroupMasterKey(), toRevision, System.currentTimeMillis());
  }

  @Override
  public boolean onShouldRetry(@NonNull Exception e) {
    return e instanceof PushNetworkException ||
           e instanceof NoCredentialForRedemptionTimeException ||
           e instanceof GroupChangeBusyException;
  }

  @Override
  public void onFailure() {
  }

  public static final class Factory implements Job.Factory<RequestGroupV2InfoJob> {

    @Override
    public @NonNull RequestGroupV2InfoJob create(@NonNull Parameters parameters, @NonNull Data data) {
      return new RequestGroupV2InfoJob(parameters,
                                       GroupId.parseOrThrow(data.getString(KEY_GROUP_ID)).requireV2(),
                                       data.getInt(KEY_TO_REVISION));
    }
  }
}
