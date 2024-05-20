package com.safelet.android.models.event.bluetooth;

public class BluetoothEnabledEvent {
    private boolean enabled;

    public BluetoothEnabledEvent(boolean enabled) {
        this.enabled = enabled;
    }

    public void setEnabled(boolean newState) {
        enabled = newState;
    }

    public boolean isEnabled() {
        return enabled;
    }

}
