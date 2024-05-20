package com.safelet.android.services;

import android.app.IntentService;
import android.content.Intent;

public class LocationUpdatesIntentService extends IntentService {

//    static final String ACTION_PROCESS_UPDATES = "com.safelet.android.action.PROCESS_UPDATES";

    public LocationUpdatesIntentService() {
        super("FollowMeLocationUpdate");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

    }

//    @Override
//    protected void onHandleIntent(Intent intent) {
//        if (intent != null) {
//            final String action = intent.getAction();
//            if (ACTION_PROCESS_UPDATES.equals(action)) {
//                LocationResult result = LocationResult.extractResult(intent);
//                if (result != null) {
//                    List<Location> locations = result.getLocations();
//                    LocationResultHelper locationResultHelper = new LocationResultHelper(this, locations);
    // Save the location data to SharedPreferences.
//                    locationResultHelper.saveResults();
    // Show notification with the location data.
//                    locationResultHelper.showNotification();
//                    Timber.tag(BaseActivity.TAG).d(LocationResultHelper.getSavedLocationResult(this));
//                }
//            }
//        }
//    }
}
