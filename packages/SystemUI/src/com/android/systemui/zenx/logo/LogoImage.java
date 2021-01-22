/*
 * Copyright (C) 2018 crDroid Android Project
 * Copyright (C) 2018-2019 AICP
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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.os.Handler;

import android.graphics.Color;
import java.util.Random;

import java.util.Timer;
import java.util.TimerTask;

import com.android.settingslib.Utils;
import com.android.systemui.R;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver;

import com.android.systemui.tuner.TunerService;

public abstract class LogoImage extends ImageView implements
        TunerService.Tunable {

    private Context mContext;

    private boolean mAttached;
    private boolean mLogo;
    private int mLogoColor;

    public int mLogoPosition;
    private int mLogoStyle;
    private int mTintColor = Color.WHITE;
    private int mColorMode;
    private int mInterval;

    private Handler mHandler = new Handler();
    private Timer mTimer = null;

    private static final String STATUS_BAR_LOGO =
            "system:" + Settings.System.STATUS_BAR_LOGO;
    private static final String STATUS_BAR_LOGO_COLOR =
            "system:" + Settings.System.STATUS_BAR_LOGO_COLOR;
    private static final String STATUS_BAR_LOGO_POSITION =
            "system:" + Settings.System.STATUS_BAR_LOGO_POSITION;
    private static final String STATUS_BAR_LOGO_STYLE =
            "system:" + Settings.System.STATUS_BAR_LOGO_STYLE;
    private static final String STATUS_BAR_LOGO_COLOR_MODE =
            "system:" + Settings.System.STATUS_BAR_LOGO_COLOR_MODE;
    private static final String STATUSBAR_LOGO_RANDOM_COLOR_INTERVAL =
            "system:" + Settings.System.STATUSBAR_LOGO_RANDOM_COLOR_INTERVAL;

    public LogoImage(Context context) {
        this(context, null);
    }

    public LogoImage(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LogoImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        final Resources resources = getResources();
        mContext = context;
    }

    protected abstract boolean isLogoPosition();

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mAttached)
            return;

        mAttached = true;

        Dependency.get(DarkIconDispatcher.class).addDarkReceiver(this);

        Dependency.get(TunerService.class).addTunable(this,
                STATUS_BAR_LOGO,
                STATUS_BAR_LOGO_COLOR,
                STATUS_BAR_LOGO_POSITION,
                STATUSBAR_LOGO_RANDOM_COLOR_INTERVAL,
                STATUS_BAR_LOGO_COLOR_MODE,
                STATUS_BAR_LOGO_STYLE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!mAttached)
            return;

        mAttached = false;
        Dependency.get(TunerService.class).removeTunable(this);
        Dependency.get(DarkIconDispatcher.class).removeDarkReceiver(this);
    }

    public void onDarkChanged(Rect area, float darkIntensity, int tint) {
        mTintColor = DarkIconDispatcher.getTint(area, this, tint);
        if (mLogo && isLogoPosition() == false &&
                mLogoColor == 0xFFFFFFFF) {
            updateLogo();
        }
    }

    public void updateLogo() {
        Drawable drawable = null;

        if (!mLogo || isLogoPosition() == true) {
            if (mTimer != null) {
                mTimer.cancel();
            }
            setImageDrawable(null);
            setVisibility(View.GONE);
            return;
        } else {
            setVisibility(View.VISIBLE);
        }
        switch(mLogoStyle){
            case 0:
	        drawable = mContext.getResources().getDrawable(R.drawable.ic_zenx_logo);
                break;
	        case 1:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_android_logo);
                break;
            case 2:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_apple_logo);
                break;
            case 3:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_ios_logo);
                break;
            case 4:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_emoticon);
                break;
            case 5:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_emoticon_cool);
                break;
            case 6:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_emoticon_dead);
                break;
            case 7:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_emoticon_devil);
                break;
            case 8:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_emoticon_happy);
                break;
            case 9:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_emoticon_neutral);
                break;
            case 10:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_emoticon_poop);
                break;
            case 11:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_emoticon_sad);
                break;
            case 12:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_emoticon_tongue);
                break;
            case 13:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_blackberry);
                break;
            case 14:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_cake);
                break;
            case 15:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_blogger);
                break;
            case 16:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_biohazard);
                break;
            case 17:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_linux);
                break;
            case 18:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_yin_yang);
                break;
            case 19:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_windows);
                break;
            case 20:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_robot);
                break;
            case 21:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_ninja);
                break;
            case 22:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_heart);
                break;
            case 23:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_flower);
                break;
            case 24:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_ghost);
                break;
            case 25:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_google);
                break;
            case 26:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_human_male);
                break;
            case 27:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_human_female);
                break;
            case 28:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_human_male_female);
                break;
            case 29:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_gender_male);
                break;
            case 30:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_gender_female);
                break;
            case 31:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_gender_male_female);
                break;
            case 32:
                drawable = mContext.getResources().getDrawable(R.drawable.ic_guitar_electric);
                break;
        }

        setImageDrawable(null);
        updateLogoColor(drawable);
        setImageDrawable(drawable);
    }

    private void updateLogoColor(Drawable drawable) {

        clearColorFilter();

        switch(mColorMode){
            case 0:
                if (mLogoColor == 0xFFFFFFFF) {
                    drawable.setTint(mTintColor);
                } else {
                    setColorFilter(mLogoColor, PorterDuff.Mode.SRC_IN);
                }
                break;
            case 1:
                setColorFilter(Utils.getColorAttrDefaultColor(mContext, android.R.attr.colorAccent),PorterDuff.Mode.SRC_IN);
                break;
            case 2:
                if(mTimer != null) {
                    mTimer.cancel();
                }
                mTimer = new Timer();
                setColorFilter(getRandomColor(),PorterDuff.Mode.SRC_IN);
                int interval = mInterval * 1000;
                mTimer.scheduleAtFixedRate(new TimeDisplay(), 0, interval);
                break;
        }
    }

    //class TimeDisplay for handling task
        class TimeDisplay extends TimerTask {
            @Override
            public void run() {
                // run on another thread
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if(mLogo && mColorMode == 2 ) {
                            setColorFilter(getRandomColor(),PorterDuff.Mode.SRC_IN);
                        } else {
                            if (mTimer != null) {
                                 mTimer.cancel();
                            }
                        }
                    }
                });
            }
        }


    private int getRandomColor(){
        Random rnd = new Random();
            return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }


    @Override
    public void onTuningChanged(String key, String newValue) {
        Drawable drawable = null;
        switch (key) {
            case STATUS_BAR_LOGO:
                mLogo = newValue != null && Integer.parseInt(newValue) == 1;
                break;
            case STATUS_BAR_LOGO_COLOR:
                mLogoColor =
                        newValue == null ? 0xFFFFFFFF : Integer.parseInt(newValue);
                break;
            case STATUS_BAR_LOGO_COLOR_MODE:
                mColorMode =
                        newValue == null ? 0 : Integer.parseInt(newValue);
                break;
            case STATUSBAR_LOGO_RANDOM_COLOR_INTERVAL:
                mInterval =
                        newValue == null ? 3 : Integer.parseInt(newValue);
                break;
            case STATUS_BAR_LOGO_POSITION:
                mLogoPosition =
                        newValue == null ? 0 : Integer.parseInt(newValue);
                break;
            case STATUS_BAR_LOGO_STYLE:
                mLogoStyle =
                        newValue == null ? 0 : Integer.parseInt(newValue);
                break;
            default:
                break;
        }
        updateLogo();
    }
}
