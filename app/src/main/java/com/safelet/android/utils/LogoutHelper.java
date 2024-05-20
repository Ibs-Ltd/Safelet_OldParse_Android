package com.safelet.android.utils;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.safelet.android.R;
import com.safelet.android.activities.LoginCreateActivity;
import com.safelet.android.global.PopDialog;
import com.safelet.android.interactors.AlarmManager;
import com.safelet.android.interactors.EventBusManager;
import com.safelet.android.interactors.EventsManager;
import com.safelet.android.interactors.MessageManager;
import com.safelet.android.interactors.PhoneContactsManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.models.event.LogoutEvent;
import com.safelet.android.services.LocationUpdatesIntentService;
import com.safelet.android.services.RecordSoundService;
import com.safelet.android.services.SafeletService;

import global.BluetoothState;

public class LogoutHelper {

    public static final String TAG = LogoutHelper.class.getSimpleName();

    public static void logout(Context context) {

        context.stopService(new Intent(context, RecordSoundService.class));

        EventBusManager.instance().postEvent(new LogoutEvent());

        BluetoothState.get().reset();

        ParsePush.unsubscribeInBackground(UserManager.instance().getUserId());

        AlarmManager.instance().logout();
        UserManager.instance().logout();
        EventsManager.instance().logout();
        PhoneContactsManager.instance().clear();
        MessageManager.instance().logout();

        FirebaseCrashlytics.getInstance().setUserId(null);
//        FirebaseCrashlytics.getInstance().setCustomKey("setUserIdentifier", "null");
        FirebaseCrashlytics.getInstance().setCustomKey("setUserEmail", "null");
        FirebaseCrashlytics.getInstance().setCustomKey("setUserName", "null");

        Intent serviceSafeletService = new Intent(context, SafeletService.class);
        context.stopService(serviceSafeletService);

//        PreferencesManager.instance(context).setFollowMeaObjectId("");
        context.stopService(new Intent(context, LocationUpdatesIntentService.class));

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancelAll();

//        PreferencesManager.instance(context).setGuardianNetworkSkip(false);
        Intent startIntent = new Intent(context, LoginCreateActivity.class);
        startIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(startIntent);
        ((Activity) context).finish();
    }

    public static boolean handleExpiredSession(final Context context, Error error) {
        if (error != null && error.getParseErrorCode() == ParseException.INVALID_SESSION_TOKEN) {
            PopDialog.showDialog(context,
                    context.getString(R.string.invalid_session_title),
                    context.getString(R.string.invalid_session_message),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            logout(context);
                        }
                    });
            return true;
        }
        return false;
    }
}
