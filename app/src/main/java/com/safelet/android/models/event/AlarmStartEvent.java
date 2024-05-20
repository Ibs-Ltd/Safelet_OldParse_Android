package com.safelet.android.models.event;

import com.parse.ParseObject;
import com.safelet.android.utils.Error;

public class AlarmStartEvent extends BaseEvent {

    public AlarmStartEvent(ParseObject result) {
        super(result);
    }

    public AlarmStartEvent(Error error) {
        super(error);
    }
}
