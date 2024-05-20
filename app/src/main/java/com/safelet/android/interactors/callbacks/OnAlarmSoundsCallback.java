package com.safelet.android.interactors.callbacks;

import com.safelet.android.interactors.callbacks.base.BaseResponseCallback;
import com.safelet.android.models.AlarmRecordingChunk;

import java.util.List;

/**
 *
 */
public interface OnAlarmSoundsCallback extends BaseResponseCallback {

    void onSuccess(List<AlarmRecordingChunk> objects);
}
