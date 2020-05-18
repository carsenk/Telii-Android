package org.denarius.telii.groups.ui;

import androidx.annotation.NonNull;

import org.denarius.telii.recipients.Recipient;

public interface RecipientLongClickListener {
  boolean onLongClick(@NonNull Recipient recipient);
}
