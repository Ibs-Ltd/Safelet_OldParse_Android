package com.safelet.android.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.activities.AlarmActivity;
import com.safelet.android.activities.HomeActivity;
import com.safelet.android.activities.LastCheckinActivity;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.global.PopDialog;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.AlarmManager;
import com.safelet.android.interactors.EventBusManager;
import com.safelet.android.interactors.EventsManager;
import com.safelet.android.models.Alarm;
import com.safelet.android.models.GuardianInvitation;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.NetworkAvailableEvent;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.models.event.NoInternetConnectionEvent;
import com.safelet.android.models.event.NotificationReceivedEvent;
import com.safelet.android.receivers.SafeletPushBroadcastReceiver;
import com.safelet.android.services.RecordSoundService;
import com.safelet.android.utils.Utility;
import com.safelet.android.utils.media.AlarmNotificationSoundPlayer;
import com.safelet.android.views.AlarmView;
import com.safelet.android.views.NotificationPopUpNetworkErrorView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Base fragment
 * <p/>
 * Created by alin on 13.10.2015.
 */
public abstract class BaseFragment extends Fragment {

    protected static final int NO_TITLE = 0;
    protected AlarmView alarmView;
    protected NotificationPopUpNetworkErrorView networkError;
    protected Dialog dialog;

    private BroadcastReceiver notificationsBroadcastReceiver;
    private BroadcastReceiver recordingStatusChangedBroadcastReceiver;

    private AlertDialog inviteAlertDialog = null;
    private AlertDialog checkinAlertDialog = null;
    private AlertDialog alarmAlertDialog = null;
    private static final String TAG = "SafeletApp ";

    public abstract int getTitleResId();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notificationsBroadcastReceiver = new NotificationsReceiver();
        recordingStatusChangedBroadcastReceiver = new RecordingStatusChangedBroadcastReceiver();

        // Create loading dialog
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        networkError = view.findViewById(R.id.notificationPopUpNetworkErrorView);
        hideNetworkError();

