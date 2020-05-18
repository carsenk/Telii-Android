package org.denarius.telii.groups.ui;

import androidx.annotation.NonNull;

import org.denarius.telii.recipients.Recipient;

public interface RecipientClickListener {
  void onClick(@NonNull Recipient recipient);
}
