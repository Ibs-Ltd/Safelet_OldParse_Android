package com.safelet.android.interactors.callbacks.base;

//import utils.Error;

import com.safelet.android.utils.Error;

public interface BaseResponseCallback {
    void onFailed(Error error);
}
