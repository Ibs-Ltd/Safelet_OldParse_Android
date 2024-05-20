package com.safelet.android.models.enums;

import com.safelet.android.R;

public enum NavigationMenu {
    //    GETTING_STARTED(R.string.txt_menu_getting_started),
    HOME(R.string.txt_menu_home),
    MY_PROFILE(R.string.myprofile_title),
    MY_CONNECTIONS(R.string.connections_title),
    EVENTS(R.string.events_title),
    //    GUARDIAN_NETWORK(R.string.community_title),
    CHECK_IN(R.string.checkin_title),
    ALARM(R.string.alarm_title),
    FEEDBACK(R.string.home_feedback_title),
    PRIVACY(R.string.privacy_policy_title),
    OPTIONS(R.string.options_title),
    LOGOUT(R.string.home_logout_title),

    SAFELET_STATUS_AREA(R.string.txt_menu_safelet_status_area),
    CONNECT_SAFELET(R.string.safelet_connect_title),
    DISCONNECT_SAFELET(R.string.safelet_disconnect_title),
    DISCONNECTING_SAFELET(R.string.safelet_disconnecting_title);

    private int titleResId;

    NavigationMenu(int titleResourceId) {
        this.titleResId = titleResourceId;
    }

    public int getTitleResId() {
        return titleResId;
    }

    public static NavigationMenu[] getMenuWithBluetooth() {
        return values();
    }

    public static NavigationMenu[] getConnectedMenu() {
        return new NavigationMenu[]{MY_PROFILE, MY_CONNECTIONS, EVENTS, FEEDBACK, PRIVACY, OPTIONS, LOGOUT, SAFELET_STATUS_AREA, DISCONNECT_SAFELET};
    }

    public static NavigationMenu[] getDisconnectingMenu() {
        return new NavigationMenu[]{MY_PROFILE, MY_CONNECTIONS, EVENTS, FEEDBACK, PRIVACY, OPTIONS, LOGOUT, SAFELET_STATUS_AREA, DISCONNECTING_SAFELET};
    }

    public static NavigationMenu[] getDisconnectedMenu() {
        return new NavigationMenu[]{MY_PROFILE, MY_CONNECTIONS, EVENTS, FEEDBACK, PRIVACY, OPTIONS, LOGOUT, SAFELET_STATUS_AREA, CONNECT_SAFELET};
    }

    public static NavigationMenu[] getMenuWithoutBluetooth() {
        return new NavigationMenu[]{MY_PROFILE, MY_CONNECTIONS, EVENTS, FEEDBACK, PRIVACY, OPTIONS, LOGOUT, SAFELET_STATUS_AREA};
    }
}
