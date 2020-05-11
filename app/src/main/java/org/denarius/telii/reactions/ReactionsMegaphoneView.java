package org.denarius.telii.reactions;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.denarius.telii.R;
import org.denarius.telii.megaphone.Megaphone;
import org.denarius.telii.megaphone.MegaphoneActionController;

public class ReactionsMegaphoneView extends FrameLayout {

  private View closeButton;

  public ReactionsMegaphoneView(Context context) {
    super(context);
    initialize(context);
  }

  public ReactionsMegaphoneView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initialize(context);
  }

  private void initialize(@NonNull Context context) {
    inflate(context, R.layout.reactions_megaphone, this);

    this.closeButton = findViewById(R.id.reactions_megaphone_x);
  }

  public void present(@NonNull Megaphone megaphone, @NonNull MegaphoneActionController listener) {
    this.closeButton.setOnClickListener(v -> listener.onMegaphoneCompleted(megaphone.getEvent()));
  }
}
