package org.denarius.telii.lock.v2;

import android.content.Context;

import androidx.annotation.NonNull;

import org.denarius.telii.keyvalue.SignalStore;
import org.denarius.telii.util.CensorshipUtil;
import org.denarius.telii.util.FeatureFlags;
import org.denarius.telii.util.TextSecurePreferences;

public final class RegistrationLockUtil {

  private RegistrationLockUtil() {}

  public static boolean userHasRegistrationLock(@NonNull Context context) {
    return TextSecurePreferences.isV1RegistrationLockEnabled(context) || SignalStore.kbsValues().isV2RegistrationLockEnabled();
  }
}
