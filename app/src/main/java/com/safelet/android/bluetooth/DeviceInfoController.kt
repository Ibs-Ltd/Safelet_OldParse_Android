package bluetooth

import android.content.Context
import bluetooth.base.BatteryController
import com.polidea.rxandroidble.RxBleConnection
import com.polidea.rxandroidble.helpers.ValueInterpreter
import com.safelet.android.global.PreferencesManager
import com.safelet.android.utils.log.BluetoothLog
import java.util.*

class DeviceInfoController(val context: Context, connection: RxBleConnection) {

    init {
        connection.readCharacteristic(UUID_MANUFACTURER_NAME).subscribe({ bytes ->
            saveDeviceInfoToPreferences(
                PreferencesManager.DEVICE_MANUFACTURER_NAME,
                ValueInterpreter.getStringValue(bytes, 0)
            )
        }, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
        connection.readCharacteristic(UUID_MODEL_NUMBER).subscribe({ bytes ->
            saveDeviceInfoToPreferences(
                PreferencesManager.DEVICE_MODEL_NUMBER,
                ValueInterpreter.getStringValue(bytes, 0)
            )
        }, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
        connection.readCharacteristic(UUID_HARDWARE_REVISION).subscribe({ bytes ->
            saveDeviceInfoToPreferences(
                PreferencesManager.DEVICE_HARDWARE_REVISION,
                ValueInterpreter.getStringValue(bytes, 0)
            )
        }, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
        connection.readCharacteristic(UUID_FIRMWARE_REVISION).subscribe({ bytes ->
            saveDeviceInfoToPreferences(
                PreferencesManager.DEVICE_FIRMWARE_REVISION,
                ValueInterpreter.getStringValue(bytes, 0)
            )
        }, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
        connection.readCharacteristic(UUID_FIRMWARE_VERSION_CODE).subscribe({ bytes ->
            saveDeviceInfoToPreferences(
                PreferencesManager.DEVICE_FIRMWARE_VERSION_CODE,
                ValueInterpreter.getIntValue(bytes, ValueInterpreter.FORMAT_UINT32, 0)
            )
        }, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
    }

    private fun saveDeviceInfoToPreferences(storageKey: String, value: String) {
        BluetoothLog.writeLog(TAG, "Saving $storageKey value: $value")
        PreferencesManager.instance(context).setString(storageKey, value)
    }

    private fun saveDeviceInfoToPreferences(storageKey: String, value: Int) {
        BluetoothLog.writeLog(TAG, "Saving $storageKey value: $value")
        PreferencesManager.instance(context).setInt(storageKey, value)
    }

    companion object {
        private val TAG = BatteryController::class.java.simpleName
        private val UUID_MANUFACTURER_NAME = UUID.fromString("00002A29-0000-1000-8000-00805F9B34FB")
        private val UUID_MODEL_NUMBER = UUID.fromString("00002A24-0000-1000-8000-00805F9B34FB")
        private val UUID_HARDWARE_REVISION = UUID.fromString("00002A27-0000-1000-8000-00805F9B34FB")
        private val UUID_FIRMWARE_REVISION = UUID.fromString("00002A26-0000-1000-8000-00805F9B34FB")
        private val UUID_FIRMWARE_VERSION_CODE =
            UUID.fromString("95503EB8-F283-CFB6-8045-54501B3E54DD")
    }
}
