package com.safelet.android.interactors.callbacks;

import com.parse.ParseObject;
import com.safelet.android.interactors.callbacks.base.BaseResponseCallback;

import java.util.List;
import java.util.Map;

public interface OnResponseListCallback extends BaseResponseCallback {

    void onSuccess(Map<String, List<ParseObject>> objects);
}
