package com.safelet.android.activities;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.safelet.android.BuildConfig;
import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.activities.base.HomeBaseActivity;
import com.safelet.android.fragments.AlarmFragment;
import com.safelet.android.fragments.HomeFragment;
import com.safelet.android.global.PopDialog;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.AlarmManager;
import com.safelet.android.interactors.EventBusManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.AlarmStartEvent;
import com.safelet.android.models.event.NetworkAvailableEvent;
import com.safelet.android.models.event.NewAlarmEvent;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.models.event.StartAlarmEvent;
import com.safelet.android.services.SafeletService;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.LogoutHelper;
import com.safelet.android.utils.PermissionCallback;
import com.safelet.android.utils.PermissionManager;
import com.safelet.android.utils.VersionChecker;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.ExecutionException;

import activities.PairSafeletWizard01Welcome;
import services.BluetoothConnectionService;
import timber.log.Timber;

public final class HomeActivity extends HomeBaseActivity {

    public static final String EXTRA_FROM_NOTIFICATION_KEY = "ha.extraFromNotification.key";

    private final UserManager userManager = UserManager.instance();
    private final AlarmManager alarmManager = AlarmManager.instance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Starts service required for handling notifications and alarms
        ContextCompat.startForegroundService(getApplicationContext(), new Intent(getApplicationContext(), SafeletService.class));

        // Checks if the user has a previous alarm
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();

            if (extras != null){
                System.out.println("This is extras " + extras);
                System.out.println("This is extras00 " + extras.containsKey(EXTRA_FROM_NOTIFICATION_KEY));
            } else {
                System.out.println("This is extras 1 " + extras);
            }

            if (extras != null && extras.containsKey(EXTRA_FROM_NOTIFICATION_KEY)) {
                NewNotificationEvent notificationEvent = getIntent().getExtras().getParcelable(EXTRA_FROM_NOTIFICATION_KEY);
                System.out.println("This is event " + notificationEvent);
                System.out.println("This is extra " + getIntent().getExtras().getParcelable(EXTRA_FROM_NOTIFICATION_KEY));
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.fragment_container, MyConnectionsFragment.newInstance(notificationEvent))
//                        .commit();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment.newInstance(notificationEvent))
                        .commit();
            } else {
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.fragment_container, MyConnectionsFragment.newInstance())
//                        .commit();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment.newInstance())
                        .commit();
            }
        }

//        appUpdater();


        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TAG", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        Timber.tag(BaseActivity.TAG).d("".concat("onSuccess Token: ").concat(token));
                        ParseInstallation.getCurrentInstallation().setDeviceToken(token);
                        ParseInstallation.getCurrentInstallation().saveInBackground();
                    }
                });


    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBusManager.instance().register(this);
        checkPreviousAlarm();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        appUpdater();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBusManager.instance().unRegister(this);
    }

    /**
     * Starts alarm screen by dispatching events on server and updating user interface
     */
    @Override
    public void startAlarm() {
        showLoading();
        if (AlarmManager.instance().isActiveAlarm()) {
            hideLoading();
            if (navigationDrawerFragment.getMenuSelected() != NavigationMenu.ALARM) {
                // Shows alert screen
                addFragmentToRoot(AlarmFragment.newInstance());
                navigationDrawerFragment.setMenuSelected(NavigationMenu.ALARM);
            }
        } else if (Utils.isOnline()) {
            // No previous alarm is available
            // Starts alarm if user is available
            EventBusManager.instance().postEvent(new StartAlarmEvent());
        } else if (!Utils.isOnline()) {
            // Device internet connectivity unavailable
            hideLoading();
            PopDialog.showDialog(HomeActivity.this,
                    getString(R.string.home_dialog_title),
                    getString(R.string.home_dialog_message_networkerror));
        } else {
            // Fallback
            hideLoading();
        }
    }

    private void checkPreviousAlarm() {
        alarmManager.getActiveAlarmForUser(userManager.getUserModel().getObjectId(), new CheckPreviousAlarmListener());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAlarmStarted(AlarmStartEvent event) {
        hideLoading();
        if (event.getError() == null) {
            navigationDrawerFragment.setMenuSelected(NavigationMenu.ALARM);
            addFragmentToRoot(AlarmFragment.newInstance());
        } else if (!LogoutHelper.handleExpiredSession(this, event.getError())) {
            PopDialog.showDialog(this, getString(R.string.home_dialog_title), getString(R.string.home_dialog_message_networkerror));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewAlarmEvent(NewAlarmEvent event) {
        if (event.getError() == null) {
            startAlarm();
        } else {
            LogoutHelper.handleExpiredSession(this, event.getError());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onInternetAvailable(NetworkAvailableEvent event) {
        if (userManager.isUserLoggedIn()) {
            alarmManager.getActiveAlarmForUser(userManager.getUserModel().getObjectId(), new CheckPreviousAlarmListener());
        }
    }

    @Override
    public void onPermissionGranted() {
        Intent intent = new Intent(this, PairSafeletWizard01Welcome.class);
        startActivity(intent);
    }

    @Override
    public void onPermissionDenied() {
        Toast.makeText(this, "Permission denied. Please enable the permission in app settings.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        intent.putExtra("android.provider.extra.APP_PACKAGE", getPackageName());
        startActivity(intent);
    }

    private static class CheckPreviousAlarmListener implements OnResponseCallback {

        @Override
        public void onSuccess(ParseObject object) {
            EventBusManager.instance().postEvent(new NewAlarmEvent(object));
        }

        @Override
        public void onFailed(Error error) {
            EventBusManager.instance().postEvent(new NewAlarmEvent(error));
        }
    }

    private void updateShowDialog() {

        final Dialog dialog = new Dialog(HomeActivity.this);
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

        });

        dialog.show();
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
