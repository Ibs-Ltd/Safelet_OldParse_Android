package com.safelet.android.activities.base;

import static com.safelet.android.fragments.HomeFragment.REQUEST_NOTIFICATION;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.safelet.android.BuildConfig;
import com.safelet.android.R;
import com.safelet.android.callback.NavigationDrawerCallbacks;
import com.safelet.android.fragments.CheckInFragment;
import com.safelet.android.fragments.EventsFragment;
import com.safelet.android.fragments.MyConnectionsFragment;
import com.safelet.android.fragments.MyProfileFragment;
import com.safelet.android.fragments.NavigationDrawerFragment;
import com.safelet.android.fragments.OptionsFragment;
import com.safelet.android.global.PopDialog;
import com.safelet.android.global.PreferencesManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.utils.LogoutHelper;
import com.safelet.android.utils.PermissionCallback;
import com.safelet.android.utils.PermissionManager;

import activities.PairSafeletWizard01Welcome;
import global.BluetoothState;
import models.enums.DeviceState;
import timber.log.Timber;

public abstract class HomeBaseActivity extends BaseActivity implements NavigationDrawerCallbacks, PermissionCallback {

    private static final String MAILTO = "mailto";

    private static final String CONTACT_SAFELET = "safeletcomsupport@safelet.freshdesk.com";

    /**
     * Navigation drawer layout
     */
    protected DrawerLayout drawerLayout;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    protected NavigationDrawerFragment navigationDrawerFragment;

//    private final UserManager userManager = UserManager.instance();

    private static final String TAG = "SafeletApp ";

    protected abstract void startAlarm();

