package activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
//import android.support.v4.app.ActivityCompat
import androidx.core.app.ActivityCompat
import android.view.View
import com.polidea.rxandroidble.RxBleClient
import com.safelet.android.R
import com.safelet.android.global.ApplicationSafelet
import rx.Subscription

class PairSafeletWizard01Welcome : Activity(), View.OnClickListener {

    private val rxBleClient by lazy { ApplicationSafelet.getBleClient() }

    private lateinit var subscription: Subscription

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pair_safelet_wizard01_welcome)

        findViewById<View>(R.id.btnCancelPairWizardWelcome).setOnClickListener(this)
        findViewById<View>(R.id.btnNextPairWizardWelcome).setOnClickListener(this)
    }

    override fun onStart() {
        super.onStart()

        subscription = rxBleClient.observeStateChanges().subscribe { state -> handleState(state) }
    }

    override fun onStop() {
        super.onStop()

        subscription.unsubscribe()
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btnCancelPairWizardWelcome) {
            finish()
        } else if (v.id == R.id.btnNextPairWizardWelcome) {
            handleState(rxBleClient.state)
        }
    }

    private fun handleState(state: RxBleClient.State) {
        when (state) {
            RxBleClient.State.READY -> {
                subscription.unsubscribe()
                startActivity(Intent(this@PairSafeletWizard01Welcome, PairSafeletWizard03SearchForSafelet::class.java))
                finish()
            }
            RxBleClient.State.LOCATION_PERMISSION_NOT_GRANTED -> {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
            }
            RxBleClient.State.BLUETOOTH_NOT_ENABLED -> {
                subscription.unsubscribe()
                startActivity(Intent(this@PairSafeletWizard01Welcome, PairSafeletWizard02Bluetooth::class.java))
                finish()
            }
            RxBleClient.State.LOCATION_SERVICES_NOT_ENABLED -> {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            RxBleClient.State.BLUETOOTH_NOT_AVAILABLE -> {
                finish()
            }
        }
    }
}
