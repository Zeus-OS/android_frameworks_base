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

package com.android.systemui.synth.gamma;

import android.annotation.ColorInt;
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
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.net.Uri;
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

import com.android.internal.util.aosip.aosipUtils;
import com.android.settingslib.Utils;
import com.android.systemui.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
              default:
                  mText.setTypeface(Typeface.create("googlesans-sys", Typeface.NORMAL));
                  break;
          }
      }

      public void setTextColorTypeFromVariable(TextView tv, String var, String varColor) {
          TextView mText = tv;
          final String variable = var;
          final String variableColor = varColor;
          int intVariable = Settings.System.getIntForUser(mContext.getContentResolver(), variable, 2, UserHandle.USER_CURRENT);
          @ColorInt int textColor = Utils.getColorAttrDefaultColor(mContext,R.attr.wallpaperTextColor);
          @ColorInt int customColor = (variableColor == null ? 0xFF3980FF : Settings.System.getIntForUser(mContext.getContentResolver(), variableColor, 0xFF3980FF, UserHandle.USER_CURRENT));

          switch (intVariable) {
             case 0:
                mText.getPaint().setShader(null);
                mText.setTextColor(textColor);
                break;
             case 1:
                mText.getPaint().setShader(null);
                mText.setTextColor(Utils.getColorAccentDefaultColor(mContext));
                break;
             case 2:
                mText.getPaint().setShader(null);
                mText.getPaint().setShader(new LinearGradient(0, mText.getHeight(), mText.getWidth(), 0,
                                          mContext.getResources().getColor(com.android.internal.R.color.gradient_start),
                                          mContext.getResources().getColor(com.android.internal.R.color.gradient_end),
                                          Shader.TileMode.REPEAT));
                break;
             case 3:
                mText.getPaint().setShader(null);
                mText.setTextColor(customColor);
                break;
          }
      }

      public void setTextSizeFromVariable(TextView tv, String var) {
          final String variable = var;
          TextView mText = tv;
          int intVariable = Settings.System.getIntForUser(mContext.getContentResolver(),
                 variable, 55, UserHandle.USER_CURRENT);

          switch (intVariable) {
              case 0:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_0));
              case 1:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_1));
              case 2:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_2));
              case 3:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_3));
              case 4:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_4));
              case 5:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_5));
              case 6:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_6));
              case 7:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_7));
              case 8:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_8));
              case 9:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_9));
              case 10:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_10));
              case 11:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_11));
              case 12:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_12));
              case 13:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_13));
              case 14:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_14));
              case 15:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_15));
              case 16:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_16));
              case 17:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_17));
              case 18:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_18));
              case 19:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_19));
              case 20:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_20));
              case 21:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_21));
              case 22:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_22));
              case 23:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_23));
              case 24:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_24));
              case 25:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_25));
              case 26:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_26));
              case 27:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_27));
              case 28:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_28));
              case 29:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_29));
              case 30:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_30));
              case 31:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_31));
              case 32:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_32));
              case 33:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_33));
              case 34:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_34));
              case 35:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_35));
              case 36:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_36));
              case 37:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_37));
              case 38:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_38));
              case 39:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_39));
              case 40:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_40));
              case 41:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_41));
              case 42:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_42));
              case 43:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_43));
              case 44:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_44));
              case 45:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_45));
              case 46:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_46));
              case 47:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_47));
              case 48:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_48));
              case 49:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_49));
              case 50:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_50));
              case 51:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_51));
              case 52:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_52));
              case 53:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_53));
              case 54:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_54));
              case 55:
              default:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_55));
              case 56:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_56));
              case 57:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_57));
              case 58:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_58));
              case 59:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_59));
              case 60:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_60));
              case 61:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_61));
              case 62:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_62));
              case 63:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_63));
              case 64:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_64));
              case 65:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_65));
              case 66:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_66));
              case 67:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_67));
              case 68:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_68));
              case 69:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_69));
              case 70:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_70));
              case 71:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_71));
              case 72:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_72));
              case 73:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_73));
              case 74:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_74));
              case 75:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_75));
              case 76:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_76));
              case 77:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_77));
              case 78:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_78));
              case 79:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_79));
              case 80:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_80));
              case 81:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_81));
              case 82:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_82));
              case 83:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_83));
              case 84:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_84));
              case 85:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_85));
              case 86:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_86));
              case 87:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_87));
              case 88:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_88));
              case 89:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_89));
              case 90:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_90));
              case 91:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_91));
              case 92:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_92));
              case 93:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_93));
              case 94:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_94));
              case 95:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_95));
              case 96:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_96));
              case 97:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_97));
              case 98:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_98));
              case 99:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_99));
              case 100:
                  mText.setTextSize((int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_100));
          }
      }
}
