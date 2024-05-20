package com.safelet.android.activities;

import static com.safelet.android.global.ApplicationSafelet.getContext;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.parse.ParseObject;
import com.safelet.android.BuildConfig;
import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.global.PopDialog;
import com.safelet.android.global.PreferencesManager;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.DeviceInformationsManager;
import com.safelet.android.interactors.EventsManager;
import com.safelet.android.interactors.PhoneContactsManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.interactors.callbacks.OnResponseListCallback;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.ParsePushUtil;
import com.safelet.android.utils.VersionChecker;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class LoginActivity extends BaseActivity implements OnClickListener {

    private final static int FORGOT_PASSWORD_REQUEST_CODE = 1001;

    private EditText mEmailEt;
    private EditText mPasswordEt;
    private TextView mEmailAlertMessageTv;
    private TextView mPasswordAlertMessageTv;
    private CheckBox privacyCheckBox;
    private LinearLayout privacyPolicy;

    private final UserManager userManager = UserManager.instance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEmailAlertMessageTv = findViewById(R.id.login_email_alert_tv);
        mPasswordAlertMessageTv = findViewById(R.id.login_password_alert_tv);



        findViewById(R.id.login_forgot_tv).setOnClickListener(this);
        findViewById(R.id.login_register_tv).setOnClickListener(this);
        findViewById(R.id.login_login_btn).setOnClickListener(this);
        mEmailEt = findViewById(R.id.login_email_et);
        mPasswordEt = findViewById(R.id.login_password_et);
        privacyCheckBox = findViewById(R.id.privacyCheckBox);
        privacyPolicy = findViewById(R.id.privacyPolicy);

        privacyPolicy.setOnClickListener(v -> {
            Intent iTermsAndCon = new Intent(getContext(), AcceptTermsAndConditionActivity.class);
            startActivity(iTermsAndCon);
        });

//        appUpdater();

        setScreenState(ScreenStates.StateDefault);

        mEmailEt.addTextChangedListener(mTextWatcher);
        mPasswordEt.addTextChangedListener(mTextWatcher);

        String email = PreferencesManager.instance(this).getString(PreferencesManager.LOGIN_EMAIL_KEY);
        if (!email.isEmpty()) {
            mEmailEt.setText(email);
        }

        ImageView wings = findViewById(R.id.background_wings);
        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_in_wings);
        wings.startAnimation(fadeInAnimation);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.login_register_tv) {
            onRegister();
        } else if (v.getId() == R.id.login_forgot_tv) {
            onForgot();
        } else if (v.getId() == R.id.login_login_btn) {
            onLogin();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FORGOT_PASSWORD_REQUEST_CODE) { // happened when user go from forgot pass screen to register screen
            if (resultCode == RESULT_OK) {
                finish();
            }
        }
    }

    private void onRegister() {
        Intent registerIntent = new Intent(LoginActivity.this, SignupActivity.class);
        startActivity(registerIntent);
        finish();
    }

    private void onForgot() {
        Intent forgotPassIntent = new Intent(this, ForgotPasswordActivity.class);
        startActivityForResult(forgotPassIntent, FORGOT_PASSWORD_REQUEST_CODE);
    }

    private void onLogin() {
        Utils.hideKeyboard(this);
        String email = mEmailEt.getEditableText().toString().toLowerCase(Locale.getDefault());
        String password = mPasswordEt.getEditableText().toString();
        PreferencesManager.instance(this).setString(PreferencesManager.LOGIN_EMAIL_KEY, email);

        // Verifies and proceed with login
        if (Utils.validateEmailWithString(email) && !TextUtils.isEmpty(password) && privacyCheckBox.isChecked()) {
            showLoading();
            userManager.loginAccountForUser(email, password, new LoginCallback(this));
        } else {
            if (TextUtils.isEmpty(email)) {
                setEmailMessage(getString(R.string.login_alert_message_emailcannotbeempty));
                if (TextUtils.isEmpty(password)) {
                    setPasswordMessage(getString(R.string.login_alert_message_passwordcannotbeempty));
                    setScreenState(ScreenStates.StateEmailWrongPasswordWrong);
                } else {
                    setScreenState(ScreenStates.StateEmailWrongPasswordOk);
                }
            } else if (!Utils.validateEmailWithString(email)) {
                setEmailMessage(getString(R.string.login_alert_message_incorrectemailaddress));
                if (TextUtils.isEmpty(password)) {
                    setPasswordMessage(getString(R.string.login_alert_message_passwordcannotbeempty));
                    setScreenState(ScreenStates.StateEmailWrongPasswordWrong);
                } else {
                    setScreenState(ScreenStates.StateEmailWrongPasswordOk);
                }
            } else if (TextUtils.isEmpty(password)) {
                setPasswordMessage(getString(R.string.login_alert_message_passwordcannotbeempty));
                setScreenState(ScreenStates.StateEmailOkPasswordWrong);
            } else if(!privacyCheckBox.isChecked()) {
                Toast.makeText(mContext, "Check Privacy Policy", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loginFinished() {
        hideLoading();
        setScreenState(ScreenStates.StateEmailOkPasswordOk);
        ParsePushUtil.subscribeInBackground(UserManager.instance().getUserId());
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            PhoneContactsManager.instance().initCountryCode(getApplication(), UserManager.instance().getUserModel().getCountryPrefixCode());
            PhoneContactsManager.instance().readPhoneContactsAsync(getContentResolver());
        }
        boolean isGuardianNetworkSkip = PreferencesManager.instance(mContext).getGuardianNetworkSkip();
        if (!isGuardianNetworkSkip) {
            Intent homeIntent = new Intent(LoginActivity.this, GuardianNetworkActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        } else {
            Intent homeIntent = new Intent(LoginActivity.this, HomeActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        }
        LoginActivity.this.finish();
    }

    private void loadEvents() {
        EventsManager.instance().getEventsForUser(userManager.getUserModel().getObjectId(), new GetNotificationsListener(this));
    }

    private void setEmailMessage(String message) {
        if (mEmailAlertMessageTv != null) {
            mEmailAlertMessageTv.setText(message);
        }
    }

    private void setPasswordMessage(String message) {
        if (mPasswordAlertMessageTv != null) {
            mPasswordAlertMessageTv.setText(message);
        }
    }

    private void setScreenState(ScreenStates screenState) {
        if (mEmailEt == null) {
            // In case that the ui is uninitialized
            return;
        }
        int paddingEmailEt = mEmailEt.getPaddingTop();
        int paddingPasswordEt = mPasswordEt.getPaddingTop();
        switch (screenState) {
            case StateDefault:
                mEmailEt.setBackgroundResource(R.drawable.input_box);
                mPasswordEt.setBackgroundResource(R.drawable.input_box);
                mEmailAlertMessageTv.setVisibility(View.GONE);
                mPasswordAlertMessageTv.setVisibility(View.GONE);
                mEmailEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                mPasswordEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                break;
            case StateEmailOkPasswordOk:
                mEmailEt.setBackgroundResource(R.drawable.input_box_accepted);
                mPasswordEt.setBackgroundResource(R.drawable.input_box_accepted);
                mEmailAlertMessageTv.setVisibility(View.GONE);
                mPasswordAlertMessageTv.setVisibility(View.GONE);
                mEmailEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                mPasswordEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                break;
            case StateEmailWrongPasswordOk:
                mEmailEt.setBackgroundResource(R.drawable.input_box_rejected);
                mPasswordEt.setBackgroundResource(R.drawable.input_box_accepted);
                mEmailAlertMessageTv.setVisibility(View.VISIBLE);
                mPasswordAlertMessageTv.setVisibility(View.GONE);
                mEmailEt.setTextColor(getResources().getColor(R.color.red_alert_text));
                mPasswordEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                break;
            case StateEmailOkPasswordWrong:
                mEmailEt.setBackgroundResource(R.drawable.input_box_accepted);
                mPasswordEt.setBackgroundResource(R.drawable.input_box_rejected);
                mEmailAlertMessageTv.setVisibility(View.GONE);
                mPasswordAlertMessageTv.setVisibility(View.VISIBLE);
                mEmailEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                mPasswordEt.setTextColor(getResources().getColor(R.color.red_alert_text));
                break;
            case StateEmailWrongPasswordWrong:
                mEmailEt.setBackgroundResource(R.drawable.input_box_rejected);
                mPasswordEt.setBackgroundResource(R.drawable.input_box_rejected);
                mEmailAlertMessageTv.setVisibility(View.VISIBLE);
                mPasswordAlertMessageTv.setVisibility(View.VISIBLE);
                mEmailEt.setTextColor(getResources().getColor(R.color.red_alert_text));
                mPasswordEt.setTextColor(getResources().getColor(R.color.red_alert_text));
                break;
            case StateEmailWrongPasswordDefault:
                mEmailEt.setBackgroundResource(R.drawable.input_box_rejected);
                mPasswordEt.setBackgroundResource(R.drawable.input_box);
                mEmailAlertMessageTv.setVisibility(View.VISIBLE);
                mPasswordAlertMessageTv.setVisibility(View.GONE);
                mEmailEt.setTextColor(getResources().getColor(R.color.red_alert_text));
                mPasswordEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                break;
            default:
                break;
        }
        // Restores padding for text inputs
        mEmailEt.setPadding(paddingEmailEt, paddingEmailEt, paddingEmailEt, paddingEmailEt);
        mPasswordEt.setPadding(paddingPasswordEt, paddingPasswordEt, paddingPasswordEt, paddingPasswordEt);
    }

    private TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //Restores text input appearence
            if (mEmailEt.getEditableText().length() == 0) {
                int paddingEmailEt = mEmailEt.getPaddingTop();
                mEmailEt.setBackgroundResource(R.drawable.input_box);
                mEmailEt.setPadding(paddingEmailEt, paddingEmailEt, paddingEmailEt, paddingEmailEt);
                mEmailEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                mEmailAlertMessageTv.setVisibility(View.GONE);
            }
            if (mPasswordEt.getEditableText().length() == 0) {
                int paddingPasswordEt = mPasswordEt.getPaddingTop();
                mPasswordEt.setBackgroundResource(R.drawable.input_box);
                mPasswordEt.setPadding(paddingPasswordEt, paddingPasswordEt, paddingPasswordEt, paddingPasswordEt);
                mPasswordEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                mPasswordAlertMessageTv.setVisibility(View.GONE);
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
    };

    private enum ScreenStates {
        StateDefault,
        StateEmailOkPasswordWrong,
        StateEmailWrongPasswordDefault,
        StateEmailWrongPasswordOk,
        StateEmailWrongPasswordWrong,
        StateEmailOkPasswordOk
    }

    private static class LoginCallback implements OnResponseCallback {
        private WeakReference<LoginActivity> loginActivityWeakReference;

        public LoginCallback(LoginActivity activity) {
            loginActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onSuccess(ParseObject object) {
            LoginActivity activity = loginActivityWeakReference.get();
            if (activity == null) {
                return;
            }
            DeviceInformationsManager.instance().uploadPhoneDetails();
            activity.loadEvents();
        }

        @Override
        public void onFailed(Error error) {
            LoginActivity activity = loginActivityWeakReference.get();
            if (activity == null) {
                return;
            }
            activity.hideLoading();
            PopDialog.showDialog(activity, error.getErrorMessage(activity));
        }
    }

    private static class GetNotificationsListener implements OnResponseListCallback {
        private WeakReference<LoginActivity> loginActivityWeakReference;

        GetNotificationsListener(LoginActivity activity) {
            loginActivityWeakReference = new WeakReference<>(activity);
        }

        @Override
        public void onFailed(Error error) {
            loginFinished();
        }

        @Override
        public void onSuccess(Map<String, List<ParseObject>> objects) {
            loginFinished();
        }

        private void loginFinished() {
            LoginActivity activity = loginActivityWeakReference.get();
            if (activity == null) {
                return;
            }
            activity.loginFinished();
        }

    }


    private void updateShowDialog() {

        final Dialog dialog = new Dialog(LoginActivity.this);
        dialog.setContentView(R.layout.update_item);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(false);

        Button update_btn = dialog.findViewById(R.id.update_btn);

        update_btn.setOnClickListener(v -> {
            Intent httpIntent = new Intent(Intent.ACTION_VIEW);
            httpIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.safelet.android"));

            startActivity(httpIntent);
            dialog.dismiss();
        });

        dialog.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        appUpdater();
    }

    public void appUpdater(){
        try {
            VersionChecker versionChecker = new VersionChecker();
            double latestVersion = Double.parseDouble(versionChecker.execute().get());
            String currentVersionName = BuildConfig.VERSION_NAME;
            String currentVersion[] = currentVersionName.split("-");
            double currentVersionDouble = Double.parseDouble(currentVersion[0]);
            if (currentVersionDouble < latestVersion) {
                updateShowDialog();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



}
 