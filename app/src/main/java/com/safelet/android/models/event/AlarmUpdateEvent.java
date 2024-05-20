package com.safelet.android.models.event;

import com.parse.ParseObject;
import com.safelet.android.utils.Error;

public class AlarmUpdateEvent extends BaseEvent {

    public AlarmUpdateEvent(ParseObject object) {
        super(object);
    }

    public AlarmUpdateEvent(Error error) {
        super(error);
    }
}
