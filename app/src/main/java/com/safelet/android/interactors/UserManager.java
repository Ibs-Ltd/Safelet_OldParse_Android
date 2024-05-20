package com.safelet.android.interactors;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.parse.DeleteCallback;
import com.parse.FunctionCallback;
import com.parse.GetDataCallback;
import com.parse.LogInCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;
import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.fragments.BaseFragment;
import com.safelet.android.global.PreferencesManager;
import com.safelet.android.interactors.base.BaseManager;
import com.safelet.android.interactors.callbacks.OnFirebaseCallback;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.interactors.callbacks.RetrievePictureCallback;
import com.safelet.android.interactors.callbacks.base.BaseResponseCallback;
import com.safelet.android.interactors.utils.ParseConstants;
import com.safelet.android.models.Alarm;
import com.safelet.android.models.ContactModel;
import com.safelet.android.models.UserModel;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.enums.SmsInviteStatus;
import com.safelet.android.models.enums.UserRelationStatus;
import com.safelet.android.models.event.ContactsLoadedEvent;
import com.safelet.android.models.event.MatchedUsers;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.models.event.UpdateServiceNotificationEvent;
import com.safelet.android.utils.Error;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

public class UserManager extends BaseManager {

    private static final int USER_EXIST_RESPONSE = 1;
    private static final String IM_GUARDING_USERS_COUNT_KEY = "um.imGuardingUsersCount.key";
    private static final String MY_GUARDIANS_USERS_COUNT_KEY = "um.myGuardiansUsersCount.key";
    private static final String IM_GUARDING_USERS_NAME_KEY = "um.imGuardingUsersName.key";
    private static final String MY_GUARDIANS_USERS_NAME_KEY = "um.myGuardiansUsersName.key";

    private static UserManager sInstance;

    private UserModel userModel;
    private List<UserModel> myGuardiansCache = null;
    private List<UserModel> imGuardingCache = null;

    public static UserManager instance() {
        if (sInstance == null) {
            sInstance = new UserManager();
        }
        return sInstance;
    }

    private UserManager() {
        super();
        EventBusManager.instance().register(this);
        userModel = (UserModel) UserModel.getCurrentUser();
    }

    public UserModel getUserModel() {
        return userModel;
    }

    public String getUserName() {
        return userModel.getOriginalName();
    }

    public String getUserEmail() {
        return userModel.getEmail();
    }

    public String getPhoneNumber() {
        return userModel.getPhoneNumber();
    }

    public String getUserId() {
        return userModel.getObjectId();
    }

    public List<UserModel> getImGuardingCache() {
        return imGuardingCache;
    }

    public int getImGuardingUsersCount(Context context) {
        if (imGuardingCache != null) {
            int size = imGuardingCache.size();
            PreferencesManager.instance(context).setInt(IM_GUARDING_USERS_COUNT_KEY, size);
            return size;
        }
        return PreferencesManager.instance(context).getInt(IM_GUARDING_USERS_COUNT_KEY, 0);
    }

    public String getImGuardingUsersName(Context context) {
        if (imGuardingCache != null && imGuardingCache.size() > 0) {
            String name = imGuardingCache.get(0).getName();
            PreferencesManager.instance(context).setString(IM_GUARDING_USERS_NAME_KEY, name);
            return name;
        }
        return PreferencesManager.instance(context).getString(IM_GUARDING_USERS_NAME_KEY, "");
    }

    public List<UserModel> getMyGuardiansCache() {
        return myGuardiansCache;
    }

    public int getMyGuardiansUsersCount(Context context) {
        if (myGuardiansCache != null) {
            int size = myGuardiansCache.size();
            PreferencesManager.instance(context).setInt(MY_GUARDIANS_USERS_COUNT_KEY, size);
            return size;
        }
        return PreferencesManager.instance(context).getInt(MY_GUARDIANS_USERS_COUNT_KEY, 0);
    }

    public String getMyGuardiansUsersName(Context context) {
        if (myGuardiansCache != null && myGuardiansCache.size() > 0) {
            String name = myGuardiansCache.get(0).getName();
            PreferencesManager.instance(context).setString(MY_GUARDIANS_USERS_NAME_KEY, name);
            return name;
        }
        return PreferencesManager.instance(context).getString(MY_GUARDIANS_USERS_NAME_KEY, "");
    }

