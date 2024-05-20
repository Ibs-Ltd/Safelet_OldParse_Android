package bluetooth

import android.content.Context
import bluetooth.base.DeviceController
import com.polidea.rxandroidble.RxBleConnection
import com.polidea.rxandroidble.RxBleDevice
import com.safelet.android.interactors.EventBusManager
import com.safelet.android.models.event.UpdateServiceNotificationEvent
import com.safelet.android.utils.log.BluetoothLog
import global.BluetoothState
import rx.Observable
import java.util.*

class SafeletController(context: Context, device: RxBleDevice, connection: RxBleConnection) : DeviceController(context, device, connection) {

    override fun onConnected(connection: RxBleConnection) {
        super.onConnected(connection)
        connection.readCharacteristic(UUID_MODE_CHARACTERISTIC).subscribe({ bytes ->
            if (bytes.single() == STATE_EMERGENCY) {
                startAlarm(connection)
            }
        }, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
    }

    override fun update(data: ByteArray) {
        var updatePosition = 0
        connection.writeCharacteristic(UUID_MODE_CHARACTERISTIC, byteArrayOf(STATE_UPDATE)).subscribe({ _ ->
            connection.createNewLongWriteBuilder().setCharacteristicUuid(UUID_UPDATE_CHARACTERISTIC)
                    .setBytes(data).setMaxBatchSize(BATCH_SIZE).setWriteOperationAckStrategy { item ->
                item.doOnNext {
                    updatePosition = Math.min(updatePosition + BATCH_SIZE, data.size)
                    BluetoothState.get().updatePercentage = (updatePosition * 100f / data.size).toInt()
                    EventBusManager.instance().postEvent(UpdateServiceNotificationEvent())
                }
            }.build().subscribe({ _ ->
                BluetoothState.get().updatePercentage = 0
                EventBusManager.instance().postEvent(UpdateServiceNotificationEvent())
                connection.writeCharacteristic(UUID_MODE_CHARACTERISTIC, byteArrayOf(STATE_RESET)).subscribe({}, { throwable ->
                    BluetoothLog.writeThrowable(TAG, throwable)
                })
            }, { throwable ->
                BluetoothLog.writeThrowable(TAG, throwable)
            })
        }, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
    }

    override fun getDeviceRelated(connection: RxBleConnection): Observable<Boolean> {
        return connection.readCharacteristic(UUID_RELATION_CHARACTERISTIC).map { bytes ->
            bytes.any { byte -> byte > 0 }
        }
    }

    override fun alarmStarted() {
        vibrate()
    }

    override fun alarmStopped() {
        connection.writeCharacteristic(UUID_MODE_CHARACTERISTIC, byteArrayOf(STATE_CANCEL_EMERGENCY)).subscribe({}, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
    }

    override fun guardianJoined(name: String) {
        vibrate()
    }

    override fun reset(): Observable<Boolean> {
        return connection.writeCharacteristic(UUID_RELATION_CHARACTERISTIC, byteArrayOf(0)).map { _ -> true }
    }

    private fun vibrate() {
        connection.writeCharacteristic(UUID_VIBRATION_CHARACTERISTIC, byteArrayOf(1)).subscribe({}, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
    }

    override val relationCharacteristic: UUID
        get() = UUID_RELATION_CHARACTERISTIC

    override val modeCharacteristic: UUID
        get() = UUID_MODE_CHARACTERISTIC

    override val emergencyCharacteristic: UUID
        get() = UUID_EMERGENCY_CHARACTERISTIC

    override val batteryCharacteristic: UUID
        get() = UUID_BATTERY_CHARACTERISTIC

    override val relationId: String
        get() = instanceId.padEnd(16, '*').substring(0, 16)

    companion object {
        private val TAG = SafeletController::class.java.simpleName
        private val UUID_MODE_CHARACTERISTIC = UUID.fromString("C0695A31-FF11-81A7-8441-DBEE27470FD3")
        private val UUID_RELATION_CHARACTERISTIC = UUID.fromString("F9AC1281-66E2-1DDE-0921-92B490F10A54")
        private val UUID_EMERGENCY_CHARACTERISTIC = UUID.fromString("1C52356C-7D50-7D9B-ED47-1D607B39650F")
        private val UUID_UPDATE_CHARACTERISTIC = UUID.fromString("95503EB8-F283-CFB6-8045-54501B3E54DD")
        private val UUID_VIBRATION_CHARACTERISTIC = UUID.fromString("E34369EE-FCAB-11E5-86AA-5E5517507C66")
        private val UUID_BATTERY_CHARACTERISTIC = UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB")

        private val STATE_EMERGENCY: Byte = 2
        private val STATE_CANCEL_EMERGENCY: Byte = 4
        private val STATE_UPDATE: Byte = 5
        private val STATE_RESET: Byte = 6

        private val BATCH_SIZE = 20
    }
}