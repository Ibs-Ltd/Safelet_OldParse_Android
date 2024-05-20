package com.safelet.android.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.callback.NavigationDrawerCallbacks;
import com.safelet.android.global.PreferencesManager;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.CheckInManager;
import com.safelet.android.interactors.UserManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.interactors.utils.ParseConstants;
import com.safelet.android.models.UserModel;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.receivers.LocationUpdatesBroadcastReceiver;
import com.safelet.android.utils.AppIntents;
import com.safelet.android.utils.Error;
import com.safelet.android.utils.LatLngInterpolator;
import com.safelet.android.utils.MarkerAnimation;
import com.safelet.android.utils.Utility;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class HomeFragment extends BaseFragment implements View.OnClickListener, OnMapReadyCallback
        , GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String NEW_NOTIFICATION_KEY = "mcf.newNotification.key";
    private NavigationDrawerCallbacks parentDrawerCallback;
    private NewNotificationEvent notificationEvent = null;

    // New
    private static final int REQUEST_ACCESS_FINE_LOCATION = 5445;
    public static final int REQUEST_NOTIFICATION = 101;
    private GoogleMap googleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Marker mCurrentLocationMarker, mFollowMeMarker;
    private Location currentLocation;
    private boolean firstTimeFlag = true;

    private Activity mActivity;

    private List<UserModel> myGuardiansCache = null;
    private List<UserModel> imGuardingCache = null;

    private LinearLayout layoutFollowMeUser, layoutSingleUser;
    private ImageView imgFollowMeUserPic;
    private TextView txtFollowMeUserName, txtFollowMeAddress;
    private RecyclerView rvFollowMeUser;
    private TextView txtFollowMeUserStop;

    // FollowMe Single Location get
    Handler h = new Handler();
    int delay = 10 * 1000; //1 second=1000 milisecond, 15*1000=15seconds
    Runnable runnable;

    // New Added
//    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;
    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    //  5/16/17
    private static final long UPDATE_INTERVAL = 10 * 1000;

    /**
     * The fastest rate for active location updates. Updates will never be more frequent
     * than this value, but they may be less frequent.
     */
    // 5/14/17
    private static final long FASTEST_UPDATE_INTERVAL = UPDATE_INTERVAL / 2;

    /**
     * The max time before batched results are delivered by location services. Results may be
     * delivered sooner than this interval.
     */
//    private static final long MAX_WAIT_TIME = UPDATE_INTERVAL * 3;
    private static final long MAX_WAIT_TIME = 6;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * The entry point to Google Play Services.
     */
    private GoogleApiClient mGoogleApiClient;

    public static HomeFragment newInstance(NewNotificationEvent newNotificationEvent) {
        HomeFragment fragment = new HomeFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(NEW_NOTIFICATION_KEY, newNotificationEvent);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
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

        notificationRunTimePermission();

        myGuardiansCache = UserManager.instance().getMyGuardiansCache();
        imGuardingCache = UserManager.instance().getImGuardingCache();
    }

    private void notificationRunTimePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(requireContext(),Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIFICATION);
            }
            else {
                System.out.println("Permission Grant for Notification");
                // repeat the permission or open app details
            }
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        onRefreshMyConnections(myGuardiansCache == null || imGuardingCache == null);
        Timber.d("".concat(BaseActivity.TAG).concat("Home Fragment onStart"));

        // Check if the user revoked runtime permissions.
        if (!checkPermissions()) {
            requestPermissions();
        }

        buildGoogleApiClient();

        checkCurrentFollowUpStatus();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home_layout, container, false);
        SupportMapFragment mapFrag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFrag != null)
            mapFrag.getMapAsync(this);
        else {
            Toast.makeText(mActivity, "Map is empty", Toast.LENGTH_SHORT).show();
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!UserManager.instance().getUserModel().isTermsConditionAccepted()) {
            showDialogTermsConditionAccept();
        }

        TextView txtFollowMe = view.findViewById(R.id.txtFollowMe);
        txtFollowMe.setOnClickListener(this);
        TextView txtIMHere = view.findViewById(R.id.txtIMHere);
        txtIMHere.setOnClickListener(this);
        TextView txtSOS = view.findViewById(R.id.txtSOS);
        txtSOS.setOnClickListener(this);
        ImageButton imgBtnCurrentLocation = view.findViewById(R.id.imgBtnCurrentLocation);
        imgBtnCurrentLocation.setOnClickListener(this);

        // Follow Me Setup
        layoutFollowMeUser = view.findViewById(R.id.layoutFollowMeUser);
        layoutFollowMeUser.setVisibility(View.GONE);
        layoutSingleUser = view.findViewById(R.id.layoutSingleUser);
        layoutSingleUser.setVisibility(View.GONE);
        imgFollowMeUserPic = view.findViewById(R.id.imgFollowMeUserPic);
        txtFollowMeUserName = view.findViewById(R.id.txtFollowMeUserName);
        txtFollowMeAddress = view.findViewById(R.id.txtFollowMeAddress);

        txtFollowMeUserStop = view.findViewById(R.id.txtFollowMeUserStop);
        txtFollowMeUserStop.setOnClickListener(this);
        TextView txtSingleUserStop = view.findViewById(R.id.txtSingleUserStop);
        txtSingleUserStop.setOnClickListener(this);

        rvFollowMeUser = view.findViewById(R.id.rvFollowMeUser);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true);
        layoutManager.setReverseLayout(false);
