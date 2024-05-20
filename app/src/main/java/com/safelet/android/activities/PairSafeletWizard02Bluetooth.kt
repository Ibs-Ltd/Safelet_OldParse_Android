package activities

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.polidea.rxandroidble.RxBleClient
import com.safelet.android.R
import com.safelet.android.global.ApplicationSafelet
import com.safelet.android.utils.PermissionManager
import rx.Subscription

class PairSafeletWizard02Bluetooth : Activity() {

    private val rxBleClient by lazy { ApplicationSafelet.getBleClient() }

    private lateinit var subscription: Subscription

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pair_safelet_wizard02_bluetooth)

        val buttonCancel = findViewById<Button>(R.id.btnCancelPairWizardBluetooth)
        buttonCancel.setOnClickListener { finish() }

        val buttonNext = findViewById<Button>(R.id.btnNextPairWizardBluetooth)
        buttonNext.setOnClickListener {
            if (rxBleClient.state == RxBleClient.State.READY) {
                nextActivityInWizard()
            } else {
                Toast.makeText(this@PairSafeletWizard02Bluetooth, R.string.safelet_pair_wizard_no_bluetooth, Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onStart() {
        super.onStart()

        subscription = rxBleClient.observeStateChanges().subscribe { state ->
            if (state == RxBleClient.State.READY) {
                subscription.unsubscribe()
                nextActivityInWizard()
            }
            // Todo This is Old Code Changed By Praveen Sundriyal 12/09/2023
//            else if (state == RxBleClient.State.BLUETOOTH_NOT_ENABLED) {
//                startActivityForResult(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), BLUETOOTH_ENABLE)
//            }
        }
    }

    override fun onStop() {
        super.onStop()
        subscription.unsubscribe()
    }

    /**
     * We start the next activity in this wizard and close the current.
     */
    private fun nextActivityInWizard() {
        startActivity(Intent(this@PairSafeletWizard02Bluetooth, PairSafeletWizard03SearchForSafelet::class.java))
        finish()
    }

    companion object {
        private val BLUETOOTH_ENABLE = 235
    }
}
