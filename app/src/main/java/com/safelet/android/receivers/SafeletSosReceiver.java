package com.safelet.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.safelet.android.interactors.EventBusManager;
import com.safelet.android.models.event.StartAlarmEvent;


public class SafeletSosReceiver extends BroadcastReceiver {

    private static final String ACTION_SOS = "android.intent.action.soskeydown";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_SOS.equals(intent.getAction())) {
            EventBusManager.instance().postEvent(new StartAlarmEvent());
        }
    }
}
