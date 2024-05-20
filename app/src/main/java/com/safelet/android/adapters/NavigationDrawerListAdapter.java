package com.safelet.android.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.safelet.android.R;
import com.safelet.android.interactors.EventsManager;
import com.safelet.android.models.enums.NavigationMenu;

import global.BluetoothState;
import models.enums.DeviceState;

public class NavigationDrawerListAdapter extends BaseAdapter {

    private Context context;
    private NavigationMenu[] menuItems = NavigationMenu.getMenuWithBluetooth();

    public NavigationDrawerListAdapter(Context context, boolean bluetoothFeature) {
        this.context = context;
        if (!bluetoothFeature) {
            menuItems = NavigationMenu.getMenuWithoutBluetooth();
        } else if (BluetoothState.get().isDevicePaired()) {
            menuItems = NavigationMenu.getConnectedMenu();
        } else if (BluetoothState.get().getDeviceState() == DeviceState.DISCONNECTING) {
            menuItems = NavigationMenu.getDisconnectingMenu();
        } else {
            menuItems = NavigationMenu.getDisconnectedMenu();
        }
    }

    @Override
    public int getCount() {
        return menuItems.length;
    }

    @Override
    public NavigationMenu getItem(int position) {
        return menuItems[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.cell_navigation_drawer_item, parent, false);
            holder = new ViewHolder();
            holder.itemNameTextView = convertView.findViewById(R.id.cell_navigation_drawer_item_name_tv);
            holder.rightView = convertView.findViewById(R.id.cell_navigation_drawer_right_ll);
            holder.viewTop = convertView.findViewById(R.id.viewTop);
            holder.viewBottom = convertView.findViewById(R.id.viewBottom);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        NavigationMenu menuItem = getItem(position);
        holder.itemNameTextView.setText(menuItem.getTitleResId());
//        holder.itemNameTextView.setPadding(32, 4, 12, 4);
//        if (menuItem.equals(NavigationMenu.GETTING_STARTED)) {
//            holder.itemNameTextView.setTextSize(16);
//            holder.itemNameTextView.setPadding(32, 32, 12, 32);
//            holder.viewTop.setVisibility(View.GONE);
//            holder.viewBottom.setVisibility(View.VISIBLE);
//        } else
        if (menuItem.equals(NavigationMenu.EVENTS)) {
            String mEventCounts = String.valueOf(EventsManager.instance().getNumberOfActiveEvents());
            if (!mEventCounts.equals("") && !mEventCounts.equals("0")) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.gravity = Gravity.END;
                TextView notificationsTv = new TextView(context);
                notificationsTv.setBackgroundResource(R.drawable.red_notification);
                notificationsTv.setTextColor(Color.WHITE);
                notificationsTv.setGravity(Gravity.CENTER);
                notificationsTv.setLayoutParams(params);
                notificationsTv.setText(String.valueOf(EventsManager.instance().getNumberOfActiveEvents()));
                if (holder.rightView.getChildCount() > 0) {
                    holder.rightView.removeAllViews();
                }
                holder.rightView.addView(notificationsTv);
                holder.rightView.setVisibility(View.VISIBLE);
            } else {
                holder.rightView.setVisibility(View.GONE);
            }
//            holder.viewTop.setVisibility(View.GONE);
//            holder.viewBottom.setVisibility(View.GONE);
        }
//        else if (menuItem.equals(NavigationMenu.GUARDIAN_NETWORK)) {
//            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            params.gravity = Gravity.END;
//            ImageView safeletCommunityIv = new ImageView(context);
//            safeletCommunityIv.setImageResource(R.drawable.check_mark);
//            safeletCommunityIv.setLayoutParams(params);
//            if (holder.rightView.getChildCount() > 0) {
//                holder.rightView.removeAllViews();
//            }
//            holder.rightView.addView(safeletCommunityIv);
//            if (UserManager.instance().getUserModel().isCommunityMember()) {
//                safeletCommunityIv.setVisibility(View.VISIBLE);
//            } else {
//                safeletCommunityIv.setVisibility(View.GONE);
//            }
//            holder.viewTop.setVisibility(View.GONE);
//            holder.viewBottom.setVisibility(View.GONE);
//        }
        else if (menuItem.equals(NavigationMenu.CONNECT_SAFELET) || menuItem.equals(NavigationMenu.DISCONNECT_SAFELET)) {
//            holder.itemNameTextView.setTextColor(0xFF287aa9);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.END;
            ImageView bluetoothIv = new ImageView(context);
            bluetoothIv.setImageResource(R.drawable.check_mark);
            bluetoothIv.setLayoutParams(params);
            if (holder.rightView.getChildCount() > 0) {
                holder.rightView.removeAllViews();
            }
            holder.rightView.addView(bluetoothIv);
            bluetoothIv.setVisibility(View.GONE);
            holder.rightView.setVisibility(View.VISIBLE);
//            holder.viewTop.setVisibility(View.GONE);
//            holder.viewBottom.setVisibility(View.GONE);
        }
//        else if (menuItem.equals(NavigationMenu.ALARM)) {
//            holder.itemNameTextView.setTextColor(context.getResources().getColor(R.color.safelet_red));
//        }
        else if (menuItem.equals(NavigationMenu.SAFELET_STATUS_AREA)) {
//            holder.itemNameTextView.setTextSize(16);
//            holder.itemNameTextView.setPadding(32, 32, 12, 32);
//            holder.viewTop.setVisibility(View.VISIBLE);
//            holder.viewBottom.setVisibility(View.GONE);
            holder.itemNameTextView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.black));
            holder.itemNameTextView.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            holder.rightView.setVisibility(View.GONE);
        } else {
//            holder.viewTop.setVisibility(View.GONE);
//            holder.viewBottom.setVisibility(View.GONE);
            holder.itemNameTextView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.white));
            holder.itemNameTextView.setTextColor(ContextCompat.getColor(context, android.R.color.black));
            holder.rightView.setVisibility(View.GONE);
        }

//        // Second and Last Second position add padding
//        if (position == 1) {
//            holder.itemNameTextView.setPadding(32, 32, 12, 4);
//        } else if (position == menuItems.length - 2) {
//            holder.itemNameTextView.setPadding(32, 4, 12, 32);
//        }

        return convertView;
    }

    public void refreshMenu(boolean bluetoothFeature) {
        if (!bluetoothFeature) {
            menuItems = NavigationMenu.getMenuWithoutBluetooth();
        } else if (BluetoothState.get().isDevicePaired()) {
            menuItems = NavigationMenu.getConnectedMenu();
        } else if (BluetoothState.get().getDeviceState() == DeviceState.DISCONNECTING) {
            menuItems = NavigationMenu.getDisconnectingMenu();
        } else {
            menuItems = NavigationMenu.getDisconnectedMenu();
        }
    }

    private class ViewHolder {
        private TextView itemNameTextView;
        private LinearLayout rightView;
        private View viewTop, viewBottom;
    }
}
