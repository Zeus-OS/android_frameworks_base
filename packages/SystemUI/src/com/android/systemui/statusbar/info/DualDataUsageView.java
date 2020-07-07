package com.android.systemui.statusbar.info;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.provider.Settings;
import android.os.UserHandle;
import android.telephony.SubscriptionManager;
import android.text.BidiFormatter;
import android.text.format.Formatter;
import android.text.format.Formatter.BytesResult;
import android.util.AttributeSet;
import android.widget.TextView;
import android.content.res.TypedArray;

import com.android.internal.util.zenx.ZenxUtils;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver;
import android.graphics.Rect;
import com.android.settingslib.Utils;

import com.android.systemui.FontSizeUtils;
import android.graphics.Typeface;

public class DualDataUsageView extends TextView implements DarkReceiver  {

    private Context mContext;
    private NetworkController mNetworkController;
    private static boolean shouldUpdateData;
    private String formatedinfo;

    /**
     * Whether we should use colors that adapt based on wallpaper/the scrim behind quick settings
     * for text.
     */
    private boolean mUseWallpaperTextColor;

    /**
     * Color to be set on this {@link TextView}, when wallpaperTextColor is <b>not</b> utilized.
     */
    private int mNonAdaptedColor;

    private final boolean mShowDark;
    private boolean mAttached;

    public int DEFAULT_UD_SIZE = 14;
    private int mUsedDataSizeStatusBar = 14;

    public static final int FONT_NORMAL = 0;
    public static final int FONT_ITALIC = 1;
    public static final int FONT_BOLD = 2;
    public static final int FONT_BOLD_ITALIC = 3;
    public static final int FONT_LIGHT = 4;
    public static final int FONT_LIGHT_ITALIC = 5;
    public static final int FONT_THIN = 6;
    public static final int FONT_THIN_ITALIC = 7;
    public static final int FONT_CONDENSED = 8;
    public static final int FONT_CONDENSED_ITALIC = 9;
    public static final int FONT_CONDENSED_LIGHT = 10;
    public static final int FONT_CONDENSED_LIGHT_ITALIC = 11;
    public static final int FONT_CONDENSED_BOLD = 12;
    public static final int FONT_CONDENSED_BOLD_ITALIC = 13;
    public static final int FONT_MEDIUM = 14;
    public static final int FONT_MEDIUM_ITALIC = 15;
    public static final int FONT_BLACK = 16;
    public static final int FONT_BLACK_ITALIC = 17;
    public static final int FONT_DANCINGSCRIPT = 18;
    public static final int FONT_DANCINGSCRIPT_BOLD = 19;
    public static final int FONT_COMINGSOON = 20;
    public static final int FONT_NOTOSERIF = 21;
    public static final int FONT_NOTOSERIF_ITALIC = 22;
    public static final int FONT_NOTOSERIF_BOLD = 23;
    public static final int FONT_NOTOSERIF_BOLD_ITALIC = 24;
    public static final int GOBOLD_LIGHT = 25;
    public static final int ROADRAGE = 26;
    public static final int SNOWSTORM = 27;
    public static final int GOOGLESANS = 28;
    public static final int NEONEON = 29;
    public static final int THEMEABLE = 30;

