package bluetooth.base

import android.content.Context
import com.google.firebase.installations.FirebaseInstallations
import com.polidea.rxandroidble.RxBleConnection
import com.polidea.rxandroidble.RxBleDevice
import com.polidea.rxandroidble.helpers.ValueInterpreter
import com.safelet.android.interactors.EventBusManager
import com.safelet.android.models.event.StartAlarmEvent
import com.safelet.android.models.event.StopAlarmEvent
import com.safelet.android.models.event.bluetooth.DeviceConnectionStateChangedEvent
import com.safelet.android.utils.log.BluetoothLog
import global.BluetoothState
import models.enums.DeviceState
import rx.Observable
import java.nio.charset.Charset
import java.util.*

abstract class DeviceController(
    val context: Context,
    device: RxBleDevice,
    val connection: RxBleConnection
) : BatteryController(device, connection) {

    protected val instanceId: String by lazy { FirebaseInstallations.getInstance().id.toString() }

    protected val defaultCharset: Charset by lazy { Charset.forName(ASCII)!! }

    init {
        getDeviceRelated(connection).flatMap { isRelated ->
            BluetoothLog.writeLog(TAG, "Device related: $isRelated")
            if (isRelated) {
                connection.readCharacteristic(relationCharacteristic)
            } else {
                connection.writeCharacteristic(
                    relationCharacteristic,
                    relationId.toByteArray(defaultCharset)
                )
            }
        }.subscribe({ bytes ->
            val identifier =
                ValueInterpreter.getStringValue(bytes.filter { byte -> byte > 0 }.toByteArray(), 0)
            BluetoothLog.writeLog(TAG, "Identifier: $identifier relationId: $relationId")
            if (relationId == identifier) {
                onConnected(connection)
            } else {
                // this device cannot be used anymore
                BluetoothState.get().macAddress = null
            }
        }, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
    }

    protected open fun onConnected(connection: RxBleConnection) {
        BluetoothLog.writeLog(TAG, "Connection established")
        BluetoothState.get().deviceState = DeviceState.CONNECTED
        EventBusManager.instance().postEvent(DeviceConnectionStateChangedEvent(true))
        connection.setupNotification(emergencyCharacteristic).flatMap { notificationObservable ->
            notificationObservable
        }.takeUntil(device.observeConnectionStateChanges().publish { selector ->
            selector.filter { connectionState ->
                connectionState == RxBleConnection.RxBleConnectionState.DISCONNECTING ||
                        connectionState == RxBleConnection.RxBleConnectionState.DISCONNECTED
            }.take(1)
        }).doOnCompleted {
            BluetoothLog.writeLog(TAG, "Emergency subscription completed")
        }.subscribe({ bytes ->
            if (ValueInterpreter.getIntValue(bytes, ValueInterpreter.FORMAT_UINT8, 0) == 1) {
                startAlarm(connection)
            } else {
                stopAlarm()
            }
        }, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
    }

    protected open fun startAlarm(connection: RxBleConnection) {
        BluetoothLog.writeLog(TAG, "Starting alarm")
        EventBusManager.instance().postEvent(StartAlarmEvent())
    }

    private fun stopAlarm() {
        BluetoothLog.writeLog(TAG, "Stopping alarm")
        EventBusManager.instance().postEvent(StopAlarmEvent())
    }

    abstract fun getDeviceRelated(connection: RxBleConnection): Observable<Boolean>

    abstract fun update(data: ByteArray)

    abstract fun alarmStarted()

    abstract fun alarmStopped()

    abstract fun guardianJoined(name: String)

    abstract fun reset(): Observable<Boolean>

    protected abstract val relationCharacteristic: UUID

    protected abstract val modeCharacteristic: UUID

    protected abstract val emergencyCharacteristic: UUID

    protected abstract val relationId: String

    companion object {
        private val TAG = DeviceController::class.java.simpleName

        private val ASCII = "ASCII"
    }
}