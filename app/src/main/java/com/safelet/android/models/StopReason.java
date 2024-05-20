package com.safelet.android.models;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.safelet.android.interactors.utils.ParseConstants;

@ParseClassName("StopAlarmReason")
public class StopReason extends ParseObject {

    public StopReason() {
    }

    public Reason getReason() {
        return Reason.fromId(getString(ParseConstants.STOP_ALARM_REASON_ID_KEY));
    }

    public String getReasonDescription() {
        return getString(ParseConstants.STOP_ALARM_OTHER_DESCRIPTION_KEY);
    }

    public enum Reason {
        TEST_ALARM("testAlarm"), ACCIDENTAL_ALARM("accidentalAlarm"), ALARM_NOT_NEEDED("alarmNotNeeded"), OTHER("other");

        private String id;

        Reason(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public static Reason fromId(String id) {
            for (Reason reason : values()) {
                if (reason.getId().equals(id)) {
                    return reason;
                }
            }
            return null;
        }
    }

}
