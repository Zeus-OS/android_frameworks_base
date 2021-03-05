/*
 * Copyright (C) 2020 The Zeus-OS Project
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

package com.android.keyguard.clock;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint.Style;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextClock;
import android.content.Context;
import com.android.internal.util.zeus.ZeusUtils;
import android.text.Html;
import com.airbnb.lottie.LottieAnimationView;
import android.graphics.Typeface;

import com.android.systemui.R;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.plugins.ClockPlugin;

import java.util.TimeZone;

/**
 * Plugin for the default clock face used only to provide a preview.
 */
public class CityCircleController implements ClockPlugin {

    /**
     * Resources used to get title and thumbnail.
     */
    private final Resources mResources;

    /**
     * LayoutInflater used to inflate custom clock views.
     */
    private final LayoutInflater mLayoutInflater;

    /**
     * Renders preview from clock view.
     */
    private final ViewPreviewer mRenderer = new ViewPreviewer();

    /**
     * Root view of clock.
     */
    private ClockLayout mBigClockView;

    /**
     * Text clock in preview view hierarchy.
     */
    private TextClock mClock;

    /**
     * Swing animation
     */
     private LottieAnimationView mLogo;

    private Context mContext;

    /**
     * Helper to extract colors from wallpaper palette for clock face.
     */
    private final ClockPalette mPalette = new ClockPalette();

    /**
     * Create a DefaultClockController instance.
     *
     * @param res Resources contains title and thumbnail.
     * @param inflater Inflater used to inflate custom clock views.
     * @param colorExtractor Extracts accent color from wallpaper.
     */
    public CityCircleController(Resources res, LayoutInflater inflater,
            SysuiColorExtractor colorExtractor, Context context) {
        mResources = res;
        mLayoutInflater = inflater;
        mContext = context;
    }

    private void createViews() {
        mBigClockView = (ClockLayout) mLayoutInflater
                .inflate(R.layout.city_circle_animation, null);
        mClock = (TextClock) mBigClockView.findViewById(R.id.clock);
        mLogo = (LottieAnimationView) mBigClockView.findViewById(R.id.logo);
        setClockColors();
    }


    private void setClockColors() {
        int mAccentColor = mContext.getResources().getColor(R.color.lockscreen_clock_accent_color);
        int mWhiteColor = mContext.getResources().getColor(R.color.lockscreen_clock_white_color);

        if(ZeusUtils.useLockscreenClockMinuteAccentColor(mContext) && ZeusUtils.useLockscreenClockHourAccentColor(mContext)) {
             mClock.setFormat12Hour(Html.fromHtml("<font color=" + mAccentColor + ">hh</font><br><font color=" + mAccentColor + ">mm</font>"));
             mClock.setFormat24Hour(Html.fromHtml("<font color=" + mAccentColor + ">kk</font><br><font color=" + mAccentColor + ">mm</font>"));
        } else if(ZeusUtils.useLockscreenClockHourAccentColor(mContext)) {
             mClock.setFormat12Hour(Html.fromHtml("<font color=" + mAccentColor + ">hh</font><br><font color=" + mWhiteColor + ">mm</font>"));
             mClock.setFormat24Hour(Html.fromHtml("<font color=" + mAccentColor + ">kk</font><br><font color=" + mWhiteColor + ">mm</font>"));
        } else {
             mClock.setFormat12Hour(Html.fromHtml("<font color=" + mWhiteColor + ">hh</font><br><font color=" + mWhiteColor + ">mm</font>"));
             mClock.setFormat24Hour(Html.fromHtml("<font color=" + mWhiteColor + ">kk</font><br><font color=" + mWhiteColor + ">mm</font>"));
        }
    }

    @Override
    public void onDestroyView() {
        mBigClockView = null;
        mClock = null;
    }

    @Override
    public String getName() {
        return "city_circle";
    }

    @Override
    public String getTitle() {
        return mResources.getString(R.string.clock_title_city_circle);
    }

    @Override
    public Bitmap getThumbnail() {
        return BitmapFactory.decodeResource(mResources, R.drawable.city_preview);
    }

    @Override
    public Bitmap getPreview(int width, int height) {

        View previewView = getView();
        // TextClock clock = previewView.findViewById(R.id.clock);
        // clock.setFormat12Hour("hh<br>mm");
        // clock.setFormat24Hour("kk<br>mm");
        // onTimeTick();
        return mRenderer.createPreview(previewView, width, height);
    }

    @Override
    public View getView() {
        return null;
    }

    @Override
    public View getBigClockView() {
        if (mBigClockView == null) {
            createViews();
        }
        return mBigClockView;
    }

    @Override
    public int getPreferredY(int totalHeight) {
        return totalHeight / 2;
    }
    @Override
    public void setStyle(Style style) {}

    @Override
    public void setTextColor(int color) {
        setClockColors();
    }

    @Override
    public void setTypeface(Typeface tf) {
        mClock.setTypeface(tf);
    }

    @Override
    public void setColorPalette(boolean supportsDarkText, int[] colorPalette) {
        mPalette.setColorPalette(supportsDarkText, colorPalette);
    }

    @Override
    public void onTimeTick() {
        mBigClockView.onTimeChanged();
        mClock.refreshTime();
    }

    @Override
    public void setDarkAmount(float darkAmount) {
        mPalette.setDarkAmount(darkAmount);
        mBigClockView.setDarkAmount(darkAmount);
    }

    @Override
    public void onTimeZoneChanged(TimeZone timeZone) {}

    @Override
    public boolean shouldShowStatusArea() {
        return true;
    }
}