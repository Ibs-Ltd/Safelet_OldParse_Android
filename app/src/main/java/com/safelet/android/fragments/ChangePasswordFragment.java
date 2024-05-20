package com.safelet.android.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.activities.ForgotPasswordActivity;
import com.safelet.android.global.PopDialog;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.EventBusManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.models.event.NoInternetConnectionEvent;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.LogoutHelper;
import com.safelet.android.views.AlarmView;

import java.lang.ref.WeakReference;

/**
 * Change user password fragment
 * <p/>
 * Created by alin on 13.10.2015.
 */
public class ChangePasswordFragment extends BaseFragment implements View.OnClickListener {
    /**
     * Input text field for current password
     */
    protected EditText mPassCurrentEt;
    /**
     * Input text field for new password
     */
    protected EditText mPassNewEt;
    /**
     * Input text field for repeat new password
     */
    protected EditText mPassRepeatNewEt;

    private TextView mAlertCurrentPassTv;
    private TextView mAlertRepeatPassTv;

    private final UserManager userManager = UserManager.instance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPassCurrentEt = (EditText) view.findViewById(R.id.change_pass_current_et);
        mPassNewEt = (EditText) view.findViewById(R.id.change_pass_new_password_et);
        mPassRepeatNewEt = (EditText) view.findViewById(R.id.change_pass_new_repeat_password_et);
        mAlertCurrentPassTv = (TextView) view.findViewById(R.id.change_pass_current_alert_tv);
        mAlertRepeatPassTv = (TextView) view.findViewById(R.id.change_pass_password_alert_tv);

        view.findViewById(R.id.change_pass_forgot_tv).setOnClickListener(this);
        view.findViewById(R.id.change_pass_btn).setOnClickListener(this);

