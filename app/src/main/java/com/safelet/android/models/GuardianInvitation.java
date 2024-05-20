package com.safelet.android.models;

import android.text.TextUtils;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.safelet.android.models.enums.UserRelationStatus;

import java.util.Locale;

@ParseClassName("GuardianInvitation")
public class GuardianInvitation extends ParseObject {

    private static final String STATUS_KEY = "status";
    private static final String FROM_USER_KEY = "fromUser";
    private static final String TO_USER_KEY = "toUser";

    public GuardianInvitation() {
    }

    public UserRelationStatus getRelationStatus() {
        String status = getString(STATUS_KEY);
        if (TextUtils.isEmpty(status)) {
            return UserRelationStatus.NONE;
        }
        return UserRelationStatus.valueOf(status.toUpperCase(Locale.ENGLISH));
    }

    public void setRelationStatus(UserRelationStatus status) {
        put(STATUS_KEY, status.toString());
    }

    public UserModel getFromUser() {
        return (UserModel) getParseUser(FROM_USER_KEY);
    }

    public void setFromUser(UserModel user) {
        put(FROM_USER_KEY, user);
    }

    public UserModel getToUser() {
        return (UserModel) getParseUser(TO_USER_KEY);
    }

    public void setToUser(UserModel user) {
        put(TO_USER_KEY, user);
    }
}
