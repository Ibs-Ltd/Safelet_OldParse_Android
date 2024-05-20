package com.safelet.android.interactors;

import android.os.Build;

import com.parse.ParseCloud;
import com.safelet.android.BuildConfig;
import com.safelet.android.interactors.utils.ParseConstants;

import java.util.HashMap;
import java.util.Map;

public class DeviceInformationsManager {

    private static DeviceInformationsManager sInstance;

    public static DeviceInformationsManager instance() {
        if (sInstance == null) {
            sInstance = new DeviceInformationsManager();
        }
        return sInstance;
    }

    public void uploadPhoneDetails() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.DEVICE_PARAM_KEY, Build.MANUFACTURER);
        parameters.put(ParseConstants.MODEL_PARAM_KEY, Build.MODEL);
        parameters.put(ParseConstants.OS_VERSION_PARAM_KEY, "aos v" + Build.VERSION.RELEASE + " lvl" + Build.VERSION.SDK_INT);
        parameters.put(ParseConstants.APP_VERSION_PARAM_KEY, BuildConfig.VERSION_NAME);
        ParseCloud.callFunctionInBackground(ParseConstants.SAVE_PHONE_DETAILS, parameters);
    }
}
