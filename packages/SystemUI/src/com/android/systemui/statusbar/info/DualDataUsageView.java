package com.android.systemui.statusbar.info;


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.provider.Settings;
import android.telephony.SubscriptionManager;
import android.text.BidiFormatter;
import android.text.format.Formatter;
import android.text.format.Formatter.BytesResult;
import android.util.AttributeSet;
import android.widget.TextView;

import com.android.internal.util.zenx.ZenxUtils;
import com.android.settingslib.net.DataUsageController;
import com.android.systemui.Dependency;
import com.android.systemui.R;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.plugins.DarkIconDispatcher;
import com.android.systemui.plugins.DarkIconDispatcher.DarkReceiver;
import android.graphics.Rect;

public class DualDataUsageView extends TextView {

    private Context mContext;
    private NetworkController mNetworkController;
    private static boolean shouldUpdateData;
    private String formatedinfo;
    private int mNonAdaptedColor;

    public DualDataUsageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mNonAdaptedColor = getCurrentTextColor();
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

    public void onDarkChanged(Rect area, float darkIntensity, int tint) {
        mNonAdaptedColor = DarkIconDispatcher.getTint(area, this, tint);
        setTextColor(mNonAdaptedColor);
        updateUsageData();
    }
}