package com.safelet.android.interactors;

import com.parse.FunctionCallback;
import com.parse.GetCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.safelet.android.R;
import com.safelet.android.interactors.base.BaseManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.interactors.callbacks.OnStringResponseCallback;
import com.safelet.android.interactors.utils.ParseConstants;
import com.safelet.android.models.CheckIn;
import com.safelet.android.utils.Error;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckInManager extends BaseManager {

    private static CheckInManager sInstance;

    public static CheckInManager instance() {
        if (sInstance == null) {
            sInstance = new CheckInManager();
        }
        return sInstance;
    }

    public void getLastCheckInForUser(String userId, final OnResponseCallback callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.USER_ID_PARAM_KEY, userId);
        ParseCloud.callFunctionInBackground(ParseConstants.GET_LAST_CHECKIN_FOR_USER, parameters, new FunctionCallback<Object>() {
            @Override
            public void done(Object checkIn, ParseException parseException) {
                if (parseException == null) {
                    if (checkIn instanceof CheckIn) {
                        try {
                            ((CheckIn) checkIn).getUser().fetchIfNeeded();
                            callback.onSuccess((CheckIn) checkIn);
                        } catch (ParseException e) {
                            callback.onFailed(new Error(R.string.error_last_checkin_2));
                        }
                    } else {
                        callback.onFailed(new Error(R.string.error_last_checkin_2));
                    }
                } else {
                    callback.onFailed(new Error(parseException));
                }
            }
        });
    }

    public void getCheckInById(String checkInId, final OnResponseCallback callback) {
        ParseQuery<CheckIn> query = ParseQuery.getQuery(CheckIn.class);
        query.getInBackground(checkInId, new GetCallback<CheckIn>() {
            @Override
            public void done(CheckIn checkIn, ParseException e) {
                if (e == null) {
                    try {
                        checkIn.getUser().fetchIfNeeded();
                        callback.onSuccess(checkIn);
                    } catch (ParseException ignore) {
                        callback.onFailed(new Error(R.string.error_last_checkin_3));
                    }
                } else {
                    callback.onFailed(new Error(R.string.error_last_checkin_3));
                }
            }
        });
    }

    public void createCheckInForUser(String userId, ParseGeoPoint location, String address, String msg,
                                     final OnResponseCallback callback) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ParseConstants.USER_ID_PARAM_KEY, userId);
        parameters.put(ParseConstants.CHECKIN_GEOPOINT_PARAM_KEY, location);
        parameters.put(ParseConstants.CHECKIN_ADDRESS_PARAM_KEY, address);
        parameters.put(ParseConstants.CHECKIN_MESSAGE_PARAM_KEY, msg);
        ParseCloud.callFunctionInBackground(ParseConstants.CREATE_CHECKIN_FOR_USER, parameters, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer integer, ParseException e) {
                if (e == null) {
                    if (integer == SUCCESS_RESPONSE) {
                        callback.onSuccess(null);
                    } else {
                        callback.onFailed(new Error(R.string.error_send_checkin));
                    }
                } else {
                    callback.onFailed(new Error(e));
                }
            }
        });
    }

    // Share Location User
    public void onShareLocationUser(String userId, ParseGeoPoint location, String address
            , String msg, List<String> mMulUserArr, final OnResponseCallback callback) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ParseConstants.USER_ID_PARAM_KEY, userId);
        parameters.put(ParseConstants.CHECKIN_GEOPOINT_PARAM_KEY, location);
        parameters.put(ParseConstants.CHECKIN_ADDRESS_PARAM_KEY, address);
        parameters.put(ParseConstants.CHECKIN_MESSAGE_PARAM_KEY, msg);
        parameters.put(ParseConstants.SELECTED_USERS_OBJECT_IDS_KEY, mMulUserArr);
        ParseCloud.callFunctionInBackground(ParseConstants.CREATE_CHECK_IN_FOR_MULTIPLE_USER
                , parameters, new FunctionCallback<Integer>() {
                    @Override
                    public void done(Integer integer, ParseException e) {
                        if (e == null) {
                            if (integer == SUCCESS_RESPONSE) {
                                callback.onSuccess(null);
                            } else {
                                callback.onFailed(new Error(R.string.error_send_checkin));
                            }
                        } else {
                            callback.onFailed(new Error(e));
                        }
                    }
                });
    }

    // Start Follow Me To Multiple Guardians Task
    public void onStartFollowMeToMultipleGuardians(String userId, ParseGeoPoint location, String address
            , List<String> mMulUserArr, final OnStringResponseCallback callback) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(ParseConstants.USER_ID_PARAM_KEY, userId);
        parameters.put(ParseConstants.CHECKIN_GEOPOINT_PARAM_KEY, location);
        parameters.put(ParseConstants.CHECKIN_ADDRESS_PARAM_KEY, address);
        parameters.put(ParseConstants.SELECTED_USERS_OBJECT_IDS_KEY, mMulUserArr);
        ParseCloud.callFunctionInBackground(ParseConstants.START_FOLLOW_ME_TO_MULTIPLE_GUARDIANS
                , parameters, new FunctionCallback<String>() {
                    @Override
                    public void done(String object, ParseException e) {
                        if (e == null) {
                            if (object != null && !object.equals("")) {
                                callback.onSuccess(object);
                            } else {
                                callback.onFailed(new Error(R.string.error_send_checkin));
                            }
                        } else {
                            callback.onFailed(new Error(e));
                        }
                    }
                });
    }

    //  updateFollowMeLocation
    public void onFollowMeUpdateLocation(String aObjectId, String aUserObjectId, ParseGeoPoint location
            , String checkInAddress, final OnResponseCallback callback) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("aObjectId", aObjectId);
        parameters.put("aUserObjectId", aUserObjectId);
        parameters.put("checkInGeoPoint", location);
        parameters.put("checkInAddress", checkInAddress);
        ParseCloud.callFunctionInBackground("updateFollowMeLocation", parameters, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer integer, ParseException e) {
                if (e == null) {
                    if (integer == SUCCESS_RESPONSE) {
                        callback.onSuccess(null);
                    } else {
                        callback.onFailed(new Error(R.string.error_send_checkin));
                    }
                } else {
                    callback.onFailed(new Error(e));
                }
            }
        });
    }

    // Stop Follow Me
    public void onFollowMeStop(String userId, String aObjectId, final OnResponseCallback callback) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("aUserObjectId", userId);
        parameters.put("aObjectId", aObjectId);
        ParseCloud.callFunctionInBackground(ParseConstants.STOP_MULTIPLE_FOLLOW_ME_USER
                , parameters, new FunctionCallback<Integer>() {
                    @Override
                    public void done(Integer integer, ParseException e) {

                        if (e == null) {
                            if (integer == SUCCESS_RESPONSE) {
                                callback.onSuccess(null);
                            } else {
                                callback.onFailed(new Error(R.string.error_send_checkin));
                            }
                        } else {
                            callback.onFailed(new Error(e));
                        }
                    }
                });
    }


}