        try {
            alarmView = view.findViewById(R.id.globalAlertPopView);
            alarmView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAlarmClicked();
                }
            });
        } catch (Exception ignore) {
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Updates navigation bar color if alerts are available
        setAlertMode(AlarmManager.instance().isActiveAlarm());
        int titleResId = getTitleResId();
        if (titleResId != NO_TITLE) {
            getActivity().setTitle(titleResId);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(notificationsBroadcastReceiver,
                new IntentFilter(SafeletPushBroadcastReceiver.NEW_NOTIFICATION_INTENT));
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(recordingStatusChangedBroadcastReceiver,
                new IntentFilter(RecordSoundService.RECORDING_STATUS_CHANGED));
        EventBusManager.instance().register(this);
        if (!Utils.isOnline()) {
            EventBusManager.instance().postEvent(new NoInternetConnectionEvent());
        }

        Timber.d(TAG.concat("Activity Fragment Name ".concat(this.getClass().getSimpleName())));
        Utility.hideKeyboard(getActivity());
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(notificationsBroadcastReceiver);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(recordingStatusChangedBroadcastReceiver);
        super.onPause();
        EventBusManager.instance().unRegister(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        dialog.dismiss();
    }

    /**
     * Shows loading dialog
     */
    public void showLoading() {
        dialog.show();
    }

    /**
     * Hides loading dialog
     */
    public void hideLoading() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    protected void setAlertMode(boolean alertModeAlarm) {
        if (alertModeAlarm) {
            showAlarmViewWithAlarm(AlarmManager.instance().getActiveAlarm());
        } else {
            hideAlarmView();
        }
    }

    private void showAlarmViewWithAlarm(Alarm alarm) {
        if (alarmView != null) {
            alarmView.setVisibilityNoAnimation(View.VISIBLE);
            alarmView.showAlarm(alarm, EventsManager.instance().getNumberOfActiveAlarms());
            alarmView.setIsRecording(alarm.canRecord());
        }
    }

    private void hideAlarmView() {
        if (alarmView != null) {
            alarmView.setVisibility(View.GONE);
        }
    }

    private void showNetworkError() {
        if (networkError != null) {
            if (!Utils.isOnline())
                networkError.setVisibility(View.VISIBLE);
        }
    }

    private void hideNetworkError() {
        if (networkError != null) {
            networkError.setVisibility(View.GONE);
        }
    }

    public void onAlarmClicked() {
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public final void onNotificationsNumberChanged(NotificationReceivedEvent event) {
        setAlertMode(AlarmManager.instance().isActiveAlarm());
        showAlarmViewWithAlarm(AlarmManager.instance().getActiveAlarm());
    }

    protected void onAlertFromSafelet() {
        goToHomeScreenWithNotification(new NewNotificationEvent(NewNotificationEvent.NotificationType.ALARM_FROM_SAFELET, new ArrayList<String>(), ""));
    }

    protected void goToHomeScreenWithNotification(NewNotificationEvent event) {
        Intent newIntent = new Intent(getActivity(), HomeActivity.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        newIntent.putExtra(HomeActivity.EXTRA_FROM_NOTIFICATION_KEY, event);
        startActivity(newIntent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInternetConnection(NoInternetConnectionEvent event) {
        showNetworkError();
        hideLoading();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInternetAvailable(NetworkAvailableEvent nAvailable) {
        // Internet available, resume application state
        hideNetworkError();
    }

    protected void handleNewNotificationEvent(NewNotificationEvent notificationEvent) {
        switch (notificationEvent.getNotificationType()) {
            case SEND_INVITATION:
            case ACCEPT_INVITATION:
            case REJECT_INVITATION:
            case CANCEL_INVITATION:
                onNavigationDrawerItemSelected(NavigationMenu.EVENTS);
                break;

            case CHECK_IN_SHORT:
            case CHECK_IN_LONG:
                Intent checkInIntent = new Intent(getActivity(), LastCheckinActivity.class);
                checkInIntent.putExtra(LastCheckinActivity.KEY_MODEL_CHECKIN, notificationEvent.getObjectId());
                startActivity(checkInIntent);
                onNavigationDrawerItemSelected(NavigationMenu.EVENTS);
                break;

            case ALARM_FROM_SAFELET:
            case JOIN_ALARM:
                onAlarmClicked();
                break;

            case DISPATCH_ALARM:
                Intent alarmIntent = new Intent(getActivity(), AlarmActivity.class);
                alarmIntent.putExtra(AlarmActivity.ALARM_ID_KEY, notificationEvent.getObjectId());
                startActivity(alarmIntent);
                onNavigationDrawerItemSelected(NavigationMenu.EVENTS);
                break;

            default:
                break;
        }
    }

    private class NotificationsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, final Intent intent) {
            AlarmNotificationSoundPlayer.instance().stopSound();
            final NewNotificationEvent notificationEvent = intent.getParcelableExtra(SafeletPushBroadcastReceiver.NOTIFICATION_EXTRA);
            ParseObject notificationObject = EventsManager.instance().getEventForId(notificationEvent.getObjectId());

            switch (notificationEvent.getNotificationType()) {
                case SEND_INVITATION:
                case ACCEPT_INVITATION:
                case REJECT_INVITATION:
                case CANCEL_INVITATION:
                    GuardianInvitation guardianInvitation = (GuardianInvitation) notificationObject;
                    if (guardianInvitation != null) {
                        if (inviteAlertDialog != null) {
                            inviteAlertDialog.dismiss();
                            inviteAlertDialog = null;
                        }
                        inviteAlertDialog = showIntentDialog(context, notificationEvent);
                    }
                    return;

                case CHECK_IN_SHORT:
                case CHECK_IN_LONG:
                    if (checkinAlertDialog != null) {
                        checkinAlertDialog.dismiss();
                        checkinAlertDialog = null;
                    }
                    checkinAlertDialog = showIntentDialog(context, notificationEvent);
                    return;

                case JOIN_ALARM:
                case DISPATCH_ALARM:
                    if (alarmAlertDialog != null) {
                        alarmAlertDialog.dismiss();
                        alarmAlertDialog = null;
                    }
                    Alarm alarm = (Alarm) notificationObject;
                    if (alarm != null && alarm.isAlarmActive()) {
                        alarmAlertDialog = showIntentDialog(context, notificationEvent);
                        return;
                    }
                    return;

                case START_FOLLOW_ME:
                case STOP_FOLLOW_ME:
                case STOP_FOLLOW_USER:
                    FragmentManager fm = getFragmentManager();
                    if (fm != null) {
                        HomeFragment mHomeFragment = (HomeFragment) fm.findFragmentById(R.id.fragment_container);
                        if (mHomeFragment != null) {
                            // Stop Follow Me Notification Remove Location Update and Map marker
                            if (notificationEvent.getNotificationType() == NewNotificationEvent.NotificationType.START_FOLLOW_ME) {
                                Timber.tag(BaseActivity.TAG).d("".concat("Start Follow Me Notification"));
                                mHomeFragment.checkCurrentFollowUpStatus();
                            } else if (notificationEvent.getNotificationType() == NewNotificationEvent.NotificationType.STOP_FOLLOW_ME) {
                                Timber.tag(BaseActivity.TAG).d("".concat("Stop Follow Me Notification"));
                                mHomeFragment.onStopSingleUserRequest();
                            } else if (notificationEvent.getNotificationType() == NewNotificationEvent.NotificationType.STOP_FOLLOW_USER) {
                                Timber.tag(BaseActivity.TAG).d("".concat("Stop Follow User Notification"));
                                mHomeFragment.onStopFollowMeUser();
                            }
                        }
                    }

                    PopDialog.showDialog(getActivity(),
                            getString(R.string.alert_screen_title),
                            getString(notificationEvent.getNotificationType().getMessageResId(), notificationEvent.getMessageArgs().toArray()),
                            PopDialog.TYPE_DISMISS,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    onDismissNotificationDialogClicked(notificationEvent);
                                }
                            });
                    return;

                case POLICY_NOTIFICATION:

                    return;
            }

            PopDialog.showDialog(getActivity(),
                    getString(R.string.alert_screen_title),
                    getString(notificationEvent.getNotificationType().getMessageResId(), notificationEvent.getMessageArgs().toArray()),
                    PopDialog.TYPE_DISMISS,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            onDismissNotificationDialogClicked(notificationEvent);
                        }
                    });
        }

        private AlertDialog showIntentDialog(final Context context, final NewNotificationEvent event) {
            return PopDialog.showDialog(getActivity(),
                    getString(R.string.alert_screen_title),
                    getString(event.getNotificationType().getMessageResId(), event.getMessageArgs().toArray()),
                    getString(R.string.view),
                    getString(R.string.dismiss),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            onViewNotificationDialogClicked(context, event);
                        }
                    },
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            onDismissNotificationDialogClicked(event);
                        }
                    });
        }
    }

    private class RecordingStatusChangedBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            showAlarmViewWithAlarm(AlarmManager.instance().getActiveAlarm());
        }
    }

    protected abstract void onViewNotificationDialogClicked(Context context, NewNotificationEvent event);

    protected abstract void onNavigationDrawerItemSelected(NavigationMenu navigationMenu);

    protected void onDismissNotificationDialogClicked(NewNotificationEvent event) {
    }
}
