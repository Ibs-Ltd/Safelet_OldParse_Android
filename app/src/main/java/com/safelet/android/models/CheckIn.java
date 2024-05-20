package com.safelet.android.models;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

@ParseClassName("CheckIn")
public class CheckIn extends ParseObject {
    private static final String LOCATION_KEY = "location";
    private static final String LOCATION_NAME_KEY = "locationName";
    private static final String MESSAGE_KEY = "message";
    private static final String USER_KEY = "user";

    public CheckIn() {
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(LOCATION_KEY);
    }

    public void setLocation(ParseGeoPoint location) {
        put(LOCATION_KEY, location);
    }

    public String getLocationName() {
        return getString(LOCATION_NAME_KEY);
    }

    public void setLocationName(String locationName) {
        put(LOCATION_NAME_KEY, locationName);
    }

    public String getCheckInMessage() {
        return getString(MESSAGE_KEY);
    }

    public void setCheckInMessage(String message) {
        put(MESSAGE_KEY, message);
    }

    public UserModel getUser() {
        return (UserModel) getParseUser(USER_KEY);
    }

    public void setUser(UserModel user) {
        put(USER_KEY, user);
    }

    public String getUserName() {
        return getUser().getName();
    }

    public String getUserImageUrl() {
        return getUser().getImageUrl();
    }
}
