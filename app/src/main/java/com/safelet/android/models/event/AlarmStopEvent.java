package com.safelet.android.models.event;

import com.safelet.android.utils.Error;

public class AlarmStopEvent extends BaseEvent {

    public AlarmStopEvent() {
        super();
    }

    public AlarmStopEvent(Error error) {
        super(error);
    }
}
