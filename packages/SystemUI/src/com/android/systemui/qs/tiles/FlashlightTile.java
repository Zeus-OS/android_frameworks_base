/*
 * Copyright (C) 2014 The Android Open Source Project
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
 * limitations under the License
 */

package com.android.systemui.qs.tiles;

import android.app.ActivityManager;
import android.content.Intent;
import android.provider.MediaStore;
import android.service.quicksettings.Tile;
import android.widget.Switch;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.FlashlightController.FlashlightListener;
import com.android.systemui.plugins.ActivityStarter;
import com.android.internal.util.zeus.ZeusUtils;

import javax.inject.Inject;

/** Quick settings tile: Control flashlight **/
public class FlashlightTile extends QSTileImpl<BooleanState> {

    private final Icon mIcon = ResourceIcon.get(com.android.internal.R.drawable.ic_qs_flashlight);
    private final Icon mIconLow = ResourceIcon.get(R.drawable.ic_qs_flashlight_low);
    private final Icon mIconMedium = ResourceIcon.get(R.drawable.ic_qs_flashlight_medium);
    private final Icon mIconHigh = ResourceIcon.get(R.drawable.ic_qs_flashlight_high);
    private final FlashlightController mFlashlightController;
    private final ActivityStarter mActivityStarter;
    private final Callback mCallback = new Callback();
    private int mTapCounter = 0;
    private int currentMode = -1;
    private boolean isBrightnessSupported = false;

    @Inject
    public FlashlightTile(QSHost host, FlashlightController flashlightController, ActivityStarter activityStarter) {
        super(host);
        mFlashlightController = flashlightController;
        mActivityStarter = activityStarter;
        isBrightnessSupported = mContext.getResources().getBoolean(
            com.android.internal.R.bool.config_flashlight_brightness_enabled);
        if(isBrightnessSupported) {
            switchMode();
        }
        mFlashlightController.observe(this, mCallback);
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void handleLongClick() {
        mFlashlightController.setFlashlight(false);
        mTapCounter = 0;
        switchMode();
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return mFlashlightController.hasFlashlight();
    }

    @Override
    protected void handleClick() {
        if(isBrightnessSupported) {
            switchMode();
        } else {
            if(mTapCounter == 0) {
                mFlashlightController.setFlashlight(true);
                mTapCounter = mTapCounter + 1;
            } else {
                mFlashlightController.setFlashlight(false);
                mTapCounter = 0;
            }

        }

    }

    private void switchMode() {
         mFlashlightController.setFlashlight(false);
         int brightnesValue;
         
         switch (mTapCounter) {
             case 1:
                currentMode = 0;
                mTapCounter = mTapCounter + 1;
                break;
             case 2:
                currentMode = 1;
                mTapCounter = mTapCounter + 1;
                break;
             case 3:
                currentMode = 2;
                mTapCounter = 0;
                break;
             default:
                currentMode = -1;
                mTapCounter = mTapCounter + 1;
                break;
         }

        if (currentMode == -1) {
            // Flashlight Off - do nothing here
        } else if (currentMode == 0) {
            // Flashlight brightness low
            brightnesValue = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_flashlight_brightness_value_low);
            ZeusUtils.setFlashlightBrightness(mContext, brightnesValue);
            mFlashlightController.setFlashlight(true);
        } else if (currentMode == 1) {
            // Flashlight brightness default
            brightnesValue = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_flashlight_brightness_value_medium);
           ZeusUtils.setFlashlightBrightness(mContext, brightnesValue);
           mFlashlightController.setFlashlight(true);
        } else {
            // Flashlight brightness high
            brightnesValue = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_flashlight_brightness_value_high);
           ZeusUtils.setFlashlightBrightness(mContext, brightnesValue);
           mFlashlightController.setFlashlight(true);
        }
    }

    @Override
    public CharSequence getTileLabel() {
        return mContext.getString(R.string.quick_settings_flashlight_label);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {
        if (state.slash == null) {
            state.slash = new SlashState();
        }

        if(isBrightnessSupported) {
            switch (currentMode) {
                case 0:
                    state.value = true;
                    state.label = mContext.getString(R.string.quick_settings_flashlight_label);
                    state.secondaryLabel = mContext.getString(R.string.quick_settings_flashlight_brightness_secondary_low);
                    state.icon = mIconLow;
                    state.state = Tile.STATE_ACTIVE;
                    break;
                case 1:
                    state.value = true;
                    state.label = mContext.getString(R.string.quick_settings_flashlight_label);
                    state.secondaryLabel = mContext.getString(R.string.quick_settings_flashlight_brightness_secondary_medium);
                    state.icon = mIconMedium;
                    state.state = Tile.STATE_ACTIVE;
                    break;
                case 2:
                    state.value = true;
                    state.label = mContext.getString(R.string.quick_settings_flashlight_label);
                    state.secondaryLabel = mContext.getString(R.string.quick_settings_flashlight_brightness_secondary_high);
                    state.icon = mIconHigh;
                    state.state = Tile.STATE_ACTIVE;
                    break;
                case -1:
                    state.value = false;
                    state.label = mContext.getString(R.string.quick_settings_flashlight_label);
                    state.secondaryLabel = mContext.getString(R.string.quick_settings_flashlight_brightness_secondary_off);
                    state.icon = mIcon;
                    state.state = Tile.STATE_INACTIVE;
                    break;
            }
            state.expandedAccessibilityClassName = Switch.class.getName();
        }  else {
            if(mTapCounter == 0) {
                state.value = false;
                state.label = mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_off);
                state.icon = mIcon;
                state.state = Tile.STATE_INACTIVE;
            } else {
                state.value = true;
                state.label = mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_on);
                state.icon = mIcon;
                state.state = Tile.STATE_ACTIVE;
            }
        }
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.QS_FLASHLIGHT;
    }

    @Override
    protected String composeChangeAnnouncement() {
        if (mState.value) {
            return mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_on);
        } else {
            return mContext.getString(R.string.accessibility_quick_settings_flashlight_changed_off);
        }
    }

    private final class Callback implements FlashlightListener {
        @Override
        public void onFlashlightChanged(boolean enabled) {
            refreshState();
        }
        
        @Override
        public void onFlashlightAvailabilityChanged(boolean enabled) {
            refreshState();
        }

    };
}
