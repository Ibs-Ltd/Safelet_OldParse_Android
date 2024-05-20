package com.safelet.android.models;

import android.net.Uri;

public class ContactModel {

    private String name;

    private String phoneNumber;

    private Uri contactPhotoUri;

    public ContactModel(String name, String phoneNumber, Uri contactPhotoUri) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.contactPhotoUri = contactPhotoUri;
    }

    public String getName() {
        return name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Uri getPhotoUri() {
        return contactPhotoUri;
    }
}
