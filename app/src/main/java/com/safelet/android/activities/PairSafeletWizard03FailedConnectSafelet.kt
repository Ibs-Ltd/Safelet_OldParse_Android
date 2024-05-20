package activities

import android.app.Activity
import android.os.Bundle
import android.view.View

import com.safelet.android.R
import com.safelet.android.services.BluetoothLogService

class PairSafeletWizard03FailedConnectSafelet : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startService(BluetoothLogService.getUploadBluetoothLogIntent(this))

        setContentView(R.layout.activity_pair_safelet_wizard03_failed)
        findViewById<View>(R.id.okButton).setOnClickListener { this@PairSafeletWizard03FailedConnectSafelet.finish() }
    }

}
