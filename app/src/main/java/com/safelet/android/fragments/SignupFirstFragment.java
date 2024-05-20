package com.safelet.android.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatCheckBox;

import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.activities.AcceptTermsAndConditionActivity;
import com.safelet.android.activities.LoginActivity;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.global.DKPassword;
import com.safelet.android.global.PopDialog;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.utils.Error;

import java.lang.ref.WeakReference;
import java.util.Locale;

/**
 * Sign up user fragment
 * <p/>
 * Created by Alin on 10/18/2015.
 */
public class SignupFirstFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "SignupFirstFragment";
    private static final int PASSWORD_REQUIRED_LENGTH = 3;
    private boolean mPasswordTooShort = false;

    private EditText mEmailEt;
    private EditText mPasswordEt;
    private EditText mPasswordRetypeEt;
    private TextView mEmailAlertTv;
    private TextView mPasswordAlertTv, txtTermsAndConMsg;
    private LinearLayout txtTermsAndCon;
    private AppCompatCheckBox cbTermsAndCon;

    private BaseActivity activity;

    private final UserManager userManager = UserManager.instance();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.activity = (BaseActivity) activity;
        } catch (ClassCastException e) {
            Log.e(TAG, "SignupFirstFragment should be inflated in BaseActivity");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_first, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEmailEt = view.findViewById(R.id.register_email_et);
        mPasswordEt = view.findViewById(R.id.register_password_et);
        mPasswordRetypeEt = view.findViewById(R.id.register_repeat_password_et);

        //Label for displaying errors regarding user strength password
        mEmailAlertTv = view.findViewById(R.id.register_email_alert_tv);
        mPasswordAlertTv = view.findViewById(R.id.register_password_alert_tv);

        cbTermsAndCon = view.findViewById(R.id.cbTermsAndCon);
        txtTermsAndCon = view.findViewById(R.id.txtTermsAndCon);
        txtTermsAndConMsg = view.findViewById(R.id.txtTermsAndConMsg);
        txtTermsAndCon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent iTermsAndCon = new Intent(getContext(), AcceptTermsAndConditionActivity.class);
                startActivity(iTermsAndCon);
            }
        });

        mEmailEt.addTextChangedListener(new EmailTextWatcher(this));
        mPasswordEt.addTextChangedListener(new PasswordTextWatcher(this));
        mPasswordRetypeEt.addTextChangedListener(new PasswordTextWatcher(this));

        view.findViewById(R.id.register_login_tv).setOnClickListener(this);
        view.findViewById(R.id.register_create_btn).setOnClickListener(this);

        setScreenState(ScreenStates.StateDefault);
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
        if (v.getId() == R.id.register_login_tv) {
            onLogin();
        } else if (v.getId() == R.id.register_create_btn) {
            onSignUp();
        }
    }

    @Override
    public int getTitleResId() {
        return NO_TITLE;
    }

    private void onSignUp() {
        if (!Utils.isOnline()) {
            PopDialog.showDialog(getActivity(),
                    getString(R.string.home_dialog_title),
                    getString(R.string.home_dialog_message_networkerror));
            return;
        }
        Utils.hideKeyboard(getActivity());
        String username = mEmailEt.getEditableText().toString().toLowerCase(Locale.getDefault());
        String password = mPasswordEt.getEditableText().toString();
        String passwordRetype = mPasswordRetypeEt.getEditableText().toString();

        txtTermsAndConMsg.setVisibility(View.GONE);

        // Verifies data from user and updates the screen according user input
        if (password.equals(passwordRetype)
                && !TextUtils.isEmpty(password)
                && Utils.validateEmailWithString(username)
                && !mPasswordTooShort
                && cbTermsAndCon.isChecked()) {
            showLoading();
            userManager.checkIfUserExist(username, cbTermsAndCon.isChecked(), new CheckIfUserExistCallback(this, username, password, cbTermsAndCon.isChecked()));
        } else if (TextUtils.isEmpty(username) && TextUtils.isEmpty(password)) {
            setScreenState(ScreenStates.StateEmailWrongPasswordWrong);
            setEmailAlertMessage(getString(R.string.signup_first_alert_message_emailcannotbeempty));
            setPasswordAlertMessage(getString(R.string.signup_first_alert_message_passwordcannotbeempty));
        } else if (TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            setScreenState(ScreenStates.StateEmailWrongPasswordWrong);
            setEmailAlertMessage(getString(R.string.signup_first_alert_message_emailcannotbeempty));
        } else if (TextUtils.isEmpty(password) && !Utils.validateEmailWithString(username)) {
            setScreenState(ScreenStates.StateEmailWrongPasswordWrong);
            setEmailAlertMessage(getString(R.string.signup_first_alert_message_incorrectemailaddress));
            setPasswordAlertMessage(getString(R.string.signup_first_alert_message_passwordcannotbeempty));
        } else if (!password.equals(passwordRetype) && !Utils.validateEmailWithString(username)) {
            setScreenState(ScreenStates.StateEmailWrongPasswordWrong);
            setEmailAlertMessage(getString(R.string.signup_first_alert_message_incorrectemailaddress));
            setPasswordAlertMessage(getString(R.string.signup_first_alert_message_passwordsdonotmatch));
        } else if (TextUtils.isEmpty(password) && Utils.validateEmailWithString(username)) {
            setScreenState(ScreenStates.StateEmailOkPasswordWrong);
            setPasswordAlertMessage(getString(R.string.signup_first_alert_message_passwordcannotbeempty));
        } else if (!password.equals(passwordRetype) && Utils.validateEmailWithString(username)) {
            setScreenState(ScreenStates.StateEmailOkPasswordWrong);
            setPasswordAlertMessage(getString(R.string.signup_first_alert_message_passwordsdonotmatch));
        } else if (!Utils.validateEmailWithString(username)) {
            setScreenState(ScreenStates.StateEmailWrongPasswordOk);
            setEmailAlertMessage(getString(R.string.signup_first_alert_message_incorrectemailaddress));
        } else if (!cbTermsAndCon.isChecked()) {
            txtTermsAndConMsg.setVisibility(View.VISIBLE);
        } else {
            setScreenState(ScreenStates.StateDefault);
        }
    }

    private void onLogin() {
        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }

    /**
     * Checks password strength when the password text input is changed
     */
    private void passwordChanged() {
        String passText = mPasswordEt.getEditableText().toString();
        String passRetypeText = mPasswordRetypeEt.getEditableText().toString();

        String passwordStrengthSentence = "";
        // Validate and checks password strength updating the user interface according to them
        if (passText.equals(passRetypeText) && !TextUtils.isEmpty(passText) && !TextUtils.isEmpty(passRetypeText)
                && (passText.length() > PASSWORD_REQUIRED_LENGTH || passRetypeText.length() > PASSWORD_REQUIRED_LENGTH)) {
            mPasswordTooShort = false;
            int value = DKPassword.passwordStrength(passText);
            int color = 0;
            if (value < 25) {
                color = Color.rgb(244, 0, 0);
                passwordStrengthSentence = getString(R.string.signup_first_alert_message_passwordweak);
            } else if (value >= 25 && value <= 50) {
                color = Color.rgb(234, 176, 0);
                passwordStrengthSentence = getString(R.string.signup_first_alert_message_passwordgood);
            } else if (value > 50 && value <= 75) {
                color = Color.rgb(45, 173, 0);
                passwordStrengthSentence = getString(R.string.signup_first_alert_message_passwordstrong);
            } else if (value > 100) {
                color = Color.rgb(0, 45, 173);
                passwordStrengthSentence = getString(R.string.signup_first_alert_message_passwordexcelent);
            }
            setPasswordAlertMessage(passwordStrengthSentence);
            setPasswordAlertMessageColor(color);
            setPasswordAlertMessageVisible(true);
        } else if (passText.length() <= PASSWORD_REQUIRED_LENGTH) {
            setPasswordAlertMessageVisible(true);
            setPasswordAlertMessageColor(Color.BLACK);
            setPasswordAlertMessage(getString(R.string.signup_first_alert_message_passwordtoosmall));
        } else if (TextUtils.isEmpty(passText) || TextUtils.isEmpty(passRetypeText)) {
            setPasswordAlertMessageVisible(true);
            setPasswordAlertMessageColor(Color.BLACK);
            setPasswordAlertMessage(getString(R.string.signup_first_alert_message_passwordoneofthefieldsempty));
        } else if (!passText.equals(passRetypeText)) {
            setPasswordAlertMessageVisible(true);
            setPasswordAlertMessageColor(Color.BLACK);
            setPasswordAlertMessage(getString(R.string.signup_first_alert_message_passwordsdonotmatch));
        } else if (passText.equals(passRetypeText) && (!TextUtils.isEmpty(passText) && !TextUtils.isEmpty(passRetypeText))
                && (passText.length() <= PASSWORD_REQUIRED_LENGTH || passRetypeText.length() <= PASSWORD_REQUIRED_LENGTH)) {
            setPasswordAlertMessageVisible(true);
            setPasswordAlertMessageColor(Color.BLACK);
            setPasswordAlertMessage(getString(R.string.signup_first_alert_message_passwordtoosmall));
            mPasswordTooShort = true;
        }
    }

    /**
     * Display alert message for errors on email
     *
     * @param message Text message
     */
    private void setEmailAlertMessage(String message) {
        if (mEmailAlertTv != null) {
            mEmailAlertTv.setText(message);
        }
    }

    /**
     * Display alert message for errors on password
     *
     * @param message Text message
     */
    private void setPasswordAlertMessage(String message) {
        if (mPasswordAlertTv != null) {
            mPasswordAlertTv.setText(message);
        }
    }

    /**
     * Controls visibility of alert text message for errors on password
     *
     * @param visible Visibility status
     */
    private void setPasswordAlertMessageVisible(boolean visible) {
        if (mPasswordAlertTv != null) {
            if (visible) {
                mPasswordAlertTv.setVisibility(View.VISIBLE);
            } else {
                mPasswordAlertTv.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Changes color of alert text message for errors on password
     *
     * @param colour Desired colour
     */
    private void setPasswordAlertMessageColor(int colour) {
        if (mPasswordAlertTv != null) {
            mPasswordAlertTv.setTextColor(colour);
        }
    }

    /**
     * Changes screen state based on ScreenStates enum
     *
     * @param screenState Screen state based on ScreenStates enum
     */
    private void setScreenState(ScreenStates screenState) {
        if (mEmailEt == null) {
            return;
        }
        int paddingEmailEt = mEmailEt.getPaddingTop();
        int paddingPasswordEt = mPasswordEt.getPaddingTop();
        switch (screenState) {
            case StateDefault:
                mEmailEt.setBackgroundResource(R.drawable.input_box);
                mPasswordEt.setBackgroundResource(R.drawable.input_box);
                mPasswordRetypeEt.setBackgroundResource(R.drawable.input_box);
                mEmailAlertTv.setVisibility(View.GONE);
                mPasswordAlertTv.setVisibility(View.GONE);
                mEmailEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                mPasswordEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                break;
            case StateEmailOkPasswordOk:
                mEmailEt.setBackgroundResource(R.drawable.input_box_accepted);
                mPasswordEt.setBackgroundResource(R.drawable.input_box_accepted);
                mPasswordRetypeEt.setBackgroundResource(R.drawable.input_box_accepted);
                mEmailAlertTv.setVisibility(View.GONE);
                mPasswordAlertTv.setVisibility(View.GONE);
                mEmailEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                mPasswordEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                break;
            case StateEmailWrongPasswordOk:
                mEmailEt.setBackgroundResource(R.drawable.input_box_rejected);
                mPasswordEt.setBackgroundResource(R.drawable.input_box_accepted);
                mPasswordRetypeEt.setBackgroundResource(R.drawable.input_box_accepted);
                mEmailAlertTv.setVisibility(View.VISIBLE);
                mPasswordAlertTv.setVisibility(View.GONE);
                mEmailEt.setTextColor(getResources().getColor(R.color.red_alert_text));
                mPasswordEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                break;
            case StateEmailOkPasswordWrong:
                mEmailEt.setBackgroundResource(R.drawable.input_box_accepted);
                mPasswordEt.setBackgroundResource(R.drawable.input_box_rejected);
                mPasswordRetypeEt.setBackgroundResource(R.drawable.input_box_rejected);
                mEmailAlertTv.setVisibility(View.GONE);
                mPasswordAlertTv.setVisibility(View.VISIBLE);
                mEmailEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                mPasswordEt.setTextColor(getResources().getColor(R.color.red_alert_text));
                break;
            case StateEmailWrongPasswordWrong:
                mEmailEt.setBackgroundResource(R.drawable.input_box_rejected);
                mPasswordEt.setBackgroundResource(R.drawable.input_box_rejected);
                mPasswordRetypeEt.setBackgroundResource(R.drawable.input_box_rejected);
                mEmailAlertTv.setVisibility(View.VISIBLE);
                mPasswordAlertTv.setVisibility(View.VISIBLE);
                mEmailEt.setTextColor(getResources().getColor(R.color.red_alert_text));
                mPasswordEt.setTextColor(getResources().getColor(R.color.red_alert_text));
                break;
            case StateEmailWrongPasswordDefault:
                mEmailEt.setBackgroundResource(R.drawable.input_box_rejected);
                mPasswordEt.setBackgroundResource(R.drawable.input_box);
                mPasswordRetypeEt.setBackgroundResource(R.drawable.input_box);
                mEmailAlertTv.setVisibility(View.VISIBLE);
                mPasswordAlertTv.setVisibility(View.GONE);
                mEmailEt.setTextColor(getResources().getColor(R.color.red_alert_text));
                mPasswordEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                break;
            default:
                break;
        }
        //Restores paddings for edittexts
        mEmailEt.setPadding(paddingEmailEt, paddingEmailEt, paddingEmailEt, paddingEmailEt);
        mPasswordEt.setPadding(paddingPasswordEt, paddingPasswordEt, paddingPasswordEt, paddingPasswordEt);
        mPasswordRetypeEt.setPadding(paddingPasswordEt, paddingPasswordEt, paddingPasswordEt, paddingPasswordEt);
    }

    private static class PasswordTextWatcher implements TextWatcher {
        private WeakReference<SignupFirstFragment> weakReference;

        private PasswordTextWatcher(SignupFirstFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            SignupFirstFragment fragment = weakReference.get();
            if (fragment == null) {
                return;
            }
            fragment.passwordChanged();
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            //No need to handle this
        }

        @Override
        public void afterTextChanged(Editable s) {
            //No need to handle this
        }
    }

    private static class EmailTextWatcher implements TextWatcher {
        private WeakReference<SignupFirstFragment> weakReference;

        private EmailTextWatcher(SignupFirstFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            SignupFirstFragment fragment = weakReference.get();
            if (fragment == null) {
                return;
            }
            //Update input text color if user types
            if (fragment.mEmailEt.getEditableText().length() == 0) {
                int paddingEmailEt = fragment.mEmailEt.getPaddingTop();
                fragment.mEmailEt.setBackgroundResource(R.drawable.input_box);
                fragment.mEmailEt.setPadding(paddingEmailEt, paddingEmailEt, paddingEmailEt, paddingEmailEt);
                fragment.mEmailEt.setTextColor(fragment.getResources().getColor(R.color.gray_input_color_text));
            }
            if (fragment.mPasswordEt.getEditableText().length() == 0) {
                int paddingPasswordEt = fragment.mPasswordEt.getPaddingTop();
                fragment.mPasswordEt.setBackgroundResource(R.drawable.input_box);
                fragment.mPasswordEt.setPadding(paddingPasswordEt, paddingPasswordEt, paddingPasswordEt,
                        paddingPasswordEt);
                fragment.mPasswordEt.setTextColor(fragment.getResources().getColor(R.color.gray_input_color_text));
            }
            if (fragment.mPasswordRetypeEt.getEditableText().length() == 0) {
                int paddingPasswordEt = fragment.mPasswordRetypeEt.getPaddingTop();
                fragment.mPasswordRetypeEt.setBackgroundResource(R.drawable.input_box);
                fragment.mPasswordRetypeEt.setPadding(paddingPasswordEt, paddingPasswordEt,
                        paddingPasswordEt, paddingPasswordEt);
                fragment.mPasswordRetypeEt.setTextColor(fragment.getResources()
                        .getColor(R.color.gray_input_color_text));
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            //No need to handle this
        }

        @Override
        public void afterTextChanged(Editable s) {
            //No need to handle this
        }
    }

    private static class CheckIfUserExistCallback implements OnResponseCallback {
        private WeakReference<SignupFirstFragment> weakReference;
        private String username;
        private String password;
        private boolean isTermsConditionAccepted;

        CheckIfUserExistCallback(SignupFirstFragment fragment, String username, String password, boolean isTermsConditionAccepted) {
            weakReference = new WeakReference<>(fragment);
            this.username = username;
            this.password = password;
            this.isTermsConditionAccepted = isTermsConditionAccepted;
        }

        @Override
        public void onSuccess(ParseObject object) {
            SignupFirstFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            // Updates the user interface with success state and sends the user to next screen
            fragment.userManager.createUserForSignUp(username, password, isTermsConditionAccepted);
            fragment.setScreenState(ScreenStates.StateEmailOkPasswordOk);
            fragment.activity.addFragmentToBackStack(new SignupSecondFragment());
        }

        @Override
        public void onFailed(Error error) {
            SignupFirstFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            if (error.getErrorMessageResId() == Error.ErrorType.CHECK_USER_NOT_FOUND_ERROR.getMessageResId()) {
                onSuccess(null);
            } else {
                fragment.setScreenState(ScreenStates.StateEmailWrongPasswordWrong);
                fragment.setEmailAlertMessage(error.getErrorMessage(fragment.getActivity()));
                fragment.hideLoading();
            }
        }
    }

    /**
     * Parameters for controlling states of the screen
     */
    protected enum ScreenStates {
        StateDefault,
        StateEmailOkPasswordWrong,
        StateEmailWrongPasswordDefault,
        StateEmailWrongPasswordOk,
        StateEmailWrongPasswordWrong,
        StateEmailOkPasswordOk
    }
}