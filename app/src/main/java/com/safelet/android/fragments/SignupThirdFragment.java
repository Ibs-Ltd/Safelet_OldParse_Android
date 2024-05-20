package com.safelet.android.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.activities.HomeActivity;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.global.PopDialog;
import com.safelet.android.interactors.DeviceInformationsManager;
import com.safelet.android.interactors.PhoneContactsManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.ParsePushUtil;

import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Sign up user third fragment
 * <p/>
 * Created by Alin on 10/18/2015.
 */
public class SignupThirdFragment extends BaseFragment implements View.OnClickListener {
    private final UserManager userManager = UserManager.instance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register_third, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.register_back_tv).setOnClickListener(this);
        view.findViewById(R.id.register_join_btn).setOnClickListener(this);
        view.findViewById(R.id.register_declinejoin_btn).setOnClickListener(this);
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
    public int getTitleResId() {
        return NO_TITLE;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.register_back_tv) {
            getActivity().onBackPressed();
        } else if (v.getId() == R.id.register_join_btn) {
            goToNextScreen(true);
        } else if (v.getId() == R.id.register_declinejoin_btn) {
            goToNextScreen(false);
        }
    }

    private void goToNextScreen(boolean joinToCommunity) {
        showLoading();
        userManager.getUserModel().setCommunityMember(joinToCommunity);
        userManager.createAccountForUser(new CreateAccountCallback(this));
    }

    private void onDone() {
        ParsePushUtil.subscribeInBackground(UserManager.instance().getUserId());
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            PhoneContactsManager.instance().initCountryCode(getContext().getApplicationContext(),
                    UserManager.instance().getUserModel().getCountryPrefixCode());
            PhoneContactsManager.instance().readPhoneContactsAsync(getContext().getContentResolver());
        }
        // Displays success message that sends the user to home screen
        PopDialog.showDialog(getActivity(), getString(R.string.signup_fourth_dialog_title_success),
                getString(R.string.signup_fourth_dialog_title_message_accountcreatedsuccess),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent homeIntent = new Intent(getActivity(), HomeActivity.class);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(homeIntent);
                    }
                });
    }

    private static class CreateAccountCallback implements OnResponseCallback {
        private WeakReference<SignupThirdFragment> weakReference;

        private CreateAccountCallback(SignupThirdFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onSuccess(ParseObject object) {
            SignupThirdFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            DeviceInformationsManager.instance().uploadPhoneDetails();
            fragment.hideLoading();
            fragment.onDone();
        }

        @Override
        public void onFailed(Error error) {
            SignupThirdFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.hideLoading();
            PopDialog.showDialog(fragment.getActivity(), fragment.getString(R.string.signup_fourth_dialog_title),
                    error.getErrorMessage(fragment.getActivity()));
            Timber.d("".concat(BaseActivity.TAG).concat("Create Account Upload Phone Details: ").concat(error.getErrorMessage(fragment.getActivity())));
            fragment.hideLoading();
        }
    }
}
