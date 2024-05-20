package com.safelet.android.interactors;

import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.safelet.android.R;
import com.safelet.android.interactors.base.BaseManager;
import com.safelet.android.interactors.callbacks.OnResponseCallback;
import com.safelet.android.interactors.callbacks.OnResponseListCallback;
import com.safelet.android.interactors.utils.ParseConstants;
import com.safelet.android.models.Alarm;
import com.safelet.android.models.GuardianInvitation;
import com.safelet.android.models.enums.UserRelationStatus;
import com.safelet.android.models.event.NewEventsReceivedEvent;
import com.safelet.android.utils.Error;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventsManager extends BaseManager {

    private static EventsManager sInstance;
    private Map<String, List<ParseObject>> events = new HashMap<>();

    public static EventsManager instance() {
        if (sInstance == null) {
            sInstance = new EventsManager();
        }
        return sInstance;
    }

    private boolean includeHistoricEvents = true;

    public void getEventsForUser(String userId, final OnResponseListCallback callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.USER_ID_PARAM_KEY, userId);
        parameters.put(ParseConstants.HISTORIC_EVENTS_KEY, (includeHistoricEvents ? 1 : 0) + "");
        ParseCloud.callFunctionInBackground(ParseConstants.GET_EVENTS_FOR_USER, parameters, new FunctionCallback<Map<String, List<ParseObject>>>() {
            @Override
            public void done(Map<String, List<ParseObject>> eventsList, ParseException e) {
                if (e == null && eventsList != null && !eventsList.isEmpty()) {
                    events = eventsList;
                    EventBusManager.instance().postStickyEvent(new NewEventsReceivedEvent());
                    callback.onSuccess(eventsList);
                } else {
                    if (e != null)
                        callback.onFailed(new Error(e));
                }
            }
        });
    }

    public void responseToInvitation(String fromUserId, String toUserId, UserRelationStatus status, final OnResponseCallback callback) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put(ParseConstants.FROM_USER_PARAM_KEY, fromUserId);
        parameters.put(ParseConstants.TO_USER_PARAM_KEY, toUserId);
        parameters.put(ParseConstants.STATUS_PARAM_KEY, status.toString().toLowerCase());
        ParseCloud.callFunctionInBackground(ParseConstants.RESPONSE_TO_INVITATION, parameters, new FunctionCallback<Integer>() {
            @Override
            public void done(Integer integer, ParseException e) {
                if (e == null) {
                    if (integer == SUCCESS_RESPONSE) {
                        callback.onSuccess(null);
                    } else {
                        callback.onFailed(new Error(R.string.error_response_invitation));
                    }
                } else {
                    callback.onFailed(new Error(e));
                }
            }
        });
    }

    public Alarm getAlarmById(String alarmId) {
        List<ParseObject> alarms = events.get(EventType.ALARM.getId());
        if (alarms != null) {
            for (ParseObject object : alarms) {
                if (object.getObjectId().equals(alarmId)) {
                    return (Alarm) object;
                }
            }
        }
        return null;
    }

    public ParseObject getEventForId(String objectId) {
        for (String type : events.keySet()) {
            for (ParseObject object : events.get(type)) {
                if (object.getObjectId().equals(objectId)) {
                    return object;
                }
            }
        }
        return null;
    }

    public List<ParseObject> getAlarms() {
        return events.get(EventType.ALARM.getId());
    }

    public List<ParseObject> getEventsByType(EventType type) {
        switch (type) {
            case ALARM:
                return getAlarms();
            case INVITES:
                return getInvitations();
            default:
                return getCheckins();
        }
    }

    public List<ParseObject> getInvitations() {
        return events.get(EventType.INVITES.getId());
    }

    public List<ParseObject> getCheckins() {
        return events.get(EventType.CHECKINS.getId());
    }

    public Map<String, List<ParseObject>> getEvents() {
        return events;
    }

    public int getEventsTypeCount(EventType type) {
        List<ParseObject> events = getEventsByType(type);
        return events != null ? events.size() : 0;
    }

    public int getNumberOfActiveEvents() {
        int nrEvents = 0;
        List<ParseObject> alarms = events.get(EventType.ALARM.getId());
        if (alarms != null) {
            for (ParseObject alarm : alarms) {
                if (((Alarm) alarm).isAlarmActive()) {
                    nrEvents++;
                }
            }
        }
        List<ParseObject> invitations = events.get(EventType.INVITES.getId());
        if (invitations != null) {
            for (ParseObject invitation : invitations) {
                if (((GuardianInvitation) invitation).getRelationStatus().equals(UserRelationStatus.PENDING)) {
                    nrEvents++;
                }
            }
        }
        return nrEvents;
    }

    public int getNumberOfActiveAlarms() {
        int alarmCount = 0;
        List<ParseObject> alarms = events.get(EventType.ALARM.getId());
        if (alarms != null) {
            for (ParseObject alarm : alarms) {
                if (((Alarm) alarm).isAlarmActive()) {
                    alarmCount++;
                }
            }
        }
        return alarmCount;
    }

    public void toggleHistoricEvents() {
        setIncludeHistoricEvents(!includeHistoricEvents);
    }

    public boolean includeHistoricEvents() {
        return includeHistoricEvents;
    }

    public void setIncludeHistoricEvents(boolean include) {
        this.includeHistoricEvents = include;
    }

    public void logout() {
        events.clear();
        events = new HashMap<>();
        includeHistoricEvents = false;
    }

    public enum EventType {

        ALARM("alarms"), INVITES("invites"), CHECKINS("checkIns");

        private String id;

        EventType(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static EventType fromId(String id) {
            for (EventType eventType : values()) {
                if (eventType.getId().equals(id)) {
                    return eventType;
                }
            }
            return null;
        }
    }
}
