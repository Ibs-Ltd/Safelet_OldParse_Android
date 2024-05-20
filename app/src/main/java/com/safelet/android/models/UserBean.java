package com.safelet.android.models;

import android.os.Parcel;
import android.os.Parcelable;

public class UserBean implements Parcelable {

    public String mUserName;
    public String mUserId;
    public String mPhotoURL;
    public boolean isSelected;

    public UserBean(String userName, String userId, String userPhoto, boolean isSelected) {
        this.mUserName = userName;
        this.mUserId = userId;
        this.mPhotoURL = userPhoto;
        this.isSelected = isSelected;
    }

    private UserBean(Parcel in) {
        mUserName = in.readString();
        mUserId = in.readString();
        mPhotoURL = in.readString();
        isSelected = in.readByte() != 0;
    }

    public static final Creator<UserBean> CREATOR = new Creator<UserBean>() {
        @Override
        public UserBean createFromParcel(Parcel in) {
            return new UserBean(in);
        }

        @Override
        public UserBean[] newArray(int size) {
            return new UserBean[size];
        }
    };

    public UserBean() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUserName);
        dest.writeString(mUserId);
        dest.writeString(mPhotoURL);
        dest.writeByte((byte) (isSelected ? 1 : 0));
    }
}
