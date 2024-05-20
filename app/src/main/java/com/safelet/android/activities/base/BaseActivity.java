package com.safelet.android.activities.base;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.safelet.android.R;
import com.safelet.android.fragments.BaseFragment;
import com.safelet.android.global.PopDialog;
import com.safelet.android.services.RecordSoundService;
import com.safelet.android.utils.media.AlarmNotificationSoundPlayer;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

/**
 * Base activity to be extended by the activities with action bar.
 * <p/>
 * Created by alin on 13.10.2015.
 */
public abstract class BaseActivity extends AppCompatActivity {

    public static final String TAG = "SafeletApp ";
    public Context mContext;
    private Dialog dialog;

    private BroadcastReceiver recordingStatusChangedBroadcastReceiver;

    public void addFragmentToBackStack(BaseFragment fragment) {
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(base));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = BaseActivity.this;

        recordingStatusChangedBroadcastReceiver = new RecordingStatusChangedBroadcastReceiver();

        LocalBroadcastManager.getInstance(this).registerReceiver(recordingStatusChangedBroadcastReceiver,
                new IntentFilter(RecordSoundService.RECORDING_STATUS_CHANGED));

        // Create loading dialog
        dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
    }

    @Override
    protected void onStart() {
        super.onStart();
        AlarmNotificationSoundPlayer.instance().stopSound();
    }

    @Override
    public void onStop() {
        super.onStop();
        dialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(recordingStatusChangedBroadcastReceiver);
    }

    public void showLoading() {
        dialog.show();
    }

    public void hideLoading() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    private void showRecordingStopAlert() {
        PopDialog.showDialog(this,
                getString(R.string.alert_screen_title),
                getString(R.string.alert_pop_recording_stop),
                PopDialog.TYPE_DISMISS, null
        );
    }

    private class RecordingStatusChangedBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            showRecordingStopAlert();
        }
    }
}
