package com.safelet.android.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;

import com.google.android.gms.location.LocationResult;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.global.PreferencesManager;
import com.safelet.android.global.Utils;
import com.safelet.android.interactors.CheckInManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.utils.Error;

import java.util.List;

import timber.log.Timber;

public class LocationUpdatesBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_PROCESS_UPDATES = "com.safelet.android.action.PROCESS_UPDATES";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_UPDATES.equals(action)) {
                LocationResult result = LocationResult.extractResult(intent);
                if (result != null) {
                    List<Location> mLocations = result.getLocations();
                    if (!mLocations.isEmpty()) {
                        for (Location location : mLocations) {
                            String mFollowMeaObjectId = PreferencesManager.instance(context).getFollowMeaObjectId();
                            if (mFollowMeaObjectId != null && !mFollowMeaObjectId.equals("")
                                    && ParseUser.getCurrentUser().getObjectId() != null && !ParseUser.getCurrentUser().getObjectId().equals("")) {
                                Timber.d("".concat(BaseActivity.TAG).concat(" FollowMe Location Update Api Call"));
                                Timber.tag(BaseActivity.TAG).d("".concat("App followObjectId: ").concat(mFollowMeaObjectId));
                                Timber.tag(BaseActivity.TAG).d("".concat("Latitude: ").concat(String.valueOf(location.getLatitude())));
                                Timber.tag(BaseActivity.TAG).d("".concat("Longitude: ").concat(String.valueOf(location.getLongitude())));
                                CheckInManager.instance().onFollowMeUpdateLocation(mFollowMeaObjectId,
                                        ParseUser.getCurrentUser().getObjectId(),
                                        new ParseGeoPoint(location.getLatitude(), location.getLongitude()),
                                        Utils.getAddressLatLon(context, location.getLatitude(), location.getLongitude()),
                                        new onFollowMeListener());
                            }
                        }
                    }
                }
            }
        }
    }

    // Follow Me Stop
    private class onFollowMeListener implements OnResponseCallback {

        @Override
        public void onSuccess(ParseObject object) {
//            Timber.tag(BaseActivity.TAG).d("".concat("FollowMe Location Response: ").concat(object.toString()));
//            Toast.makeText(getContext(), "Done", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed(Error error) {
//            Toast.makeText(getContext(), error.getErrorMessage(getContext()), Toast.LENGTH_SHORT).show();
        }
    }

}
