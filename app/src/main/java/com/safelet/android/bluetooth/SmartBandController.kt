package bluetooth

import android.content.Context
import android.text.TextUtils
import android.text.format.DateUtils
import bluetooth.base.DeviceController
import com.polidea.rxandroidble.RxBleConnection
import com.polidea.rxandroidble.RxBleDevice
import com.safelet.android.R
import com.safelet.android.global.Utils
import com.safelet.android.interactors.EventBusManager
import com.safelet.android.models.event.UpdateServiceNotificationEvent
import com.safelet.android.services.DfuService
import com.safelet.android.utils.log.BluetoothLog
import global.BluetoothState
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import utils.extension.toByteArray
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.math.BigInteger
import java.util.*

class SmartBandController(context: Context, device: RxBleDevice, connection: RxBleConnection) : DeviceController(context, device, connection) {

    override fun onConnected(connection: RxBleConnection) {
        super.onConnected(connection)
        val time = ((System.currentTimeMillis() + TimeZone.getDefault().getOffset(System.currentTimeMillis())) / DateUtils.SECOND_IN_MILLIS).toInt()
        val data = time.toByteArray()
        data.reverse()
        connection.writeCharacteristic(UUID_TIME_CHARACTERISTIC, data).subscribe({}, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
    }

    override fun getDeviceRelated(connection: RxBleConnection): Observable<Boolean> {
        return connection.readCharacteristic(modeCharacteristic).map { bytes ->
            bytes.single() != STATE_OPEN_RELATION
        }
    }

    override fun startAlarm(connection: RxBleConnection) {
        super.startAlarm(connection)
        connection.writeCharacteristic(UUID_EMERGENCY_CHARACTERISTIC, byteArrayOf(0)).subscribe({}, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
        vibrate()
    }

    override fun update(data: ByteArray) {
        BluetoothState.get().isReconnectEnabled = false
        val macAddress = device.macAddress
        device.observeConnectionStateChanges().filter { state -> state == RxBleConnection.RxBleConnectionState.DISCONNECTED }.take(1).subscribe({
            BluetoothLog.writeLog(TAG, "Preparing DFU update")

            DfuServiceListenerHelper.registerProgressListener(context, DfuUpdateListener(context))

            val updateFile = File(context.cacheDir, UPDATE_FILE_NAME)

            Observable.just(updateFile).subscribeOn(Schedulers.io()).doOnNext { file ->
                BluetoothLog.writeLog(TAG, "Witting DFU update to file")
                val outputStream = BufferedOutputStream(FileOutputStream(file))
                outputStream.write(data)
                outputStream.close()
            }.observeOn(AndroidSchedulers.mainThread()).subscribe({ file ->
                BluetoothLog.writeLog(TAG, "Starting DFU update")
                val dfuAddress = Utils.convertMacAddress(BigInteger(Utils.convertMacAddress(macAddress)).add(BigInteger.ONE).toByteArray())

                val starter = DfuServiceInitiator(dfuAddress)
                starter.setDisableNotification(true)
                starter.setZip(file.absolutePath)
                starter.start(context, DfuService::class.java)
            }, { throwable ->
                BluetoothLog.writeThrowable(TAG, throwable)
                BluetoothState.get().isReconnectEnabled = true
            })
        }, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
        connection.writeCharacteristic(UUID_MODE_CHARACTERISTIC, byteArrayOf(STATE_UPDATE)).subscribe({}, {})
    }

    override fun alarmStarted() {
        showText(connection, context.getString(R.string.alarm_sent))
    }

    override fun alarmStopped() {
    }

    override fun guardianJoined(name: String) {
        showText(connection, context.getString(R.string.guardian_on_his_way, name))
        vibrate()
    }

    override fun reset(): Observable<Boolean> {
        return connection.writeCharacteristic(UUID_MODE_CHARACTERISTIC, byteArrayOf(STATE_RESET)).map { _ -> true }
    }

    private fun vibrate() {
        connection.writeCharacteristic(UUID_VIBRATION_CHARACTERISTIC, byteArrayOf(0x01)).subscribe({}, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
    }

    private fun showText(connection: RxBleConnection, message: String) {
        val split = message.trim().split(" +".toRegex())
        val firstLine = TextUtils.join(" ", split.subList(0, split.size / 2 + 1))
        val secondLIne = TextUtils.join(" ", split.subList(split.size / 2 + 1, split.size))
        connection.writeCharacteristic(UUID_FIRST_LINE_CHARACTERISTIC, firstLine.toByteArray(defaultCharset)).subscribe({}, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
        connection.writeCharacteristic(UUID_SECOND_LINE_CHARACTERISTIC, secondLIne.toByteArray(defaultCharset)).subscribe({}, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
        connection.writeCharacteristic(UUID_DISPLAY_CHARACTERISTIC, byteArrayOf(10)).subscribe({}, { throwable ->
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
        get() = instanceId.padEnd(6, '*').substring(0, 6)

    companion object {
        private val TAG = SmartBandController::class.java.simpleName

        private val UUID_MODE_CHARACTERISTIC = UUID.fromString("00002E00-0000-1000-8000-00805F9B34FB")
        private val UUID_RELATION_CHARACTERISTIC = UUID.fromString("00002E01-0000-1000-8000-00805f9b34fb")
        private val UUID_EMERGENCY_CHARACTERISTIC = UUID.fromString("00002E04-0000-1000-8000-00805F9B34FB")
        private val UUID_VIBRATION_CHARACTERISTIC = UUID.fromString("00002E02-0000-1000-8000-00805F9B34FB")
        private val UUID_BATTERY_CHARACTERISTIC = UUID.fromString("00002E05-0000-1000-8000-00805F9B34FB")
        private val UUID_TIME_CHARACTERISTIC = UUID.fromString("00002E0B-0000-1000-8000-00805F9B34FB")
        private val UUID_FIRST_LINE_CHARACTERISTIC = UUID.fromString("00002E08-0000-1000-8000-00805F9B34FB")
        private val UUID_SECOND_LINE_CHARACTERISTIC = UUID.fromString("00002E09-0000-1000-8000-00805F9B34FB")
        private val UUID_DISPLAY_CHARACTERISTIC = UUID.fromString("00002E07-0000-1000-8000-00805F9B34FB")

        private val STATE_OPEN_RELATION: Byte = 1
        private val STATE_UPDATE: Byte = 5
        private val STATE_RESET: Byte = 6

        private val UPDATE_FILE_NAME = "DFUOTAFW.zip"
    }

    private inner class DfuUpdateListener(private val context: Context) : DfuProgressListenerAdapter() {

        override fun onEnablingDfuMode(deviceAddress: String?) {
            BluetoothLog.writeLog(TAG, "DFU onEnablingDfuMode() address = " + deviceAddress!!)
        }

        override fun onProgressChanged(deviceAddress: String?, percent: Int, speed: Float, avgSpeed: Float, currentPart: Int, partsTotal: Int) {
            BluetoothLog.writeLog(TAG, "DFU onProgressChanged() address = $deviceAddress percent = $percent")
            BluetoothState.get().updatePercentage = percent
            EventBusManager.instance().postEvent(UpdateServiceNotificationEvent())
        }

        override fun onFirmwareValidating(deviceAddress: String?) {
            BluetoothLog.writeLog(TAG, "DFU onFirmwareValidating() address = " + deviceAddress!!)
        }

        override fun onDeviceDisconnecting(deviceAddress: String?) {
            BluetoothLog.writeLog(TAG, "DFU onDeviceDisconnecting() address = " + deviceAddress!!)
        }

        override fun onDeviceDisconnected(deviceAddress: String?) {
            BluetoothLog.writeLog(TAG, "DFU onDeviceDisconnected() address = " + deviceAddress!!)
        }

        override fun onDeviceConnecting(deviceAddress: String?) {
            BluetoothLog.writeLog(TAG, "DFU onDeviceConnecting() address = " + deviceAddress!!)
        }

        override fun onDeviceConnected(deviceAddress: String?) {
            BluetoothLog.writeLog(TAG, "DFU onDeviceConnected() address = " + deviceAddress!!)
        }

        override fun onDfuProcessStarting(deviceAddress: String?) {
            BluetoothLog.writeLog(TAG, "DFU onDfuProcessStarting() address = " + deviceAddress!!)
        }

        override fun onDfuProcessStarted(deviceAddress: String?) {
            BluetoothLog.writeLog(TAG, "DFU onDfuProcessStarted() address = " + deviceAddress!!)
        }

        override fun onDfuCompleted(deviceAddress: String?) {
            BluetoothLog.writeLog(TAG, "DFU onDfuCompleted() address = " + deviceAddress!!)
            BluetoothState.get().isReconnectEnabled = true
            BluetoothState.get().updatePercentage = 0
            EventBusManager.instance().postEvent(UpdateServiceNotificationEvent())
            DfuServiceListenerHelper.unregisterProgressListener(context, this)
        }

        override fun onDfuAborted(deviceAddress: String?) {
            BluetoothLog.writeLog(TAG, "DFU onDfuAborted() address = " + deviceAddress!!)
            BluetoothState.get().isReconnectEnabled = true
            BluetoothState.get().updatePercentage = 0
            EventBusManager.instance().postEvent(UpdateServiceNotificationEvent())
            DfuServiceListenerHelper.unregisterProgressListener(context, this)
        }

        override fun onError(deviceAddress: String?, error: Int, errorType: Int, message: String?) {
            BluetoothLog.writeLog(TAG, "DFU onError() address = $deviceAddress error = $error errorType = $errorType message = $message")
            BluetoothState.get().isReconnectEnabled = true
            BluetoothState.get().updatePercentage = 0
            EventBusManager.instance().postEvent(UpdateServiceNotificationEvent())
            DfuServiceListenerHelper.unregisterProgressListener(context, this)
        }
    }
}