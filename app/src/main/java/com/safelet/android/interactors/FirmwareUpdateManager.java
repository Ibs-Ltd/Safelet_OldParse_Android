package com.safelet.android.interactors;

import com.parse.FunctionCallback;
import com.parse.GetDataCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.safelet.android.interactors.base.BaseManager;
import com.safelet.android.interactors.utils.ParseConstants;
import com.safelet.android.models.Firmware;
import com.safelet.android.models.event.bluetooth.FirmwareUpdateEvent;

import java.util.HashMap;
import java.util.Map;

public class FirmwareUpdateManager extends BaseManager {
    private static final String TAG = "FirmwareUpdate";

    private static FirmwareUpdateManager sInstance;

    public static FirmwareUpdateManager instance() {
        if (sInstance == null) {
            sInstance = new FirmwareUpdateManager();
        }
        return sInstance;
    }

    public void checkForFirmwareUpdate(String modelNumber, String hardwareRevision, String firmwareRevision, String versionCode,
                                       final OnUpdateFirmwareListener listener) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.MODEL_KEY, modelNumber);
        parameters.put(ParseConstants.HARDWARE_REVISION_KEY, hardwareRevision);
        parameters.put(ParseConstants.FIRMWARE_REVISION_KEY, firmwareRevision);
        parameters.put(ParseConstants.VERSION_CODE_KEY, versionCode);
        ParseCloud.callFunctionInBackground(ParseConstants.GET_LATEST_FIRMWARE, parameters, new FunctionCallback<Firmware>() {
            @Override
            public void done(Firmware firmware, ParseException e) {
                if (e != null || firmware == null) {
                    listener.onNoUpdateAvailable();
                } else {
                    firmware.getUpdateFile().getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] data, ParseException exception) {
                            if (exception == null) {
                                listener.onUpdateAvailable(data);
                            } else {
                                listener.onNoUpdateAvailable();
                            }
                        }
                    });
                }
            }
        });
    }

    public void sendUpdateSignal(byte[] firmware) {
        EventBusManager.instance().postEvent(new FirmwareUpdateEvent(firmware));
    }

    public interface OnUpdateFirmwareListener {
        void onUpdateAvailable(byte[] firmware);

        void onNoUpdateAvailable();
    }
}
