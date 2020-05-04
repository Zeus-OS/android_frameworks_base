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

package com.android.systemui.doze;

import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;

import android.service.dreams.DreamService;
import android.util.Log;

import com.android.systemui.Dependency;
import com.android.systemui.plugins.DozeServicePlugin;
import com.android.systemui.plugins.DozeServicePlugin.RequestDoze;
import com.android.systemui.plugins.FalsingManager;
import com.android.systemui.plugins.PluginListener;
import com.android.systemui.shared.plugins.PluginManager;

import com.android.internal.R;
import com.android.internal.util.zenx.ZenxUtils;
import android.content.res.Resources;
import android.provider.Settings;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import javax.inject.Inject;

public class DozeService extends DreamService
        implements DozeMachine.Service, RequestDoze, PluginListener<DozeServicePlugin> {
    private static final String TAG = "DozeService";
    static final boolean DEBUG = Log.isLoggable(TAG, Log.DEBUG);

    public static final String SPECTRUM_SYSTEM_PROPERTY = "persist.spectrum.profile";

    private DozeMachine mDozeMachine;
    private DozeServicePlugin mDozePlugin;
    private PluginManager mPluginManager;

    @Inject
    public DozeService() {
        setDebug(DEBUG);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setWindowless(true);

        if (DozeFactory.getHost(this) == null) {
            finish();
            return;
        }
        mPluginManager = Dependency.get(PluginManager.class);
        mPluginManager.addPluginListener(this, DozeServicePlugin.class, false /* allowMultiple */);
        mDozeMachine = new DozeFactory().assembleMachine(
                this, Dependency.get(FalsingManager.class));
    }

    @Override
    public void onDestroy() {
        if (mPluginManager != null) {
            mPluginManager.removePluginListener(this);
        }
        super.onDestroy();
        mDozeMachine = null;
    }

    @Override
    public void onPluginConnected(DozeServicePlugin plugin, Context pluginContext) {
        mDozePlugin = plugin;
        mDozePlugin.setDozeRequester(this);
    }

    @Override
    public void onPluginDisconnected(DozeServicePlugin plugin) {
        if (mDozePlugin != null) {
            mDozePlugin.onDreamingStopped();
            mDozePlugin = null;
        }
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();
        mDozeMachine.requestState(DozeMachine.State.INITIALIZED);
        startDozing();
        if (mDozePlugin != null) {
            mDozePlugin.onDreamingStarted();
        }
        deviceSpecificPerfermanceModeHandler(true);
    }



    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();
        mDozeMachine.requestState(DozeMachine.State.FINISH);
        if (mDozePlugin != null) {
            mDozePlugin.onDreamingStopped();
        }
        deviceSpecificPerfermanceModeHandler(false);
    }

    private void deviceSpecificPerfermanceModeHandler(boolean enabled) {
        if(getUltraPowerSavingMode() && ZenxUtils.IntelligentPerformanceProfileAvailable()) {
            if(enabled) {
                ZenxUtils.SetLastPerformanceProfileToSettings(this, SystemProperties.get(SPECTRUM_SYSTEM_PROPERTY, Resources.getSystem().getString(R.string.config_balanced_profile)));
                SystemProperties.set(SPECTRUM_SYSTEM_PROPERTY, Resources.getSystem().getString(R.string.config_screen_off_profile));
            } else {
                SystemProperties.set(SPECTRUM_SYSTEM_PROPERTY, ZenxUtils.GetLastPerformanceProfileFromSettings(this));
            }
        }
    }

    private boolean getUltraPowerSavingMode() {
        return Settings.System.getInt(this.getContentResolver(),
                Settings.System.ULTRA_POWER_SAVE, 1) != 0;
    }

    @Override
    protected void dumpOnHandler(FileDescriptor fd, PrintWriter pw, String[] args) {
        super.dumpOnHandler(fd, pw, args);
        if (mDozeMachine != null) {
            mDozeMachine.dump(pw);
        }
    }

    @Override
    public void requestWakeUp() {
        PowerManager pm = getSystemService(PowerManager.class);
        pm.wakeUp(SystemClock.uptimeMillis(), PowerManager.WAKE_REASON_GESTURE,
                "com.android.systemui:NODOZE");
    }

    @Override
    public void onRequestShowDoze() {
        if (mDozeMachine != null) {
            mDozeMachine.requestState(DozeMachine.State.DOZE_AOD);
        }
    }

    @Override
    public void onRequestHideDoze() {
        if (mDozeMachine != null) {
            mDozeMachine.requestState(DozeMachine.State.DOZE);
        }
    }
}
