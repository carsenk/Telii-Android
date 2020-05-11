package org.denarius.telii.revealable;

import android.content.Context;

import androidx.annotation.NonNull;

import org.denarius.telii.database.DatabaseFactory;
import org.denarius.telii.database.MmsDatabase;
import org.denarius.telii.database.model.MmsMessageRecord;
import org.denarius.telii.logging.Log;
import org.denarius.telii.util.concurrent.SignalExecutors;
import org.whispersystems.libsignal.util.guava.Optional;

class ViewOnceMessageRepository {

  private static final String TAG = Log.tag(ViewOnceMessageRepository.class);

  private final MmsDatabase mmsDatabase;

  ViewOnceMessageRepository(@NonNull Context context) {
    this.mmsDatabase = DatabaseFactory.getMmsDatabase(context);
  }

  void getMessage(long messageId, @NonNull Callback<Optional<MmsMessageRecord>> callback) {
    SignalExecutors.BOUNDED.execute(() -> {
      try (MmsDatabase.Reader reader = mmsDatabase.readerFor(mmsDatabase.getMessage(messageId))) {
        MmsMessageRecord record = (MmsMessageRecord) reader.getNext();
        callback.onComplete(Optional.fromNullable(record));
      }
    });
  }

  interface Callback<T> {
    void onComplete(T result);
  }
}
