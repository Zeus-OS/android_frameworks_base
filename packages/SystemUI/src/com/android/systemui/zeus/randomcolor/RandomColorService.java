/*
 * Copyright (C) 2020 Zeus-OS
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
package com.android.systemui.zeus.randomcolor;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.database.ContentObserver;
import android.content.ContentResolver;
import android.os.Handler;
import android.os.UserHandle;
import android.graphics.Color;
import android.os.SystemProperties;
import android.content.om.IOverlayManager;
import android.os.ServiceManager;
import android.os.RemoteException;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class RandomColorService extends Service  {

    private static final String TAG = "RandomColorService";
    private String ACCENT_COLOR_PROP = "persist.sys.theme.accentcolor";

    private BroadcastReceiver mPowerKeyReceiver;
    private boolean mEnabled = true;
    private Context mContext;

    private IOverlayManager mOverlayManager;

    private Handler scrOnHandler;
    private Handler scrOffHandler;
    private boolean offScheduled = true;
    private boolean onScheduled = false;
    private String strAction = "";

    private Runnable scrOffTask = new Runnable() {
        public void run() {
            Log.v(TAG,"scrOffTask");
                updateAccentColor();
                offScheduled = false;
            }
    };

    private Runnable scrOnTask = new Runnable() {
        public void run() {
                Log.v(TAG,"scrOnTask");
                onScheduled = false;
            }
    };


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        if (mEnabled){
            unregisterReceiver();
        }
    }

    @Override
    public void onStart(Intent intent, int startid)
    {
        Log.d(TAG, "onStart");
        mContext = getApplicationContext();
        
        // firewall
        int rcc = Settings.System.getIntForUser(mContext.getContentResolver(),
                    Settings.System.RANDOM_ACCENT_COLOR_ON_SCREEN_OFF, 0, UserHandle.USER_CURRENT);

        mOverlayManager = IOverlayManager.Stub.asInterface(
                ServiceManager.getService(Context.OVERLAY_SERVICE));

        if(rcc!=0) {
            mEnabled = true;
        } else {
            mEnabled = false;
        }            

        if (mEnabled){
            registerBroadcastReceiver();
        }

        scrOnHandler = new Handler();
        scrOffHandler = new Handler();
    }

    private void registerBroadcastReceiver() {
        final IntentFilter theFilter = new IntentFilter();
        /** System Defined Broadcast */
        theFilter.addAction(Intent.ACTION_SCREEN_ON);
        theFilter.addAction(Intent.ACTION_SCREEN_OFF);
        theFilter.addAction("android.intent.action.RANDOM_COLOR_SERVICE_UPDATE");

        mPowerKeyReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                strAction = intent.getAction();

                if (strAction.equals(Intent.ACTION_SCREEN_OFF)){ 
                    Log.d(TAG, "screen off");                  
                    if(onScheduled){
                        scrOnHandler.removeCallbacks(scrOnTask);
                    } else {
                        int sec = Settings.System.getIntForUser(mContext.getContentResolver(),
                                    Settings.System.RANDOM_ACCENT_COLOR_SCREENOFF_DURATION, 10, UserHandle.USER_CURRENT);
                        Log.d(TAG, "screen off change color delay: " + sec);
                        scrOffHandler.postDelayed(scrOffTask, sec * 1000);
                        offScheduled = true;
                    }
                }
                if (strAction.equals(Intent.ACTION_SCREEN_ON)) {
                    Log.d(TAG, "scren on");
                    if(offScheduled){
                        scrOffHandler.removeCallbacks(scrOffTask);
                    } else {
                        scrOnHandler.postDelayed(scrOnTask, 1 * 100);
                    }
                }
                if (strAction.equals("android.intent.action.RANDOM_COLOR_SERVICE_UPDATE")){
                    Log.d(TAG, "update color");
                    int rcs = Settings.System.getIntForUser(mContext.getContentResolver(),
                        Settings.System.RANDOM_ACCENT_COLOR_ON_SCREEN_OFF, 0, UserHandle.USER_CURRENT);
                    if(rcs!=0) {
                        registerBroadcastReceiver();
                    } else {
                        scrOffHandler.removeCallbacks(scrOffTask);
                        onDestroy();
                    }
                }
            }
        };

        Log.d(TAG, "registerBroadcastReceiver");
        mContext.registerReceiver(mPowerKeyReceiver, theFilter);
    }

    private void unregisterReceiver() {
        try {
            Log.d(TAG, "unregisterReceiver");
            mContext.unregisterReceiver(mPowerKeyReceiver);
        }
        catch (IllegalArgumentException e) {
            mPowerKeyReceiver = null;
        }
    }
    private void updateAccentColor() {
           int color = getRandomColor();
           String hexColor = String.format("%08X", (0xFFFFFFFF & color));
           if(offScheduled){
                scrOffHandler.removeCallbacks(scrOffTask);
           }
           SystemProperties.set(ACCENT_COLOR_PROP, hexColor);
                       try {
                           mOverlayManager.reloadAndroidAssets(UserHandle.USER_CURRENT);
                           mOverlayManager.reloadAssets("com.android.settings", UserHandle.USER_CURRENT);
                           mOverlayManager.reloadAssets("com.android.systemui", UserHandle.USER_CURRENT);
       } catch (RemoteException ignored) { }
    }

    public int getRandomColor(){
        Random rnd = new Random();
            return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
    }
}
