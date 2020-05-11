package org.denarius.telii.jobs;

import android.content.Context;

import androidx.annotation.NonNull;

import org.signal.zkgroup.profiles.ProfileKey;
import org.denarius.telii.crypto.ProfileKeyUtil;
import org.denarius.telii.database.DatabaseFactory;
import org.denarius.telii.dependencies.ApplicationDependencies;
import org.denarius.telii.jobmanager.Data;
import org.denarius.telii.jobmanager.Job;
import org.denarius.telii.jobmanager.impl.NetworkConstraint;
import org.denarius.telii.profiles.AvatarHelper;
import org.denarius.telii.profiles.ProfileName;
import org.denarius.telii.recipients.Recipient;
import org.denarius.telii.util.FeatureFlags;
import org.denarius.telii.util.ProfileUtil;
import org.denarius.telii.util.TextSecurePreferences;
import org.whispersystems.signalservice.api.SignalServiceAccountManager;
import org.whispersystems.signalservice.api.profiles.ProfileAndCredential;
import org.whispersystems.signalservice.api.profiles.SignalServiceProfile;
import org.whispersystems.signalservice.api.util.StreamDetails;

public final class ProfileUploadJob extends BaseJob {

  public static final String KEY = "ProfileUploadJob";

  private final Context                     context;
  private final SignalServiceAccountManager accountManager;

  public ProfileUploadJob() {
    this(new Job.Parameters.Builder()
                            .addConstraint(NetworkConstraint.KEY)
                            .setQueue(KEY)
                            .setLifespan(Parameters.IMMORTAL)
                            .setMaxAttempts(Parameters.UNLIMITED)
                            .setMaxInstances(1)
                            .build());
  }

  private ProfileUploadJob(@NonNull Parameters parameters) {
    super(parameters);

    this.context        = ApplicationDependencies.getApplication();
    this.accountManager = ApplicationDependencies.getSignalServiceAccountManager();
  }

  @Override
  protected void onRun() throws Exception {
    ProfileKey  profileKey  = ProfileKeyUtil.getSelfProfileKey();
    ProfileName profileName = Recipient.self().getProfileName();
    String      avatarPath  = null;

    try (StreamDetails avatar = AvatarHelper.getSelfProfileAvatarStream(context)) {
      if (FeatureFlags.VERSIONED_PROFILES) {
        avatarPath = accountManager.setVersionedProfile(Recipient.self().getUuid().get(), profileKey, profileName.serialize(), avatar).orNull();
      } else {
        accountManager.setProfileName(profileKey, profileName.serialize());
        avatarPath = accountManager.setProfileAvatar(profileKey, avatar).orNull();
      }
    }

    DatabaseFactory.getRecipientDatabase(context).setProfileAvatar(Recipient.self().getId(), avatarPath);
  }

  @Override
  protected boolean onShouldRetry(@NonNull Exception e) {
    return true;
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
  public void onFailure() {
  }

  public static class Factory implements Job.Factory {

    @NonNull
    @Override
    public Job create(@NonNull Parameters parameters, @NonNull Data data) {
      return new ProfileUploadJob(parameters);
    }
  }
}
