package com.safelet.android.interactors.callbacks;

import com.safelet.android.models.Alarm;
import com.safelet.android.models.CheckIn;
import com.safelet.android.models.GuardianInvitation;

public interface EventsListAdapterCallback {

    void onPlayButtonClicked(Alarm alarm);

    void onStopButtonClicked();

    void onAlarmDetails(Alarm alarm);

    void onIgnoreAlarm(Alarm alarm);

    void onAccept(GuardianInvitation invitation);

    void onDecline(GuardianInvitation invitation);

    void onCheckIn(CheckIn checkIn);
}
