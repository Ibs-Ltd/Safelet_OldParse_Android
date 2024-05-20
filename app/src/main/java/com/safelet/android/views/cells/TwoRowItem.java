package com.safelet.android.views.cells;

/**
 * This is just a simple class object to contain 2 string of a tworowitem to use in lists.
 *
 * Created by Reinier on 2-4-2015.
 */
public class TwoRowItem {
    public String title;
    public String value;
    public TwoRowItem() {
        title = "";
        value= "";
    }

    public TwoRowItem(String newTitle, String newValue) {
        title = newTitle;
        value = newValue;
    }
}