        setScreenState(ScreenStates.StateDefault);
        ChangePasswordTextWatcher textWatcher = new ChangePasswordTextWatcher(this);
        mPassCurrentEt.addTextChangedListener(textWatcher);
        mPassNewEt.addTextChangedListener(textWatcher);
        mPassRepeatNewEt.addTextChangedListener(textWatcher);
        try {
            //Retrieves a reference for alert pop view if available
            this.alarmView = (AlarmView) view.findViewById(R.id.globalAlertPopView);
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error loading alert popup");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!Utils.isOnline()) {
            EventBusManager.instance().postEvent(new NoInternetConnectionEvent());
        }
    }

    @Override
    protected void onNavigationDrawerItemSelected(NavigationMenu navigationMenu) {
        // do nothing
    }

    @Override
    protected void onViewNotificationDialogClicked(Context context, NewNotificationEvent event) {
        goToHomeScreenWithNotification(event);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.change_pass_forgot_tv) {
            Intent intent = new Intent(getActivity(), ForgotPasswordActivity.class);
            //Extra for displaying nav bar
            intent.putExtra(ForgotPasswordActivity.INTENT_EXTRA_NAV_KEY, true); // for displaying navbar
            startActivity(intent);
        } else if (v.getId() == R.id.change_pass_btn) {
            onChangePassword();
        }
    }

    private void onChangePassword() {
        Utils.hideKeyboard(getActivity());
        String currentPassword = mPassCurrentEt.getEditableText().toString();
        String newPassword = mPassNewEt.getEditableText().toString();
        String newRepeatPassword = mPassRepeatNewEt.getEditableText().toString();

        if (!currentPassword.equals(newPassword)
                && !TextUtils.isEmpty(newPassword)
                && newPassword.equals(newRepeatPassword)) {
            //OK now check!
            showLoading();
            userManager.changePasswordForUser(currentPassword, newPassword, new ChangePasswordCallback(this));
        } else if (TextUtils.isEmpty(currentPassword)) {
            // Current pass empty
            setScreenState(ScreenStates.StateCurrentWrongNextDefault);
            setCurrentPasswordMessage(getString(R.string.change_password_alert_message_passwordcannotbeempty));
        } else if (TextUtils.isEmpty(newPassword)) {
            // Current pass ok, but newpassword empty
            setScreenState(ScreenStates.StateCurrentOkNextWrong);
            setRepeatPasswordMessage(getString(R.string.change_password_alert_message_passwordcannotbeempty));
        } else if (currentPassword.equals(newPassword)) {
            // Current pass ok but new pass same as previoyus password
            setScreenState(ScreenStates.StateCurrentOkNextWrong);
            setRepeatPasswordMessage(getString(R.string.change_password_alert_message_passwordsame));
        } else if (!newPassword.equals(newRepeatPassword)) {
            //current pass ok but new pass not identical
            setScreenState(ScreenStates.StateCurrentOkNextWrong);
            setRepeatPasswordMessage(getString(R.string.change_password_alert_message_passworddontmatch));
        } else {
            // Not arriving here!
            // Current pass ok new pass ok
            setScreenState(ScreenStates.StateDefault);
        }
    }

    @Override
    public int getTitleResId() {
        return R.string.change_password_title;
    }

    /**
     * Display alert message for errors on current password
     *
     * @param alert message for errors on current password
     */
    private void setCurrentPasswordMessage(String alert) {
        if (mAlertCurrentPassTv != null && alert != null) {
            mAlertCurrentPassTv.setText(alert);
        }
    }

    /**
     * Display alert message for errors on repeat password
     *
     * @param alert message for errors on repeat password
     */
    private void setRepeatPasswordMessage(String alert) {
        if (mAlertRepeatPassTv != null) {
            mAlertRepeatPassTv.setText(alert);
        }
    }

    /**
     * Changes screen state based on ScreenStates enum
     *
     * @param screenState state based on ScreenStates enum
     */
    private void setScreenState(ScreenStates screenState) {
        if (mPassCurrentEt == null) {
            return;
        }
        int paddingEmailEt = mPassCurrentEt.getPaddingTop();
        int paddingPasswordEt = mPassNewEt.getPaddingTop();
        switch (screenState) {
            case StateDefault:
                mPassCurrentEt.setBackgroundResource(R.drawable.input_box);
                mPassNewEt.setBackgroundResource(R.drawable.input_box);
                mPassRepeatNewEt.setBackgroundResource(R.drawable.input_box);
                mAlertCurrentPassTv.setVisibility(View.GONE);
                mAlertRepeatPassTv.setVisibility(View.GONE);
                break;
            case StateCurrentWrongNextDefault:
                mPassCurrentEt.setBackgroundResource(R.drawable.input_box_rejected);
                mPassNewEt.setBackgroundResource(R.drawable.input_box);
                mPassRepeatNewEt.setBackgroundResource(R.drawable.input_box);
                mAlertCurrentPassTv.setVisibility(View.VISIBLE);
                mAlertRepeatPassTv.setVisibility(View.GONE);
                break;
            case StateCurrentWrongNextWrong:
                mPassCurrentEt.setBackgroundResource(R.drawable.input_box_rejected);
                mPassNewEt.setBackgroundResource(R.drawable.input_box_rejected);
                mPassRepeatNewEt.setBackgroundResource(R.drawable.input_box_rejected);
                mAlertCurrentPassTv.setVisibility(View.VISIBLE);
                mAlertRepeatPassTv.setVisibility(View.VISIBLE);
                break;
            case StateCurrentOkNextWrong:
                mPassCurrentEt.setBackgroundResource(R.drawable.input_box_accepted);
                mPassNewEt.setBackgroundResource(R.drawable.input_box_rejected);
                mPassRepeatNewEt.setBackgroundResource(R.drawable.input_box_rejected);
                mAlertCurrentPassTv.setVisibility(View.GONE);
                mAlertRepeatPassTv.setVisibility(View.VISIBLE);
                break;
            case StateCurrentOkNextok:
                mPassCurrentEt.setBackgroundResource(R.drawable.input_box_accepted);
                mPassNewEt.setBackgroundResource(R.drawable.input_box_accepted);
                mPassRepeatNewEt.setBackgroundResource(R.drawable.input_box_accepted);
                mAlertCurrentPassTv.setVisibility(View.GONE);
                mAlertRepeatPassTv.setVisibility(View.GONE);
                break;
            default:
                break;
        }
        //Restores padding
        mPassCurrentEt.setPadding(paddingEmailEt, paddingEmailEt, paddingEmailEt, paddingEmailEt);
        mPassNewEt.setPadding(paddingPasswordEt, paddingPasswordEt, paddingPasswordEt, paddingPasswordEt);
        mPassRepeatNewEt.setPadding(paddingPasswordEt, paddingPasswordEt, paddingPasswordEt, paddingPasswordEt);
    }

    /**
     * Parameters for controlling states of the screen
     */
    protected enum ScreenStates {
        StateDefault,
        StateCurrentWrongNextDefault,
        StateCurrentWrongNextOk,
        StateCurrentWrongNextWrong,
        StateCurrentOkNextWrong,
        StateCurrentOkNextok
    }

    private static class ChangePasswordTextWatcher implements TextWatcher {
        private WeakReference<ChangePasswordFragment> weakReference;

        public ChangePasswordTextWatcher(ChangePasswordFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            ChangePasswordFragment fragment = weakReference.get();
            if (fragment == null) {
                return;
            }
            // TODO maybe we should refactor this
            //Changes inputs colour theme to green when editing
            if (fragment.mPassCurrentEt.getEditableText().length() == 0) {
                int paddingEmailEt = fragment.mPassCurrentEt.getPaddingTop();
                fragment.mPassCurrentEt.setBackgroundResource(R.drawable.input_box);
                fragment.mPassCurrentEt.setPadding(paddingEmailEt, paddingEmailEt, paddingEmailEt, paddingEmailEt);
                fragment.mPassCurrentEt.setTextColor(fragment.getResources().getColor(R.color.gray_input_color_text));
            }
            if (fragment.mPassNewEt.getEditableText().length() == 0) {
                int paddingPasswordEt = fragment.mPassNewEt.getPaddingTop();
                fragment.mPassNewEt.setBackgroundResource(R.drawable.input_box);
                fragment.mPassNewEt.setPadding(paddingPasswordEt, paddingPasswordEt, paddingPasswordEt, paddingPasswordEt);
                fragment.mPassNewEt.setTextColor(fragment.getResources().getColor(R.color.gray_input_color_text));
            }
            if (fragment.mPassRepeatNewEt.getEditableText().length() == 0) {
                int paddingPasswordEt = fragment.mPassRepeatNewEt.getPaddingTop();
                fragment.mPassRepeatNewEt.setBackgroundResource(R.drawable.input_box);
                fragment.mPassRepeatNewEt.setPadding(paddingPasswordEt, paddingPasswordEt, paddingPasswordEt, paddingPasswordEt);
                fragment.mPassRepeatNewEt.setTextColor(fragment.getResources().getColor(R.color.gray_input_color_text));
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
    }

    /**
     * Callback for changing password
     */
    private static class ChangePasswordCallback implements OnResponseCallback {
        private WeakReference<ChangePasswordFragment> weakReference;

        public ChangePasswordCallback(ChangePasswordFragment fragment) {
            weakReference = new WeakReference<ChangePasswordFragment>(fragment);
        }

        @Override
        public void onSuccess(ParseObject object) {
            final ChangePasswordFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.hideLoading();
            // Updates the user interface with success state and displays confirmation dialog
            fragment.setScreenState(ScreenStates.StateCurrentOkNextok);
            PopDialog.showDialog(fragment.getActivity(),
                    fragment.getString(R.string.change_password_dialog_title),
                    fragment.getString(R.string.change_password_dialog_message_successfullyupdated),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fragment.getActivity().finish();
                        }
                    });
        }

        @Override
        public void onFailed(Error error) {
            ChangePasswordFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.hideLoading();
            if (!LogoutHelper.handleExpiredSession(fragment.getActivity(), error)) {
                // Updates the user interface with error state and shows the errors
                fragment.setScreenState(ScreenStates.StateCurrentWrongNextDefault);
                fragment.setCurrentPasswordMessage(error.getErrorMessage(fragment.getActivity()));
            }
        }
    }
}
