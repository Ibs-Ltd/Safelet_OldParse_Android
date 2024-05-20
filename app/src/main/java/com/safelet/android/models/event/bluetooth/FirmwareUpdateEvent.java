package com.safelet.android.models.event.bluetooth;

public class FirmwareUpdateEvent {

    private byte[] firmware;

    public FirmwareUpdateEvent(byte[] firmware) {
        this.firmware = firmware;
    }

    public byte[] getFirmware() {
        return firmware;
    }
}
