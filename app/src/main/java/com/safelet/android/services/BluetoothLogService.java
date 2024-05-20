package com.safelet.android.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.safelet.android.global.PreferencesManager;
import com.safelet.android.interactors.LoggerManager;

public class BluetoothLogService extends IntentService {

    private static final String ACTION_UPLOAD_BLUETOOTH_LOG = "upload_bluetooth_log";

    public static Intent getUploadBluetoothLogIntent(Context context) {
        Intent intent = new Intent(ACTION_UPLOAD_BLUETOOTH_LOG);
        intent.setClass(context, BluetoothLogService.class);
        return intent;
    }

    public BluetoothLogService() {
        super(BluetoothLogService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        switch (intent.getAction()) {
            case ACTION_UPLOAD_BLUETOOTH_LOG:
                String firmwareRevision = PreferencesManager.instance(this).getString(PreferencesManager.DEVICE_FIRMWARE_REVISION, "-");
                int firmwareVersionCode = PreferencesManager.instance(this).getInt(PreferencesManager.DEVICE_FIRMWARE_VERSION_CODE);
                LoggerManager.uploadBluetoothFailedLogFile(firmwareRevision + " (" + firmwareVersionCode + ")");
                break;
        }
    }


}
