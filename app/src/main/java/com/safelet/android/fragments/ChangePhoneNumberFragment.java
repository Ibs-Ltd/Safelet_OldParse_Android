package com.safelet.android.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.activities.PhonePrefixListActivity;
import com.safelet.android.global.PopDialog;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.LogoutHelper;

import java.lang.ref.WeakReference;

/**
 * Change user phone fragment
 * <p/>
 * Created by alin on 13.10.2015.
 */
public class ChangePhoneNumberFragment extends BaseFragment implements View.OnClickListener, TextWatcher {
    private static final int PREFIX_REQUEST_CODE = 1001;

    private Button confirmButton;
    private TextView mPrefixEt;
    private TextView mPhoneAlertTv;
    private EditText mPhoneNumberEt;
    private TextView mPrefixAlertTv;
    private RelativeLayout mPrefixRl;

    private String countryCode;
    private String phoneNumber;

    private final UserManager userManager = UserManager.instance();

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PREFIX_REQUEST_CODE) {
            try {
                //Handle prefix type from PhonePrefix Activtiy
                mPrefixEt.setText("+" + data.getStringExtra(PhonePrefixListActivity.PREFIX_KEY));
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error loading prefix");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_phone_number, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPrefixRl = (RelativeLayout) view.findViewById(R.id.change_phone_prefix_rl);
        mPrefixEt = (TextView) view.findViewById(R.id.change_phone_prefix_et);
        mPrefixEt.addTextChangedListener(this);
        mPhoneNumberEt = (EditText) view.findViewById(R.id.change_phone_phone_et);
        mPhoneNumberEt.addTextChangedListener(this);
        mPrefixAlertTv = (TextView) view.findViewById(R.id.change_phone_prefix_alert_tv);
        mPhoneAlertTv = (TextView) view.findViewById(R.id.change_phone_phone_alert_tv);
        mPrefixRl.setOnClickListener(this);
        confirmButton = (Button) view.findViewById(R.id.change_phone_confirm_btn);
        confirmButton.setOnClickListener(this);

        countryCode = userManager.getUserModel().getCountryPrefixCode();
        phoneNumber = userManager.getUserModel().getPhoneNumber().replace(countryCode, "");

        mPrefixEt.setText(countryCode);
        mPhoneNumberEt.setText(phoneNumber);


        mPhoneNumberEt.setOnClickListener(view1 -> {
            if(mPhoneAlertTv.getVisibility()==View.VISIBLE){
                mPhoneNumberEt.setText("");
                mPrefixRl.setBackgroundResource(R.drawable.input_box);
                mPhoneNumberEt.setBackgroundResource(R.drawable.input_box);
                mPrefixAlertTv.setVisibility(View.GONE);
                mPhoneAlertTv.setVisibility(View.GONE);
                mPrefixEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                mPhoneNumberEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
            }
        });

        //Sets screen default state
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.change_phone_prefix_rl) {
            Intent countryPrefixIntent = new Intent(getActivity(), PhonePrefixListActivity.class);
            startActivityForResult(countryPrefixIntent, PREFIX_REQUEST_CODE);
        } else if (v.getId() == R.id.change_phone_confirm_btn) {
            changePhoneNumber();
        }
    }

    @Override
    public int getTitleResId() {
        return R.string.change_phone_title;
    }

    /**
     * Changes screen state based on ScreenStates enum
     *
     * @param state state based on ScreenStates enum
     */
    protected void setScreenState(ScreenStates state) {
        if (mPhoneNumberEt == null) {
            return;
        }
        int padding = mPhoneNumberEt.getPaddingTop();
        switch (state) {
            case StateDefault:
                mPrefixRl.setBackgroundResource(R.drawable.input_box);
                mPhoneNumberEt.setBackgroundResource(R.drawable.input_box);
                mPrefixAlertTv.setVisibility(View.GONE);
                mPhoneAlertTv.setVisibility(View.GONE);
                mPrefixEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                mPhoneNumberEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                break;
            case StateMissingPrefix:
                mPrefixRl.setBackgroundResource(R.drawable.input_box_small);
                mPrefixAlertTv.setVisibility(View.VISIBLE);
                mPhoneAlertTv.setVisibility(View.GONE);
                mPrefixEt.setTextColor(getResources().getColor(R.color.red_alert_text));
                break;
            case StateMissingNumber:
                mPrefixRl.setBackgroundResource(R.drawable.input_box_small);
                mPhoneNumberEt.setBackgroundResource(R.drawable.input_box_rejected);
                mPrefixAlertTv.setVisibility(View.GONE);
                mPhoneAlertTv.setVisibility(View.VISIBLE);
                mPrefixEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                mPhoneNumberEt.setTextColor(getResources().getColor(R.color.red_alert_text));
                break;
            case StateOk:
                mPrefixRl.setBackgroundResource(R.drawable.input_box_small);
                mPhoneNumberEt.setBackgroundResource(R.drawable.input_box_accepted);
                mPrefixAlertTv.setVisibility(View.GONE);
                mPhoneAlertTv.setVisibility(View.GONE);
                mPrefixEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                mPhoneNumberEt.setTextColor(getResources().getColor(R.color.gray_input_color_text));
                break;
            default:
                break;
        }
        //Restores padding
        mPhoneNumberEt.setPadding(padding, padding, padding, padding);
    }

    public void changePhoneNumber() {
        Utils.hideKeyboard(getActivity());
        String prefix = mPrefixEt.getText().toString();
        String number = mPhoneNumberEt.getText().toString();
        // Validates prefix and phone number
        if (!prefix.isEmpty()) {
            if (!number.isEmpty()) {
                showLoading();
                UserManager.instance().changePhoneNumberForUser(phoneNumber, prefix, number, new ChangePhoneCallback(this));
            } else {
                setScreenState(ScreenStates.StateMissingNumber);
                mPhoneAlertTv.setText(getString(R.string.change_phone_number_alert_phonecannotbeempty));
            }
        } else {
            // Phone prefix is missing
            // Updates ui according to this situation
            setScreenState(ScreenStates.StateMissingPrefix);
            mPrefixAlertTv.setText(getString(R.string.change_phone_number_alert_prefixcannotbeempty));
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
        confirmButton.setEnabled(!mPrefixEt.getText().toString().equals(countryCode) ||
                !mPhoneNumberEt.getText().toString().equals(phoneNumber));
    }

    @Override
    public void afterTextChanged(Editable editable) {
    }

    /**
     * Parameters for controlling states of the screen
     */
    protected enum ScreenStates {
        StateDefault,
        StateMissingPrefix,
        StateMissingNumber,
        StateOk
    }

    /**
     * Callback for changing phone
     */
    private static class ChangePhoneCallback implements OnResponseCallback {
        private WeakReference<ChangePhoneNumberFragment> weakReference;

        public ChangePhoneCallback(ChangePhoneNumberFragment fragment) {
            weakReference = new WeakReference<ChangePhoneNumberFragment>(fragment);
        }

        @Override
        public void onSuccess(ParseObject object) {
            final ChangePhoneNumberFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            // Updates the user interface with success state and displays confirmation dialog
            fragment.setScreenState(ScreenStates.StateOk);
            PopDialog.showDialog(fragment.getActivity(),
                    fragment.getString(R.string.change_phone_dialog_title),
                    fragment.getString(R.string.change_phone_dialog_message_successfullyupdated),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            fragment.getActivity().finish();
                        }
                    });
        }

        @Override
        public void onFailed(Error error) {
            ChangePhoneNumberFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            if (!LogoutHelper.handleExpiredSession(fragment.getActivity(), error)) {
                // Updates the user interface with error state and shows the errors
                fragment.setScreenState(ScreenStates.StateMissingNumber);
                fragment.mPhoneAlertTv.setText(error.getErrorMessage(fragment.getActivity()));
            }
            fragment.hideLoading();
        }
    }
}
