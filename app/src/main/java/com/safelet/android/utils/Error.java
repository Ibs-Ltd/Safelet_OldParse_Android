package com.safelet.android.utils;

import android.content.Context;
import android.text.TextUtils;

import com.parse.ParseException;
import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class Error {

    private static final String MESSAGE_KEY = "localizedKey";
    private static final String MESSAGE_ARGS_KEY = "localizedArgs";
    private static final String MESSAGE_ERROR = "fullErrorMessage";

    private ErrorType errorType;
    private Object[] errorArgs = new String[0];
    private String errorMessage;
    private int errorMessageResId;
    private int parseErrorCode;

    public Error(int errorMessageResId) {
        this.errorMessageResId = errorMessageResId;
    }

    public Error(ParseException parseError) {
        try {
            String errorAsString = parseError.getMessage();
            Timber.tag(BaseActivity.TAG).d(" ".concat(" Error Message ").concat(errorAsString));
            JSONObject errorAsJson = new JSONObject(errorAsString);
            String localizedKey = errorAsJson.getString(MESSAGE_KEY);

            List<String> messageArgsList = new ArrayList<>();
            if (errorAsJson.has(MESSAGE_ARGS_KEY)) {
                JSONArray messageArgsJsonArray = errorAsJson.getJSONArray(MESSAGE_ARGS_KEY);
                for (int i = 0; i < messageArgsJsonArray.length(); i++) {
                    messageArgsList.add(messageArgsJsonArray.getString(i));
                }
            }

            errorType = ErrorType.fromString(localizedKey);
            errorArgs = messageArgsList.toArray();
            errorMessage = errorAsJson.optString(MESSAGE_ERROR);
            parseErrorCode = parseError.getCode();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public String getErrorMessage(Context context) {
        if (errorType != null) {
            return String.format(context.getString(errorType.getMessageResId()), errorArgs);
        } else if (!TextUtils.isEmpty(errorMessage)) {
            return errorMessage;
        } else {
            return context.getString(errorMessageResId != 0 ? errorMessageResId : R.string.error_general);
        }
    }

    public int getErrorMessageResId() {
        if (errorMessageResId != 0) {
            return errorMessageResId;
        }
        return errorType.getMessageResId();
    }

    public int getParseErrorCode() {
        return parseErrorCode;
    }

    public enum ErrorType {
        CHECK_USER_NOT_FOUND_ERROR(R.string.error_user_not_exist),
        EXISTS_ALARM_ERROR(R.string.error_existing_alarm),
        DISPATCH_ALARM_ERROR(R.string.error_dispatch_alarm),
        SERVER_ERROR(R.string.error_server),
        STOP_ALARM_ERROR(R.string.error_stop_alarm),
        IGNORE_ALARM_ERROR(R.string.error_ignore_alarm),
        GET_ACTIVE_ALARM_ERROR(R.string.error_get_active_alarm),
        GET_ALARM_PARTICIPANTS_ERROR(R.string.error_get_alarm_participants),
        CALLED_EMERGENCY_ERROR(R.string.error_called_emergency),
        REFRESH_ALARM_ERROR(R.string.error_refresh_alarm),
        GET_ALL_CHUNKS_ERROR(R.string.error_get_all_chunks),
        GET_LAST_CHUNK_ERROR(R.string.error_get_last_chunk),
        SAVE_RECORDING_ERROR(R.string.error_save_recording),
        MAX_RECORDING_ERROR(R.string.error_max_recording),
        LAST_CHECKIN_ERROR(R.string.error_last_checkin),
        LAST_CHECKIN_ERROR_2(R.string.error_last_checkin_2),
        SEND_CHECKIN_ERROR(R.string.error_send_checkin),
        GET_EVENTS_ERROR(R.string.error_get_events),
        GET_FIRMWARE_ERROR(R.string.error_get_firmware),
        GET_APPVERSION_ERROR(R.string.error_get_app_version),
        GET_PARSE_USERS_ERROR(R.string.error_get_users),
        INVITATION_ERROR(R.string.error_invitation),
        INVITATION_EXISTENT_ERROR(R.string.error_invitation_exist),
        INVITATION_STATUS_ERROR(R.string.error_invitation_status),
        BAD_INVITATION_STATUS_RESPONSE_ERROR(R.string.error_bad_invitation_status),
        RESPOND_INVITATION_ERROR(R.string.error_response_invitation),
        CANCEL_INVITATION_INVALID_STATUS_ERROR(R.string.error_cancel_invitation_invalid_status),
        CANCEL_INVITATION_ERROR(R.string.error_cancel_invitation),
        CANCEL_INVITATION_INEXISTENT_ERROR(R.string.error_cancel_invitation_inexistent),
        REMOVE_INVITATION_INVALID_STATUS_ERROR(R.string.error_remove_invitation_invalid_status),
        REMOVE_INVITATION_ERROR(R.string.error_remove_invitation),
        REMOVE_INVITATION_INEXISTENT_ERROR(R.string.error_remove_invitation_inexistent),
        GET_GUARDIANS_ERROR(R.string.error_get_guardians),
        GET_GUARDED_ERROR(R.string.error_get_guarded),
        CHECK_USER_ERROR(R.string.error_check_user),
        PHONE_DETAILS_ERROR(R.string.error_phone_details),
        BLUETOOTH_NOTIFICATION_ERROR(R.string.error_bt_notification),
        SAVE_USER_EMPTY_EMAIL_ERROR(R.string.error_save_user_empty_email),
        SAVE_USER_EMPTY_USERNAME_ERROR(R.string.error_save_user_empty_username),
        SAVE_USER_EMPTY_NAME_ERROR(R.string.error_save_user_empty_name),
        SAVE_USER_INVALID_NAME_ERROR(R.string.error_save_user_invalid_name);

        private int messageResId;

        ErrorType(int messageResId) {
            this.messageResId = messageResId;
        }

        public int getMessageResId() {
            return messageResId;
        }

        public static ErrorType fromString(String text) {
            for (ErrorType errorType : ErrorType.values()) {
                if (errorType.name().equalsIgnoreCase(text)) {
                    return errorType;
                }
            }
            return null;
        }
    }
}
