package com.safelet.android.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.global.PopDialog;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.LogoutHelper;
import com.safelet.android.views.AlarmView;

import java.lang.ref.WeakReference;

/**
 * Edit user email fragment
 * <p/>
 * Created by alin on 13.10.2015.
 */
public class EditEmailFragment extends BaseFragment implements View.OnClickListener {

    private Button confirmButton;
    /**
     * Input text field for email of the user
     */
    private EditText mEmailEt;
    private TextView mAlertTv;

    private final UserManager userManager = UserManager.instance();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_email, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        try {
            //Retrieves a reference for alert pop view if available
            alarmView = (AlarmView) view.findViewById(R.id.globalAlertPopView);
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), "Error loading alert popup");
        }
        mEmailEt = (EditText) view.findViewById(R.id.change_email_et);
        mEmailEt.addTextChangedListener(new EmailTextWatcher(this));
        mAlertTv = (TextView) view.findViewById(R.id.change_email_alert_tv);
        confirmButton = (Button) view.findViewById(R.id.change_email_confirm_btn);
        confirmButton.setOnClickListener(this);
        mEmailEt.setText(userManager.getUserModel().getEmail());
        setScreenState(ScreenStates.StateDefault);
    }

    @Override
    protected void onNavigationDrawerItemSelected(NavigationMenu navigationMenu) {
        // do nothing
    }

    @Override
    protected void onViewNotificationDialogClicked(Context context, NewNotificationEvent event) {
        goToHomeScreenWithNotification(event);
    }

    /**
     * Display alert message for errors
     *
     * @param alert message to be displayed
     */
    private void setAlertMessage(String alert) {
        if (mAlertTv != null && alert != null) {
            mAlertTv.setText(alert);
        }
    }

    /**
     * Changes screen state based on ScreenStates enum
     *
     * @param screenState Requested screenstate
     */
    private void setScreenState(ScreenStates screenState) {
        if (mEmailEt == null) {
            return;
        }
        int paddingNameEt = mEmailEt.getPaddingTop();
        switch (screenState) {
            case StateDefault:
                mEmailEt.setBackgroundResource(R.drawable.input_box);
                mEmailEt.setPadding(paddingNameEt, paddingNameEt, paddingNameEt, paddingNameEt);
                mAlertTv.setVisibility(View.GONE);
                break;
            case StateEmailEmpty:
                mEmailEt.setBackgroundResource(R.drawable.input_box_rejected);
                mEmailEt.setPadding(paddingNameEt, paddingNameEt, paddingNameEt, paddingNameEt);
                mAlertTv.setVisibility(View.VISIBLE);
                break;
            case StateInvalidEmailAddress:
                mEmailEt.setBackgroundResource(R.drawable.input_box_rejected);
                mEmailEt.setPadding(paddingNameEt, paddingNameEt, paddingNameEt, paddingNameEt);
                mAlertTv.setVisibility(View.VISIBLE);
                break;
            case StateUpdateFailed:
                mEmailEt.setBackgroundResource(R.drawable.input_box_rejected);
                mEmailEt.setPadding(paddingNameEt, paddingNameEt, paddingNameEt, paddingNameEt);
                mAlertTv.setVisibility(View.VISIBLE);
                break;
            case StateOk:
                mEmailEt.setBackgroundResource(R.drawable.input_box_accepted);
                mEmailEt.setPadding(paddingNameEt, paddingNameEt, paddingNameEt, paddingNameEt);
                mAlertTv.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    private void onChangeEmail() {
        Utils.hideKeyboard(getActivity());
        final String email = mEmailEt.getEditableText().toString();
        if (!email.isEmpty()) {
            if (Utils.validateEmailWithString(email)) {
                showLoading();
                // Validation successfully, change email for user
                userManager.changeEmailForUser(email, new ChangeEmailCallback(this));
            } else {
                // Validation failed
                setScreenState(ScreenStates.StateInvalidEmailAddress);
                setAlertMessage(getString(R.string.change_email_alert_message_invalidemailaddress));
            }
        } else {
            // Validation failed
            setScreenState(ScreenStates.StateEmailEmpty);
            setAlertMessage(getString(R.string.change_email_alert_message_emailcannotbeempty));
        }
    }

    @Override
    public int getTitleResId() {
        return R.string.change_email_title;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.change_email_confirm_btn) {
            onChangeEmail();
        }
    }

    /**
     * Parameters for controlling states of the screen
     */
    protected enum ScreenStates {
        StateDefault,
        StateEmailEmpty,
        StateInvalidEmailAddress,
        StateUpdateFailed,
        StateOk,
    }

    private static class EmailTextWatcher implements TextWatcher {
        private WeakReference<EditEmailFragment> weakReference;

        EmailTextWatcher(EditEmailFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            EditEmailFragment fragment = weakReference.get();
            if (fragment == null) {
                return;
            }
            //Changes input colour theme to green when editing
            if (fragment.mEmailEt.getEditableText().length() == 0) {
                int paddingNameEt = fragment.mEmailEt.getPaddingTop();
                fragment.mEmailEt.setBackgroundResource(R.drawable.input_box);
                fragment.mEmailEt.setPadding(paddingNameEt, paddingNameEt, paddingNameEt, paddingNameEt);
                fragment.mEmailEt.setTextColor(fragment.getResources().getColor(R.color.gray_input_color_text));
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
            //No need to handle
        }

        @Override
        public void afterTextChanged(Editable s) {
            EditEmailFragment fragment = weakReference.get();
            if (fragment == null) {
                return;
            }
            fragment.confirmButton.setEnabled(!fragment.mEmailEt.getText().toString().equals(
                    fragment.userManager.getUserModel().getEmail()));
        }
    }

    /**
     * Callback for when user updates his email via server
     */
    private static class ChangeEmailCallback implements OnResponseCallback {
        private WeakReference<EditEmailFragment> weakReference;

        ChangeEmailCallback(EditEmailFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onSuccess(ParseObject object) {
            final EditEmailFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.hideLoading();
            // Updates the user interface with success state and displays confirmation dialog
            fragment.setScreenState(ScreenStates.StateOk);
            PopDialog.showDialog(fragment.getActivity(),
                    fragment.getString(R.string.change_email_dialog_title),
                    fragment.getString(R.string.change_email_dialog_message_successfullyupdated),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fragment.getActivity().finish();
                        }
                    });
        }

        @Override
        public void onFailed(Error error) {
            EditEmailFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            if (!LogoutHelper.handleExpiredSession(fragment.getActivity(), error)) {
                // Changes screen state to error state
                fragment.setScreenState(ScreenStates.StateUpdateFailed);
                fragment.setAlertMessage(error.getErrorMessage(fragment.getActivity()));
            }
            fragment.hideLoading();
        }
    }
}
