package org.denarius.telii.components.webrtc;

import android.media.AudioManager;

import org.denarius.telii.dependencies.ApplicationDependencies;
import org.denarius.telii.util.ServiceUtil;

class WebRtcCallRepository {

  private final AudioManager audioManager;

  WebRtcCallRepository() {
    this.audioManager = ServiceUtil.getAudioManager(ApplicationDependencies.getApplication());
  }

  WebRtcAudioOutput getAudioOutput() {
    if (audioManager.isBluetoothScoOn()) {
      return WebRtcAudioOutput.HEADSET;
    } else if (audioManager.isSpeakerphoneOn()) {
      return WebRtcAudioOutput.SPEAKER;
    } else {
      return WebRtcAudioOutput.HANDSET;
    }
  }
}
