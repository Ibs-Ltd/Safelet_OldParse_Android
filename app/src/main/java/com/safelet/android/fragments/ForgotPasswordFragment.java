package com.safelet.android.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.activities.SignupActivity;
import com.safelet.android.global.PopDialog;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.utils.Error;

import java.lang.ref.WeakReference;

/**
 * Forgot user password fragment
 * <p/>
 * Created by alin on 13.10.2015.
 */
public class ForgotPasswordFragment extends BaseFragment implements View.OnClickListener {
    private static final String IS_FORGOT_PASSWORD_KEY = "fpf.isForgotPassword.key";

    private EditText mEmailEt;
    private TextView mEmailAlertTv;
    private boolean isForgotPassword;

    public static ForgotPasswordFragment newInstance(boolean isForgotPassword) {
        ForgotPasswordFragment forgotPasswordFragment = new ForgotPasswordFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(IS_FORGOT_PASSWORD_KEY, isForgotPassword);
        forgotPasswordFragment.setArguments(bundle);
        return forgotPasswordFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isForgotPassword = getArguments().getBoolean(IS_FORGOT_PASSWORD_KEY);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmailEt = (EditText) view.findViewById(R.id.forgot_email_et);
        mEmailAlertTv = (TextView) view.findViewById(R.id.forgot_alert_tv);
        mEmailEt.addTextChangedListener(mTextWatcher);
        view.findViewById(R.id.forgot_reset_btn).setOnClickListener(this);
        view.findViewById(R.id.forgot_register_tv).setOnClickListener(this);
        view.findViewById(R.id.forgot_login_tv).setOnClickListener(this);
        setUiWithNav(view, isForgotPassword);
    }

    @Override
    protected void onNavigationDrawerItemSelected(NavigationMenu navigationMenu) {
        // do nothing
    }

    @Override
    protected void onViewNotificationDialogClicked(Context context, NewNotificationEvent event) {
        // do nothing
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.forgot_register_tv) {
            onRegister();
        } else if (v.getId() == R.id.forgot_reset_btn) {
            onResetPassword();
        } else if (v.getId() == R.id.forgot_login_tv) {
            getActivity().finish();
        }
    }

    /**
     * Handle ui type
     *
     * @param nav True if screen is desired to be shown with a navigation bar, false otherwise
     */
    private void setUiWithNav(View view, boolean nav) {
        if (nav) {
            view.findViewById(R.id.forgot_title_tv).setVisibility(View.GONE);
            view.findViewById(R.id.forgot_register_tv).setVisibility(View.GONE);
            view.findViewById(R.id.forgot_login_tv).setVisibility(View.GONE);
            view.findViewById(R.id.forgot_password_iv).setVisibility(View.GONE);
            view.findViewById(R.id.background_wings).setVisibility(View.GONE);
        } else {
            ImageView wings = (ImageView) view.findViewById(R.id.background_wings);
            Animation fadeInAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in_wings);
            wings.startAnimation(fadeInAnimation);
        }
    }

    private void onResetPassword() {
        Utils.hideKeyboard(getActivity());
        final String email = mEmailEt.getEditableText().toString();

        // Email validation
        if (Utils.validateEmailWithString(email)) {
            showLoading();
            UserManager.instance().forgotPasswordForUser(email, new ForgotPasswordCallback(this));
        } else if (TextUtils.isEmpty(email)) {
            setEmailMessage(getString(R.string.forgot_password_alert_emailcannotbeempty));
            setScreenState(ScreenStates.StateEmailWrong);
        } else {
            setEmailMessage(getString(R.string.forgot_password_alert_usernotanemail));
            setScreenState(ScreenStates.StateEmailWrong);
        }
    }

    private void setEmailMessage(String message) {
        if (mEmailAlertTv != null) {
            mEmailAlertTv.setText(message);
        }
    }

    private void setScreenState(ScreenStates screenState) {
        if (mEmailEt == null) {
            return;
        }
        int paddingEmailEt = mEmailEt.getPaddingTop();
        switch (screenState) {
            case StateDefault:
                mEmailEt.setBackgroundResource(R.drawable.input_box);
                mEmailEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                mEmailAlertTv.setVisibility(View.GONE);
                break;
            case StateEmailOk:
                mEmailEt.setBackgroundResource(R.drawable.input_box_accepted);
                mEmailEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                mEmailAlertTv.setVisibility(View.GONE);
                break;
            case StateEmailWrong:
                mEmailEt.setBackgroundResource(R.drawable.input_box_rejected);
                mEmailEt.setTextColor(getResources().getColor(R.color.red_alert_text));
                mEmailAlertTv.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
        //Restores padding
        mEmailEt.setPadding(paddingEmailEt, paddingEmailEt, paddingEmailEt, paddingEmailEt);
    }

    @Override
    public int getTitleResId() {
        return R.string.forgot_password_title;
    }

    private void onRegister() {
        Intent registerIntent = new Intent(getActivity(), SignupActivity.class);
        startActivity(registerIntent);
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    private enum ScreenStates {
        StateDefault,
        StateEmailOk,
        StateEmailWrong
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //Restore text input default colours
            if (mEmailEt.getEditableText().length() == 0) {
                int paddingEmailEt = mEmailEt.getPaddingTop();
                mEmailEt.setBackgroundResource(R.drawable.input_box);
                mEmailEt.setPadding(paddingEmailEt, paddingEmailEt, paddingEmailEt, paddingEmailEt);
                mEmailEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            //No need to handle
        }

        @Override
        public void afterTextChanged(Editable s) {
            //No need to handle
        }
    };

    private static class ForgotPasswordCallback implements OnResponseCallback {
        private WeakReference<ForgotPasswordFragment> weakReference;

        ForgotPasswordCallback(ForgotPasswordFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onSuccess(ParseObject object) {
            final ForgotPasswordFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            // Updates the user interface with success state and displays confirmation dialog
            fragment.setScreenState(ScreenStates.StateEmailOk);
            PopDialog.showDialog(fragment.getActivity(),
                    fragment.getString(R.string.forgot_password_dialog_title),
                    fragment.getString(R.string.forgot_password_dialog_message_successfullyupdated),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fragment.getActivity().finish();
                        }
                    });
        }

        @Override
        public void onFailed(Error error) {
            final ForgotPasswordFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            // Changes screen state to error state
            fragment.setScreenState(ScreenStates.StateEmailWrong);
            fragment.setEmailMessage(error.getErrorMessage(fragment.getActivity()));
            fragment.hideLoading();
        }
    }
}