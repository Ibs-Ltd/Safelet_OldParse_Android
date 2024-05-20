package com.safelet.android.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.text.format.DateUtils;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.parse.ParseException;
import com.safelet.android.R;
import com.safelet.android.activities.HomeActivity;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.global.PreferencesManager;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.AlarmManager;
import com.safelet.android.interactors.EventBusManager;
import com.safelet.android.interactors.LocationManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.models.Alarm;
import com.safelet.android.models.UserModel;
import com.safelet.android.models.event.AlarmStartEvent;
import com.safelet.android.models.event.NetworkAvailableEvent;
import com.safelet.android.models.event.StartAlarmEvent;
import com.safelet.android.models.event.StopAlarmEvent;
import com.safelet.android.models.event.UpdateServiceNotificationEvent;
import com.safelet.android.models.event.bluetooth.BatteryChangedEvent;
import com.safelet.android.models.event.bluetooth.BluetoothEnabledEvent;
import com.safelet.android.models.event.bluetooth.DeviceConnectionStateChangedEvent;
import com.safelet.android.receivers.NetworkStateReceiver;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.log.BluetoothLog;
import com.safelet.android.utils.media.AlarmCreatedSoundPlayer;

import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import global.BluetoothState;
import models.enums.DeviceState;
import services.BluetoothConnectionService;
import timber.log.Timber;

/**
 * Main service that listens for event changes in background and sends signals to the app for updating data
 */
public final class SafeletService extends Service {

//    private static final String TAG = SafeletService.class.getSimpleName();

    private static final int NOTIFICATION_ID = 21;
    private static final int BATTERY_NOTIFICATION_ID = 47;

    private static final int BATTERY_LEVEL_LOW = 20;
    private static final int BATTERY_LEVEL_DANGEROUS = 10;

    private static final long UPDATE_LOCATION_INTERVAL = 5 * DateUtils.SECOND_IN_MILLIS;

    private static final String CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";

    private AtomicBoolean isWaitingToMakePhoneCall = new AtomicBoolean(false);
    private AtomicBoolean hasTriggeredAlarmWithoutInternet = new AtomicBoolean(false);

    private NetworkStateReceiver networkStateReceiver = new NetworkStateReceiver();

    private Executor alarmCreatorExecutor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());

    public SafeletService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();


//        Notification mNotificationService = getServiceNotification();
//        if (mNotificationService != null) {
//            startForeground(NOTIFICATION_ID, mNotificationService);
//        }
        startForeground(NOTIFICATION_ID, getServiceNotification());
        Timber.d(BaseActivity.TAG.concat("Service onCreate"));

        startService(new Intent(this, BluetoothConnectionService.class));

        // Register otto for handling alarm dispatch events
        EventBusManager.instance().register(this);

        // Start listening for location updates
        LocationManager.getInstance().start();

        // Alarm and FollowMe
        if (AlarmManager.instance().isActiveAlarm() && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager.getInstance().requestPeriodicLocation(UPDATE_LOCATION_INTERVAL);
        }

