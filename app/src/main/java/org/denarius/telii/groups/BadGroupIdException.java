package org.denarius.telii.groups;

import androidx.annotation.NonNull;

public final class BadGroupIdException extends Exception {

  public BadGroupIdException() {
    super();
  }

  BadGroupIdException(@NonNull String message) {
    super(message);
  }

  BadGroupIdException(@NonNull Exception e) {
    super(e);
  }
}