    public boolean isUserLoggedIn() {
        return userModel != null && userModel.getObjectId() != null;
    }

    public void logout() {
        if (imGuardingCache != null) {
            imGuardingCache.clear();
            imGuardingCache = null;
        }
        if (myGuardiansCache != null) {
            myGuardiansCache.clear();
            myGuardiansCache = null;
        }
        ParseUser.logOut();
        userModel = null;
    }

    public boolean isMyAlarm(Alarm alarm) {
        return alarm.getUser().getObjectId().equals(userModel.getObjectId());
    }

    public void createUserForSignUp(String userName, String password, boolean isTermsConditionAccepted) {
        userModel = new UserModel();
        userModel.setUsername(userName);
        userModel.setPassword(password);
        userModel.setEmail(userName);
        userModel.setTermsConditionAccepted(isTermsConditionAccepted);
    }

    public void checkIfUserExist(String emailAddress, boolean isTermsConditionAccepted, final OnResponseCallback callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(UserModel.USER_NAME_KEY, emailAddress);
        parameters.put(UserModel.IS_TERMS_CONDITION_ACCEPTED, String.valueOf(isTermsConditionAccepted));
        ParseCloud.callFunctionInBackground(ParseConstants.CHECK_IF_USER_EXIST, parameters, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer integer, ParseException e) {
                if (e != null) {
                    callback.onFailed(new Error(e));
                } else {
                    if (integer == USER_EXIST_RESPONSE) {
                        callback.onFailed(new Error(R.string.error_user_exist));
                    } else {
                        callback.onSuccess(null);
                    }
                }
            }
        });
    }

    public void loginAccountForUser(final String userName, String password,
                                    @NonNull final OnResponseCallback onResponse) {
        if (!isConnectedToInternet(onResponse)) {
            return;
        }
        ParseUser.logInInBackground(userName, password, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
//                Timber.d("".concat(BaseActivity.TAG).concat(e.getMessage()).concat(" Code : ").concat(String.valueOf(e.getCode())));
//                Timber.d("".concat(BaseActivity.TAG).concat(" parseUser : ").concat(String.valueOf(parseUser)));
                if (parseUser != null) {
                    userModel = (UserModel) parseUser;

                    FirebaseCrashlytics.getInstance().setUserId(userModel.getObjectId());
                    FirebaseCrashlytics.getInstance().setCustomKey("setUserEmail", userModel.getEmail());
                    FirebaseCrashlytics.getInstance().setCustomKey("setUserName", userModel.getOriginalName());

                    onResponse.onSuccess(parseUser);
                } else {
                    onResponse.onFailed(new Error(R.string.error_wrong_credentials));
                }
            }
        });
    }

    public void changePasswordForUser(@NonNull String currentPassword,
                                      @NonNull String newPassword,
                                      @NonNull final OnResponseCallback onResponse) {
        if (!isConnectedToInternet(onResponse)) {
            return;
        }
        checkCurrentPasswordForUser(currentPassword, newPassword, onResponse);
    }

    public void forgotPasswordForUser(String usernameEmail, @NonNull final OnResponseCallback onResponseCallback) {
        if (!isConnectedToInternet(onResponseCallback)) {
            return;
        }
        ParseUser.requestPasswordResetInBackground(usernameEmail.toLowerCase(),
                new RequestPasswordResetCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            onResponseCallback.onSuccess(null);
                        } else {
                            Error error = new Error(R.string.error_forgot_password);
                            onResponseCallback.onFailed(error);
                        }
                    }
                });
    }

    public void saveUser(final OnResponseCallback listener) {
        if (!isConnectedToInternet(listener)) {
            return;
        }
        userModel.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (listener == null) {
                    return;
                }
                if (e == null) {
                    listener.onSuccess(userModel);
                } else {
                    Error err = new Error(e);
                    listener.onFailed(err);
                }
            }
        });
    }

    public void createAccountForUser(@NonNull final OnResponseCallback listener) {
        if (!isConnectedToInternet(listener)) {
            return;
        }
        if (userModel.getUserImage() != null) {
            uploadUserImageFile(userModel.getUserImage(), new OnResponseCallback() {
                @Override
                public void onSuccess(ParseObject object) {
                    signUpNewUser(listener);
                }

                @Override
                public void onFailed(Error error) {
                    listener.onFailed(error);
                }
            });
        } else {
            signUpNewUser(listener);
        }
    }

    public void setUserCommunityMemberStatus(boolean member, @NonNull final OnResponseCallback onResponse) {
        if (!isConnectedToInternet(onResponse)) {
            return;
        }
        userModel.setCommunityMember(member);
        userModel.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    onResponse.onSuccess(userModel);
                } else {
                    Error err = new Error(e);
                    onResponse.onFailed(err);
                }
            }
        });
    }

    public void changeNameForUser(String name, @NonNull final OnResponseCallback onResponseCallback) {
        if (!isConnectedToInternet(onResponseCallback)) {
            return;
        }
        userModel.setName(name);
        userModel.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    onResponseCallback.onSuccess(userModel);
                } else {
                    Error err = new Error(e);
                    onResponseCallback.onFailed(err);
                }
            }
        });
    }

    public void changeEmailForUser(String email, @NonNull final OnResponseCallback onResponseCallback) {
        if (!isConnectedToInternet(onResponseCallback)) {
            return;
        }
        final String currentEmail = userModel.getEmail();
        userModel.setUsername(email);
        userModel.setEmail(email);
        userModel.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    onResponseCallback.onSuccess(userModel);
                } else {
                    userModel.setUsername(currentEmail);
                    userModel.setEmail(currentEmail);
                    Error err = new Error(e);
                    onResponseCallback.onFailed(err);
                }
            }
        });
    }

    public void changePhoneNumberForUser(String OldphoneNumber, String countryPrefixCode, String phone,
                                         @NonNull final OnResponseCallback onResponseCallback) {
        if (!isConnectedToInternet(onResponseCallback)) {
            return;
        }

        userModel.setCountryPrefixCode(countryPrefixCode);
        userModel.setPhoneNumber(phone);
        userModel.saveInBackground(new SaveCallback() {

            @Override
            public void done(ParseException e) {
                if (e == null) {
                    onResponseCallback.onSuccess(userModel);
                } else {
                    Error err = new Error(e);
                    userModel.setCountryPrefixCode(countryPrefixCode);
                    userModel.setPhoneNumber(OldphoneNumber);
                    onResponseCallback.onFailed(err);
                }
            }
        });
    }

    public void changeTermsConditionAccept(boolean isTermsConditionAccepted
            , @NonNull final OnResponseCallback onResponseCallback) {
        if (!isConnectedToInternet(onResponseCallback)) {
            return;
        }
        userModel.setTermsConditionAccepted(isTermsConditionAccepted);
        userModel.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                hideLoading();
                if (e == null) {
                    onResponseCallback.onSuccess(userModel);
                } else {
                    Error err = new Error(e);
                    onResponseCallback.onFailed(err);
                }
            }
        });
    }

    public void getProfilePicture(@NonNull final RetrievePictureCallback retrieveImageCallback) {
        ParseFile file = userModel.getUserImage();
        if (file != null) {
            file.getDataInBackground(new GetDataCallback() {

                @Override
                public void done(byte[] bytes, ParseException parseException) {
                    if (parseException == null) {
                        Bitmap result = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        if (result != null && userModel != null) {
                            userModel.setUserImageBitmap(result);
                            retrieveImageCallback.onRetrieveSuccess(result);
                        }
                    }
                    retrieveImageCallback.onRetrieveFailed();
                }
            });
        } else {
            retrieveImageCallback.onRetrieveFailed();
        }
    }

    public void getGuardiansForUser(final MyConnectionsCallback callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.USER_ID_PARAM_KEY, userModel.getObjectId());
        ParseCloud.callFunctionInBackground(ParseConstants.GET_GUARDIANS_FOR_USER, parameters, new FunctionCallback<List<UserModel>>() {
            @Override
            public void done(List<UserModel> userModels, ParseException e) {
                if (e != null) {
                    if (callback != null) {
                        callback.onFailed(new Error(e));
                    }
                } else {
                    myGuardiansCache = userModels;
                    if (callback != null) {
                        callback.onConnectionsReceived(userModels);
                    }
                    // update the notification from status bar
                    EventBusManager.instance().postEvent(new UpdateServiceNotificationEvent());
                }
            }
        });
    }

    public void getGuardedUsers(final MyConnectionsCallback callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.USER_ID_PARAM_KEY, userModel.getObjectId());
        ParseCloud.callFunctionInBackground(ParseConstants.GET_GUARDIAN_USER, parameters, new FunctionCallback<List<UserModel>>() {
            @Override
            public void done(List<UserModel> userModels, ParseException e) {
                if (e != null) {
                    if (callback != null) {
                        callback.onFailed(new Error(e));
                    }
                } else {
                    imGuardingCache = userModels;
                    if (callback != null) {
                        callback.onConnectionsReceived(userModels);
                    }
                    // update the notification from status bar
                    EventBusManager.instance().postEvent(new UpdateServiceNotificationEvent());
                }
            }
        });
    }

    public void removeGuardian(final UserModel guardianUser, final OnResponseCallback callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.FROM_USER_PARAM_KEY, userModel.getObjectId());
        parameters.put(ParseConstants.TO_USER_PARAM_KEY, guardianUser.getObjectId());
        parameters.put(ParseConstants.INIT_USER_ID_PARAM_KEY, userModel.getObjectId());
        ParseCloud.callFunctionInBackground(ParseConstants.REMOVE_GUARDIAN_FOR_USER, parameters, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer response, ParseException e) {
                if (e != null) {
                    callback.onFailed(new Error(e));
                } else if (response != SUCCESS_RESPONSE) {
                    callback.onFailed(new Error(R.string.error_remove_guardian));
                } else {
                    myGuardiansCache.remove(guardianUser);
                    callback.onSuccess(null); // do not return nothing
                    // update the notification from status bar
                    EventBusManager.instance().postEvent(new UpdateServiceNotificationEvent());
                }
            }
        });
    }

    public void removeImGuardian(final UserModel guardianUser, final OnResponseCallback callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.TO_USER_PARAM_KEY, userModel.getObjectId());
        parameters.put(ParseConstants.FROM_USER_PARAM_KEY, guardianUser.getObjectId());
        parameters.put(ParseConstants.INIT_USER_ID_PARAM_KEY, userModel.getObjectId());
        ParseCloud.callFunctionInBackground(ParseConstants.REMOVE_GUARDIAN_FOR_USER, parameters, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer response, ParseException e) {
                if (e != null) {
                    callback.onFailed(new Error(e));
                } else if (response != SUCCESS_RESPONSE) {
                    callback.onFailed(new Error(R.string.error_remove_guarded));
                } else {
                    imGuardingCache.remove(guardianUser);
                    callback.onSuccess(null); // do not return nothing
                    // update the notification from status bar
                    EventBusManager.instance().postEvent(new UpdateServiceNotificationEvent());
                }
            }
        });
    }

    private void signUpNewUser(@NonNull final OnResponseCallback listener) {
        userModel.setIsNewApi();
        userModel.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    listener.onSuccess(userModel);
                } else {
                    Error error = new Error(e);
                    listener.onFailed(error);
                }
            }
        });
    }

    public void uploadUserImageFile(ParseFile file, final OnResponseCallback listener) {
        if (!isConnectedToInternet(listener)) {
            return;
        }
        userModel.setUserImage(file);
        userModel.getUserImage().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    listener.onSuccess(null);
                } else {
                    listener.onFailed(new Error(e));
                }
            }
        });
    }

    private void checkCurrentPasswordForUser(String currentPassword, final String newPassword,
                                             final OnResponseCallback onResponse) {
        ParseUser.logInInBackground(userModel.getUsername(), currentPassword, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser == null) {
                    onResponse.onFailed(new Error(R.string.error_wrong_pass));
                } else {
                    userModel = (UserModel) parseUser;
                    changePasswordForUser(newPassword, onResponse);
                }
            }
        });
    }

    private void changePasswordForUser(final String newPassword, final OnResponseCallback onResponse) {
        userModel.setPassword(newPassword);
        userModel.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    loginAccountForUser(userModel.getUsername(), newPassword, onResponse);
                } else {
                    onResponse.onFailed(new Error(e));
                }
            }
        });
    }

    public void getUsersForContacts(Collection<ContactModel> contacts, final MyConnectionsCallback callback) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ParseConstants.PHONES_PARAM_KEY, getPhonesForContacts(contacts));
        ParseCloud.callFunctionInBackground(ParseConstants.GET_USERS_FOR_PHONE, parameters, new FunctionCallback<HashMap<String, Object>>() {

            @Override
            public void done(HashMap<String, Object> result, ParseException e) {
                if (e == null) {
                    MatchedUsers matchedUsers = new MatchedUsers(result);
                    List<UserModel> userModels = new ArrayList<>();
                    userModels.addAll(matchedUsers.getUninvited());
                    for (UserModel user : matchedUsers.getGuardians()) {
                        user.setUserRelationStatus(UserRelationStatus.ACCEPTED);
                        userModels.add(user);
                    }
                    for (UserModel user : matchedUsers.getInvited()) {
                        user.setUserRelationStatus(UserRelationStatus.PENDING);
                        userModels.add(user);
                    }
                    for (String phoneNumber : matchedUsers.getSmsInvited()) {
                        UserModel userModel = ParseObject.create(UserModel.class);
                        userModel.setPhoneNumber(phoneNumber);
                        userModel.setSmsInviteStatus(SmsInviteStatus.INVITED);
                        userModels.add(userModel);
                    }
                    for (String phoneNumber : matchedUsers.getUnmatched()) {
                        UserModel userModel = ParseObject.create(UserModel.class);
                        userModel.setPhoneNumber(phoneNumber);
                        userModel.setSmsInviteStatus(SmsInviteStatus.NONE);
                        userModels.add(userModel);
                    }
                    Collections.sort(userModels);
                    callback.onConnectionsReceived(userModels);
                } else {
                    callback.onFailed(new Error(e));
                }
            }
        });
    }

    public void sendGuardianRequest(UserModel guardian, final OnResponseCallback callback) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ParseConstants.FROM_USER_PARAM_KEY, userModel.getObjectId());
        parameters.put(ParseConstants.TO_USER_PARAM_KEY, guardian.getObjectId());
        ParseCloud.callFunctionInBackground(ParseConstants.SEND_GUARDIAN_REQUEST, parameters, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer response, ParseException e) {
                if (e != null) {
                    callback.onFailed(new Error(e));
                } else if (response != SUCCESS_RESPONSE) {
                    callback.onFailed(new Error(R.string.error_invitation));
                } else {
                    callback.onSuccess(null); // do not return nothing
                }
            }
        });
    }

    public void cancelGuardianRequest(String toUserId, final OnResponseCallback callback) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ParseConstants.FROM_USER_PARAM_KEY, userModel.getObjectId());
        parameters.put(ParseConstants.TO_USER_PARAM_KEY, toUserId);
        ParseCloud.callFunctionInBackground(ParseConstants.CANCEL_INVITATION, parameters, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer response, ParseException e) {
                if (e != null) {
                    callback.onFailed(new Error(e));
                } else if (response != SUCCESS_RESPONSE) {
                    callback.onFailed(new Error(R.string.error_cancel_invitation));
                } else {
                    callback.onSuccess(null); // do not return nothing
                }
            }
        });
    }

    public void createNonUserSmsInvitation(String phoneNumber, final OnResponseCallback callback) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ParseConstants.USER_ID_PARAM_KEY, userModel.getObjectId());
        parameters.put(ParseConstants.PHONE_PARAM_KEY, phoneNumber);
        ParseCloud.callFunctionInBackground(ParseConstants.CREATE_NON_USER_INVITATION, parameters, new FunctionCallback<ParseObject>() {
            @Override
            public void done(ParseObject nonUserInvitation, ParseException e) {
                if (e != null) {
                    callback.onFailed(new Error(e));
                } else {
                    callback.onSuccess(nonUserInvitation);
                }
            }
        });
    }

    public void removeNonUserSmsInvitation(String phoneNumber, final OnResponseCallback callback) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ParseConstants.PHONE_PARAM_KEY, phoneNumber);
