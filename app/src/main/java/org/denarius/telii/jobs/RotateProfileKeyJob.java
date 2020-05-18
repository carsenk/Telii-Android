package org.denarius.telii.jobs;

import androidx.annotation.NonNull;

import org.signal.zkgroup.profiles.ProfileKey;
import org.denarius.telii.crypto.ProfileKeyUtil;
import org.denarius.telii.database.DatabaseFactory;
import org.denarius.telii.database.RecipientDatabase;
import org.denarius.telii.dependencies.ApplicationDependencies;
import org.denarius.telii.groups.GroupId;
import org.denarius.telii.jobmanager.Data;
import org.denarius.telii.jobmanager.Job;
import org.denarius.telii.jobmanager.impl.NetworkConstraint;
import org.denarius.telii.profiles.AvatarHelper;
import org.denarius.telii.recipients.Recipient;
import org.denarius.telii.util.FeatureFlags;
import org.whispersystems.signalservice.api.SignalServiceAccountManager;
import org.whispersystems.signalservice.api.push.exceptions.PushNetworkException;
import org.whispersystems.signalservice.api.util.StreamDetails;

import java.util.List;

public class RotateProfileKeyJob extends BaseJob {

  public static String KEY = "RotateProfileKeyJob";

  public RotateProfileKeyJob() {
    this(new Job.Parameters.Builder()
                           .setQueue("__ROTATE_PROFILE_KEY__")
                           .addConstraint(NetworkConstraint.KEY)
                           .setMaxAttempts(25)
                           .setMaxInstances(1)
                           .build());
  }

  private RotateProfileKeyJob(@NonNull Job.Parameters parameters) {
    super(parameters);
  }

  @Override
  public @NonNull Data serialize() {
    return Data.EMPTY;
  }

  @Override
  public @NonNull String getFactoryKey() {
    return KEY;
  }

  @Override
  public void onRun() throws Exception {
    SignalServiceAccountManager accountManager    = ApplicationDependencies.getSignalServiceAccountManager();
    RecipientDatabase           recipientDatabase = DatabaseFactory.getRecipientDatabase(context);
    ProfileKey                  profileKey        = ProfileKeyUtil.createNew();
    Recipient                   self              = Recipient.self();

    recipientDatabase.setProfileKey(self.getId(), profileKey);

     try (StreamDetails avatarStream = AvatarHelper.getSelfProfileAvatarStream(context)) {
      if (FeatureFlags.VERSIONED_PROFILES) {
        accountManager.setVersionedProfile(self.getUuid().get(),
                                           profileKey,
                                           Recipient.self().getProfileName().serialize(),
                                           avatarStream);
      } else {
        accountManager.setProfileName(profileKey, Recipient.self().getProfileName().serialize());
        accountManager.setProfileAvatar(profileKey, avatarStream);
      }
    }

    ApplicationDependencies.getJobManager().add(new RefreshAttributesJob());

    updateProfileKeyOnAllV2Groups();
  }

  private void updateProfileKeyOnAllV2Groups() {
    List<GroupId.V2> allGv2Groups = DatabaseFactory.getGroupDatabase(context).getAllGroupV2Ids();

    for (GroupId.V2 groupId : allGv2Groups) {
      ApplicationDependencies.getJobManager().add(new GroupV2UpdateSelfProfileKeyJob(groupId));
    }
  }

  @Override
  public void onFailure() {

  }

  @Override
  protected boolean onShouldRetry(@NonNull Exception exception) {
    return exception instanceof PushNetworkException;
  }

  public static final class Factory implements Job.Factory<RotateProfileKeyJob> {
    @Override
    public @NonNull RotateProfileKeyJob create(@NonNull Parameters parameters, @NonNull Data data) {
      return new RotateProfileKeyJob(parameters);
    }
  }
}
