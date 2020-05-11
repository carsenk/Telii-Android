package org.denarius.telii.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import android.graphics.Color;
import android.util.AttributeSet;

import org.denarius.telii.R;
import org.denarius.telii.mms.GlideRequest;
import org.denarius.telii.mms.GlideRequests;
import org.denarius.telii.util.ThemeUtil;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class OutlinedThumbnailView extends ThumbnailView {

  private CornerMask cornerMask;
  private Outliner   outliner;

  public OutlinedThumbnailView(Context context) {
    super(context);
    init(null);
  }

  public OutlinedThumbnailView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  private void init(@Nullable AttributeSet attrs) {
    cornerMask = new CornerMask(this);
    outliner   = new Outliner();

    outliner.setColor(ThemeUtil.getThemedColor(getContext(), R.attr.conversation_item_image_outline_color));

    int radius = 0;

    if (attrs != null) {
      TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.OutlinedThumbnailView, 0, 0);
      radius = typedArray.getDimensionPixelOffset(R.styleable.OutlinedThumbnailView_otv_cornerRadius, 0);
    }

    setRadius(radius);
    setCorners(radius, radius, radius, radius);

    setWillNotDraw(false);
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);

    cornerMask.mask(canvas);
    outliner.draw(canvas);
  }

  public void setCorners(int topLeft, int topRight, int bottomRight, int bottomLeft) {
    cornerMask.setRadii(topLeft, topRight, bottomRight, bottomLeft);
    outliner.setRadii(topLeft, topRight, bottomRight, bottomLeft);
    postInvalidate();
  }
}
