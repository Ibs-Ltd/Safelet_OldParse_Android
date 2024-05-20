package com.safelet.android.models.event;

import com.safelet.android.R;
import com.safelet.android.global.ApplicationSafelet;

/**
 * Represents a signal used with eventbus {@link org.greenrobot.eventbus} for telling the app that the internet connection is not available
 */
public class NoInternetConnectionEvent extends Error {

    public NoInternetConnectionEvent() {
        super(ApplicationSafelet.getContext().getString(R.string.general_nointernetconnection));
    }
}
