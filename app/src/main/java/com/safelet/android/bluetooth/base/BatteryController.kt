package bluetooth.base

import com.polidea.rxandroidble.RxBleConnection
import com.polidea.rxandroidble.RxBleDevice
import com.polidea.rxandroidble.helpers.ValueInterpreter
import com.safelet.android.interactors.EventBusManager
import com.safelet.android.models.event.bluetooth.BatteryChangedEvent
import com.safelet.android.utils.log.BluetoothLog
import global.BluetoothState
import java.util.*

abstract class BatteryController(val device: RxBleDevice, connection: RxBleConnection) {

    init {
        connection.readCharacteristic(batteryCharacteristic).subscribe({ bytes ->
            saveBatteryLevel(bytes)
        }, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
        connection.setupNotification(batteryCharacteristic).flatMap({ notificationObservable ->
            notificationObservable
        }).takeUntil(device.observeConnectionStateChanges().publish({ selector ->
            selector.filter { connectionState ->
                connectionState == RxBleConnection.RxBleConnectionState.DISCONNECTING ||
                        connectionState == RxBleConnection.RxBleConnectionState.DISCONNECTED
            }.take(1)
        })).doOnCompleted {
            BluetoothLog.writeLog(TAG, "Battery subscription completed")
        }.subscribe({ bytes ->
            saveBatteryLevel(bytes)
        }, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
    }

    protected abstract val batteryCharacteristic: UUID

    private fun saveBatteryLevel(bytes: ByteArray) {
        val batteryLevel = ValueInterpreter.getIntValue(bytes, ValueInterpreter.FORMAT_SINT8, 0)
        if (batteryLevel == 0) {
            return
        }
        BluetoothLog.writeLog(TAG, "Saving battery value: $batteryLevel")
        BluetoothState.get().batteryLevel = batteryLevel
        EventBusManager.instance().postEvent(BatteryChangedEvent())
    }

    companion object {
        private val TAG = BatteryController::class.java.simpleName
    }
}
