package org.denarius.telii.logsubmit;

import android.content.Context;

import androidx.annotation.NonNull;

import org.denarius.telii.dependencies.ApplicationDependencies;

import java.util.List;

public class LogSectionJobs implements LogSection {

  @Override
  public @NonNull String getTitle() {
    return "JOBS";
  }

  @Override
  public @NonNull CharSequence getContent(@NonNull Context context) {
    return ApplicationDependencies.getJobManager().getDebugInfo();
  }
}
