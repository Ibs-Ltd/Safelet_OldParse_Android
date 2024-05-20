package com.safelet.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;

import com.safelet.android.interactors.UserManager;
import com.safelet.android.services.SafeletService;


public final class OnBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        //Starts safelet service on boot for receiving notification updates
        if (UserManager.instance().isUserLoggedIn()) {
            ContextCompat.startForegroundService(context, new Intent(context, SafeletService.class));
        }
    }
}
