package activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.polidea.rxandroidble.scan.ScanFilter
import com.polidea.rxandroidble.scan.ScanSettings
import com.safelet.android.R
import com.safelet.android.global.ApplicationSafelet
import com.safelet.android.utils.log.BluetoothLog
import global.BluetoothState
import models.enums.DeviceType
import rx.Subscription

class PairSafeletWizard03SearchForSafelet : Activity() {

    private val rxBleClient by lazy { ApplicationSafelet.getBleClient() }

    private lateinit var scanningSubscription: Subscription

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_pair_safelet_wizard03_searching_for_safelet)

        // Handle the cancel button
        val buttonCancel = findViewById<Button>(R.id.btnCancelPairWizardSearching)
        buttonCancel.setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()

        scanningSubscription = rxBleClient.scanBleDevices(
                ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build(),
                ScanFilter.Builder().setDeviceName(DeviceType.SAFELET.getOpenConnectionName()).build(),
                ScanFilter.Builder().setDeviceName(DeviceType.SMART_BAND.getOpenConnectionName()).build()
        ).take(1).subscribe({ scanResult ->
            BluetoothLog.writeLog(TAG, "Scan result name = " + scanResult.bleDevice.name + " address = " + scanResult.bleDevice.macAddress)
            BluetoothState.get().deviceType = DeviceType.fromConnectionName(scanResult.bleDevice.name)
            BluetoothState.get().macAddress = scanResult.bleDevice.macAddress
            nextActivityInWizard()
        }, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
            startActivity(Intent(this, PairSafeletWizard03FailedConnectSafelet::class.java))
            finish()
        })
    }

    override fun onStop() {
        super.onStop()
        scanningSubscription.unsubscribe()
    }

    /**
     * We start the next activity in this wizard and close the current.
     */
    private fun nextActivityInWizard() {
        startActivity(Intent(this, PairSafeletWizard04Connecting::class.java))
        finish()
    }

    companion object {
        private val TAG = PairSafeletWizard03SearchForSafelet::class.java.simpleName
    }
}
