package org.denarius.telii.jobmanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.denarius.telii.logging.Log;

public class BootReceiver extends BroadcastReceiver {

  private static final String TAG = BootReceiver.class.getSimpleName();

  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i(TAG, "Boot received. Application is created, kickstarting JobManager.");
  }
}
