package com.safelet.android.models.event;

import com.parse.ParseObject;
import com.safelet.android.utils.Error;

class BaseEvent {

    private Error error;

    private ParseObject result;

    BaseEvent() {
        super();
    }

    BaseEvent(Error error) {
        this.error = error;
    }

    BaseEvent(ParseObject result) {
        this.result = result;
    }

    public Error getError() {
        return error;
    }

    public ParseObject getResult() {
        return result;
    }
}