    public DualDataUsageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Clock,
                0, 0);
        try {
            mShowDark = a.getBoolean(R.styleable.Clock_showDark, true);
            mNonAdaptedColor = getCurrentTextColor();
        } finally {
            a.recycle();
        }

        mContext = context;
        mNetworkController = Dependency.get(NetworkController.class);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
            updateUsageData();
            setText(formatedinfo);
    }

    private void updateUsageData() {
        DataUsageController mobileDataController = new DataUsageController(mContext);
        mobileDataController.setSubscriptionId(
            SubscriptionManager.getDefaultDataSubscriptionId());
        final DataUsageController.DataUsageInfo info = isDataUsageEnabled() == 0 ?
                (ZenxUtils.isWiFiConnected(mContext) ?
                        mobileDataController.getDailyWifiDataUsageInfo()
                        : mobileDataController.getDailyDataUsageInfo())
                : (ZenxUtils.isWiFiConnected(mContext) ?
                        mobileDataController.getWifiDataUsageInfo()
                        : mobileDataController.getDataUsageInfo());

        formatedinfo = formatDataUsage(info.usageLevel);
    }

    public int isDataUsageEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.DUAL_ROW_DATAUSAGE, 0);
    }

    private String formatDataUsage(long byteValue) {
        final BytesResult res = Formatter.formatBytes(mContext.getResources(), byteValue,
                Formatter.FLAG_IEC_UNITS);
        return BidiFormatter.getInstance().unicodeWrap(res.value + res.units);
    }

    /**
     * Sets whether the clock uses the wallpaperTextColor. If we're not using it, we'll revert back
     * to dark-mode-based/tinted colors.
     *
     * @param shouldUseWallpaperTextColor whether we should use wallpaperTextColor for text color
     */
    public void useWallpaperTextColor(boolean shouldUseWallpaperTextColor) {
        if (shouldUseWallpaperTextColor == mUseWallpaperTextColor) {
            return;
        }
        mUseWallpaperTextColor = shouldUseWallpaperTextColor;

        if (mUseWallpaperTextColor) {
            setTextColor(Utils.getColorAttr(mContext, R.attr.wallpaperTextColor));
        } else {
            setTextColor(mNonAdaptedColor);
        }
    }

    public void onDarkChanged(Rect area, float darkIntensity, int tint) {
        mNonAdaptedColor = DarkIconDispatcher.getTint(area, this, tint);
        if (!mUseWallpaperTextColor) {
            setTextColor(mNonAdaptedColor);
        }
        updateUsageData();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mAttached) {
            mAttached = true;
            if (mShowDark) {
                Dependency.get(DarkIconDispatcher.class).addDarkReceiver(this);
            }
        }
        updateUsageData();
    }

    private void updateSettings() {
        updateUsedDataFontStyle();
        updateUsageData();
        updateUsedDataSize();
    }

    private void updateUsedDataFontStyle() {
        int mUsedDataFontStyle = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.STATUS_BAR_CLOCK_FONT_STYLE, GOOGLESANS,
                UserHandle.USER_CURRENT);
        getUsedDataFontStyle(mUsedDataFontStyle);
        updateUsageData();
    }

    public void updateUsedDataSize() {
        mUsedDataSizeStatusBar = Settings.System.getIntForUser(mContext.getContentResolver(),
                Settings.System.STATUS_BAR_CLOCK_SIZE, DEFAULT_UD_SIZE,
		UserHandle.USER_CURRENT);
        setTextSize(mUsedDataSizeStatusBar);
		updateUsageData();
    }

    public void getUsedDataFontStyle(int font) {
        switch (font) {
            case FONT_NORMAL:
            default:
                setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
                break;
            case FONT_ITALIC:
                setTypeface(Typeface.create("sans-serif", Typeface.ITALIC));
                break;
            case FONT_BOLD:
                setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
                break;
            case FONT_BOLD_ITALIC:
                setTypeface(Typeface.create("sans-serif", Typeface.BOLD_ITALIC));
                break;
            case FONT_LIGHT:
                setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                break;
            case FONT_LIGHT_ITALIC:
                setTypeface(Typeface.create("sans-serif-light", Typeface.ITALIC));
                break;
            case FONT_THIN:
                setTypeface(Typeface.create("sans-serif-thin", Typeface.NORMAL));
                break;
            case FONT_THIN_ITALIC:
                setTypeface(Typeface.create("sans-serif-thin", Typeface.ITALIC));
                break;
            case FONT_CONDENSED:
                setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
                break;
            case FONT_CONDENSED_ITALIC:
                setTypeface(Typeface.create("sans-serif-condensed", Typeface.ITALIC));
                break;
            case FONT_CONDENSED_LIGHT:
                setTypeface(Typeface.create("sans-serif-condensed-light", Typeface.NORMAL));
                break;
            case FONT_CONDENSED_LIGHT_ITALIC:
                setTypeface(Typeface.create("sans-serif-condensed-light", Typeface.ITALIC));
                break;
            case FONT_CONDENSED_BOLD:
                setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD));
                break;
            case FONT_CONDENSED_BOLD_ITALIC:
                setTypeface(Typeface.create("sans-serif-condensed", Typeface.BOLD_ITALIC));
                break;
            case FONT_MEDIUM:
                setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                break;
            case FONT_MEDIUM_ITALIC:
                setTypeface(Typeface.create("sans-serif-medium", Typeface.ITALIC));
                break;
            case FONT_BLACK:
                setTypeface(Typeface.create("sans-serif-black", Typeface.NORMAL));
                break;
            case FONT_BLACK_ITALIC:
                setTypeface(Typeface.create("sans-serif-black", Typeface.ITALIC));
                break;
            case FONT_DANCINGSCRIPT:
                setTypeface(Typeface.create("cursive", Typeface.NORMAL));
                break;
            case FONT_DANCINGSCRIPT_BOLD:
                setTypeface(Typeface.create("cursive", Typeface.BOLD));
                break;
            case FONT_COMINGSOON:
                setTypeface(Typeface.create("casual", Typeface.NORMAL));
                break;
            case FONT_NOTOSERIF:
                setTypeface(Typeface.create("serif", Typeface.NORMAL));
                break;
            case FONT_NOTOSERIF_ITALIC:
                setTypeface(Typeface.create("serif", Typeface.ITALIC));
                break;
            case FONT_NOTOSERIF_BOLD:
                setTypeface(Typeface.create("serif", Typeface.BOLD));
                break;
            case FONT_NOTOSERIF_BOLD_ITALIC:
                setTypeface(Typeface.create("serif", Typeface.BOLD_ITALIC));
                break;
            case GOBOLD_LIGHT:
                setTypeface(Typeface.create("gobold-light-sys", Typeface.NORMAL));
                break;
            case ROADRAGE:
                setTypeface(Typeface.create("roadrage-sys", Typeface.NORMAL));
                break;
            case SNOWSTORM:
                setTypeface(Typeface.create("snowstorm-sys", Typeface.NORMAL));
                break;
            case GOOGLESANS:
                setTypeface(Typeface.create("googlesans-sys", Typeface.NORMAL));
                break;
            case NEONEON:
                setTypeface(Typeface.create("neoneon-sys", Typeface.NORMAL));
                break;
            case THEMEABLE:
                setTypeface(Typeface.create("themeable-sys", Typeface.NORMAL));
                break;
        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mAttached) {
            mAttached = false;
            if (mShowDark) {
                Dependency.get(DarkIconDispatcher.class).removeDarkReceiver(this);
            }
        }
    }
}