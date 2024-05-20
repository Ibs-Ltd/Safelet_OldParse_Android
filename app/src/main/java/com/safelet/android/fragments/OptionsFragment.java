package com.safelet.android.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.safelet.android.R;
import com.safelet.android.activities.SafeletDeviceInfoActivity;
import com.safelet.android.callback.NavigationDrawerCallbacks;
import com.safelet.android.global.PreferencesManager;
import com.safelet.android.interactors.FirmwareUpdateManager;
import com.safelet.android.models.enums.NavigationMenu;
import com.safelet.android.models.event.NewNotificationEvent;
import com.safelet.android.models.event.UpdateServiceNotificationEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import global.BluetoothState;
import models.enums.DeviceState;

public class OptionsFragment extends BaseFragment implements FirmwareUpdateManager.OnUpdateFirmwareListener, View.OnClickListener {
    private NavigationDrawerCallbacks parentDrawerCallback;
    private ProgressDialog progressDialog;

    private ProgressDialog updateFirmwareDialog;

    @Override
    public int getTitleResId() {
        return R.string.options_title;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            parentDrawerCallback = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Fragment should be inflated in activities that implement NavigationDrawerCallbacks");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        progressDialog = new ProgressDialog(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_options, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.safeletInformationsLayout).setOnClickListener(this);
        view.findViewById(R.id.firmwareUpdateLayout).setOnClickListener(this);
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            ((TextView) view.findViewById(R.id.appVersionTextView)).setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException ignore) {
        }
    }

    @Override
    protected void onViewNotificationDialogClicked(Context context, NewNotificationEvent event) {
        handleNewNotificationEvent(event);
    }

    @Override
    protected void onNavigationDrawerItemSelected(NavigationMenu navigationMenu) {
        parentDrawerCallback.onNavigationDrawerItemSelected(navigationMenu);
    }

    @Override
    public void onUpdateAvailable(byte[] firmware) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        Toast.makeText(getActivity(), getString(R.string.options_firmware_update_process_in_background), Toast.LENGTH_LONG).show();
        FirmwareUpdateManager.instance().sendUpdateSignal(firmware);
    }

    @Override
    public void onNoUpdateAvailable() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        Toast.makeText(getActivity(), getString(R.string.options_firmware_update_no_update_found), Toast.LENGTH_LONG).show();
    }

    private void createFirmwareUpdateProgressDialog() {
        updateFirmwareDialog = new ProgressDialog(getActivity());
        updateFirmwareDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        updateFirmwareDialog.setTitle(getString(R.string.options_firmware_updating_title));
        updateFirmwareDialog.setMessage(getString(R.string.please_wait));
        updateFirmwareDialog.setCancelable(false);
        updateFirmwareDialog.setIndeterminate(false);
        updateFirmwareDialog.show();
    }

    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUpdateNotificationSignal(UpdateServiceNotificationEvent signal) {
        int update = BluetoothState.get().getUpdatePercentage();
        if (update > 0) {
            if (updateFirmwareDialog == null) {
                createFirmwareUpdateProgressDialog();
            }
            updateFirmwareDialog.setProgress(update);
        } else if (updateFirmwareDialog != null) {
            updateFirmwareDialog.dismiss();
            updateFirmwareDialog = null;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.safeletInformationsLayout) {
            Intent deviceInfoIntent = new Intent(getActivity(), SafeletDeviceInfoActivity.class);
            startActivity(deviceInfoIntent);
        } else if (v.getId() == R.id.firmwareUpdateLayout) {
            onUpdateLayoutClicked();
        }
    }


    private void onUpdateLayoutClicked() {
        if (!BluetoothState.get().isDevicePaired()) {
            Toast.makeText(getActivity(), getString(R.string.options_firmware_update_no_safelet_used), Toast.LENGTH_LONG).show();
        } else if (BluetoothState.get().getDeviceState() != DeviceState.CONNECTED) {
            Toast.makeText(getActivity(), getString(R.string.options_firmware_update_no_safelet_connection), Toast.LENGTH_LONG).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getString(R.string.options_firmware_update_title));
            builder.setMessage(getString(R.string.options_firmware_update_are_you_sure));
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    OptionsFragment.this.progressDialog.setMessage(getString(R.string.options_firmware_update_status_checking));
                    OptionsFragment.this.progressDialog.show();
                    String modelNumber = PreferencesManager.instance(getActivity()).getString(PreferencesManager.DEVICE_MODEL_NUMBER);
                    String firmwareRevision = PreferencesManager.instance(getActivity()).getString(PreferencesManager.DEVICE_FIRMWARE_REVISION);
                    String hardwareRevision = PreferencesManager.instance(getActivity()).getString(PreferencesManager.DEVICE_HARDWARE_REVISION);
                    int firmwareVersionCode = PreferencesManager.instance(getActivity()).getInt(PreferencesManager.DEVICE_FIRMWARE_VERSION_CODE);
                    FirmwareUpdateManager.instance().checkForFirmwareUpdate(modelNumber, hardwareRevision, firmwareRevision,
                            String.valueOf(firmwareVersionCode), OptionsFragment.this);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
