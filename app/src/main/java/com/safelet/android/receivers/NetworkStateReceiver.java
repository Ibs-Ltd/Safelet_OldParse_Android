package com.safelet.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.safelet.android.global.Utils;
import com.safelet.android.interactors.EventBusManager;
import com.safelet.android.models.event.NetworkAvailableEvent;
import com.safelet.android.models.event.NoInternetConnectionEvent;

public final class NetworkStateReceiver extends BroadcastReceiver {
    /**
     * this is used for an workaround because some devices send the CONNECTIVITY_CHANGE broadcast
     * multiple times with the same status
     */
    private static boolean connectedToNetwork = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Utils.isOnline() && !connectedToNetwork) {
            connectedToNetwork = true;
            EventBusManager.instance().postEvent(new NetworkAvailableEvent());
        } else if (connectedToNetwork) {
            EventBusManager.instance().postEvent(new NoInternetConnectionEvent());
            connectedToNetwork = false;
        }
    }
}
