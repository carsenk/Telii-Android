package org.denarius.telii.database.loaders;

import android.content.Context;
import android.database.Cursor;

import org.denarius.telii.database.DatabaseFactory;
import org.denarius.telii.util.AbstractCursorLoader;

public class BlockedContactsLoader extends AbstractCursorLoader {

  public BlockedContactsLoader(Context context) {
    super(context);
  }

  @Override
  public Cursor getCursor() {
    return DatabaseFactory.getRecipientDatabase(getContext()).getBlocked();
  }

}
