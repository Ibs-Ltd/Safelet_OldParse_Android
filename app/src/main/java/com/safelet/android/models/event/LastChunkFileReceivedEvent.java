package com.safelet.android.models.event;

import com.parse.ParseObject;
import com.safelet.android.utils.Error;

public class LastChunkFileReceivedEvent extends BaseEvent {

    public LastChunkFileReceivedEvent(ParseObject result) {
        super(result);
    }

    public LastChunkFileReceivedEvent(Error error) {
        super(error);
    }
}
