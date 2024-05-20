package com.safelet.android.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;

@ParseClassName("NonUserInvitation")
public class NonUserInvitation extends ParseObject {
    private static final String FROM_USER_KEY = "fromUser";
    private static final String PHONE_NUMBER_KEY = "toPhoneNumber";

    public NonUserInvitation() {
    }

    public UserModel getFromUser() {
        return (UserModel) getParseUser(FROM_USER_KEY);
    }

    public String getToPhoneNumber() {
        return getString(PHONE_NUMBER_KEY);
    }
}
