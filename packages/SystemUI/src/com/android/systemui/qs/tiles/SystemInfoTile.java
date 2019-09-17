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
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import android.service.quicksettings.Tile;
import com.android.systemui.R;
import com.android.systemui.plugins.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSHost;
import com.android.systemui.qs.tileimpl.QSTileImpl;

import com.android.internal.logging.MetricsLogger;
import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.inject.Inject;

public class SystemInfoTile extends QSTileImpl<BooleanState> {

    private final String TAG = "SystemInfoTile";

    private int mTap = 0;

    @Inject
    public SystemInfoTile(QSHost host) {
        super(host);
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
      mTap++;
      if(mTap == 5) {
        mTap = 0;
      }
      refreshState();
    }

    @Override
    public void handleLongClick() {
        // do nothing
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

    @Override
    protected void handleUpdateState(BooleanState state, Object arg) {

        if (state.slash == null) {
            state.slash = new SlashState();
        }

        switch (mTap) {
            case 0:
                state.icon = ResourceIcon.get(R.drawable.ic_qs_system_info);
                state.label = "System info";
                state.state = Tile.STATE_INACTIVE;
                state.secondaryLabel = " ";
                state.slash.isSlashed = false;
                break;
            case 1:
                state.icon = ResourceIcon.get(R.drawable.ic_qs_battery_info);
                state.label = "Battery temp";
                state.secondaryLabel = getBatteryTemp();
                state.state = Tile.STATE_ACTIVE;
                state.slash.isSlashed = true;
                break;
            case 2:
                state.icon = ResourceIcon.get(R.drawable.ic_qs_cpu_info);
                state.label = "CPU temp ";
                state.secondaryLabel = getCPUTemp();
                state.state = Tile.STATE_ACTIVE;
                state.slash.isSlashed = true;
                break;
            case 3:
                state.icon = ResourceIcon.get(R.drawable.ic_qs_gpu_info);
                state.label = "GPU freq" ;
                state.secondaryLabel = getGPUClock();
                state.state = Tile.STATE_ACTIVE;
                state.slash.isSlashed = true;
                break;
            case 4:
                state.icon = ResourceIcon.get(R.drawable.ic_qs_gpu_info);
                state.label = "GPU busy";
                state.secondaryLabel = getGPUBusy();
                state.state = Tile.STATE_ACTIVE;
                state.slash.isSlashed = true;
                break;
        }
    }

    
    private String getBatteryTemp() {
         String value = readOneLine(mContext.getResources().getString(
                        com.android.internal.R.string.config_battery_temp_path));
        return String.format("%s", Integer.parseInt(value) / 10) + "\u2103";
    }

    private String getCPUTemp() {
        String value = readOneLine(mContext.getResources().getString(
                        com.android.internal.R.string.config_cpu_temp_path));
        return String.format("%s", Integer.parseInt(value) / 1000) + "\u2103";
    }

    private String getGPUBusy() {
        String value = readOneLine(mContext.getResources().getString(
                     com.android.internal.R.string.config_gpu_busy_path));
        return value;
    }

    private String getGPUClock() {
        String value = readOneLine(mContext.getResources().getString(
                     com.android.internal.R.string.config_gpu_clock_path));
        return String.format("%s", Integer.parseInt(value)) + "Mhz";
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
