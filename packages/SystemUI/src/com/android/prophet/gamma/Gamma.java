/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.prophet.gamma;

import android.database.ContentObserver;
import android.os.UserHandle;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.provider.Settings;
import android.provider.Settings.Global;
import android.text.InputFilter;
import android.util.Log;
import android.util.Slog;
import android.util.SparseBooleanArray;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.View.OnLongClickListener;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.systemui.R;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class Gamma {

      private final Context mContext;
      private SettingsObserver settingsObserver;
      private final Handler mHandler = new Handler();

      @Inject
      public Gamma (Context context) {
          mContext = context;
          settingsObserver = new SettingsObserver(mHandler);
          settingsObserver.observe();
      }

      private class SettingsObserver extends ContentObserver {
          SettingsObserver(Handler handler) {
              super(handler);
          }

          protected void observe() {
              update();
          }

          @Override
          public void onChange(boolean selfChange) {
              update();
          }

          protected void update() {
          }
      }

      public void setVisOrGone (View v, boolean vis) {
         v.setVisibility(vis ? View.VISIBLE : View.GONE);
      }

      public void setTextFontFromVarible(TextView tv, String var) {
          final String variable  =  var;
          TextView mText = tv;
          int intVariable = Settings.System.getIntForUser(mContext.getContentResolver(),
                  variable, 28, UserHandle.USER_CURRENT);

          switch (intVariable) {
              case 0:
                  mText.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                  break;
              case 1:
                  mText.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                  break;
              case 2:
                  mText.setTypeface(Typeface.create("sans-serif", Typeface.ITALIC));
                  break;
              case 3:
                  mText.setTypeface(Typeface.create("sans-serif", Typeface.BOLD_ITALIC));
                  break;
              case 4:
                  mText.setTypeface(Typeface.create("sans-serif-light", Typeface.ITALIC));
                  break;
              case 5:
                  mText.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                  break;
              case 6:
                  mText.setTypeface(Typeface.create("sans-serif-thin", Typeface.ITALIC));
                  break;
              case 7:
                  mText.setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
                  break;
              case 8:
                  mText.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
                  break;
              case 9:
                  mText.setTypeface(Typeface.create("sans-serif-condensed", Typeface.ITALIC));
                  break;
              case 10:
                  mText.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
                  break;
              case 11:
                  mText.setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD_ITALIC));
                  break;
              case 12:
                  mText.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                  break;
              case 13:
                  mText.setTypeface(Typeface.create("sans-serif-medium", Typeface.ITALIC));
                  break;
              case 14:
                  mText.setTypeface(Typeface.create("sans-serif-condensed-light", Typeface.NORMAL));
                  break;
              case 15:
                  mText.setTypeface(Typeface.create("sans-serif-condensed-light", Typeface.ITALIC));
                  break;
              case 16:
                  mText.setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
                  break;
              case 17:
                  mText.setTypeface(Typeface.create("sans-serif-black", Typeface.ITALIC));
                  break;
              case 18:
                  mText.setTypeface(Typeface.create("cursive", Typeface.NORMAL));
                  break;
              case 19:
                  mText.setTypeface(Typeface.create("cursive", Typeface.BOLD));
                  break;
              case 20:
                  mText.setTypeface(Typeface.create("casual", Typeface.NORMAL));
                  break;
              case 21:
                  mText.setTypeface(Typeface.create("serif", Typeface.NORMAL));
                  break;
              case 22:
                  mText.setTypeface(Typeface.create("serif", Typeface.ITALIC));
                  break;
              case 23:
                  mText.setTypeface(Typeface.create("serif", Typeface.BOLD));
                  break;
              case 24:
                  mText.setTypeface(Typeface.create("serif", Typeface.BOLD_ITALIC));
                  break;
              case 25:
                  mText.setTypeface(Typeface.create("gobold-light-sys", Typeface.NORMAL));
                  break;
              case 26:
                  mText.setTypeface(Typeface.create("roadrage-sys", Typeface.NORMAL));
                  break;
              case 27:
                  mText.setTypeface(Typeface.create("snowstorm-sys", Typeface.NORMAL));
                  break;
              case 28:
                  mText.setTypeface(Typeface.create("googlesans-sys", Typeface.NORMAL));
                  break;
              case 29:
                  mText.setTypeface(Typeface.create("neoneon-sys", Typeface.NORMAL));
                  break;
              case 30:
                  mText.setTypeface(Typeface.create("themeable-sys", Typeface.NORMAL));
                  break;
              case 31:
                  mText.setTypeface(Typeface.create("samsung-sys", Typeface.NORMAL));
                  break;
              case 32:
                  mText.setTypeface(Typeface.create("mexcellent-sys", Typeface.NORMAL));
                  break;
              case 33:
                  mText.setTypeface(Typeface.create("burnstown-sys", Typeface.NORMAL));
                  break;
              case 34:
                  mText.setTypeface(Typeface.create("dumbledor-sys", Typeface.NORMAL));
                  break;
              case 35:
                  mText.setTypeface(Typeface.create("phantombold-sys", Typeface.NORMAL));
                  break;
              case 36:
                  mText.setTypeface(Typeface.create("sourcesanspro-sys", Typeface.NORMAL));
                  break;
              case 37:
                  mText.setTypeface(Typeface.create("circularstd-sys", Typeface.NORMAL));
                  break;
              case 38:
                  mText.setTypeface(Typeface.create("oneplusslate-sys", Typeface.NORMAL));
                  break;
              case 39:
                  mText.setTypeface(Typeface.create("aclonica-sys", Typeface.NORMAL));
                  break;
              case 40:
                  mText.setTypeface(Typeface.create("amarante-sys", Typeface.NORMAL));
                  break;
              case 41:
                  mText.setTypeface(Typeface.create("bariol-sys", Typeface.NORMAL));
                  break;
              case 42:
                  mText.setTypeface(Typeface.create("cagliostro-sys", Typeface.NORMAL));
                  break;
              case 43:
                  mText.setTypeface(Typeface.create("coolstory-sys", Typeface.NORMAL));
                  break;
              case 44:
                  mText.setTypeface(Typeface.create("lgsmartgothic-sys", Typeface.NORMAL));
                  break;
              case 45:
                  mText.setTypeface(Typeface.create("rosemary-sys", Typeface.NORMAL));
                  break;
              case 46:
                  mText.setTypeface(Typeface.create("sonysketch-sys", Typeface.NORMAL));
                  break;
              case 47:
                  mText.setTypeface(Typeface.create("surfer-sys", Typeface.NORMAL));
                  break;
              case 48:
                  mText.setTypeface(Typeface.createFromFile(getCustomFontFile()));
                  break;
              default:
                  mText.setTypeface(Typeface.create("googlesans-sys", Typeface.NORMAL));
                  break;
          }
      }

      public File getCustomFontFile() {
          String font = Settings.System.getStringForUser(mContext.getContentResolver(), Settings.System.SYNTHOS_CUSTOM_FONT, UserHandle.USER_CURRENT);
          File file = new File(mContext.getFilesDir(), font);
          return file;
      }

}
