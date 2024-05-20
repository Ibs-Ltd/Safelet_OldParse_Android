package com.safelet.android.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.activities.GuardianNetworkActivity;
import com.safelet.android.activities.HomeActivity;
import com.safelet.android.callback.NavigationDrawerCallbacks;
import com.safelet.android.global.PopDialog;
import com.safelet.android.global.PreferencesManager;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.LogoutHelper;

import java.lang.ref.WeakReference;


/**
 * Safelet not joined handling screen
 * <br/> Handles network requests and ui updating
 *
 * @author catalin
 */
public class SafeletCommunityNotJoinedFragment extends BaseFragment implements View.OnClickListener {

    private NavigationDrawerCallbacks parentDrawerCallback;
    private final UserManager userManager = UserManager.instance();
    private static Activity mActivity;
    private TextView txtGuardianNetworkSkip;
    private boolean isGuardianNetworkSkip;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
        try {
            this.parentDrawerCallback = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException ignore) {
            //this should not happen
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_safelet_community_not_joined, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.safelet_community_not_join_btn).setOnClickListener(this);
        txtGuardianNetworkSkip = view.findViewById(R.id.txtGuardianNetworkSkip);
        txtGuardianNetworkSkip.setOnClickListener(this);
        isGuardianNetworkSkip = PreferencesManager.instance(getContext()).getGuardianNetworkSkip();
        if (!isGuardianNetworkSkip) {
            txtGuardianNetworkSkip.setVisibility(View.VISIBLE);
        } else {
            txtGuardianNetworkSkip.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.safelet_community_not_join_btn) {
            if (Utils.isOnline()) {
                join();
            } else {
                PopDialog.showDialog(getActivity(),
                        getString(R.string.community_screen_dialog_title),
                        getString(R.string.community_screen_dialog_networkerror_message));
            }
        } else if (v.getId() == R.id.txtGuardianNetworkSkip) {
            PreferencesManager.instance(getContext()).setGuardianNetworkSkip(true);
            Intent homeIntent = new Intent(getContext(), HomeActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
            mActivity.finish();
        }
    }

    @Override
    public int getTitleResId() {
        return R.string.community_screen_title;
    }

    @Override
    public void onAlarmClicked() {
        parentDrawerCallback.onNavigationDrawerItemSelected(NavigationMenu.ALARM);
    }

    @Override
    protected void onNavigationDrawerItemSelected(NavigationMenu navigationMenu) {
        parentDrawerCallback.onNavigationDrawerItemSelected(navigationMenu);
    }

    @Override
    protected void onViewNotificationDialogClicked(Context context, NewNotificationEvent event) {
        handleNewNotificationEvent(event);
    }

    private void join() {
        EventsListFragment.sLastUpdateTime = 0;
        showLoading();
        userManager.setUserCommunityMemberStatus(true, new JoinCommunityCallback(this));
    }

    private class JoinCommunityCallback implements OnResponseCallback {
        private WeakReference<SafeletCommunityNotJoinedFragment> weakReference;

        JoinCommunityCallback(SafeletCommunityNotJoinedFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onSuccess(ParseObject object) {
            SafeletCommunityNotJoinedFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.hideLoading();
            if (!isGuardianNetworkSkip) {
                PreferencesManager.instance(getContext()).setGuardianNetworkSkip(true);
                Intent homeIntent = new Intent(getContext(), HomeActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);
                mActivity.finish();
            } else {
                ((GuardianNetworkActivity) mActivity).checkCommunityMember();
            }
        }

        @Override
        public void onFailed(Error error) {
            SafeletCommunityNotJoinedFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.hideLoading();
            if (!LogoutHelper.handleExpiredSession(fragment.getActivity(), error)) {
                Toast.makeText(fragment.getActivity(), error.getErrorMessage(fragment.getActivity()), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
