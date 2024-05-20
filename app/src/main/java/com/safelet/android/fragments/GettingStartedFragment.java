package com.safelet.android.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.safelet.android.R;
import com.safelet.android.callback.NavigationDrawerCallbacks;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.NewNotificationEvent;

public class GettingStartedFragment extends BaseFragment {

    private static final String NEW_NOTIFICATION_KEY = "mcf.newNotification.key";
    private NavigationDrawerCallbacks parentDrawerCallback;

    private NewNotificationEvent notificationEvent = null;

    public static GettingStartedFragment newInstance(NewNotificationEvent newNotificationEvent) {
        GettingStartedFragment fragment = new GettingStartedFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(NEW_NOTIFICATION_KEY, newNotificationEvent);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static GettingStartedFragment newInstance() {
        return new GettingStartedFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            this.parentDrawerCallback = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException ignore) {
            // should not happen
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            notificationEvent = getArguments().getParcelable(NEW_NOTIFICATION_KEY);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_getting_started_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (notificationEvent != null) {
            handleNewNotificationEvent(notificationEvent);
            notificationEvent = null;
        }
    }

    @Override
    public int getTitleResId() {
        return R.string.txt_menu_getting_started;
    }

    @Override
    public void onAlarmClicked() {
        parentDrawerCallback.onNavigationDrawerItemSelected(NavigationMenu.ALARM);
    }

    @Override
    protected void onViewNotificationDialogClicked(Context context, NewNotificationEvent event) {
        handleNewNotificationEvent(event);
    }

    @Override
    protected void onNavigationDrawerItemSelected(NavigationMenu navigationMenu) {
        parentDrawerCallback.onNavigationDrawerItemSelected(navigationMenu);
    }


}