//        if (!PreferencesManager.instance(this).getFollowMeaObjectId().equals(""))
//            Timber.d(BaseActivity.TAG.concat("Service FollowMeaObjectId: ").concat(PreferencesManager.instance(this).getFollowMeaObjectId()));

        registerReceiver(networkStateReceiver, new IntentFilter(CONNECTIVITY_CHANGE));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public void updateNotificationForegroundService() {
        Notification mNotificationService = getServiceNotification();
        if (mNotificationService != null) {
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(NOTIFICATION_ID, mNotificationService);
        }
    }

    public void updateBatteryNotification() {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        PreferencesManager preference = PreferencesManager.instance(this);
        boolean batteryLevelLow = preference.getBoolean(PreferencesManager.BATTERY_LEVEL_LOW, false);
        boolean batteryLevelDangerous = preference.getBoolean(PreferencesManager.BATTERY_LEVEL_DANGEROUS, false);
        int batteryLevel = BluetoothState.get().getBatteryLevel();
        if (batteryLevel <= BATTERY_LEVEL_DANGEROUS && !batteryLevelDangerous) {
            mNotificationManager.notify(BATTERY_NOTIFICATION_ID, getBatteryNotification(batteryLevel));
            preference.setBoolean(PreferencesManager.BATTERY_LEVEL_DANGEROUS, true);
        } else if (batteryLevel <= BATTERY_LEVEL_LOW && !batteryLevelLow) {
            mNotificationManager.notify(BATTERY_NOTIFICATION_ID, getBatteryNotification(batteryLevel));
            preference.setBoolean(PreferencesManager.BATTERY_LEVEL_LOW, true);
        } else if ((batteryLevel > BATTERY_LEVEL_DANGEROUS && batteryLevelDangerous) || (batteryLevel > BATTERY_LEVEL_LOW && batteryLevelLow)) {
            mNotificationManager.cancel(BATTERY_NOTIFICATION_ID);
            preference.setBoolean(PreferencesManager.BATTERY_LEVEL_LOW, false);
            preference.setBoolean(PreferencesManager.BATTERY_LEVEL_DANGEROUS, false);
        }
    }

    /**
     * This function creates a notification of the global Safelet state (guardian-user, safelet-user) with
     * additional information
     *
     * @return Notification object with new Safelet State.
     */
    @SuppressLint("StringFormatMatches")
    private Notification getServiceNotification() {
        String channelId = Utils.createNotificationChannel(this, getString(R.string.notification_channel_status));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
//        builder.setContentTitle(getString(R.string.safelet_notification_state_no_guardian_no_safelet_title));
//        builder.setContentText(getString(R.string.safelet_notification_state_no_guardian_no_safelet_description));
//        builder.setSmallIcon(R.drawable.notification_safelet_connected_white);

//        builder.setContentTitle("");
//        builder.setContentText("");
//        builder.setSmallIcon(R.drawable.notification_safelet_connected_white);

        // Setting the mode specific information of the notifications
        int iAmGuardingCount = UserManager.instance().getImGuardingUsersCount(this);
        int guardiansCount = UserManager.instance().getMyGuardiansUsersCount(this);
        String iAmGuardingName = UserManager.instance().getImGuardingUsersName(this);
        String guardianName = UserManager.instance().getMyGuardiansUsersName(this);
        if (guardianName == null || guardianName.equals("") || guardianName.isEmpty())
            guardianName = getString(R.string.safelet_notification_state_guardian_unknown);

        // Create the variable part of the notification
        if (BluetoothState.get().isDevicePaired()) {
            if (BluetoothState.get().getDeviceState() == DeviceState.CONNECTED) {
                if (BluetoothState.get().getUpdatePercentage() > 0) {
                    builder.setSmallIcon(R.drawable.notification_safelet_firmware_update);
                    builder.setContentText(getString(R.string.safelet_notification_state_firmware_update_desc, String.valueOf(BluetoothState.get()
                            .getUpdatePercentage()) + "%"));
                    builder.setContentTitle(getString(R.string.safelet_notification_state_firmware_update_title));
                } else if (guardiansCount < 1) {
                    builder.setSmallIcon(R.drawable.notification_safelet_issue);
                    builder.setContentTitle(getString(R.string.safelet_notification_state_no_guardians));
                } else if (!Utils.isLocationEnabled(this)) {
                    builder.setSmallIcon(R.drawable.notification_safelet_issue);
                    builder.setContentTitle(getString(R.string.safelet_notification_state_no_location_enabled));
                } else if (!Utils.isOnline()) {
                    builder.setSmallIcon(R.drawable.notification_safelet_issue);
                    builder.setContentTitle(getString(R.string.safelet_notification_state_safelet_connected_no_internet));
                } else if (guardiansCount > 1) {
                    builder.setContentTitle(getString(R.string.safelet_notification_state_safelet_connected_title_multiple, guardiansCount));
                    builder.setSmallIcon(R.drawable.notification_safelet_protected);
                } else {
                    builder.setContentTitle(getString(R.string.safelet_notification_state_safelet_connected_title, guardianName));
                    builder.setSmallIcon(R.drawable.notification_safelet_protected);
                }
            } else {
                // Safelet User Disconnected
                builder.setContentTitle(getString(R.string.safelet_notification_state_safelet_disconnected_title));
                if (BluetoothState.get().getDeviceState() == DeviceState.SCANNING) {
                    builder.setContentText(getString(R.string.safelet_notification_state_searching));
                } else if (BluetoothState.get().getDeviceState() == DeviceState.CONNECTING) {
                    builder.setContentText(getString(R.string.safelet_notification_state_connecting));
                } else if (!BluetoothState.get().isBluetoothEnabled()) {
                    builder.setContentText(getString(R.string.safelet_notification_state_safelet_disconnected_description_no_bluetooth));
                } else if (!Utils.isOnline()) {
                    builder.setContentText(getString(R.string.safelet_notification_state_safelet_disconnected_description_no_internet));
                } else {
                    builder.setContentText(getString(R.string.safelet_notification_state_safelet_disconnected_description_no_safelet));
                }
                if (!BluetoothState.get().isBluetoothEnabled()) {
                    builder.setContentText(getString(R.string.safelet_notification_state_safelet_disconnected_description_no_bluetooth));
                }
                builder.setSmallIcon(R.drawable.notification_safelet_disconnected);
            }
        } else {
            if (iAmGuardingCount == 0) {
                UserModel user = UserManager.instance().getUserModel();
                if (user != null && user.isCommunityMember() && (!Utils.isLocationEnabled(this))) {
                    builder.setSmallIcon(R.drawable.notification_safelet_issue);
                    builder.setContentTitle(getString(R.string.safelet_notification_state_no_location_safelet_community_title));
                    builder.setContentText(getString(R.string.safelet_notification_state_no_location_safelet_community_desc));
                } else {
                    // No Guardian, No Safelet-User
                    builder.setContentTitle(getString(R.string.safelet_notification_state_no_guardian_no_safelet_title));
                    builder.setContentText(getString(R.string.safelet_notification_state_no_guardian_no_safelet_description));
                    builder.setSmallIcon(R.drawable.notification_safelet_connected_white);
//                    builder.setContentTitle("");
//                    builder.setContentText("");
//                    builder.setSmallIcon(R.drawable.notification_safelet_connected_white);
                }
            } else {
                // Guardian
                builder.setContentTitle(getString(R.string.safelet_notification_state_guardian_only_title));
                if (!Utils.isOnline()) {
                    builder.setSmallIcon(R.drawable.notification_safelet_issue);
                    builder.setContentText(getString(R.string.safelet_notification_state_safelet_disconnected_description_no_internet));
                } else {
                    if (iAmGuardingCount > 1)
                        builder.setContentText(getString(R.string.safelet_notification_state_guardian_only_description_multiple_persons, iAmGuardingCount));
                    else if (iAmGuardingName == null)
                        builder.setContentText(getString(R.string.safelet_notification_state_guarded_unknown));
                    else {
                        builder.setContentText(getString(R.string.safelet_notification_state_guardian_only_description, iAmGuardingName));
                    }
                    builder.setSmallIcon(R.drawable.notification_safelet_guardian);
                }
            }

//                // Guardian
//            if (iAmGuardingCount != 0) {
//                builder.setContentTitle(getString(R.string.safelet_notification_state_guardian_only_title));
//                if (!Utils.isOnline()) {
//                    builder.setSmallIcon(R.drawable.notification_safelet_issue);
//                    builder.setContentText(getString(R.string.safelet_notification_state_safelet_disconnected_description_no_internet));
//                } else {
//                    if (iAmGuardingCount > 1)
//                        builder.setContentText(getString(R.string.safelet_notification_state_guardian_only_description_multiple_persons, iAmGuardingCount));
//                    else if (iAmGuardingName == null)
//                        builder.setContentText(getString(R.string.safelet_notification_state_guarded_unknown));
//                    else {
//                        builder.setContentText(getString(R.string.safelet_notification_state_guardian_only_description, iAmGuardingName));
//                    }
//                    builder.setSmallIcon(R.drawable.notification_safelet_guardian);
//                }
//            }
        }

        // Building the notification
        Intent intentActivity = new Intent(getApplicationContext(), HomeActivity.class);
        intentActivity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                intentActivity, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        builder.setSilent(true);
        builder.setContentIntent(pendingIntent);
        return builder.build();
    }

    public Notification getBatteryNotification(int batteryLevel) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this
                , Utils.createNotificationChannel(this
                , getString(R.string.notification_channel_battery), true));
        builder.setSmallIcon(R.drawable.notification_safelet_issue);
        builder.setContentText(getString(R.string.safelet_notification_state_battery_level, batteryLevel));
        return builder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        EventBusManager.instance().unRegister(this);

        unregisterReceiver(networkStateReceiver);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(NOTIFICATION_ID);

        LocationManager.getInstance().stop();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onNetworkAvailable(NetworkAvailableEvent networkAvailableModel) {
        // We need to change the information on the notification if something has changed.
        updateNotificationForegroundService();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onStartAlarm(StartAlarmEvent startAlarmEvent) {
        BluetoothLog.writeLog(BaseActivity.TAG, "Starting alarm)");
        startAlarm();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onStopAlarm(StopAlarmEvent stopAlarmEvent) {
        BluetoothLog.writeLog(BaseActivity.TAG, "Stopping alarm)");
        isWaitingToMakePhoneCall.set(false);
    }

    /**
     * This function handles what happens when we receive a BTLE GattConnected status.
     *
     * @param deviceConnectionStateChangedEvent, the object containing the Gatt Connected status.
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onDeviceConnectionStateChanged(DeviceConnectionStateChangedEvent deviceConnectionStateChangedEvent) {
        BluetoothLog.writeLog(BaseActivity.TAG, "Update Signal Notification (Gatt Connected Trigger)");
        updateNotificationForegroundService();
    }

    /**
     * This function handles what happens when we receive a Bluetooth enabled/disabled change.
     *
     * @param bluetoothEnabledEvent, the object containing the BT enabled status.
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onBluetoothEnabledChange(BluetoothEnabledEvent bluetoothEnabledEvent) {
        BluetoothLog.writeLog(BaseActivity.TAG, "Update Signal Notification (Bluetooth Trigger)");
        updateNotificationForegroundService();
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onUpdateNotificationSignal(UpdateServiceNotificationEvent signal) {
        BluetoothLog.writeLog(BaseActivity.TAG, "Update Signal Notification (General Update Trigger)");
        updateNotificationForegroundService();
        if (BluetoothState.get().getUpdatePercentage() == 100) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Utils.createNotificationChannel(this,
                    getString(R.string.notification_channel_battery), true));
            builder.setContentTitle(getString(R.string.safelet_notification_state_firmware_update_done_title));
            builder.setContentText(getString(R.string.safelet_notification_state_firmware_update_done_desc));
            builder.setSmallIcon(R.drawable.notification_safelet_message);
            builder.setDefaults(Notification.DEFAULT_SOUND);
            builder.setAutoCancel(true);
            Notification notification = builder.build();
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(90901, notification);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe
    public void onBatteryChanged(BatteryChangedEvent batteryChangedEvent) {
        BluetoothLog.writeLog(BaseActivity.TAG, "Update Signal Notification (Bluetooth Trigger)");
        updateBatteryNotification();
    }

    /**
     * This function is called when the app receives that internet is again available. It checks
     * whether the user has started the alarm from the Safelet when internet was not available.
     *
     * @param signal, the signal for Otto to describe what we need.
     */
    @SuppressWarnings("unused")
    @Subscribe
    public void onInternetAvailable(NetworkAvailableEvent signal) {
        BluetoothLog.writeLog(BaseActivity.TAG, "onInternetAvailable()");
        if (hasTriggeredAlarmWithoutInternet.compareAndSet(true, false)) {
            // The user has triggered an alarm while we did not have internet. We should now try again!
            startAlarm();
        }
    }

    @Subscribe
    public void onLogout() {
        EventBusManager.instance().unRegister(this);
        stopSelf();
    }

    /**
     * This function will start the alarm.
     */
    private void startAlarm() {
        alarmCreatorExecutor.execute(new Runnable() {
            @Override
            public void run() {
                // We only need to start an alarm if it is not already active
                // Also if an alarm is already active, no need to start it again.
                BluetoothLog.writeLog(BaseActivity.TAG, "startAlarm() start");
                if (!AlarmManager.instance().isActiveAlarm()) {
                    BluetoothLog.writeLog(BaseActivity.TAG, "startAlarm() no active alarm yet");
                    if (Utils.isOnline()) {
                        BluetoothLog.writeLog(BaseActivity.TAG, "startAlarm() we are online");
                        // Starts alarm if user is available
                        if (UserManager.instance().isUserLoggedIn()) {
                            BluetoothLog.writeLog(BaseActivity.TAG, "startAlarm() current user found");
                            Alarm alarm = null;
                            try {
                                alarm = AlarmManager.instance().createAlarmForUser(UserManager.instance().getUserId());
                            } catch (ParseException exception) {
                                BluetoothLog.writeLog(BaseActivity.TAG, "startAlarm() Failed so start a new alarm");
                                EventBusManager.instance().postEvent(new AlarmStartEvent(new Error(exception)));
                            }
                            if (alarm != null) {
                                BluetoothLog.writeLog(BaseActivity.TAG, "startAlarm() open Alert view IF APP OPEN ");
                                // play alarm created sound
                                AlarmCreatedSoundPlayer.playSound();

                                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) ==
                                        PackageManager.PERMISSION_GRANTED) {
                                    // start recording
                                    RecordSoundService.startRecording(getApplicationContext());
                                }

                                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                                        PackageManager.PERMISSION_GRANTED) {
                                    LocationManager.getInstance().requestPeriodicLocation(UPDATE_LOCATION_INTERVAL);
                                }

                                EventBusManager.instance().postEvent(new AlarmStartEvent(alarm));
                            }
                        } else {
                            // This should never happen. If we are active, so it parse and you should be logged in, otherwise this service is never
                            // running.
                            BluetoothLog.writeLog(BaseActivity.TAG, "You are able to communicate with the Safelet but you are not logged in as a user so the " +
                                    "alert cannot be connected to a user and thus is not send to other users.");
                            //Implement failsafe no matter what.
                        }
                    } else {
                        // We have received an alert trigger but we are not online.
                        // Our service waits for a internet is on signal to then try to start the alarm again.
                        BluetoothLog.writeLog(BaseActivity.TAG, "startAlarm() not online so we cannot start alarm, save this information and try to start alarm " +
                                "when internet is back on.");
                        hasTriggeredAlarmWithoutInternet.set(true);
                    }
                }
                BluetoothLog.writeLog(BaseActivity.TAG, "startAlarm() end");
            }
        });
    }
}
