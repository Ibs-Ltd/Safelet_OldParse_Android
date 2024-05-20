package com.safelet.android.models.event.bluetooth;

public class DeviceConnectionStateChangedEvent {
    private boolean isGattConnected;

    public DeviceConnectionStateChangedEvent(boolean connected) {
        isGattConnected = connected;
    }

    public void setConnected(boolean newState) {
        isGattConnected = newState;
    }

    public boolean isConnected() {
        return isGattConnected;
    }
}
