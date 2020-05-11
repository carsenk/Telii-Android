package org.denarius.telii.megaphone;

import org.denarius.telii.keyvalue.SignalStore;
import org.denarius.telii.util.FeatureFlags;

final class SignalPinReminderSchedule implements MegaphoneSchedule {

  @Override
  public boolean shouldDisplay(int seenCount, long lastSeen, long firstVisible, long currentTime) {
    if (!SignalStore.kbsValues().hasPin()) {
      return false;
    }

    if (!FeatureFlags.pinsForAll()) {
      return false;
    }

    long lastSuccessTime = SignalStore.pinValues().getLastSuccessfulEntryTime();
    long interval        = SignalStore.pinValues().getCurrentInterval();

    return currentTime - lastSuccessTime >= interval;
  }
}