    Toolbar mToolBarMain;
    public String mFragmentName = "HomeFragment";
    private PermissionManager permissionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_base);

        mToolBarMain = findViewById(R.id.safelet_toolbar);
        mToolBarMain.setNavigationIcon(R.drawable.ic_menu_white);
        setSupportActionBar(mToolBarMain);

        drawerLayout = findViewById(R.id.home_base_drawer_layout);
        navigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        if (navigationDrawerFragment != null)
            navigationDrawerFragment.setUp(R.id.navigation_drawer, drawerLayout, this);

        mToolBarMain.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // || mFragmentName.equalsIgnoreCase("HomeActivity")
                if (mFragmentName.equalsIgnoreCase("HomeFragment")
                        || mToolBarMain.getTitle().toString().equalsIgnoreCase("Home")) {
                    navigationDrawerFragment.toggleMenu();
                } else {
//                    navigationDrawerFragment.setMenuSelected(NavigationMenu.HOME);
                    onBackPressed();
                }
                onToolBarSetUp();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mFragmentName = this.getClass().getSimpleName();
        onToolBarSetUp();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionManager.REQUEST_BLUETOOTH_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, invoke the callback
                onPermissionGranted();
            } else {
                // Permission denied, invoke the callback
                onPermissionDenied();
            }
        }

        if (requestCode == REQUEST_NOTIFICATION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(getApplicationContext(), "Permission denied by user", Toast.LENGTH_SHORT).show();
                System.out.println("Notification Permission Not Granted");
                openSettingForNotification();
            }
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                System.out.println("Notification Permission Granted");
            }
        }
    }

    private void openSettingForNotification() {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(intent);
    }


    @Override
    public void updateMenu() {
        navigationDrawerFragment.refreshMenuList();
    }

    @Override
    public void onBackPressed() {
        mFragmentName = this.getClass().getSimpleName();
        onToolBarSetUp();
        if (navigationDrawerFragment.isDrawerOpen()) {
            navigationDrawerFragment.toggleMenu();
        } else {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                navigationDrawerFragment.setMenuSelected(NavigationMenu.HOME);
            }
            super.onBackPressed();
            supportInvalidateOptionsMenu();
        }
    }

    @Override
    public void onNavigationDrawerItemSelected(NavigationMenu menuItem) {
        Fragment fragment = null;
        switch (menuItem) {
//            case GETTING_STARTED:
//                navigationDrawerFragment.setMenuSelected(menuItem);
//                fragment = new GettingStartedFragment();
//                break;

//            case HOME:
//                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
//                    navigationDrawerFragment.setMenuSelected(menuItem);
//                    getSupportFragmentManager().popBackStackImmediate();
//                    supportInvalidateOptionsMenu();
//                }
//                break;

            case MY_CONNECTIONS:
                navigationDrawerFragment.setMenuSelected(menuItem);
                fragment = new MyConnectionsFragment();
                break;

            case EVENTS:
                navigationDrawerFragment.setMenuSelected(menuItem);
                fragment = new EventsFragment();
                break;

            case MY_PROFILE:
                navigationDrawerFragment.setMenuSelected(menuItem);
                fragment = new MyProfileFragment();
                break;

//            case GUARDIAN_NETWORK:
//                navigationDrawerFragment.setMenuSelected(menuItem);
//                if (userManager.getUserModel().isCommunityMember()) {
//                    fragment = new SafeletCommunityJoinedFragment();
//                } else {
//                    fragment = new SafeletCommunityNotJoinedFragment();
//                }
//                break;

            case CHECK_IN:
                navigationDrawerFragment.setMenuSelected(menuItem);
                fragment = new CheckInFragment();
                break;

            case CONNECT_SAFELET:
                // We will start the pair wizard to initiate first use of the safelet.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
                    permissionManager = new PermissionManager();
                    permissionManager.requestBluetoothPermission(this, this);
                } else {
                    Intent intent = new Intent(this, PairSafeletWizard01Welcome.class);
                    startActivity(intent);
                }
                break;

            case DISCONNECT_SAFELET:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.safelet_disconnect_title);
                if (BluetoothState.get().getDeviceState() == DeviceState.CONNECTED) {
                    builder.setMessage(R.string.safelet_disconnect_alert_connected_text);
                } else {
                    builder.setMessage(R.string.safelet_disconnect_alert_disconnected_text);
                }
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        BluetoothState.get().setMacAddress(null);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                AlertDialog dialog = builder.create();
                dialog.show();
                break;

            case ALARM:
                startAlarm();
                break;

            case PRIVACY:
                onPrivacy();
                break;

            case OPTIONS:
                navigationDrawerFragment.setMenuSelected(menuItem);
                fragment = new OptionsFragment();
                break;

            case FEEDBACK:
                String safeletInfo;
                String firmwaveRevision = PreferencesManager.instance(this).getString(PreferencesManager.DEVICE_FIRMWARE_REVISION);
                if (!TextUtils.isEmpty(firmwaveRevision)) {
                    safeletInfo = getString(R.string.feedback_firmware_version, firmwaveRevision);
                } else {
                    safeletInfo = getString(R.string.feedback_safelet_not_connected);
                }

                String user = UserManager.instance().getUserEmail();
                if (TextUtils.isEmpty(user)) {
                    user = UserManager.instance().getUserName();
                }

                Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                emailIntent.setData(Uri.fromParts(MAILTO, CONTACT_SAFELET, null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
                emailIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.feedback_body, user, Build.MODEL, Build.VERSION.SDK_INT,
                        getString(R.string.feedback_safelet_version, BuildConfig.VERSION_NAME), safeletInfo));
                startActivity(Intent.createChooser(emailIntent, null));
                break;

            case LOGOUT:
                logoutUser();
                break;
        }

        if (fragment != null) {
            addFragmentToRoot(fragment);
        }
    }

    // Todo : Privacy Policy
    private void onPrivacy() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_policy_url))));
    }

    /**
     * User logout function<br/>
     * Clears all user data
     */
    public final void logoutUser() {
        PopDialog.showDialog(this, getString(R.string.home_dialog_logout_title),
                getString(R.string.home_dialog_logout_message),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LogoutHelper.logout(mContext);
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // no
                        dialog.cancel();
                    }
                });
    }

    public void addFragmentToRoot(Fragment fragment) {
//        if (!fragment.getClass().getSimpleName().equalsIgnoreCase("MyProfileFragment"))
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            // pop back stack in case the stack count is bigger then 0
            // (New Changes) we want to have only one fragment (HomeFragment) in stack every time
            // we want to have only one fragment (My connections fragment) in stack every time
            getSupportFragmentManager().popBackStackImmediate();
            supportInvalidateOptionsMenu();
        }
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();

        mFragmentName = fragment.getClass().getSimpleName();
        onToolBarSetUp();
    }

    private void onToolBarSetUp() {
        mToolBarMain.post(new Runnable() {
            @Override
            public void run() {
                Timber.d(TAG.concat("onToolBarSetUp mFragmentName: ").concat(mFragmentName));
                Timber.d(TAG.concat("onToolBarSetUp Toolbar Title: ").concat(mToolBarMain.getTitle().toString()));
                // || mFragmentName.equalsIgnoreCase("HomeActivity")
                if (mFragmentName.equalsIgnoreCase("HomeFragment")
                        || mToolBarMain.getTitle().toString().equalsIgnoreCase("Home")) {
                    mToolBarMain.setNavigationIcon(R.drawable.ic_menu_white);
                } else {
                    mToolBarMain.setNavigationIcon(R.drawable.ic_arrow_back_white);
                }
            }
        });
    }
}
