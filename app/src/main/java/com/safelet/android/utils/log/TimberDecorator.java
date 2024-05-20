package com.safelet.android.utils.log;

import timber.log.Timber;

public class TimberDecorator {
    public static void d(String tag, String message) {
        Timber.tag(tag);
        Timber.d(message);
    }

    public static void d(String tag, String message, Exception e) {
        Timber.tag(tag);
        Timber.d(e, message);
    }

    public static void e(String tag, String message) {
        Timber.tag(tag);
        Timber.e(message);
    }

    public static void w(String tag, String message) {
        Timber.tag(tag);
        Timber.w(message);
    }
}
