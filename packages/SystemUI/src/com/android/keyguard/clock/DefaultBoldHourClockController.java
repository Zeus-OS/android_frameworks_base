/*
 * Copyright (C) 2019 The Android Open Source Project
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

import android.app.WallpaperManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint.Style;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextClock;
import android.content.Context;
import com.android.internal.util.zenx.ZenxUtils;

import com.android.internal.colorextraction.ColorExtractor;
import com.android.systemui.R;
import com.android.systemui.colorextraction.SysuiColorExtractor;
import com.android.systemui.plugins.ClockPlugin;

import java.util.TimeZone;

import static com.android.systemui.statusbar.phone
        .KeyguardClockPositionAlgorithm.CLOCK_USE_DEFAULT_Y;

/**
 * Plugin for the default clock face used only to provide a preview.
 */
public class DefaultBoldHourClockController implements ClockPlugin {

    /**
     * Resources used to get title and thumbnail.
     */
    private final Resources mResources;

    /**
     * LayoutInflater used to inflate custom clock views.
     */
    private final LayoutInflater mLayoutInflater;

    /**
     * Extracts accent color from wallpaper.
     */
    private final SysuiColorExtractor mColorExtractor;

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

    private Context mContext;

    /**
     * Create a DefaultClockController instance.
     *
     * @param res Resources contains title and thumbnail.
     * @param inflater Inflater used to inflate custom clock views.
     * @param colorExtractor Extracts accent color from wallpaper.
     */
    public DefaultBoldHourClockController(Resources res, LayoutInflater inflater,
            SysuiColorExtractor colorExtractor, Context context) {
        mResources = res;
        mLayoutInflater = inflater;
        mColorExtractor = colorExtractor;
        mContext = context;
    }

    private void createViews() {
        mBigClockView = (ClockLayout) mLayoutInflater
                .inflate(R.layout.digital_clock_custom, null);
        mClock = mBigClockView.findViewById(R.id.clock);
        int mAccentColor = mContext.getResources().getColor(R.color.lockscreen_clock_accent_color);

        if(ZenxUtils.useLockscreenClockMinuteAccentColor(mContext) && ZenxUtils.useLockscreenClockHourAccentColor(mContext)) {
             mClock.setFormat12Hour(Html.fromHtml("<strong><font color=" + mAccentColor + ">h</font></strong>:<font color=" + mAccentColor + ">mm</font>"));
             mClock.setFormat24Hour(Html.fromHtml("<strong><font color=" + mAccentColor + ">kk</font></strong>:<font color=" + mAccentColor + ">mm</font>"));
        } else if(ZenxUtils.useLockscreenClockHourAccentColor(mContext)) {
             mClock.setFormat12Hour(Html.fromHtml("<strong><font color=" + mAccentColor + ">h</font></strong>:mm"));
             mClock.setFormat24Hour(Html.fromHtml("<strong><font color=" + mAccentColor + ">kk</font></strong>:mm"));
        } else if(ZenxUtils.useLockscreenClockMinuteAccentColor(mContext)) {
             mClock.setFormat12Hour(Html.fromHtml("<strong>h</strong>:<font color=" + mAccentColor + ">mm</font>"));
             mClock.setFormat24Hour(Html.fromHtml("<strong>kk</strong>:<font color=" + mAccentColor + ">mm</font>"));
        } else {
            mClock.setFormat12Hour(Html.fromHtml("<strong>h</strong>:mm"));
            mClock.setFormat24Hour(Html.fromHtml("<strong>kk</strong>:mm"));
        }
    }

    @Override
    public void onDestroyView() {
        mBigClockView = null;
        mClock = null;
    }

    @Override
    public String getName() {
        return "default_bold_hour";
    }

    @Override
    public String getTitle() {
        return mResources.getString(R.string.clock_title_default_hour_bold);
    }

    @Override
    public Bitmap getThumbnail() {
        return BitmapFactory.decodeResource(mResources, R.drawable.default_bold_thumbnail);
    }

    @Override
    public Bitmap getPreview(int width, int height) {

        View previewView = mLayoutInflater.inflate(R.layout.default_clock_preview, null);
        TextClock previewTime = previewView.findViewById(R.id.time);
        previewTime.setFormat12Hour(Html.fromHtml("<strong>h</strong>:mm"));
        previewTime.setFormat24Hour(Html.fromHtml("<strong>kk</strong>:mm"));
        TextClock previewDate = previewView.findViewById(R.id.date);

        // Initialize state of plugin before generating preview.
        previewTime.setTextColor(Color.WHITE);
        previewDate.setTextColor(Color.WHITE);
        ColorExtractor.GradientColors colors = mColorExtractor.getColors(
                WallpaperManager.FLAG_LOCK);
        setColorPalette(colors.supportsDarkText(), colors.getColorPalette());
        onTimeTick();

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
    }

    @Override
    public void setColorPalette(boolean supportsDarkText, int[] colorPalette) {}

    @Override
    public void onTimeTick() {
    }

    @Override
    public void setDarkAmount(float darkAmount) {
        mBigClockView.setDarkAmount(darkAmount);
    }

    @Override
    public void onTimeZoneChanged(TimeZone timeZone) {}

    @Override
    public boolean shouldShowStatusArea() {
        return true;
    }
}
