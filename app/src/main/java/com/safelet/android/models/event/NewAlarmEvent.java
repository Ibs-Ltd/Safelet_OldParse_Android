package com.safelet.android.models.event;

import com.parse.ParseObject;
import com.safelet.android.utils.Error;

public class NewAlarmEvent extends BaseEvent {

    public NewAlarmEvent(ParseObject result) {
        super(result);
    }

    public NewAlarmEvent(Error error) {
        super(error);
    }
}
