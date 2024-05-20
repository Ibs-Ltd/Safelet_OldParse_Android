package com.safelet.android.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.safelet.android.R;
import com.safelet.android.views.cells.TwoRowItem;

import java.util.ArrayList;

/**
 * Listview adapter for the device information list
 * <p/>
 * Created by Reinier on 2-4-2015.
 */
public class DeviceInfoListAdapter extends BaseAdapter {
    private Context context;
    private ArrayList<TwoRowItem> deviceInfoCollection;

    public DeviceInfoListAdapter(Context context, ArrayList<TwoRowItem> deviceInfoCollection) {
        this.context = context;
        this.deviceInfoCollection = deviceInfoCollection;
    }

    @Override
    public int getCount() {
        return deviceInfoCollection.size();
    }

    @Override
    public Object getItem(int position) {
        return deviceInfoCollection.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            rowView = inflater.inflate(R.layout.view_deviceinfo_listviewitem, null);
        }

        TextView title = (TextView) rowView.findViewById(R.id.deviceInfoTitleTextView);
        TextView value = (TextView) rowView.findViewById(R.id.deviceInfoValueTextView);

        title.setText(deviceInfoCollection.get(position).title);
        value.setText(deviceInfoCollection.get(position).value);

        return rowView;
    }
}


