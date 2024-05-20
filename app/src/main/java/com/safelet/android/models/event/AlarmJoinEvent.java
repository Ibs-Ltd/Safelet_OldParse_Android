package com.safelet.android.models.event;

import com.safelet.android.utils.Error;

public class AlarmJoinEvent extends BaseEvent {

    public AlarmJoinEvent() {
        super();
    }

    public AlarmJoinEvent(Error error) {
        super(error);
    }
}
