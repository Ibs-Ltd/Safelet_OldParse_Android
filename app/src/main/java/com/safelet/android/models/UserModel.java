package com.safelet.android.models;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.safelet.android.interactors.PhoneContactsManager;
import com.safelet.android.models.enums.SmsInviteStatus;
import com.safelet.android.models.enums.UserRelationStatus;

@ParseClassName("_User")
public class UserModel extends ParseUser implements Comparable<UserModel> {

    public static final String USER_NAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String EMAIL_VERIFIED_KEY = "emailVerified";
    private static final String EMAIL_KEY = "email";
    private static final String IS_COMMUNITY_MEMBER_KEY = "isCommunityMember";
    private static final String LOCATION_KEY = "locationCoordinates";
    private static final String LOCATION_NAME_KEY = "locationName";
    private static final String NAME_KEY = "name";
    private static final String COUNTRY_CODE_KEY = "phoneCountryCode";
    private static final String PHONE_NUMBER_KEY = "phoneNumber";
    private static final String TW_UID_KEY = "twitterId";
    private static final String USER_IMAGE_KEY = "userImage";
    private static final String UNKNOWN_LOCATION_NAME = "Unknown address";
    private static final String USER_HAS_BRACELET = "hasBracelet";
    private static final String IS_NEW_API = "isNewAPI";
    public static final String IS_TERMS_CONDITION_ACCEPTED = "isTermsConditionAccepted";
    private static boolean isSelected = false;
    private static boolean isFollowMeSelected = false;
    private Bitmap userImageBitmap = null;

    // used to keep the relation between this user and current logged user
    private UserRelationStatus userRelation = UserRelationStatus.NONE;

    private SmsInviteStatus smsInviteStatus = SmsInviteStatus.NONE;

    public UserRelationStatus getUserRelationStatus() {
        return userRelation;
    }

    public void setUserRelationStatus(UserRelationStatus userRelation) {
        this.userRelation = userRelation;
    }

    public SmsInviteStatus getSmsInviteStatus() {
        return smsInviteStatus;
    }

    public void setSmsInviteStatus(SmsInviteStatus smsInviteStatus) {
        this.smsInviteStatus = smsInviteStatus;
    }

    public boolean isMember() {
        return getObjectId() != null && !getObjectId().isEmpty();
    }

    public UserModel() {
    }

    public boolean isEmailVerified() {
        return getBoolean(EMAIL_VERIFIED_KEY);
    }

    public String getEmail() {
        return getString(EMAIL_KEY);
    }

    public void setEmail(String email) {
        if (email != null) {
            put(EMAIL_KEY, email);
        }
    }

    public boolean isCommunityMember() {
        return getBoolean(IS_COMMUNITY_MEMBER_KEY);
    }

    public void setCommunityMember(boolean communityMember) {
        put(IS_COMMUNITY_MEMBER_KEY, communityMember);
    }

    public String getTwUid() {
        return getString(TW_UID_KEY);
    }

    public void setTwUid(String twUid) {
        put(TW_UID_KEY, twUid);
    }

    public String getPhoneNumber() {
        return getString(PHONE_NUMBER_KEY);
    }

    public void setPhoneNumber(String phoneNumber) {
        put(PHONE_NUMBER_KEY, phoneNumber);
    }

    public String getName() {
        ContactModel contactModel = PhoneContactsManager.instance().getContact(getPhoneNumber());
        if (contactModel != null) {
            return contactModel.getName();
        }
        return getOriginalName();
    }

    public String getOriginalName() {
        return getString(NAME_KEY);
    }

    public void setName(String name) {
        put(NAME_KEY, name);
    }

    public String getPassword() {
        return getString(PASSWORD_KEY);
    }

    public String getCountryPrefixCode() {
        return getString(COUNTRY_CODE_KEY);
    }

    public void setCountryPrefixCode(String countryCode) {
        if (countryCode != null) {
            put(COUNTRY_CODE_KEY, countryCode);
        }
    }

    public String getImageUrl() {
        ParseFile image = getUserImage();
        if (image == null) {
            return "";
        }
        return image.getUrl();
    }

    public ParseFile getUserImage() {
        return getParseFile(USER_IMAGE_KEY);
    }

    public void setUserImage(ParseFile imageFile) {
        if (imageFile != null) {
            put(USER_IMAGE_KEY, imageFile);
        }
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(LOCATION_KEY);
    }

    public void setLocation(ParseGeoPoint location) {
        if (location != null) {
            put(LOCATION_KEY, location);
        } else {
            remove(LOCATION_KEY);
        }
    }

    public String getLocationName() {
        return getString(LOCATION_NAME_KEY);
    }

    public void setLocationName(String address) {
        put(LOCATION_NAME_KEY, address == null ? UNKNOWN_LOCATION_NAME : address);
    }

    public void removeLastLocationName() {
        remove(LOCATION_NAME_KEY);
    }

    public Bitmap getUserImageBitmap() {
        return userImageBitmap;
    }

    public void setUserImageBitmap(Bitmap userImageBitmap) {
        this.userImageBitmap = userImageBitmap;
    }

    public boolean hasBracelet() {
        return getBoolean(USER_HAS_BRACELET);
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setFollowMeSelected(boolean isFollowMeSelected) {
        this.isFollowMeSelected = isFollowMeSelected;
    }

    public boolean isFollowMeSelected() {
        return isFollowMeSelected;
    }

    public boolean isTermsConditionAccepted() {
        return getBoolean(IS_TERMS_CONDITION_ACCEPTED);
    }

    public void setTermsConditionAccepted(boolean isTermsConditionAccepted) {
        put(IS_TERMS_CONDITION_ACCEPTED, isTermsConditionAccepted);
    }

    public void setHasBracelet() {
        put(USER_HAS_BRACELET, true);
        saveInBackground();
    }

    public void setIsNewApi() {
        put(IS_NEW_API, true);
    }

    @Override
    public int compareTo(@NonNull UserModel another) {
        return this.getName().compareToIgnoreCase(another.getName());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (getObjectId() == null) {
            return false;
        }
        if (((UserModel) o).getObjectId() == null) {
            return false;
        }
        return getObjectId().equals(((UserModel) o).getObjectId());
    }
}
