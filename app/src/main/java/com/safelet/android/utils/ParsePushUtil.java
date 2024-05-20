package com.safelet.android.utils;

import android.text.TextUtils;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.parse.ParseInstallation;
import com.parse.ParsePush;

import java.util.List;

public class ParsePushUtil {

    private static final String CHANNELS = "channels";

    public static void subscribeInBackground(String channel) {
        if (TextUtils.isEmpty(channel)) {
            FirebaseCrashlytics.getInstance().log("Empty channel provided");
        }
        boolean subscribed = false;
        if (ParseInstallation.getCurrentInstallation() != null) {
            List<String> channels = ParseInstallation.getCurrentInstallation().getList(CHANNELS);
            if (channels != null) {
                subscribed = channels.contains(channel);
                for (String oldChannel : channels) {
                    if (!TextUtils.isEmpty(oldChannel) && !oldChannel.equals(channel)) {
                        ParsePush.unsubscribeInBackground(oldChannel);
                    }
                }
            }
        }
        if (!subscribed) {
            ParsePush.subscribeInBackground(channel);
        }
    }

}
