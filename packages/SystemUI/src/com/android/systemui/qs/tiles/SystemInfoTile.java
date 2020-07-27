/*
 * Copyright (C) 2012 The Android Open Source Project
 * Copyright (C) 2013 The SlimRoms Project
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

package com.android.systemui.qs.tiles;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteController;
import android.media.session.MediaSessionLegacyHelper;
import android.os.Handler;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.content.res.Resources;
import android.os.UserHandle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.graphics.drawable.Icon;
import android.provider.Settings;
import com.android.systemui.Dependency;
import com.android.systemui.plugins.ActivityStarter;

import android.service.quicksettings.Tile;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.internal.util.zenx.ZenxUtils;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.inject.Inject;

public class SystemInfoTile extends QSTileImpl<BooleanState> {

    private final String TAG = "SystemInfoTile";

    private int mTap = 0;

    private final ActivityStarter mActivityStarter;
    private int mBatteryLevel = 0;
    private int lastMode = 0;

    @Inject
    public SystemInfoTile(QSHost host) {
        super(host);
        mActivityStarter = Dependency.get(ActivityStarter.class);
        lastMode = getQsSystemInfoMode();
    }

    @Override
    public BooleanState newTileState() {
        return new BooleanState();
    }

    @Override
    public void handleSetListening(boolean listening) {
    }

    @Override
    public void handleClick() {

     lastMode++;
     if(lastMode == 6) {
        lastMode = 0;
     }
      refreshState();
    }


    @Override
    public void handleLongClick() {
        if(lastMode == 2) {
            mActivityStarter.postStartActivityDismissingKeyguard(new Intent(
                Intent.ACTION_POWER_USAGE_SUMMARY), 0);
        }
    }

    @Override
    public Intent getLongClickIntent() {
        return null;
    }

    @Override
    public CharSequence getTileLabel() {
        return "System info";
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.ZENX_SETTINGS;
    }

    private int getQsSystemInfoMode() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.QS_SYSTEM_INFO_TILE_MODE, 0);
    }

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {

        if (state.slash == null) {
            state.slash = new SlashState();
        }

        switch (lastMode) {
            case 0:
                state.icon = ResourceIcon.get(R.drawable.ic_qs_system_info);
                state.label = "System info";
                state.state = Tile.STATE_INACTIVE;
                state.secondaryLabel = " ";
                state.slash.isSlashed = false;
                Settings.System.putIntForUser(mContext.getContentResolver(),
                    Settings.System.QS_SYSTEM_INFO_TILE_MODE, 0 , UserHandle.USER_CURRENT);
                break;
            case 1:
                state.icon = ResourceIcon.get(R.drawable.ic_qs_battery_info);
                state.label = "Battery temp";
                state.secondaryLabel = getBatteryTemp();
                state.state = Tile.STATE_ACTIVE;
                state.slash.isSlashed = true;
                Settings.System.putIntForUser(mContext.getContentResolver(),
                    Settings.System.QS_SYSTEM_INFO_TILE_MODE, 1 , UserHandle.USER_CURRENT);
                break;
            case 2:
                state.icon = getBatteryLevelIcon();
                state.label = "Battery Level";
                state.secondaryLabel = getBatteryLevel();
                state.state = Tile.STATE_ACTIVE;
                state.slash.isSlashed = true;
                Settings.System.putIntForUser(mContext.getContentResolver(),
                    Settings.System.QS_SYSTEM_INFO_TILE_MODE, 2 , UserHandle.USER_CURRENT);
                break;
            case 3:
                state.icon = ResourceIcon.get(R.drawable.ic_qs_gpu_info);
                state.label = "GPU freq" ;
                state.secondaryLabel = getGPUClock();
                state.state = Tile.STATE_ACTIVE;
                state.slash.isSlashed = true;
                Settings.System.putIntForUser(mContext.getContentResolver(),
                    Settings.System.QS_SYSTEM_INFO_TILE_MODE, 3 , UserHandle.USER_CURRENT);
                break;
            case 4:
                state.icon = ResourceIcon.get(R.drawable.ic_qs_gpu_info);
                state.label = "GPU busy";
                state.secondaryLabel = getGPUBusy();
                state.state = Tile.STATE_ACTIVE;
                state.slash.isSlashed = true;
                Settings.System.putIntForUser(mContext.getContentResolver(),
                    Settings.System.QS_SYSTEM_INFO_TILE_MODE, 4 , UserHandle.USER_CURRENT);
                break;
            case 5:
                state.icon = ResourceIcon.get(R.drawable.ic_qs_cpu_info);
                state.label = "CPU temp ";
                state.secondaryLabel = getCPUTemp();
                state.state = Tile.STATE_ACTIVE;
                state.slash.isSlashed = true;
                Settings.System.putIntForUser(mContext.getContentResolver(),
                    Settings.System.QS_SYSTEM_INFO_TILE_MODE, 5 , UserHandle.USER_CURRENT);
                break;
        }
    }

    
    private String getBatteryTemp() {
        String value;
        if(ZenxUtils.fileExists(mContext.getResources().getString(
                     com.android.internal.R.string.config_battery_temp_path))) {
                        value = readOneLine(mContext.getResources().getString(
                            com.android.internal.R.string.config_battery_temp_path));
                     } else {
                         value = "Error";
                     }

        return value == "Error" ? "N/A" : String.format("%s", Integer.parseInt(value) / 10) + "\u2103";
    }

    private String getCPUTemp() {
        String value;
        if(ZenxUtils.fileExists(mContext.getResources().getString(
                     com.android.internal.R.string.config_cpu_temp_path))) {
                        value = readOneLine(mContext.getResources().getString(
                            com.android.internal.R.string.config_cpu_temp_path));
                     } else {
                         value = "Error";
                     }

        return value == "Error" ? "N/A" : String.format("%s", Integer.parseInt(value) / 1000) + "\u2103";
    }

    private String getGPUBusy() {
        String value;
        if(ZenxUtils.fileExists(mContext.getResources().getString(
                     com.android.internal.R.string.config_gpu_busy_path))) {
                        value = readOneLine(mContext.getResources().getString(
                            com.android.internal.R.string.config_gpu_busy_path));
                     } else {
                         value = "Error";
                     }

        return value == "Error" ? "N/A" : value;
    }

    private String getGPUClock() {
        String value;
        if(ZenxUtils.fileExists(mContext.getResources().getString(
                     com.android.internal.R.string.config_gpu_clock_path))) {
                        value = readOneLine(mContext.getResources().getString(
                            com.android.internal.R.string.config_gpu_clock_path));
                     } else {
                         value = "Error";
                     }

        return value == "Error" ? "N/A" : String.format("%s", Integer.parseInt(value)) + "Mhz";
    }

    private String getBatteryLevel() {
        String value;
        if(ZenxUtils.fileExists(mContext.getResources().getString(
                     com.android.internal.R.string.config_battery_level_path))) {
                        value = readOneLine(mContext.getResources().getString(
                            com.android.internal.R.string.config_battery_level_path));
                     } else {
                         value = "Error";
                     }
        mBatteryLevel = Integer.parseInt(value);
        return value == "Error" ? "N/A" :  String.format("%s", Integer.parseInt(value)) + "\u0025";

    }

    private Icon getBatteryLevelIcon() {

        if(mBatteryLevel < 5) {
            return ResourceIcon.get(R.drawable.ic_qs_battery_info_empty_warning);
        }

        if(mBatteryLevel > 5 && mBatteryLevel <= 10 ) {
            return ResourceIcon.get(R.drawable.ic_qs_battery_info_10);
        }

        if(mBatteryLevel > 10 && mBatteryLevel <= 20) {
            return ResourceIcon.get(R.drawable.ic_qs_battery_info_20);
        }

        if(mBatteryLevel > 20 && mBatteryLevel <= 30 ) {
            return ResourceIcon.get(R.drawable.ic_qs_battery_info_30);
        }

        if(mBatteryLevel > 30 && mBatteryLevel <= 40 ) {
            return ResourceIcon.get(R.drawable.ic_qs_battery_info_40);
        }

        if(mBatteryLevel > 40 && mBatteryLevel <= 50 ) {
            return ResourceIcon.get(R.drawable.ic_qs_battery_info_50);
        }

        if(mBatteryLevel > 50 && mBatteryLevel <= 60) {
            return ResourceIcon.get(R.drawable.ic_qs_battery_info_60);
        }

        if(mBatteryLevel > 60 && mBatteryLevel <= 70) {
            return ResourceIcon.get(R.drawable.ic_qs_battery_info_70);
        }

        if(mBatteryLevel > 70 && mBatteryLevel <= 80 ) {
            return ResourceIcon.get(R.drawable.ic_qs_battery_info_80);
        }

        if(mBatteryLevel > 80 && mBatteryLevel <= 90 ) {
            return ResourceIcon.get(R.drawable.ic_qs_battery_info_90);
        }

        if(mBatteryLevel > 90 && mBatteryLevel <= 100 ) {
            return ResourceIcon.get(R.drawable.ic_qs_battery_info);
        }

        return ResourceIcon.get(R.drawable.ic_qs_battery_info);
    }

    private static String readOneLine(String fname) {
        BufferedReader br;
        String line = null;
        try {
            br = new BufferedReader(new FileReader(fname), 512);
            try {
                line = br.readLine();
            } finally {
                br.close();
            }
        } catch (Exception e) {
            return null;
        }
        return line;
    }

}
