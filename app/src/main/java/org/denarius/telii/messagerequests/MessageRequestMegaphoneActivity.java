package org.denarius.telii.messagerequests;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.airbnb.lottie.LottieAnimationView;

import org.denarius.telii.PassphraseRequiredActionBarActivity;
import org.denarius.telii.R;
import org.denarius.telii.dependencies.ApplicationDependencies;
import org.denarius.telii.megaphone.Megaphones;
import org.denarius.telii.profiles.ProfileName;
import org.denarius.telii.profiles.edit.EditProfileActivity;
import org.denarius.telii.recipients.Recipient;
import org.denarius.telii.util.DynamicNoActionBarTheme;
import org.denarius.telii.util.DynamicTheme;
import org.denarius.telii.util.TextSecurePreferences;

public class MessageRequestMegaphoneActivity extends PassphraseRequiredActionBarActivity {

  public static final short EDIT_PROFILE_REQUEST_CODE = 24563;

  private DynamicTheme dynamicTheme = new DynamicNoActionBarTheme();

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState, boolean isReady) {
    dynamicTheme.onCreate(this);

    setContentView(R.layout.message_requests_megaphone_activity);


    LottieAnimationView lottie            = findViewById(R.id.message_requests_lottie);
    TextView            profileNameButton = findViewById(R.id.message_requests_confirm_profile_name);

    lottie.setAnimation(R.raw.lottie_message_requests_splash);
    lottie.playAnimation();

    profileNameButton.setOnClickListener(v -> {
      final Intent profile = new Intent(this, EditProfileActivity.class);

      profile.putExtra(EditProfileActivity.SHOW_TOOLBAR, false);
      profile.putExtra(EditProfileActivity.NEXT_BUTTON_TEXT, R.string.save);

      startActivityForResult(profile, EDIT_PROFILE_REQUEST_CODE);
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    if (requestCode == EDIT_PROFILE_REQUEST_CODE &&
        resultCode == RESULT_OK                  &&
        Recipient.self().getProfileName() != ProfileName.EMPTY) {
      ApplicationDependencies.getMegaphoneRepository().markFinished(Megaphones.Event.MESSAGE_REQUESTS);
      setResult(RESULT_OK);
      finish();
    }
  }

  @Override
  public void onBackPressed() {
  }

  @Override
  protected void onResume() {
    super.onResume();

    dynamicTheme.onResume(this);
  }
}