//        layoutManager.setStackFromEnd(true);
        rvFollowMeUser.setLayoutManager(layoutManager);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent();
            String packageName = getContext().getPackageName();
            PowerManager pm = (PowerManager) getContext().getSystemService(Context.POWER_SERVICE);
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + packageName));
                startActivity(intent);
            }
        }
    }

    public void checkCurrentFollowUpStatus() {
        if (UserManager.instance().isUserLoggedIn()) {
            if (Utils.isOnline()) {
                showLoading();
                Timber.d("".concat(BaseActivity.TAG).concat("Login aObjectId: ").concat(ParseUser.getCurrentUser().getObjectId()));
                Map<String, String> parameters = new HashMap<>();
                parameters.put(ParseConstants.USER_ID_PARAM_KEY, ParseUser.getCurrentUser().getObjectId());
                ParseCloud.callFunctionInBackground("checkCurrentFollow", parameters, new FunctionCallback<Object>() {
                    @Override
                    public void done(Object checkIn, ParseException e) {
                        hideLoading();
                        if (e == null && !checkIn.toString().equals("")) {
                            // {loginUserCreateFollow=1, followObjectId=oABIgOsHQn}
                            // loginUserCreateFollow equal 1 for multiple users
                            // loginUserCreateFollow equal 0 for single user
                            Timber.tag(BaseActivity.TAG).d("".concat("CheckCurrentFollow Response: ").concat(checkIn.toString()));
                            try {
                                JSONObject object = new JSONObject(checkIn.toString());
                                if (object.has("followObjectId")) {
                                    if (object.getString("followObjectId") != null && !object.getString("followObjectId").equals("")) {
                                        Timber.tag(BaseActivity.TAG).d("".concat("API followObjectId: ").concat(object.getString("followObjectId")));
                                        PreferencesManager.instance(getContext()).setFollowMeaObjectId(object.getString("followObjectId"));
                                        if (object.has("loginUserCreateFollow")) {
                                            if (object.getString("loginUserCreateFollow") != null
                                                    && !object.getString("loginUserCreateFollow").equals("")) {
                                                if (object.getString("loginUserCreateFollow").equals("1")) {
                                                    // Follow Me Location Update
                                                    getFollowMeUser();
                                                } else {
                                                    h.postDelayed(runnable = new Runnable() {
                                                        public void run() {
                                                            // Get Single User Location
                                                            getSingleUserLocation();
                                                            h.postDelayed(runnable, delay);
                                                        }
                                                    }, delay);
                                                }
                                            }
                                        }
                                    } else {
                                        PreferencesManager.instance(getContext()).setFollowMeaObjectId("");
                                        if (isGooglePlayServicesAvailable()) {
                                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mActivity);
                                            startCurrentLocationUpdates();
                                        }
                                    }
                                }
                            } catch (JSONException ee) {
                                ee.printStackTrace();
                                PreferencesManager.instance(getContext()).setFollowMeaObjectId("");
                                removeLocationUpdates();
//                                googleMap.clear();
                            }
                        } else {
                            Timber.tag(BaseActivity.TAG).d("".concat("Error: " + e.toString()));
                            PreferencesManager.instance(getContext()).setFollowMeaObjectId("");
                            removeLocationUpdates();
//                            googleMap.clear();
                        }
                    }
                });
            } else {
                Toast.makeText(mActivity, getString(R.string.alert_screen_dialog_message_error_network), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Get Follow Me User
    public void getFollowMeUser() {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.USER_ID_PARAM_KEY, ParseUser.getCurrentUser().getObjectId());
        parameters.put("aObjectId", PreferencesManager.instance(getContext()).getFollowMeaObjectId());
        parameters.put("checkInGeoPoint", "");
        parameters.put("checkInAddress", "");
        ParseCloud.callFunctionInBackground(ParseConstants.GET_FOLLOw_ME_USERS, parameters, new FunctionCallback<List<UserModel>>() {
            @Override
            public void done(List<UserModel> mUserArr, ParseException e) {
                if (mUserArr != null && !mUserArr.isEmpty()) {
                    Timber.tag(BaseActivity.TAG).d("".concat("Follow Me mUserArr Size: ").concat(String.valueOf(mUserArr.size())));
                    FollowMeUserAdapter mFollowMeUserAdapter = new FollowMeUserAdapter(getContext(), mUserArr);
                    rvFollowMeUser.setAdapter(mFollowMeUserAdapter);
                    layoutFollowMeUser.setVisibility(View.VISIBLE);
                    mFollowMeUserAdapter.notifyDataSetChanged();
                    requestLocationUpdates();
                } else {
                    layoutFollowMeUser.setVisibility(View.GONE);
                }
            }
        });
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale = ActivityCompat.shouldShowRequestPermissionRationale(mActivity, Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Timber.tag(BaseActivity.TAG).d("Displaying permission rationale to provide additional context.");
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
        } else {
            Timber.d("Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onPause() {
        h.removeCallbacks(runnable);
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isGooglePlayServicesAvailable()) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mActivity);
            startCurrentLocationUpdates();
        }

        if (notificationEvent != null) {
            handleNewNotificationEvent(notificationEvent);
            notificationEvent = null;
        }

        if (mActivity != null)
            Utility.hideKeyboard(mActivity);

        hideLoading();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient = null;
        googleMap = null;
    }

    @Override
    public int getTitleResId() {
        return R.string.txt_menu_home;
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txtFollowMe:
                if (currentLocation != null) {
                    AppIntents.intentFollowMe(mActivity, currentLocation.getLatitude(), currentLocation.getLongitude());
                } else {
                    Toast.makeText(mActivity, "Current location is empty", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.txtSOS:
                // Alarm open
                parentDrawerCallback.onNavigationDrawerItemSelected(NavigationMenu.ALARM);
//                AppIntents.intentAlarmActivity(mActivity);
                break;

            case R.id.txtIMHere:
                // Check in open
//                parentDrawerCallback.onNavigationDrawerItemSelected(NavigationMenu.CHECK_IN);
                if (currentLocation != null) {
                    AppIntents.intentIAmHere(mActivity, currentLocation.getLatitude(), currentLocation.getLongitude());
                } else {
                    Toast.makeText(mActivity, "Current location is empty", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.imgBtnCurrentLocation:
                onCurrentLocation(true);
                break;

            case R.id.txtFollowMeUserStop:
                onStopFollowMeUser();
                break;

            case R.id.txtSingleUserStop:
                if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getObjectId() != null) {
                    onStopSingleUserRequest();
                } else {
                    Toast.makeText(getContext(), "Login user not found", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                break;
        }
    }

    private void onCurrentLocation(boolean isShowMsg) {
        if (googleMap != null && currentLocation != null)
            animateCamera(currentLocation);
        else {
            if (isShowMsg)
                Toast.makeText(mActivity, "Location is not available", Toast.LENGTH_SHORT).show();
        }
    }

    public void onStopFollowMeUser() {
        if (Utils.isOnline()) {
            if (ParseUser.getCurrentUser() != null && ParseUser.getCurrentUser().getObjectId() != null) {
                showLoading();
                CheckInManager.instance().onFollowMeStop(ParseUser.getCurrentUser().getObjectId()
                        , PreferencesManager.instance(getContext()).getFollowMeaObjectId(), new onFollowMeStopListener());
            } else {
                Toast.makeText(getContext(), "Login user not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mActivity, getString(R.string.general_nointernetconnection), Toast.LENGTH_SHORT).show();
        }
    }

    private final LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult.getLastLocation() == null)
                return;
            currentLocation = locationResult.getLastLocation();
            if (firstTimeFlag && googleMap != null) {
                animateCamera(currentLocation);
                firstTimeFlag = false;
            }
            showMarker(currentLocation);
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    private void startCurrentLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mActivity
                    , Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_ACCESS_FINE_LOCATION);
                return;
            }
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(mActivity);
        if (ConnectionResult.SUCCESS == status)
            return true;
        else {
            if (googleApiAvailability.isUserResolvableError(status))
                Toast.makeText(mActivity, "Please Install google play services to use this application", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                Toast.makeText(mActivity, "Permission denied by uses", Toast.LENGTH_SHORT).show();
            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCurrentLocationUpdates();
            }
        }

//        if (requestCode == REQUEST_NOTIFICATION) {
//            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                Toast.makeText(mActivity, "Permission denied by user", Toast.LENGTH_SHORT).show();
//                System.out.println("Notification Permission Not Granted");
//            }
//            else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                System.out.println("Notification Permission Granted");
//            }
//        }
    }


    private void animateCamera(@NonNull Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(getCameraPositionWithBearing(latLng)));
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        googleMap.animateCamera(CameraUpdateFactory.zoomTo(11));
    }

    @NonNull
    private CameraPosition getCameraPositionWithBearing(LatLng latLng) {
        return new CameraPosition.Builder().target(latLng).zoom(16).build();
    }

    private void showMarker(@NonNull Location currentLocation) {
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        if (mCurrentLocationMarker == null) {
            mCurrentLocationMarker = googleMap.addMarker(new MarkerOptions()
                    .title("Current Location")
                    .icon(BitmapDescriptorFactory.defaultMarker())
                    .position(latLng));
//            mCurrentLocationMarker.setVisible(true);
        } else {
//            mCurrentLocationMarker.setVisible(true);
            animateCamera(currentLocation);
            MarkerAnimation.animateMarkerToGB(mCurrentLocationMarker, latLng, new LatLngInterpolator.Spherical());
        }
    }

    /**
     * Retrieves connections from server
     */
    private void onRefreshMyConnections(boolean showLoading) {
        if (showLoading) {
            // Shows loading
//            progressBar.setVisibility(View.VISIBLE);
        }

        // Retrieve data call
        if (UserManager.instance().isUserLoggedIn()) {
            UserManager.instance().getGuardiansForUser(new OnConnectionsReceiveCallback(this, true));
        }
    }

    private static class OnConnectionsReceiveCallback implements UserManager.MyConnectionsCallback {
        private WeakReference<HomeFragment> weakReference;
        private boolean isMyGuardians = true;

        OnConnectionsReceiveCallback(HomeFragment fragment, boolean isMyGuardians) {
            weakReference = new WeakReference<>(fragment);
            this.isMyGuardians = isMyGuardians;
        }

        @Override
        public void onConnectionsReceived(List<UserModel> connections) {
            HomeFragment fragment = weakReference.get();
            if (fragment != null && fragment.isAdded()) {
                if (isMyGuardians) {
                    fragment.myGuardiansCache = connections;
                } else {
                    fragment.imGuardingCache = connections;
                }
                if (isMyGuardians) {
                    UserManager.instance().getGuardedUsers(new OnConnectionsReceiveCallback(fragment, false));
                } else {
                    reloadData();
                }
            }
        }

        @Override
        public void onFailed(Error error) {
            reloadData();
        }

        private void reloadData() {
            HomeFragment fragment = weakReference.get();
//            if (fragment != null && fragment.isAdded()) {
//                if (fragment.myGuardiansTab.isSelected()) {
//                    fragment.onMyGuardians();
//                } else if (fragment.guardianTab.isSelected()) {
//                    fragment.onImGuarding();
//                }
//            }
        }
    }

    // Follow Me User Adapter
    public class FollowMeUserAdapter extends RecyclerView.Adapter<FollowMeUserAdapter.ViewHolder> {

        private List<UserModel> mFollowMeUserAdpArr;
        private LayoutInflater mInflater;

        FollowMeUserAdapter(Context context, List<UserModel> mFollowMeUserAdpArr) {
            this.mInflater = LayoutInflater.from(context);
            this.mFollowMeUserAdpArr = mFollowMeUserAdpArr;
        }

        @NotNull
        @Override
        public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            View view = mInflater.inflate(R.layout.layout_follow_me_user_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NotNull ViewHolder holder, int position) {
            final UserModel model = mFollowMeUserAdpArr.get(position);
            ParseFile photoFile = model.getUserImage();
            String photoUrl = photoFile != null ? photoFile.getUrl() : null;
            Picasso.get()
                    .load(photoUrl)
                    .placeholder(R.drawable.generic_icon)
                    .noFade()
                    .error(R.drawable.generic_icon)
                    .into(holder.imgFollowMeUserPic);
        }

        @Override
        public int getItemCount() {
            return mFollowMeUserAdpArr.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgFollowMeUserPic;

            ViewHolder(View itemView) {
                super(itemView);
                imgFollowMeUserPic = itemView.findViewById(R.id.imgFollowMeUserPic);
            }
        }

        UserModel getItem(int id) {
            return mFollowMeUserAdpArr.get(id);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Timber.tag(BaseActivity.TAG).d("GoogleApiClient connected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        final String text = "Connection suspended";
        Timber.tag(BaseActivity.TAG).d(text + ": Error code: " + i);
        Timber.tag(BaseActivity.TAG).d("Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        final String text = "Exception while connecting to Google Play services";
        Timber.tag(BaseActivity.TAG).d(text + ": " + connectionResult.getErrorMessage());
    }

    private void buildGoogleApiClient() {
        if (mGoogleApiClient != null) {
            return;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    private void createLocationRequest() {
        mGoogleApiClient.connect();
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        mLocationRequest.setMaxWaitTime(MAX_WAIT_TIME);
    }

    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(mActivity, LocationUpdatesBroadcastReceiver.class);
        intent.setAction(LocationUpdatesBroadcastReceiver.ACTION_PROCESS_UPDATES);
        return PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /**
     * Handles the Request Updates button and requests start of location updates.
     */
    public void requestLocationUpdates() {
        try {
            layoutFollowMeUser.setVisibility(View.VISIBLE);
            if (mGoogleApiClient.isConnected()) {
                Timber.tag(BaseActivity.TAG).d("Starting location updates");
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, getPendingIntent());
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    /*
     * Handles the Remove Updates button, and requests removal of location updates.
     */
    public void removeLocationUpdates() {
        Timber.tag(BaseActivity.TAG).d("Removing location updates");
        if (mGoogleApiClient != null)
            if (mGoogleApiClient.isConnected())
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, getPendingIntent());
    }


    // Follow Me Stop Request
    private class onFollowMeStopListener implements OnResponseCallback {

        @Override
        public void onSuccess(ParseObject object) {
            hideLoading();
            layoutFollowMeUser.setVisibility(View.GONE);
            PreferencesManager.instance(getContext()).setFollowMeaObjectId("");
            if (mFollowMeMarker != null)
                mFollowMeMarker.setVisible(false);
            removeLocationUpdates();
        }

        @Override
        public void onFailed(Error error) {
            hideLoading();
        }
    }

    // Follow Me Single User
    private void getSingleUserLocation() {
        showLoading();
        Timber.tag(BaseActivity.TAG).d("Single User Location api call");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ParseConstants.USER_ID_PARAM_KEY, ParseUser.getCurrentUser().getObjectId());
        parameters.put("aObjectId", PreferencesManager.instance(getContext()).getFollowMeaObjectId());
        ParseCloud.callFunctionInBackground("getFollowUserLocation", parameters, new FunctionCallback<HashMap<String, Object>>() {
            @Override
            public void done(HashMap<String, Object> mResponse, ParseException e) {
                hideLoading();
                if (e == null) {
                    UserModel userModel = (UserModel) mResponse.get("user");
                    ParseObject follow = (ParseObject) mResponse.get("follow");
                    String mLocName = follow.getString("locationName");
                    ParseGeoPoint mLocation = follow.getParseGeoPoint("location");

                    ParseFile photoFile = userModel.getUserImage();
                    String photoUrl = photoFile != null ? photoFile.getUrl() : "";
                    if (photoUrl != null && !photoUrl.equals(""))
                        Picasso.get()
                                .load(photoUrl)
                                .placeholder(R.drawable.generic_icon)
                                .noFade()
                                .error(R.drawable.generic_icon)
                                .into(imgFollowMeUserPic);

                    txtFollowMeUserName.setText("".concat(userModel.getName()));

                    if (mLocName != null && !mLocName.equals(""))
                        txtFollowMeAddress.setText("".concat(mLocName));
                    else txtFollowMeAddress.setText("");

                    layoutSingleUser.setVisibility(View.VISIBLE);

                    if (mLocation != null) {
                        // New added
                        // Remove Current Location Update
                        if (mCurrentLocationMarker != null)
                            mCurrentLocationMarker.setVisible(false);
                        if (fusedLocationProviderClient != null)
                            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
                        removeLocationUpdates();

                        Timber.tag(BaseActivity.TAG).d("".concat(userModel.getName()).concat(" Latitude: ").concat(String.valueOf(mLocation.getLatitude())));
                        Timber.tag(BaseActivity.TAG).d("".concat(userModel.getName()).concat(" Longitude: ").concat(String.valueOf(mLocation.getLongitude())));
                        LatLng mLatLng = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                        if (mFollowMeMarker == null) {
                            mFollowMeMarker = googleMap.addMarker(new MarkerOptions()
                                    .title(userModel.getName())
                                    .icon(BitmapDescriptorFactory.defaultMarker())
                                    .position(mLatLng));
//                            mFollowMeMarker.setVisible(true);
//                            animateCamera(mLatLng);
                            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                    getCameraPositionWithBearing(mLatLng)));
                        } else {
                            if (googleMap != null) {
//                                animateMarker(mFollowMeMarker, mLatLng, true);
                                mFollowMeMarker.setPosition(mLatLng);
                                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                                        getCameraPositionWithBearing(mLatLng)));
                            }
//                            MarkerAnimation.animateMarkerToGB(mFollowMeMarker, mLatLng, new LatLngInterpolator.Spherical());
                        }
                    } else {
                        Toast.makeText(mActivity, "".concat(userModel.getName()).concat("current location not found"), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Timber.tag(BaseActivity.TAG).d("".concat("Error: " + e.toString()));
                    layoutSingleUser.setVisibility(View.GONE);
                }
            }
        });
    }

    // Follow Me Single User Stop Request
    public void onStopSingleUserRequest() {
        if (Utils.isOnline()) {
            showLoading();
            Timber.tag(BaseActivity.TAG).d("Single User Stop Request api call");
            Map<String, String> parameters = new HashMap<>();
            parameters.put(ParseConstants.USER_ID_PARAM_KEY, ParseUser.getCurrentUser().getObjectId());
            parameters.put("aObjectId", PreferencesManager.instance(getContext()).getFollowMeaObjectId());
            ParseCloud.callFunctionInBackground("stopFollowUser", parameters, new FunctionCallback<Object>() {
                @Override
                public void done(Object mResponse, ParseException e) {
                    hideLoading();
                    if (e == null) {
                        Timber.tag(BaseActivity.TAG).d("".concat("Stop Single User Request Response: ").concat(mResponse.toString()));
                        layoutSingleUser.setVisibility(View.GONE);
                        PreferencesManager.instance(getContext()).setFollowMeaObjectId("");
                        if (mFollowMeMarker != null)
                            mFollowMeMarker.setVisible(false);
                        h.removeCallbacks(runnable);
                        firstTimeFlag = true;
                        startCurrentLocationUpdates();
                    } else {
                        Timber.tag(BaseActivity.TAG).d("".concat("Error: " + e.toString()));
                    }
                }
            });
        } else {
            Toast.makeText(mActivity, R.string.alert_screen_dialog_message_error_network, Toast.LENGTH_SHORT).show();
        }
    }

    public void animateMarker(final Marker marker, final LatLng toPosition, final boolean hideShowMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = googleMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 500;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    marker.setVisible(hideShowMarker);
                }
            }
        });
    }

    // Show Dialog Terms Condition Accepted
    public void showDialogTermsConditionAccept() {
        if (getContext() != null) {
            final Dialog dialog = new Dialog(getContext());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_accept_terms_and_condition_layout);

            final WebView wvLoadURL = dialog.findViewById(R.id.wvLoadURL);
            wvLoadURL.getSettings().setJavaScriptEnabled(true);
            wvLoadURL.loadUrl("https://safelet.com/terms-of-service/");
            wvLoadURL.setWebViewClient(new WebViewClient() {
                @SuppressWarnings("deprecation")
                @Override
                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                    Toast.makeText(getContext(), description, Toast.LENGTH_SHORT).show();
                }

                @TargetApi(android.os.Build.VERSION_CODES.M)
                @Override
                public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                    // Redirect to deprecated method, so you can use it in all SDK versions
                    onReceivedError(view, rerr.getErrorCode(), rerr.getDescription().toString(), req.getUrl().toString());
                }
            });

            AppCompatButton btnAccept = dialog.findViewById(R.id.btnAccept);
            btnAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    showLoading();
                    UserManager.instance().changeTermsConditionAccept(true
                            , new changeTermsConditionAccept(new HomeFragment()));

//                    ParseObject object = new UserModel();
//                    object.put(UserModel.IS_TERMS_CONDITION_ACCEPTED, true);
//                    object.saveInBackground();
                }
            });
            dialog.show();
        }
    }

    private static class changeTermsConditionAccept implements OnResponseCallback {
        private WeakReference<HomeFragment> weakReference;

        changeTermsConditionAccept(HomeFragment fragment) {
            weakReference = new WeakReference<>(fragment);
        }

        @Override
        public void onSuccess(ParseObject object) {
            final HomeFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.hideLoading();
        }

        @Override
        public void onFailed(Error error) {
            HomeFragment fragment = weakReference.get();
            if (fragment == null || !fragment.isAdded()) {
                return;
            }
            fragment.hideLoading();
        }
    }
}
