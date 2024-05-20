package com.safelet.android.interactors;

import com.safelet.android.BuildConfig;

import org.greenrobot.eventbus.EventBus;

public class EventBusManager {

    private EventBus eventBus;

    private static EventBusManager sInstance;

    public static EventBusManager instance() {
        if (sInstance == null) {
            sInstance = new EventBusManager();
        }
        return sInstance;
    }

    private EventBusManager() {
        eventBus = EventBus.builder().logNoSubscriberMessages(BuildConfig.DEBUG).throwSubscriberException(true)
                .installDefaultEventBus();
    }

    public void postEvent(Object event) {
        eventBus.post(event);
    }

    public void postStickyEvent(Object event) {
        eventBus.postSticky(event);
    }

    public void postGattEvent(Object gattEvent) {
        postEvent(gattEvent);
    }

    public void register(Object object) {
        if (!eventBus.isRegistered(object)) {
            eventBus.register(object);
        }
    }

    public void unRegister(Object object) {
        eventBus.unregister(object);
    }
}
