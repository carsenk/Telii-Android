package org.denarius.telii.webrtc;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;

import org.denarius.telii.WebRtcCallActivity;
import org.denarius.telii.recipients.Recipient;
import org.denarius.telii.ringrtc.RemotePeer;
import org.denarius.telii.service.WebRtcCallService;
import org.denarius.telii.util.concurrent.SimpleTask;
import org.whispersystems.signalservice.api.messages.calls.OfferMessage;

public class VoiceCallShare extends Activity {
  
  private static final String TAG = VoiceCallShare.class.getSimpleName();
  
  @Override
  public void onCreate(Bundle icicle) {
    super.onCreate(icicle);

    if (getIntent().getData() != null && "content".equals(getIntent().getData().getScheme())) {
      Cursor cursor = null;
      
      try {
        cursor = getContentResolver().query(getIntent().getData(), null, null, null, null);

        if (cursor != null && cursor.moveToNext()) {
          String destination = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.RawContacts.Data.DATA1));

          SimpleTask.run(() -> Recipient.external(this, destination), recipient -> {
            if (!TextUtils.isEmpty(destination)) {
              Intent serviceIntent = new Intent(this, WebRtcCallService.class);
              serviceIntent.setAction(WebRtcCallService.ACTION_OUTGOING_CALL)
                           .putExtra(WebRtcCallService.EXTRA_REMOTE_PEER, new RemotePeer(recipient.getId()))
                           .putExtra(WebRtcCallService.EXTRA_OFFER_TYPE, OfferMessage.Type.AUDIO_CALL.getCode());
              startService(serviceIntent);

              Intent activityIntent = new Intent(this, WebRtcCallActivity.class);
              activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
              startActivity(activityIntent);
            }
          });
        }
      } finally {
        if (cursor != null) cursor.close();
      }
    }
    
    finish();
  }
}
