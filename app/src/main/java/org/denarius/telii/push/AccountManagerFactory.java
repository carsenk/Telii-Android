package org.denarius.telii.push;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.denarius.telii.logging.Log;

import com.google.android.gms.security.ProviderInstaller;

import org.denarius.telii.BuildConfig;
import org.denarius.telii.util.TextSecurePreferences;
import org.denarius.telii.util.concurrent.SignalExecutors;
import org.whispersystems.signalservice.api.SignalServiceAccountManager;

import java.util.UUID;

public class AccountManagerFactory {

  private static final String TAG = AccountManagerFactory.class.getSimpleName();

  public static @NonNull SignalServiceAccountManager createAuthenticated(@NonNull Context context,
                                                                         @NonNull UUID uuid,
                                                                         @NonNull String number,
                                                                         @NonNull String password)
  {
    if (new SignalServiceNetworkAccess(context).isCensored(number)) {
      SignalExecutors.BOUNDED.execute(() -> {
        try {
          ProviderInstaller.installIfNeeded(context);
        } catch (Throwable t) {
          Log.w(TAG, t);
        }
      });
    }

    return new SignalServiceAccountManager(new SignalServiceNetworkAccess(context).getConfiguration(number),
                                           uuid, number, password, BuildConfig.SIGNAL_AGENT);
  }

  /**
   * Should only be used during registration when you haven't yet been assigned a UUID.
   */
  public static @NonNull SignalServiceAccountManager createUnauthenticated(@NonNull Context context,
                                                                           @NonNull String number,
                                                                           @NonNull String password)
  {
    if (new SignalServiceNetworkAccess(context).isCensored(number)) {
      SignalExecutors.BOUNDED.execute(() -> {
        try {
          ProviderInstaller.installIfNeeded(context);
        } catch (Throwable t) {
          Log.w(TAG, t);
        }
      });
    }

    return new SignalServiceAccountManager(new SignalServiceNetworkAccess(context).getConfiguration(number),
                                           null, number, password, BuildConfig.SIGNAL_AGENT);
  }

}
