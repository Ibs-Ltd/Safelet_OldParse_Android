package com.safelet.android.interactors.callbacks;

import com.safelet.android.interactors.callbacks.base.BaseResponseCallback;

public interface OnFirebaseCallback extends BaseResponseCallback {

    void onSuccess(String token);

}
