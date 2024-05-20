package com.safelet.android.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

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

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Edit user name fragment.
 * <p/>
 * Created by alin on 13.10.2015.
 */
public class EditNameFragment extends BaseFragment implements View.OnClickListener, TextWatcher {

    private Button confirmButton;
    private EditText nameEditText;
    private TextView alertTextView;

    private final UserManager userManager = UserManager.instance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_name, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        confirmButton = view.findViewById(R.id.edit_name_confirm_btn);
        confirmButton.setOnClickListener(this);
        nameEditText = view.findViewById(R.id.edit_name_et);
        nameEditText.addTextChangedListener(this);
        alertTextView = view.findViewById(R.id.edit_name_alert_tv);
//        nameEditText.setText(userManager.getUserModel().getName());
        nameEditText.setText(userManager.getUserModel().getOriginalName());

        setScreenState(ScreenStates.StateDefault);

        try {
            //Retrieves a reference for alert pop view if available
            this.alarmView = view.findViewById(R.id.globalAlertPopView);
        } catch (Exception e) {
            Timber.tag(getClass().getSimpleName()).e(" Error loading alertpopup");
            e.printStackTrace();
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
        if (v.getId() == R.id.edit_name_confirm_btn) {
            onChangeName();
        }
    }

    @Override
    public int getTitleResId() {
        return R.string.change_name_title;
    }

    /**
     * Display alert message for errors
     *
     * @param alert message for errors
     */
    private void setAlertMessage(String alert) {
        if (alertTextView != null && alert != null) {
            alertTextView.setText(alert);
        }
    }

    /**
     * Changes screen state based on ScreenStates enum
     *
     * @param screenState state based on ScreenStates enum
     */
    private void setScreenState(ScreenStates screenState) {
        if (nameEditText == null) {
            return;
        }
        int paddingNameEt = nameEditText.getPaddingTop();
        switch (screenState) {
            case StateDefault:
                nameEditText.setBackgroundResource(R.drawable.input_box);
                alertTextView.setVisibility(View.GONE);
                break;
            case StateEmptyName:
                nameEditText.setBackgroundResource(R.drawable.input_box_rejected);
                alertTextView.setVisibility(View.VISIBLE);
                break;
            case StateUpdateFailed:
                nameEditText.setBackgroundResource(R.drawable.input_box_rejected);
                alertTextView.setVisibility(View.VISIBLE);
                break;
            case StateOk:
                nameEditText.setBackgroundResource(R.drawable.input_box_accepted);
                alertTextView.setVisibility(View.GONE);
                break;
            default:
                break;
        }
        //Restores padding
        nameEditText.setPadding(paddingNameEt, paddingNameEt, paddingNameEt, paddingNameEt);
    }

    private void onChangeName() {
        Utils.hideKeyboard(getActivity());
        String name = nameEditText.getEditableText().toString();
        if (!TextUtils.isEmpty(name)) {
            showLoading();
            UserManager.instance().changeNameForUser(name, new ChangeNameCallback(this));
        } else {
            setScreenState(ScreenStates.StateEmptyName);
            setAlertMessage(getString(R.string.change_name_alert_namecannotbeempty));
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
//        confirmButton.setEnabled(!nameEditText.getText().toString().equals(userManager.getUserModel().getName()));
        confirmButton.setEnabled(!nameEditText.getText().toString().equals(userManager.getUserModel().getOriginalName()));
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    /**
     * Parameters for controlling states of the screen
     */
    protected enum ScreenStates {
        StateDefault,
        StateEmptyName,
        StateUpdateFailed,
        StateOk
    }

    /**
     * Callback for changing name
     */
    private static class ChangeNameCallback implements OnResponseCallback {
        private WeakReference<EditNameFragment> weakReference;

        ChangeNameCallback(EditNameFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onSuccess(ParseObject object) {
            final EditNameFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.hideLoading();
            fragment.setScreenState(ScreenStates.StateOk);

            PopDialog.showDialog(fragment.getActivity(),
                    fragment.getString(R.string.change_name_dialog_title),
                    fragment.getString(R.string.change_name_dialog_message_successfullyupdated),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (fragment.getActivity() != null)
                                fragment.getActivity().finish();
                        }
                    });
        }

        @Override
        public void onFailed(Error error) {
            EditNameFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            if (!LogoutHelper.handleExpiredSession(fragment.getActivity(), error)) {
                fragment.setScreenState(ScreenStates.StateUpdateFailed);
                if (error != null) {
                    fragment.setAlertMessage(error.getErrorMessage(fragment.getActivity()));
                }
            }
            fragment.hideLoading();
        }
    }
}
