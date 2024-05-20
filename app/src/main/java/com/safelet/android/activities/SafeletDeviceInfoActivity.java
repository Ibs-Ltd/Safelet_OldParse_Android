package com.safelet.android.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.widget.Toolbar;

import com.safelet.android.R;
import com.safelet.android.activities.base.BaseActivity;
import com.safelet.android.adapters.DeviceInfoListAdapter;
import com.safelet.android.global.PreferencesManager;
import com.safelet.android.views.cells.TwoRowItem;

import java.util.ArrayList;

/**
 * This activity shows all the retrieved from the device and locally stored information of the last
 * connected BLE device.
 * <p/>
 * Created by Reinier on 2-4-2015.
 */
public class SafeletDeviceInfoActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);

        Toolbar toolbar = (Toolbar) findViewById(R.id.safelet_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        setTitle(R.string.device_info_title);

        // Retrieving the list
        ListView deviceInfoListview = (ListView) findViewById(R.id.deviceInfoListview);

        // Our container
        ArrayList<TwoRowItem> deviceInfoItems = new ArrayList<>();

        // Filling the list with locally stored information
        String manufacturer = PreferencesManager.instance(this).getString(PreferencesManager.DEVICE_MANUFACTURER_NAME, "-");
        String modelnumber = PreferencesManager.instance(this).getString(PreferencesManager.DEVICE_MODEL_NUMBER, "-");
        String hardware = PreferencesManager.instance(this).getString(PreferencesManager.DEVICE_HARDWARE_REVISION, "-");
        String firmware = PreferencesManager.instance(this).getString(PreferencesManager.DEVICE_FIRMWARE_REVISION, "-");
        int firmwareNumber = PreferencesManager.instance(this).getInt(PreferencesManager.DEVICE_FIRMWARE_VERSION_CODE);

        // Create our collection
        deviceInfoItems.add(new TwoRowItem(getString(R.string.device_info_category_manufacturer), manufacturer));
        deviceInfoItems.add(new TwoRowItem(getString(R.string.device_info_category_model_number), modelnumber));
        deviceInfoItems.add(new TwoRowItem(getString(R.string.device_info_category_hardware_revision), hardware));
        deviceInfoItems.add(new TwoRowItem(getString(R.string.device_info_category_firmware_revision), firmware + " (" + firmwareNumber + ")"));

        // Attach our collection to the listview
        DeviceInfoListAdapter adapter = new DeviceInfoListAdapter(this, deviceInfoItems);
        deviceInfoListview.setAdapter(adapter);
    }
}
