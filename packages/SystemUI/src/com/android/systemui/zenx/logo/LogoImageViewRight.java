/*
 * Copyright (C) 2018 crDroid Android Project
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

package com.android.systemui.zenx.logo;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.android.systemui.R;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;

public class LogoImageViewRight extends ImageView {

    private Context mContext;

    private boolean mAttached;
    private boolean mZenxLogo;
    private int mZenxLogoPosition;
    private int mZenxLogoStyle;
    private int mTintColor = Color.WHITE;
    private final Handler mHandler = new Handler();
    private ContentResolver mContentResolver;

    private class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_LOGO), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_LOGO_POSITION), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_LOGO_STYLE), false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSettings();
        }
    }

    private SettingsObserver mSettingsObserver = new SettingsObserver(mHandler);

    public LogoImageViewRight(Context context) {
        this(context, null);
    }

    public LogoImageViewRight(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LogoImageViewRight(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final Resources resources = getResources();
        mContext = context;
        mSettingsObserver.observe();
        updateSettings();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mAttached) {
            return;
        }
        mAttached = true;
        Dependency.get(DarkIconDispatcher.class).addDarkReceiver(this);
        updateSettings();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!mAttached) {
            return;
        }
        mAttached = false;
        Dependency.get(DarkIconDispatcher.class).removeDarkReceiver(this);
    }

    public void onDarkChanged(Rect area, float darkIntensity, int tint) {
        mTintColor = DarkIconDispatcher.getTint(area, this, tint);
        if (mZenxLogo && mZenxLogoPosition == 1) {
            updateZenxLogo();
        }
    }

    public void updateZenxLogo() {
        Drawable drawable = null;

        if (!mZenxLogo || mZenxLogoPosition == 0) {
            setImageDrawable(null);
            setVisibility(View.GONE);
            return;
        } else {
            setVisibility(View.VISIBLE);
        }

        if (mZenxLogoStyle == 0) {
            drawable = mContext.getDrawable(R.drawable.ic_zenx_logo);
        } else if (mZenxLogoStyle == 1) {
            drawable = mContext.getDrawable(R.drawable.ic_android_logo);
        } else if (mZenxLogoStyle == 2) {
            drawable = mContext.getDrawable(R.drawable.ic_apple_logo);
        } else if (mZenxLogoStyle == 3) {
            drawable = mContext.getDrawable(R.drawable.ic_beats);
        } else if (mZenxLogoStyle == 4) {
            drawable = mContext.getDrawable(R.drawable.ic_biohazard);
        } else if (mZenxLogoStyle == 5) {
            drawable = mContext.getDrawable(R.drawable.ic_blackberry);
        } else if (mZenxLogoStyle == 6) {
            drawable = mContext.getDrawable(R.drawable.ic_blogger);
        } else if (mZenxLogoStyle == 7) {
            drawable = mContext.getDrawable(R.drawable.ic_bomb);
        } else if (mZenxLogoStyle == 8) {
            drawable = mContext.getDrawable(R.drawable.ic_brain);
        } else if (mZenxLogoStyle == 9) {
            drawable = mContext.getDrawable(R.drawable.ic_cake);
        } else if (mZenxLogoStyle == 10) {
            drawable = mContext.getDrawable(R.drawable.ic_cannabis);
        } else if (mZenxLogoStyle == 11) {
            drawable = mContext.getDrawable(R.drawable.ic_death_star);
        } else if (mZenxLogoStyle == 12) {
            drawable = mContext.getDrawable(R.drawable.ic_emoticon);
        } else if (mZenxLogoStyle == 13) {
            drawable = mContext.getDrawable(R.drawable.ic_emoticon_cool);
        } else if (mZenxLogoStyle == 14) {
            drawable = mContext.getDrawable(R.drawable.ic_emoticon_dead);
        } else if (mZenxLogoStyle == 15) {
            drawable = mContext.getDrawable(R.drawable.ic_emoticon_devil);
        } else if (mZenxLogoStyle == 16) {
            drawable = mContext.getDrawable(R.drawable.ic_emoticon_happy);
        } else if (mZenxLogoStyle == 17) {
            drawable = mContext.getDrawable(R.drawable.ic_emoticon_neutral);
        } else if (mZenxLogoStyle == 18) {
            drawable = mContext.getDrawable(R.drawable.ic_emoticon_poop);
        } else if (mZenxLogoStyle == 19) {
            drawable = mContext.getDrawable(R.drawable.ic_emoticon_sad);
        } else if (mZenxLogoStyle == 20) {
            drawable = mContext.getDrawable(R.drawable.ic_emoticon_tongue);
        } else if (mZenxLogoStyle == 21) {
            drawable = mContext.getDrawable(R.drawable.ic_fire);
        } else if (mZenxLogoStyle == 22) {
            drawable = mContext.getDrawable(R.drawable.ic_flask);
        } else if (mZenxLogoStyle == 23) {
            drawable = mContext.getDrawable(R.drawable.ic_gender_female);
        } else if (mZenxLogoStyle == 24) {
            drawable = mContext.getDrawable(R.drawable.ic_gender_male);
        } else if (mZenxLogoStyle == 25) {
            drawable = mContext.getDrawable(R.drawable.ic_gender_male_female);
        } else if (mZenxLogoStyle == 26) {
            drawable = mContext.getDrawable(R.drawable.ic_ghost);
        } else if (mZenxLogoStyle == 27) {
            drawable = mContext.getDrawable(R.drawable.ic_google);
        } else if (mZenxLogoStyle == 28) {
            drawable = mContext.getDrawable(R.drawable.ic_guitar_acoustic);
        } else if (mZenxLogoStyle == 29) {
            drawable = mContext.getDrawable(R.drawable.ic_guitar_electric);
        } else if (mZenxLogoStyle == 30) {
            drawable = mContext.getDrawable(R.drawable.ic_heart);
        } else if (mZenxLogoStyle == 31) {
            drawable = mContext.getDrawable(R.drawable.ic_human_female);
        } else if (mZenxLogoStyle == 32) {
            drawable = mContext.getDrawable(R.drawable.ic_human_male);
        } else if (mZenxLogoStyle == 33) {
            drawable = mContext.getDrawable(R.drawable.ic_human_male_female);
        } else if (mZenxLogoStyle == 34) {
            drawable = mContext.getDrawable(R.drawable.ic_incognito);
        } else if (mZenxLogoStyle == 35) {
            drawable = mContext.getDrawable(R.drawable.ic_ios_logo);
        } else if (mZenxLogoStyle == 36) {
            drawable = mContext.getDrawable(R.drawable.ic_linux);
        } else if (mZenxLogoStyle == 37) {
            drawable = mContext.getDrawable(R.drawable.ic_lock);
        } else if (mZenxLogoStyle == 38) {
            drawable = mContext.getDrawable(R.drawable.ic_music_note);
        } else if (mZenxLogoStyle == 39) {
            drawable = mContext.getDrawable(R.drawable.ic_ninja);
        } else if (mZenxLogoStyle == 40) {
            drawable = mContext.getDrawable(R.drawable.ic_pac_man);
        } else if (mZenxLogoStyle == 41) {
            drawable = mContext.getDrawable(R.drawable.ic_peace);
        } else if (mZenxLogoStyle == 42) {
            drawable = mContext.getDrawable(R.drawable.ic_robot);
        } else if (mZenxLogoStyle == 43) {
            drawable = mContext.getDrawable(R.drawable.ic_skull);
        } else if (mZenxLogoStyle == 44) {
            drawable = mContext.getDrawable(R.drawable.ic_smoking);
        } else if (mZenxLogoStyle == 45) {
            drawable = mContext.getDrawable(R.drawable.ic_wallet);
        } else if (mZenxLogoStyle == 46) {
            drawable = mContext.getDrawable(R.drawable.ic_windows);
        } else if (mZenxLogoStyle == 47) {
            drawable = mContext.getDrawable(R.drawable.ic_xbox);
        } else if (mZenxLogoStyle == 48) {
            drawable = mContext.getDrawable(R.drawable.ic_xbox_controller);
        } else if (mZenxLogoStyle == 49) {
            drawable = mContext.getDrawable(R.drawable.ic_yin_yang);
        }

        setImageDrawable(null);

        clearColorFilter();

        drawable.setTint(mTintColor);
        setImageDrawable(drawable);
    }

    public void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();
        mZenxLogo = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_LOGO, 0) == 1;
        mZenxLogoPosition = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_LOGO_POSITION, 0);
        mZenxLogoStyle = Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_LOGO_STYLE, 0);
        updateZenxLogo();
    }
}