//        ParseCloud.callFunctionInBackground(ParseConstants.REMOVE_NON_USER_INVITATION, parameters, new FunctionCallback<ParseObject>() {
//            @Override
//            public void done(ParseObject nonUserInvitation, ParseException e) {
//                if (e != null) {
//                    callback.onFailed(new Error(e));
//                } else {
//                    callback.onSuccess(nonUserInvitation);
//                }
//            }
//        });

        ParseCloud.callFunctionInBackground(ParseConstants.REMOVE_NON_USER_INVITATION, parameters, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer nonUserInvitation, ParseException e) {
                if (e != null) {
                    callback.onFailed(new Error(e));
                } else {
//                    callback.onSuccess(nonUserInvitation);
                    callback.onSuccess(null); // do not return nothing
                }
            }
        });

    }

    public void getFirebaseToken(final OnFirebaseCallback callback) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("key", "value");

        ParseCloud.callFunctionInBackground(ParseConstants.GET_FIREBASE_TOKEN, parameters/*Collections.<String, String>emptyMap()*/, new FunctionCallback<String>() {
            @Override
            public void done(String token, ParseException e) {
                if (e != null) {
                    Log.d("FirebaseTokenError", e.getMessage());
                    callback.onFailed(new Error(e));
                } else {
                    callback.onSuccess(token);
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onContactsLoaded(ContactsLoadedEvent event) {
        List<Map<String, String>> contacts = new ArrayList<>();
        for (ContactModel contact : event.getContacts()) {
            Map<String, String> contactInfo = new HashMap<>();
            contactInfo.put(ParseConstants.PHONE_PARAM_KEY, contact.getPhoneNumber());
            contactInfo.put(ParseConstants.CONTACT_NAME_PARAM_KEY, contact.getName());
            contacts.add(contactInfo);
        }
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ParseConstants.CONTACTS_PARAM_KEY, contacts);
        ParseCloud.callFunctionInBackground(ParseConstants.SEND_USER_PHONE_CONTACTS, parameters);
    }

    private List<String> getPhonesForContacts(Collection<ContactModel> contactModels) {
        List<String> phonesList = new ArrayList<>();
        for (ContactModel userModel : contactModels) {
            String phone = userModel.getPhoneNumber();
            if (phone != null && !phone.isEmpty()) {
                phonesList.add(phone);
            }
        }
        return phonesList;
    }

    public interface MyConnectionsCallback extends BaseResponseCallback {
        void onConnectionsReceived(List<UserModel> connections);
    }

    public void deleteMyAccount(final Context context, final UserModel guardianUser
            , final OnResponseCallback callback) {
//        Map<String, String> parameters = new HashMap<>();
//        parameters.put(ParseConstants.FROM_USER_PARAM_KEY, userModel.getObjectId());
//        parameters.put(ParseConstants.TO_USER_PARAM_KEY, guardianUser.getObjectId());
//        parameters.put(ParseConstants.INIT_USER_ID_PARAM_KEY, userModel.getObjectId());
//        ParseCloud.callFunctionInBackground(ParseConstants.DELETE_MY_ACCOUNT, parameters, new FunctionCallback<Integer>() {
//            @Override
//            public void done(Integer response, ParseException e) {
//                if (e != null) {
//                    callback.onFailed(new Error(e));
//                } else if (response != SUCCESS_RESPONSE) {
//                    callback.onFailed(new Error(R.string.msg_delete_my_account_text));
//                } else {
//                    callback.onSuccess(null);
//                }
//            }
//        });

        // --------------
        ParseUser user = ParseUser.getCurrentUser();
        user.deleteInBackground(new DeleteCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                if (e == null) {
                    //user deleted
                    callback.onSuccess(null);
                } else {
//                    ccd.showDialog(mContext, e.getMessage());
//                    callback.onFailed(new Error(R.string.msg_delete_my_account_text));
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }

    private void hideLoading() {
        Timber.tag(BaseActivity.TAG).d("".concat("UserManager hideLoading"));
        new BaseFragment() {
            @Override
            public int getTitleResId() {
                return 0;
            }

            @Override
            protected void onViewNotificationDialogClicked(Context context, NewNotificationEvent event) {

            }

            @Override
            protected void onNavigationDrawerItemSelected(NavigationMenu navigationMenu) {

            }
        }.hideLoading();
    }
}
