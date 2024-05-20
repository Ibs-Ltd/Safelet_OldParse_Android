package com.safelet.android.models.event;

public class GuardianJoinedEvent {

    private String name;

    public GuardianJoinedEvent(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
