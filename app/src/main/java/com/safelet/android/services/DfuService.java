package com.safelet.android.services;

import android.app.Activity;

import com.safelet.android.BuildConfig;
import com.safelet.android.activities.NotificationActivity;

import no.nordicsemi.android.dfu.DfuBaseService;

public class DfuService extends DfuBaseService {

    @Override
    protected Class<? extends Activity> getNotificationTarget() {
        return NotificationActivity.class;
    }

    @Override
    protected boolean isDebug() {
        return BuildConfig.DEBUG;
    }
}