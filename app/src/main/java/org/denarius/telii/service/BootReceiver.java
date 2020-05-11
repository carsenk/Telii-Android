package org.denarius.telii.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.denarius.telii.ApplicationContext;
import org.denarius.telii.dependencies.ApplicationDependencies;
import org.denarius.telii.jobs.PushNotificationReceiveJob;

public class BootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    ApplicationDependencies.getJobManager().add(new PushNotificationReceiveJob(context));
  }
}
