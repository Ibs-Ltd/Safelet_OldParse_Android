package com.safelet.android.interactors.callbacks;

import com.safelet.android.interactors.callbacks.base.BaseResponseCallback;
import com.safelet.android.models.UserModel;

import java.util.List;

public interface OnAlarmParticipantsCallback extends BaseResponseCallback {

    void onSuccess(List<UserModel> objects);
}
