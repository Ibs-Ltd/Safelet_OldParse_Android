package com.safelet.android.models.event;

import com.safelet.android.models.Message;

public class FirebaseMessageEvent {

    private Message message;

    public FirebaseMessageEvent(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }
}
