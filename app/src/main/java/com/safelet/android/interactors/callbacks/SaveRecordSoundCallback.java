package com.safelet.android.interactors.callbacks;

public interface SaveRecordSoundCallback {

    int ERROR_UNKNOWN = 0;
    int ERROR_CODE_STOP_RECORDING = 1;
    int ERROR_CODE_STOP_RECORDING_ALARM_DISMISSED = 2;

    void onSuccess();

    void onFailed(int errorCode);
}
