package com.safelet.android.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.safelet.android.R;
import com.safelet.android.global.Utils;
import com.safelet.android.models.PhonePrefixes;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Adapter for phone prefix screen {@link}
 */
public final class PhonePrefixAdapter extends BaseAdapter {
    private Context context;

    private List<PhonePrefixes> items = new ArrayList<>();
    private List<PhonePrefixes> filteredItems = new ArrayList<>();

    private Filter filter = new ItemFilter();

    /**
     * Loads prefixes from local assets file
     *
     * @param ctx Context
     */
    public PhonePrefixAdapter(Context ctx) {
        context = ctx;
        // Loads country prefixes from local json file
        String jsonJaCountries = null;
        try {
            jsonJaCountries = Utils.inputStream2String(ctx.getAssets().open(
                    "jsn/telephoneprefix.jsn"));
        } catch (IOException e) {
            Timber.tag(getClass().getSimpleName()).e(e);
        }
        JSONArray ja = null;
        try {
            ja = new JSONArray(jsonJaCountries);
        } catch (JSONException e) {
            Timber.tag(getClass().getSimpleName()).e(e);
        }
        if (ja != null) {
            int l = ja.length();
            for (int i = 0; i < l; i++) {
                try {
                    JSONObject jo = ja.getJSONObject(i);
                    items.add(new PhonePrefixes(jo.getString("countryCode"), jo
                            .getString("countryName")));
                } catch (JSONException e) {
                    Timber.tag(getClass().getSimpleName()).e(e);
                }
            }
        }
    }

    public List<PhonePrefixes> getAllPrefixItems() {
        return items;
    }

    /**
     * Load data prefixes
     *
     * @param items phone prefixes list
     */
    public void setItems(List<PhonePrefixes> items) {
        this.items = items;
        this.filteredItems = items;
    }

    @Override
    public int getCount() {
        if (filteredItems == null) {
            return 0;
        }
        return filteredItems.size();
    }

    @Override
    public PhonePrefixes getItem(int position) {
        return filteredItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.row_phone_prefix, parent, false);
            holder = new ViewHolder();
            holder.text = convertView.findViewById(R.id.phone_prefix_main_text);
            holder.textPrefix = convertView.findViewById(R.id.phone_prefix_prefix_text);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        PhonePrefixes phonePrefix = getItem(position);
        if (phonePrefix.type == PhonePrefixes.TYPE_LETTER) {
            holder.text.setText(phonePrefix.countryName);
            holder.textPrefix.setText("");
        } else {
            holder.textPrefix.setText("+".concat(phonePrefix.prefix));
            holder.text.setText(phonePrefix.countryName);
        }
        return convertView;
    }

    public Filter getFilter() {
        return filter;
    }

    private class ItemFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String filterString = constraint.toString().toLowerCase();
            FilterResults results = new FilterResults();
            final List<PhonePrefixes> filteredList = new ArrayList<>();
            String filterableName;
            for (PhonePrefixes prefix : items) {
                filterableName = prefix.countryName;
                if (filterableName.toLowerCase().startsWith(filterString)) {
                    filteredList.add(prefix);
                }
            }
            results.values = filteredList;
            results.count = filteredList.size();
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredItems = (List<PhonePrefixes>) results.values;
            notifyDataSetChanged();
        }
    }

    /**
     * Data model for storing phone prefixs
     */
//    public static class PhonePrefixes {
////        public static final int TYPE_DATE = 0;
//        public static final int TYPE_LETTER = 1;
//        public String prefix;
//        public String countryName;
//        public int type;
//
//        public PhonePrefixes(String prefix, String country) {
//            this.prefix = prefix;
//            this.countryName = country;
//        }
//    }

    private static class ViewHolder {
        TextView text;
        TextView textPrefix;
    }
}
