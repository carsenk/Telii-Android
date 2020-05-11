package org.denarius.telii.util;

import androidx.annotation.StyleRes;

import org.denarius.telii.R;

public class DynamicRegistrationTheme extends DynamicTheme {

  protected @StyleRes int getLightThemeStyle() {
    return R.style.TextSecure_LightRegistrationTheme;
  }

  protected @StyleRes int getDarkThemeStyle() {
    return R.style.TextSecure_DarkRegistrationTheme;
  }
}
