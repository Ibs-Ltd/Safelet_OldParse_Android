package com.safelet.android.interactors.base;

import com.safelet.android.R;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.EventBusManager;
import com.safelet.android.interactors.callbacks.base.BaseResponseCallback;
import com.safelet.android.models.event.NoInternetConnectionEvent;
import com.safelet.android.utils.Error;

//import utils.Error;

public class BaseManager {
    protected static final int SUCCESS_RESPONSE = 1;

    /**
     * Checks if device is connected to the internet and sends error if not
     *
     * @param onResponse if null then send broadcast to the application using BUS mechanism
     * @return true if the device has internet connection false otherwise
     */
    protected boolean isConnectedToInternet(BaseResponseCallback onResponse) {
        if (Utils.isOnline()) {
            return true;
        }
        java.lang.Error err = new NoInternetConnectionEvent();
        EventBusManager.instance().postEvent(err);
        if (onResponse != null) {
            onResponse.onFailed(new Error(R.string.home_dialog_message_networkerror));
        }
        return false;
    }
}
