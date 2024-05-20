package com.safelet.android.interactors.callbacks;

import com.parse.ParseObject;
import com.safelet.android.interactors.callbacks.base.BaseResponseCallback;


public interface OnResponseCallback extends BaseResponseCallback {

    void onSuccess(ParseObject object);

}
