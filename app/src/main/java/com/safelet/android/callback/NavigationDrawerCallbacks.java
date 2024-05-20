package com.safelet.android.callback;

import com.safelet.android.models.enums.NavigationMenu;

/**
 * Callbacks interface that all activities using this fragment must implement.
 */
public interface NavigationDrawerCallbacks {

    /**
     * Called when an item in the navigation drawer is selected
     */
    void onNavigationDrawerItemSelected(NavigationMenu menuItem);

    /**
     * Called to update the navigation menu list
     */
    void updateMenu();
}
