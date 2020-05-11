package org.denarius.telii.jobs;

import androidx.annotation.NonNull;

import org.denarius.telii.BuildConfig;
import org.denarius.telii.TextSecureExpiredException;
import org.denarius.telii.attachments.Attachment;
import org.denarius.telii.database.AttachmentDatabase;
import org.denarius.telii.database.DatabaseFactory;
import org.denarius.telii.jobmanager.Job;
import org.denarius.telii.logging.Log;
import org.denarius.telii.mms.MediaConstraints;
import org.denarius.telii.transport.UndeliverableMessageException;
import org.denarius.telii.util.Util;

import java.util.List;

public abstract class SendJob extends BaseJob {

  @SuppressWarnings("unused")
  private final static String TAG = SendJob.class.getSimpleName();

  public SendJob(Job.Parameters parameters) {
    super(parameters);
  }

  @Override
  public final void onRun() throws Exception {
    if (Util.getDaysTillBuildExpiry() <= 0) {
      throw new TextSecureExpiredException(String.format("TextSecure expired (build %d, now %d)",
                                                         BuildConfig.BUILD_TIMESTAMP,
                                                         System.currentTimeMillis()));
    }

    Log.i(TAG, "Starting message send attempt");
    onSend();
    Log.i(TAG, "Message send completed");
  }

  protected abstract void onSend() throws Exception;

  protected void markAttachmentsUploaded(long messageId, @NonNull List<Attachment> attachments) {
    AttachmentDatabase database = DatabaseFactory.getAttachmentDatabase(context);

    for (Attachment attachment : attachments) {
      database.markAttachmentUploaded(messageId, attachment);
    }
  }
}
