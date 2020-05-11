package org.denarius.telii.mms;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.load.engine.cache.DiskCache;
import com.bumptech.glide.load.engine.cache.DiskCacheAdapter;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.UnitModelLoader;
import com.bumptech.glide.load.resource.bitmap.Downsampler;
import com.bumptech.glide.load.resource.bitmap.StreamBitmapDecoder;
import com.bumptech.glide.load.resource.gif.ByteBufferGifDecoder;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.load.resource.gif.StreamGifDecoder;
import com.bumptech.glide.module.AppGlideModule;

import org.denarius.telii.blurhash.BlurHash;
import org.denarius.telii.blurhash.BlurHashModelLoader;
import org.denarius.telii.blurhash.BlurHashResourceDecoder;
import org.denarius.telii.contacts.avatars.ContactPhoto;
import org.denarius.telii.crypto.AttachmentSecret;
import org.denarius.telii.crypto.AttachmentSecretProvider;
import org.denarius.telii.giph.model.ChunkedImageUrl;
import org.denarius.telii.glide.ChunkedImageUrlLoader;
import org.denarius.telii.glide.ContactPhotoLoader;
import org.denarius.telii.glide.OkHttpUrlLoader;
import org.denarius.telii.glide.cache.EncryptedBitmapCacheDecoder;
import org.denarius.telii.glide.cache.EncryptedBitmapResourceEncoder;
import org.denarius.telii.glide.cache.EncryptedCacheEncoder;
import org.denarius.telii.glide.cache.EncryptedGifCacheDecoder;
import org.denarius.telii.glide.cache.EncryptedGifDrawableResourceEncoder;
import org.denarius.telii.mms.AttachmentStreamUriLoader.AttachmentModel;
import org.denarius.telii.mms.DecryptableStreamUriLoader.DecryptableUri;
import org.denarius.telii.stickers.StickerRemoteUri;
import org.denarius.telii.stickers.StickerRemoteUriLoader;

import java.io.File;
import java.io.InputStream;

@GlideModule
public class SignalGlideModule extends AppGlideModule {

  @Override
  public boolean isManifestParsingEnabled() {
    return false;
  }

  @Override
  public void applyOptions(Context context, GlideBuilder builder) {
    builder.setLogLevel(Log.ERROR);
//    builder.setDiskCache(new NoopDiskCacheFactory());
  }

  @Override
  public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
    AttachmentSecret attachmentSecret = AttachmentSecretProvider.getInstance(context).getOrCreateAttachmentSecret();
    byte[]           secret           = attachmentSecret.getModernKey();

    registry.prepend(File.class, File.class, UnitModelLoader.Factory.getInstance());
    registry.prepend(InputStream.class, new EncryptedCacheEncoder(secret, glide.getArrayPool()));
    registry.prepend(File.class, Bitmap.class, new EncryptedBitmapCacheDecoder(secret, new StreamBitmapDecoder(new Downsampler(registry.getImageHeaderParsers(), context.getResources().getDisplayMetrics(), glide.getBitmapPool(), glide.getArrayPool()), glide.getArrayPool())));
    registry.prepend(File.class, GifDrawable.class, new EncryptedGifCacheDecoder(secret, new StreamGifDecoder(registry.getImageHeaderParsers(), new ByteBufferGifDecoder(context, registry.getImageHeaderParsers(), glide.getBitmapPool(), glide.getArrayPool()), glide.getArrayPool())));

    registry.prepend(BlurHash.class, Bitmap.class, new BlurHashResourceDecoder());

    registry.prepend(Bitmap.class, new EncryptedBitmapResourceEncoder(secret));
    registry.prepend(GifDrawable.class, new EncryptedGifDrawableResourceEncoder(secret));

    registry.append(ContactPhoto.class, InputStream.class, new ContactPhotoLoader.Factory(context));
    registry.append(DecryptableUri.class, InputStream.class, new DecryptableStreamUriLoader.Factory(context));
    registry.append(AttachmentModel.class, InputStream.class, new AttachmentStreamUriLoader.Factory());
    registry.append(ChunkedImageUrl.class, InputStream.class, new ChunkedImageUrlLoader.Factory());
    registry.append(StickerRemoteUri.class, InputStream.class, new StickerRemoteUriLoader.Factory());
    registry.append(BlurHash.class, BlurHash.class, new BlurHashModelLoader.Factory());
    registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory());
  }

  public static class NoopDiskCacheFactory implements DiskCache.Factory {
    @Override
    public DiskCache build() {
      return new DiskCacheAdapter();
    }
  }
}
