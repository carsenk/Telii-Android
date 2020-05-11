package org.denarius.telii.jobmanager.workmanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.denarius.telii.jobs.AttachmentDownloadJob;
import org.denarius.telii.jobs.AttachmentUploadJob;
import org.denarius.telii.jobs.AvatarGroupsV1DownloadJob;
import org.denarius.telii.jobs.CleanPreKeysJob;
import org.denarius.telii.jobs.CreateSignedPreKeyJob;
import org.denarius.telii.jobs.DirectoryRefreshJob;
import org.denarius.telii.jobs.FailingJob;
import org.denarius.telii.jobs.FcmRefreshJob;
import org.denarius.telii.jobs.LocalBackupJob;
import org.denarius.telii.jobs.MmsDownloadJob;
import org.denarius.telii.jobs.MmsReceiveJob;
import org.denarius.telii.jobs.MmsSendJob;
import org.denarius.telii.jobs.MultiDeviceBlockedUpdateJob;
import org.denarius.telii.jobs.MultiDeviceConfigurationUpdateJob;
import org.denarius.telii.jobs.MultiDeviceContactUpdateJob;
import org.denarius.telii.jobs.MultiDeviceGroupUpdateJob;
import org.denarius.telii.jobs.MultiDeviceProfileKeyUpdateJob;
import org.denarius.telii.jobs.MultiDeviceReadUpdateJob;
import org.denarius.telii.jobs.MultiDeviceVerifiedUpdateJob;
import org.denarius.telii.jobs.PushDecryptMessageJob;
import org.denarius.telii.jobs.PushGroupSendJob;
import org.denarius.telii.jobs.PushGroupUpdateJob;
import org.denarius.telii.jobs.PushMediaSendJob;
import org.denarius.telii.jobs.PushNotificationReceiveJob;
import org.denarius.telii.jobs.PushTextSendJob;
import org.denarius.telii.jobs.RefreshAttributesJob;
import org.denarius.telii.jobs.RefreshPreKeysJob;
import org.denarius.telii.jobs.RequestGroupInfoJob;
import org.denarius.telii.jobs.RetrieveProfileAvatarJob;
import org.denarius.telii.jobs.RetrieveProfileJob;
import org.denarius.telii.jobs.RotateCertificateJob;
import org.denarius.telii.jobs.RotateProfileKeyJob;
import org.denarius.telii.jobs.RotateSignedPreKeyJob;
import org.denarius.telii.jobs.SendDeliveryReceiptJob;
import org.denarius.telii.jobs.SendReadReceiptJob;
import org.denarius.telii.jobs.ServiceOutageDetectionJob;
import org.denarius.telii.jobs.SmsReceiveJob;
import org.denarius.telii.jobs.SmsSendJob;
import org.denarius.telii.jobs.SmsSentJob;
import org.denarius.telii.jobs.TrimThreadJob;
import org.denarius.telii.jobs.TypingSendJob;
import org.denarius.telii.jobs.UpdateApkJob;

import java.util.HashMap;
import java.util.Map;

public class WorkManagerFactoryMappings {

  private static final Map<String, String> FACTORY_MAP = new HashMap<String, String>() {{
    put("AttachmentDownloadJob", AttachmentDownloadJob.KEY);
    put("AttachmentUploadJob", AttachmentUploadJob.KEY);
    put("AvatarDownloadJob", AvatarGroupsV1DownloadJob.KEY);
    put("CleanPreKeysJob", CleanPreKeysJob.KEY);
    put("CreateSignedPreKeyJob", CreateSignedPreKeyJob.KEY);
    put("DirectoryRefreshJob", DirectoryRefreshJob.KEY);
    put("FcmRefreshJob", FcmRefreshJob.KEY);
    put("LocalBackupJob", LocalBackupJob.KEY);
    put("MmsDownloadJob", MmsDownloadJob.KEY);
    put("MmsReceiveJob", MmsReceiveJob.KEY);
    put("MmsSendJob", MmsSendJob.KEY);
    put("MultiDeviceBlockedUpdateJob", MultiDeviceBlockedUpdateJob.KEY);
    put("MultiDeviceConfigurationUpdateJob", MultiDeviceConfigurationUpdateJob.KEY);
    put("MultiDeviceContactUpdateJob", MultiDeviceContactUpdateJob.KEY);
    put("MultiDeviceGroupUpdateJob", MultiDeviceGroupUpdateJob.KEY);
    put("MultiDeviceProfileKeyUpdateJob", MultiDeviceProfileKeyUpdateJob.KEY);
    put("MultiDeviceReadUpdateJob", MultiDeviceReadUpdateJob.KEY);
    put("MultiDeviceVerifiedUpdateJob", MultiDeviceVerifiedUpdateJob.KEY);
    put("PushContentReceiveJob", FailingJob.KEY);
    put("PushDecryptJob", PushDecryptMessageJob.KEY);
    put("PushGroupSendJob", PushGroupSendJob.KEY);
    put("PushGroupUpdateJob", PushGroupUpdateJob.KEY);
    put("PushMediaSendJob", PushMediaSendJob.KEY);
    put("PushNotificationReceiveJob", PushNotificationReceiveJob.KEY);
    put("PushTextSendJob", PushTextSendJob.KEY);
    put("RefreshAttributesJob", RefreshAttributesJob.KEY);
    put("RefreshPreKeysJob", RefreshPreKeysJob.KEY);
    put("RefreshUnidentifiedDeliveryAbilityJob", FailingJob.KEY);
    put("RequestGroupInfoJob", RequestGroupInfoJob.KEY);
    put("RetrieveProfileAvatarJob", RetrieveProfileAvatarJob.KEY);
    put("RetrieveProfileJob", RetrieveProfileJob.KEY);
    put("RotateCertificateJob", RotateCertificateJob.KEY);
    put("RotateProfileKeyJob", RotateProfileKeyJob.KEY);
    put("RotateSignedPreKeyJob", RotateSignedPreKeyJob.KEY);
    put("SendDeliveryReceiptJob", SendDeliveryReceiptJob.KEY);
    put("SendReadReceiptJob", SendReadReceiptJob.KEY);
    put("ServiceOutageDetectionJob", ServiceOutageDetectionJob.KEY);
    put("SmsReceiveJob", SmsReceiveJob.KEY);
    put("SmsSendJob", SmsSendJob.KEY);
    put("SmsSentJob", SmsSentJob.KEY);
    put("TrimThreadJob", TrimThreadJob.KEY);
    put("TypingSendJob", TypingSendJob.KEY);
    put("UpdateApkJob", UpdateApkJob.KEY);
  }};

  public static @Nullable String getFactoryKey(@NonNull String workManagerClass) {
    return FACTORY_MAP.get(workManagerClass);
  }
}
