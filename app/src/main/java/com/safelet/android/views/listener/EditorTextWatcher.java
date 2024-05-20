package com.safelet.android.views.listener;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

public class EditorTextWatcher implements TextWatcher {

    private View view;

    public EditorTextWatcher(View view) {
        this.view = view;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        view.setEnabled(!TextUtils.isEmpty(s.toString().trim()));
    }
}
