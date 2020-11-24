package com.android.systemui.statusbar.info;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.os.AsyncTask;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.text.BidiFormatter;
import android.text.format.Formatter;
import android.text.format.Formatter.BytesResult;
import android.widget.TextView;
import android.provider.Settings;
import android.view.View;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver;
import com.android.systemui.statusbar.StatusIconDisplayable;
import android.graphics.Rect;
import android.content.res.Resources;
import android.os.UserHandle;
import android.content.res.TypedArray;
import com.android.settingslib.Utils;

import com.android.internal.util.zenx.ZenxUtils;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import android.graphics.Rect;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NetworkController.SignalCallback;
import com.android.settingslib.net.DataUsageController;

public class DualDataUsageView extends TextView implements DarkReceiver {


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

    private Context mContext;
    private NetworkController mNetworkController;
    private static boolean shouldUpdateData;
    private String formatedinfo;
    private Handler mHandler;
    private static long mTime;
    private int mTintColor;
    private final Resources resources;

    public DualDataUsageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        resources = getResources();

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
        mHandler = new Handler();
        mTintColor = resources.getColor(android.R.color.white);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if ((isDataUsageEnabled() == 0) && this.getText().toString() != "") {
            setText("No Data");
        }
        if (isDataUsageEnabled() != 0) {
            if(shouldUpdateData) {
                shouldUpdateData = false;
                updateDataUsage();
            } else {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateDataUsage();
                    }
                }, 2000);
            }
        }
    }

    private void updateDataUsage() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                updateUsageData();
            }
        });
        setText(formatedinfo);
        setTextColor(mTintColor);
    }

    private void updateUsageData() {
        DataUsageController mobileDataController = new DataUsageController(mContext);
        mobileDataController.setSubscriptionId(
            SubscriptionManager.getDefaultDataSubscriptionId());
        final DataUsageController.DataUsageInfo info = isDataUsageEnabled() == 1 ?
                (ZenxUtils.isWiFiConnected(mContext) ?
                        mobileDataController.getDailyWifiDataUsageInfo()
                        : mobileDataController.getDailyDataUsageInfo())
                : (ZenxUtils.isWiFiConnected(mContext) ?
                        mobileDataController.getWifiDataUsageInfo()
                        : mobileDataController.getDataUsageInfo());
        formatedinfo = formatDataUsage(info.usageLevel) + " ";
    }

    public int isDataUsageEnabled() {
        return Settings.System.getInt(mContext.getContentResolver(),
                Settings.System.DUAL_ROW_DATAUSAGE, 1);
    }

    public static void updateUsage() {
        // limit to one update per second
        long time = System.currentTimeMillis();
        if (time - mTime > 1000) {
            shouldUpdateData = true;
        }
        mTime = time;
    }

    private CharSequence formatDataUsage(long byteValue) {
        final BytesResult res = Formatter.formatBytes(mContext.getResources(), byteValue,
                Formatter.FLAG_IEC_UNITS);
        return BidiFormatter.getInstance().unicodeWrap(mContext.getString(
                com.android.internal.R.string.fileSizeSuffix, res.value, res.units));
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
