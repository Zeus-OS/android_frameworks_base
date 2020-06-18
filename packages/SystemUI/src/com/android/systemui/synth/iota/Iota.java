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

package com.android.systemui.synth.iota;

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
import android.graphics.PixelFormat;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.synth.gamma.SynthMusic;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

public class Iota {

      public interface ThemeUpdater {
          void updateVolumeThemes();
      }

      private boolean mHideRinger;
      private boolean mHideExtended;
      private int mVolumePanelStyle;
      private int mVolumeAlignment;
      private final Context mContext;
      private ArrayList<ThemeUpdater> mObjects;

      private SettingsObserver settingsObserver;
      private final Handler mHandler = new Handler();

      private class SettingsObserver extends ContentObserver {
          SettingsObserver(Handler handler) {
              super(handler);
          }

          protected void observe() {
              mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.SYNTHOS_HIDE_RINGER_VOLUMEPANEL), false, this, UserHandle.USER_ALL);
              mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.SYNTHOS_HIDE_EXTENDED_VOLUMEPANEL), false, this, UserHandle.USER_ALL);
              mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.SYNTHOS_MUSIC_VOLUME_PANEL_TEXT), false, this, UserHandle.USER_ALL);
              mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.SYNTHOS_VOLUME_PANEL_THEME), false, this, UserHandle.USER_ALL);
              mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.SYNTHOS_VOLUME_PANEL_PADDING_TOP), false, this, UserHandle.USER_ALL);
              mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.SYNTHOS_VOLUME_PANEL_PADDING_BOTTOM), false, this, UserHandle.USER_ALL);
              mContext.getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.VOLUME_PANEL_ALIGNMENT), false, this, UserHandle.USER_ALL);
              update();
          }

          @Override
          public void onChange(boolean selfChange) {
              update();
          }

          protected void update() {
               mHideRinger = Settings.System.getIntForUser(mContext.getContentResolver(), Settings.System.SYNTHOS_HIDE_RINGER_VOLUMEPANEL, 1, UserHandle.USER_CURRENT) == 1;
               mHideExtended = Settings.System.getIntForUser(mContext.getContentResolver(), Settings.System.SYNTHOS_HIDE_EXTENDED_VOLUMEPANEL, 0, UserHandle.USER_CURRENT) == 1;
               mVolumePanelStyle = Settings.System.getIntForUser(mContext.getContentResolver(), Settings.System.SYNTHOS_VOLUME_PANEL_THEME, 0, UserHandle.USER_CURRENT);
               mVolumeAlignment = Settings.System.getIntForUser(mContext.getContentResolver(), Settings.System.VOLUME_PANEL_ALIGNMENT, 1, UserHandle.USER_CURRENT);
               notifyObservers();
          }
      }

      @Inject
      public Iota (Context context) {
          mContext = context;
          mObjects = new ArrayList<>();
          settingsObserver = new SettingsObserver(mHandler);
          settingsObserver.observe();
      }

      public void registerObserver(ThemeUpdater updater) {
          if(!mObjects.contains(updater)) {
              mObjects.add(updater);
          }
      }

      public void removeObserver(ThemeUpdater updater) {
          if(mObjects.contains(updater)) {
              mObjects.remove(updater);
          }
      }

      public void notifyObservers() {
          for (ThemeUpdater updater : mObjects) {
              updater.updateVolumeThemes();
          }
      }

      public int getVolumeTheme() {
          mVolumePanelStyle = Settings.System.getIntForUser(mContext.getContentResolver(), Settings.System.SYNTHOS_VOLUME_PANEL_THEME, 0, UserHandle.USER_CURRENT);
          return mVolumePanelStyle;
      }

      public int getDimensionFromVariable(String var) {
          final String variable  =  var;
          int intVariable = Settings.System.getIntForUser(mContext.getContentResolver(),
                  variable, 20, UserHandle.USER_CURRENT);

          switch (intVariable) {
              case 0:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_0);
              case 1:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_1);
              case 2:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_2);
              case 3:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_3);
              case 4:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_4);
              case 5:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_5);
              case 6:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_6);
              case 7:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_7);
              case 8:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_8);
              case 9:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_9);
              case 10:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_10);
              case 11:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_11);
              case 12:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_12);
              case 13:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_13);
              case 14:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_14);
              case 15:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_15);
              case 16:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_16);
              case 17:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_17);
              case 18:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_18);
              case 19:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_19);
              case 20:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_20);
              case 21:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_21);
              case 22:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_22);
              case 23:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_23);
              case 24:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_24);
              case 25:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_25);
              case 26:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_26);
              case 27:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_27);
              case 28:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_28);
              case 29:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_29);
              case 30:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_30);
              case 31:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_31);
              case 32:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_32);
              case 33:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_33);
              case 34:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_34);
              case 35:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_35);
              case 36:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_36);
              case 37:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_37);
              case 38:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_38);
              case 39:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_39);
              case 40:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_40);
              case 41:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_41);
              case 42:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_42);
              case 43:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_43);
              case 44:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_44);
              case 45:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_45);
              case 46:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_46);
              case 47:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_47);
              case 48:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_48);
              case 49:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_49);
              case 50:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_50);
              case 51:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_51);
              case 52:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_52);
              case 53:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_53);
              case 54:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_54);
              case 55:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_55);
              case 56:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_56);
              case 57:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_57);
              case 58:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_58);
              case 59:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_59);
              case 60:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_60);
              case 61:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_61);
              case 62:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_62);
              case 63:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_63);
              case 64:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_64);
              case 65:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_65);
              case 66:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_66);
              case 67:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_67);
              case 68:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_68);
              case 69:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_69);
              case 70:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_70);
              case 71:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_71);
              case 72:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_72);
              case 73:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_73);
              case 74:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_74);
              case 75:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_75);
              case 76:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_76);
              case 77:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_77);
              case 78:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_78);
              case 79:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_79);
              case 80:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_80);
              case 81:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_81);
              case 82:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_82);
              case 83:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_83);
              case 84:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_84);
              case 85:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_85);
              case 86:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_86);
              case 87:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_87);
              case 88:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_88);
              case 89:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_89);
              case 90:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_90);
              case 91:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_91);
              case 92:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_92);
              case 93:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_93);
              case 94:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_94);
              case 95:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_95);
              case 96:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_96);
              case 97:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_97);
              case 98:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_98);
              case 99:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_99);
              case 100:
                  return (int) mContext.getResources().getDimension(R.dimen.volume_panel_padding_100);
          }

          return 0;
      }

      public void setPaddingLocation(Window window, SynthMusic music, ViewGroup dialog) {

        boolean mLeftVolumeRocker = mContext.getResources().getBoolean(R.bool.config_audioPanelOnLeftSide);

        if (window != null) {
            Window mWindow = window;
            WindowManager.LayoutParams lp = mWindow.getAttributes();
            switch (mVolumeAlignment) {
                case 0:
                    lp.gravity = (mLeftVolumeRocker ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP;
                    break;
                case 1:
                default:
                    lp.gravity = (mLeftVolumeRocker ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL;
                    break;
                case 2:
                    lp.gravity = (mLeftVolumeRocker ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM;
                    break;
            }
            mWindow.setAttributes(lp);
        }

        if (music != null) {
            SynthMusic mSynthMusic = music;
            LinearLayout.LayoutParams mlp = (LinearLayout.LayoutParams) mSynthMusic.getLayoutParams();
            switch (mVolumeAlignment) {
                case 0:
                    mlp.gravity = Gravity.TOP;
                    break;
                case 1:
                default:
                    mlp.gravity = Gravity.CENTER_VERTICAL;
                    break;
                case 2:
                    mlp.gravity = Gravity.BOTTOM;
                    break;
            }
            mlp.setMargins(0,
                          (getDimensionFromVariable(Settings.System.SYNTHOS_VOLUME_PANEL_PADDING_TOP) * 3),
                          0,
                          (getDimensionFromVariable(Settings.System.SYNTHOS_VOLUME_PANEL_PADDING_BOTTOM) * 3));
            mSynthMusic.setLayoutParams(mlp);
        }


        if (dialog != null) {
            ViewGroup mDialogView = dialog;
            LinearLayout.LayoutParams dlp = (LinearLayout.LayoutParams) mDialogView.getLayoutParams();
            switch (mVolumeAlignment) {
                case 0:
                    dlp.gravity = Gravity.TOP;
                    break;
                case 1:
                default:
                    dlp.gravity = Gravity.CENTER_VERTICAL;
                    break;
                case 2:
                    dlp.gravity = Gravity.BOTTOM;
                    break;
            }
            dlp.setMargins(0,
                          (getDimensionFromVariable(Settings.System.SYNTHOS_VOLUME_PANEL_PADDING_TOP) * 3),
                          0,
                          (getDimensionFromVariable(Settings.System.SYNTHOS_VOLUME_PANEL_PADDING_BOTTOM) * 3));
            mDialogView.setLayoutParams(dlp);
        }

      }

      public void hideThings(View mRinger, View mExpandRowsView, View mBackgroundThings) {

        if (mRinger != null && mHideRinger){
            mRinger.setVisibility(View.GONE);
        } else if (mRinger != null && !mHideRinger) {
            mRinger.setVisibility(View.VISIBLE);
        }

        if (mExpandRowsView != null && mHideExtended) {
            mExpandRowsView.setVisibility(View.GONE);
        } else if (mExpandRowsView != null && !mHideExtended) {
            mExpandRowsView.setVisibility(View.VISIBLE);
        }

        if (mBackgroundThings != null && (mVolumePanelStyle == 0) && mHideRinger && mHideExtended) {
            mBackgroundThings.setVisibility(View.GONE);
        } else if (mBackgroundThings != null && (mVolumePanelStyle == 0) && (!mHideRinger || !mHideExtended))  {
            mBackgroundThings.setVisibility(View.VISIBLE);
        }

      }

}
