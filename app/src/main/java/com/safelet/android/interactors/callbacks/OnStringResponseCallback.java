package com.safelet.android.interactors.callbacks;

import com.safelet.android.interactors.callbacks.base.BaseResponseCallback;

public interface OnStringResponseCallback extends BaseResponseCallback {
    void onSuccess(String mResponse);
}
