/*
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (C) 2020 ZenX-OS
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

package com.android.internal.util.zenx;

import android.os.UserHandle;

import android.app.ActivityManager;
import android.app.UiModeManager;
import android.content.Context;
import android.content.om.OverlayManager;
import android.content.om.OverlayInfo;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;

public class ThemesUtils {

    public static final String TAG = "ThemesUtils";

    public static final String[] SOLARIZED_DARK = {
            "com.android.theme.solarizeddark.system",
            "com.android.theme.solarizeddark.systemui",
    };

    public static final String[] BAKED_GREEN = {
            "com.android.theme.bakedgreen.system",
            "com.android.theme.bakedgreen.systemui",
    };

    public static final String[] CHOCO_X = {
            "com.android.theme.chocox.system",
            "com.android.theme.chocox.systemui",
    };

    public static final String[] PITCH_BLACK = {
            "com.android.theme.pitchblack.system",
            "com.android.theme.pitchblack.systemui",
    };

    public static final String[] DARK_GREY = {
            "com.android.theme.darkgrey.system",
            "com.android.theme.darkgrey.systemui",
    };

    public static final String[] MATERIAL_OCEAN = {
            "com.android.theme.materialocean.system",
            "com.android.theme.materialocean.systemui",
    };

    // Switch themes
    private static final String[] SWITCH_THEMES = {
        "com.android.system.switch.stock", // 0
	    "com.android.system.switch.narrow", // 1
        "com.android.system.switch.contained", // 2
        "com.android.system.switch.telegram", // 3
        "com.android.system.switch.md2", // 4
        "com.android.system.switch.retro", // 5
    };

        public static final String[] QS_TILE_THEMES = {
            "com.android.systemui.qstile.default",
            "com.android.systemui.qstile.square",
            "com.android.systemui.qstile.squircletrim",
            "com.android.systemui.qstile.diamond",
            "com.android.systemui.qstile.star",
            "com.android.systemui.qstile.gear",
            "com.android.systemui.qstile.badge",
            "com.android.systemui.qstile.badgetwo",
            "com.android.systemui.qstile.circletrim",
            "com.android.systemui.qstile.dualtonecircletrim",
            "com.android.systemui.qstile.cookie",
            "com.android.systemui.qstile.circleoutline",
            "com.android.systemui.qstile.wavey",
            "com.android.systemui.qstile.ninja",
            "com.android.systemui.qstile.dottedcircle",
            "com.android.systemui.qstile.attemptmountain",
            "com.android.systemui.qstile.inktober",
            "com.android.systemui.qstile.neonlike",
            "com.android.systemui.qstile.triangles",
            // "com.android.systemui.qstile.deletround",
            "com.android.systemui.qstile.bottom_triangle",
            "com.android.systemui.qstile.oos",
    };

    public static final String[] UI_THEMES = {
            "com.android.systemui.ui.default",
            "com.android.systemui.ui.nocornerradius",
            "com.android.systemui.ui.rectangle",
            "com.android.systemui.ui.aosp",
            "com.android.systemui.ui.roundmedium",
    };

    public static final String[] BRIGHTNESS_SLIDER_THEMES = {
            "com.android.systemui.brightness.slider.default",
            "com.android.systemui.brightness.slider.daniel",
            "com.android.systemui.brightness.slider.mememini",
            "com.android.systemui.brightness.slider.memeround",
            "com.android.systemui.brightness.slider.memeroundstroke",
            "com.android.systemui.brightness.slider.memestroke",
    };

    // Navbar styles
    public static final String[] NAVBAR_STYLES = {
        "com.android.system.navbar.stock", //0
        "com.android.system.navbar.asus", //1
        "com.android.system.navbar.oneplus", //2
        "com.android.system.navbar.oneui", //3
        "com.android.system.navbar.tecno", //4
    };

    public static void updateSwitchStyle(OverlayManager om, int switchStyle) {
        UserHandle userId = UserHandle.of(ActivityManager.getCurrentUser());
        if (switchStyle == 0) {
            stockSwitchStyle(om, userId);
        } else {
            try {
                om.setEnabled(SWITCH_THEMES[switchStyle],
                        true, userId);
            } catch (Exception e) {
                Log.w(TAG, "Can't change switch theme", e);
            }
        }
    }

    public static void stockSwitchStyle(OverlayManager om, UserHandle userId) {
        for (int i = 0; i < SWITCH_THEMES.length; i++) {
            String switchtheme = SWITCH_THEMES[i];
            try {
                om.setEnabled(switchtheme,
                        false /*disable*/, userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateUIStyle(OverlayManager om, int uiStyle) {
        UserHandle userId = UserHandle.of(ActivityManager.getCurrentUser());
        if (uiStyle == 0) {
            stockUIStyle(om, userId);
        } else {
            try {
                om.setEnabled(UI_THEMES[uiStyle],
                        true, userId);
            } catch (Exception e) {
                Log.w(TAG, "Can't change switch theme", e);
            }
        }
    }

    public static void stockUIStyle(OverlayManager om, UserHandle userId) {
        for (int i = 0; i < UI_THEMES.length; i++) {
            String uitheme = UI_THEMES[i];
            try {
                om.setEnabled(uitheme,
                        false /*disable*/, userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void updateBrightnessSliderStyle(OverlayManager om, int brightnessSliderStyle) {
        UserHandle userId = UserHandle.of(ActivityManager.getCurrentUser());
        if (brightnessSliderStyle == 0) {
            stockBrightnessSliderStyle(om, userId);
        } else {
            try {
                om.setEnabled(UI_THEMES[brightnessSliderStyle],
                        true, userId);
            } catch (Exception e) {
                Log.w(TAG, "Can't change brightness slider theme", e);
            }
        }
    }

    public static void stockBrightnessSliderStyle(OverlayManager om, UserHandle userId) {
        for (int i = 0; i < UI_THEMES.length; i++) {
            String brightnessSlidertheme = BRIGHTNESS_SLIDER_THEMES[i];
            try {
                om.setEnabled(brightnessSlidertheme,
                        false /*disable*/, userId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

        // Unloads the navbar styles
    private static void unloadNavbarStyle(OverlayManager om, UserHandle userId) {
        for (String style : NAVBAR_STYLES) {
            try {
                om.setEnabled(style, false, userId);
            } catch (Exception e) {
            }
        }
    }

    // Set navbar style
    public static void setNavbarStyle(OverlayManager om, UserHandle userId, int navbarStyle) {
        // Always unload navbar styles
        unloadNavbarStyle(om, userId);

        if (navbarStyle == 0) return;

        try {
            om.setEnabled(NAVBAR_STYLES[navbarStyle], true, userId);
        } catch (Exception e) {
        }
    }

}
