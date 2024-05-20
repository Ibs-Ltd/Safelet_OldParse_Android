package com.safelet.android.models;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

@ParseClassName("Alarm")
public class Alarm extends ParseObject {
    public static final int MAX_RECORD_CHUNKS = 30;

    private static final String IS_ACTIVE_KEY = "isActive";
    private static final String PARTICIPANTS_KEY = "participants";
    private static final String USER_KEY = "user";
    private static final String CAN_RECORD_KEY = "canRecord";
    private static final String RECORDING_CHUNKS_COUNT_KEY = "recordingChunksCount";
    private static final String STOP_ALARM_REASON = "stopAlarmReason";

    public Alarm() {
    }

    public boolean isAlarmActive() {
        return getBoolean(IS_ACTIVE_KEY);
    }

    public void setAlertActive(boolean active) {
        put(IS_ACTIVE_KEY, active);
    }


    public List<UserModel> getParticipants() {
        List<UserModel> participants = getList(PARTICIPANTS_KEY);
        ;
        if (participants == null) {
            return new ArrayList<>();
        }
        return participants;
    }

    public void setParticipants(List<UserModel> participants) {
        put(PARTICIPANTS_KEY, participants);
    }

    public StopReason getStopReason() {
        return containsKey(STOP_ALARM_REASON) ? (StopReason) getParseObject(STOP_ALARM_REASON) : null;
    }

    public UserModel getUser() {
        return (UserModel) getParseUser(USER_KEY);
    }

    public void setUser(UserModel userId) {
        put(USER_KEY, userId);
    }


    public String getLocationName() {
        return getUser().getLocationName();
    }

    public ParseGeoPoint getAlertLocation() {
        return getUser().getLocation();
    }

    public ParseFile getUserImage() {
        return getUser().getUserImage();
    }

    public String getUserImageUrl() {
        return getUser().getImageUrl();
    }

    public boolean canRecord() {
        return getRecordingChunksCount() + 1 < MAX_RECORD_CHUNKS;
    }

    public int getRecordingChunksCount() {
        return getInt(RECORDING_CHUNKS_COUNT_KEY);
    }
}
