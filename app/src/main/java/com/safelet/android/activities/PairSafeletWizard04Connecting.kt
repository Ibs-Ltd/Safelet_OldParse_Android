package activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.safelet.android.R
import com.safelet.android.activities.PairSafeletWizard05Connected
import com.safelet.android.interactors.EventBusManager
import com.safelet.android.models.event.bluetooth.DeviceConnectionStateChangedEvent
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class PairSafeletWizard04Connecting : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        EventBusManager.instance().register(this)

        setContentView(R.layout.activity_pair_safelet_wizard04_connecting)
    }

    override fun onBackPressed() {
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBusManager.instance().unRegister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onSafeletConnectionChanged(event: DeviceConnectionStateChangedEvent) {
        if (event.isConnected) {
            nextActivityInWizard()
        }
    }

    /**
     * We start the next activity in this wizard and close the current.
     */
    private fun nextActivityInWizard() {
        startActivity(Intent(this, PairSafeletWizard05Connected::class.java))
        finish()
    }

    companion object {
        private val TAG = PairSafeletWizard04Connecting::class.java.simpleName
    }
}
