package org.denarius.telii.keyvalue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.denarius.telii.logging.Log;
import org.denarius.telii.util.Base64;
import org.denarius.telii.util.JsonUtils;
import org.whispersystems.signalservice.api.KbsPinData;
import org.whispersystems.signalservice.api.kbs.MasterKey;
import org.whispersystems.signalservice.internal.contacts.entities.TokenResponse;

import java.io.IOException;
import java.security.SecureRandom;

public final class KbsValues {

  public  static final String V2_LOCK_ENABLED              = "kbs.v2_lock_enabled";
  private static final String MASTER_KEY                   = "kbs.registration_lock_master_key";
  private static final String TOKEN_RESPONSE               = "kbs.token_response";
  private static final String LOCK_LOCAL_PIN_HASH          = "kbs.registration_lock_local_pin_hash";
  private static final String LAST_CREATE_FAILED_TIMESTAMP = "kbs.last_create_failed_timestamp";

  private final KeyValueStore store;

  KbsValues(KeyValueStore store) {
    this.store = store;
  }

  /**
   * Deliberately does not clear the {@link #MASTER_KEY}.
   *
   * Should only be called by {@link org.denarius.telii.pin.PinState}
   */
  public void clearRegistrationLockAndPin() {
    store.beginWrite()
         .remove(V2_LOCK_ENABLED)
         .remove(TOKEN_RESPONSE)
         .remove(LOCK_LOCAL_PIN_HASH)
         .remove(LAST_CREATE_FAILED_TIMESTAMP)
         .commit();
  }

  /** Should only be set by {@link org.denarius.telii.pin.PinState}. */
  public synchronized void setKbsMasterKey(@NonNull KbsPinData pinData, @NonNull String localPinHash) {
    MasterKey masterKey     = pinData.getMasterKey();
    String    tokenResponse;
    try {
      tokenResponse = JsonUtils.toJson(pinData.getTokenResponse());
    } catch (IOException e) {
      throw new AssertionError(e);
    }

    store.beginWrite()
         .putString(TOKEN_RESPONSE, tokenResponse)
         .putBlob(MASTER_KEY, masterKey.serialize())
         .putString(LOCK_LOCAL_PIN_HASH, localPinHash)
         .putLong(LAST_CREATE_FAILED_TIMESTAMP, -1)
         .commit();
  }

  /** Should only be set by {@link org.denarius.telii.pin.PinState}. */
  public synchronized void setV2RegistrationLockEnabled(boolean enabled) {
    store.beginWrite().putBoolean(V2_LOCK_ENABLED, enabled).apply();
  }

  /**
   * Whether or not registration lock V2 is enabled.
   */
  public synchronized boolean isV2RegistrationLockEnabled() {
    return store.getBoolean(V2_LOCK_ENABLED, false);
  }

  /** Should only be set by {@link org.denarius.telii.pin.PinState}. */
  public synchronized void onPinCreateFailure() {
    store.beginWrite().putLong(LAST_CREATE_FAILED_TIMESTAMP, System.currentTimeMillis()).apply();
  }

  /**
   * Whether or not the last time the user attempted to create a PIN, it failed.
   */
  public synchronized boolean lastPinCreateFailed() {
    return store.getLong(LAST_CREATE_FAILED_TIMESTAMP, -1) > 0;
  }

  /**
   * Finds or creates the master key. Therefore this will always return a master key whether backed
   * up or not.
   * <p>
   * If you only want a key when it's backed up, use {@link #getPinBackedMasterKey()}.
   */
  public synchronized @NonNull MasterKey getOrCreateMasterKey() {
    byte[] blob = store.getBlob(MASTER_KEY, null);

    if (blob == null) {
      store.beginWrite()
           .putBlob(MASTER_KEY, MasterKey.createNew(new SecureRandom()).serialize())
           .commit();
      blob = store.getBlob(MASTER_KEY, null);
    }

    return new MasterKey(blob);
  }

  /**
   * Returns null if master key is not backed up by a pin.
   */
  public synchronized @Nullable MasterKey getPinBackedMasterKey() {
    if (!isV2RegistrationLockEnabled()) return null;
    return getMasterKey();
  }

  private synchronized @Nullable MasterKey getMasterKey() {
    byte[] blob = store.getBlob(MASTER_KEY, null);
    return blob != null ? new MasterKey(blob) : null;
  }

  public @Nullable String getRegistrationLockToken() {
    MasterKey masterKey = getPinBackedMasterKey();
    if (masterKey == null) {
      return null;
    } else {
      return masterKey.deriveRegistrationLock();
    }
  }

  public synchronized @Nullable String getLocalPinHash() {
    return store.getString(LOCK_LOCAL_PIN_HASH, null);
  }

  public synchronized boolean hasPin() {
    return getLocalPinHash() != null;
  }

  public synchronized @Nullable TokenResponse getRegistrationLockTokenResponse() {
    String token = store.getString(TOKEN_RESPONSE, null);

    if (token == null) return null;

    try {
      return JsonUtils.fromJson(token, TokenResponse.class);
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }
}
