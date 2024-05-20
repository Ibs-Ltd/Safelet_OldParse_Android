package com.safelet.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.adapters.PhonePrefixAdapter;
import com.safelet.android.models.PhonePrefixes;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Activity for Phone prefix chooser Screen<br/>
 * Enable selecting a country
 *
 * @author mihai
 */
public class PhonePrefixListActivity extends BaseActivity implements OnItemClickListener, OnTouchListener {

    private static final String[] KEYS = {"A", "B", "C", "D", "E", "F", "G",
            "H", "I", "J", "K", "L", "M", "N", "O", "P", "q", "R", "S", "T",
            "U", "V", "W", "X", "Y", "Z"};
    public static final String PREFIX_KEY = "prefix";

    private final Map<String, Integer> alphaIndexes = new HashMap<>();
    private ListView prefixesListView;
    private PhonePrefixAdapter prefixesListAdapter;
    private LinearLayout sideIndexLinearLayout;
    private boolean windowFocused;
    private float deltaX;
    private int tempIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_prefix_list);

        Toolbar toolbar = findViewById(R.id.safelet_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setTitle(R.string.change_phone_prefix_title);

        prefixesListView = findViewById(R.id.phone_list);
        prefixesListAdapter = new PhonePrefixAdapter(this);
        prefixesListAdapter.setItems(getIndexedContacts(prefixesListAdapter.getAllPrefixItems()));
        prefixesListView.setAdapter(prefixesListAdapter);
        prefixesListView.setOnItemClickListener(this);
        sideIndexLinearLayout = findViewById(R.id.sideIndexPhone);
        sideIndexLinearLayout.setOnTouchListener(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        windowFocused = hasFocus;
        if (sideIndexLinearLayout != null && windowFocused) {
            int indexHeight = sideIndexLinearLayout.getHeight();
            deltaX = (float) indexHeight / 26f;
        }
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.phone_prefix_menu, menu);
        MenuItem searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        if (searchView != null)
            searchView.setOnQueryTextListener(new SearchTextListener(this));
        return true;
    }

    private void onSearchContact(CharSequence sequence) {
        prefixesListAdapter.getFilter().filter(sequence);
        prefixesListAdapter.notifyDataSetChanged();
    }

    /**
     * Positions the list at respective letter
     *
     * @param letter Letter to position the list at item number
     */
    void displayListItem(String letter) {
        if (alphaIndexes.containsKey(letter)) {
            int index = alphaIndexes.get(letter);
            if (prefixesListView != null) {
                prefixesListView.setSelectionFromTop(index, 0);
            }
        }
    }

    /**
     * Sorts contact in a list based on letters
     *
     * @param input prefixes
     * @return alphabetically sorted country prefixes
     */
    List<PhonePrefixes> getIndexedContacts(List<PhonePrefixes> input) {
        // Retrieve it from DB in shorting order
        List<PhonePrefixes> v = new ArrayList<>();
        // Add default item
        String idx1;
        String idx2 = null;
        alphaIndexes.clear();
        int size = input.size();
        int j = 0;
        for (int i = 0; i < size; i++) {
            PhonePrefixes temp = input.get(i);
            // Insert the alphabets
            idx1 = (temp.countryName.substring(0, 1))
                    .toLowerCase(Locale.ENGLISH);
            if (!idx1.equalsIgnoreCase(idx2)) {
                PhonePrefixes letter = new PhonePrefixes("", "");
                // letter.setType( Constants.TYPE_LETTER );
                letter.countryName = idx1.toUpperCase(Locale.ENGLISH);
                letter.prefix = "";
                letter.type = PhonePrefixes.TYPE_LETTER;

                v.add(letter);
                idx2 = idx1;
                alphaIndexes.put(idx1.toUpperCase(Locale.ENGLISH), i + j);
                j += 1;
            }
            v.add(temp);
        }
        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PhonePrefixes pp = prefixesListAdapter.getItem(position);
        String result = pp.countryName + "(" + pp.prefix + ")";
        Intent intent = new Intent();
        if (pp.type != PhonePrefixes.TYPE_LETTER) {
            intent.putExtra("resultCountryPrefix", result);
            intent.putExtra(PREFIX_KEY, pp.prefix);
            intent.putExtra("countryName", pp.countryName);
            intent.putExtra("countryCode", PhoneNumberUtil.getInstance().getRegionCodeForCountryCode(Integer.valueOf(pp.prefix)));
            setResult(11, intent);
            finish();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // handle view touch event
        float x = event.getX();
        float y = event.getY();

        if (windowFocused) {
            if (x >= 0 && y >= 0) {
                int newIndex = Math.round(y / deltaX);
                if (newIndex != tempIndex) {
                    tempIndex = newIndex;
                    if (tempIndex < KEYS.length && tempIndex > -1) {
                        displayListItem(KEYS[tempIndex]);
                    }
                }
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    private static class SearchTextListener implements SearchView.OnQueryTextListener {
        private WeakReference<PhonePrefixListActivity> weakReference;

        public SearchTextListener(PhonePrefixListActivity activity) {
            weakReference = new WeakReference<PhonePrefixListActivity>(activity);
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            return true;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            PhonePrefixListActivity activity = weakReference.get();
            if (activity != null) {
                activity.onSearchContact(newText);
            }
            return false;
        }
    }
}