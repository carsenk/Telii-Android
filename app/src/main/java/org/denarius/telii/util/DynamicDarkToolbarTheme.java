package org.denarius.telii.util;

import androidx.annotation.StyleRes;

import org.denarius.telii.R;

public class DynamicDarkToolbarTheme extends DynamicTheme {

  protected @StyleRes int getLightThemeStyle() {
    return R.style.TextSecure_LightNoActionBar_DarkToolbar;
  }

  protected @StyleRes int getDarkThemeStyle() {
    return R.style.TextSecure_DarkNoActionBar_DarkToolbar;
  }
}
