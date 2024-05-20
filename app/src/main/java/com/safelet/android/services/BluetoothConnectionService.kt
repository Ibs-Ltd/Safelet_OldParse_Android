package services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.text.TextUtils
import bluetooth.DeviceInfoController
import bluetooth.SafeletController
import bluetooth.SmartBandController
import bluetooth.base.DeviceController
import com.polidea.rxandroidble.BuildConfig
import com.polidea.rxandroidble.RxBleClient
import com.polidea.rxandroidble.RxBleConnection
import com.polidea.rxandroidble.RxBleDevice
import com.polidea.rxandroidble.internal.RxBleLog
import com.polidea.rxandroidble.scan.ScanFilter
import com.polidea.rxandroidble.scan.ScanSettings
import com.safelet.android.global.ApplicationSafelet
import com.safelet.android.interactors.EventBusManager
import com.safelet.android.models.event.AlarmStartEvent
import com.safelet.android.models.event.AlarmStopEvent
import com.safelet.android.models.event.GuardianJoinedEvent
import com.safelet.android.models.event.bluetooth.DeviceConnectionStateChangedEvent
import com.safelet.android.models.event.bluetooth.FirmwareUpdateEvent
import com.safelet.android.services.BluetoothLogService
import com.safelet.android.utils.log.BluetoothLog
import global.BluetoothState
import models.enums.DeviceState
import models.enums.DeviceType
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import rx.subjects.BehaviorSubject


class BluetoothConnectionService : Service() {


//    var isBt: Boolean = false
//    var isReconnecting: Boolean = true
    var handler: Handler? = null
    var runnable: Runnable? = null
    var context: Context = this


    private val rxBleClient by lazy { ApplicationSafelet.getBleClient() }

    private val bleState by lazy { BehaviorSubject.create<RxBleClient.State>(rxBleClient.state) }

    private val macAddress by lazy { BehaviorSubject.create<String>(BluetoothState.get().macAddress) }

    private var deviceInfoController: DeviceInfoController? = null

    private var deviceController: DeviceController? = null

    private var stateSubscription: Subscription? = null

    private var connectionSubscription: Subscription? = null

    init {
        RxBleClient.setLogLevel(if (BuildConfig.DEBUG) RxBleLog.DEBUG else RxBleLog.NONE)
    }

    override fun onCreate() {
        super.onCreate()
        BluetoothLog.writeLog(TAG, "Service started")

        EventBusManager.instance().register(this)

        rxBleClient.observeStateChanges().subscribe(bleState)
        BluetoothState.get().observableMacAddress().subscribe(macAddress)

        Observable.combineLatest(macAddress, bleState.distinctUntilChanged()) { macAddress, state ->
            Pair(macAddress, state)
        }.serialize().observeOn(AndroidSchedulers.mainThread()).subscribe { info ->
            BluetoothLog.writeLog(TAG, "Mac address: " + info.first + " state: " + info.second)

            val canConnect = !TextUtils.isEmpty(info.first) && info.second == RxBleClient.State.READY
            /*if (canConnect) {
                isReconnecting = true
                isBt = true
            }else{
                isBt = false
            }*/
            if (BluetoothState.get().deviceState == DeviceState.CONNECTING ||
                    BluetoothState.get().deviceState == DeviceState.CONNECTED) {
                stopConnection(!canConnect)
            } else if (BluetoothState.get().deviceState == DeviceState.SCANNING) {
                BluetoothState.get().deviceState = DeviceState.DISCONNECTED
                EventBusManager.instance().postEvent(DeviceConnectionStateChangedEvent(false))
                val bleDevice = rxBleClient.getBleDevice(info.first!!)
                observeConnectionState(bleDevice)
                establishConnection(bleDevice)
            }
             if (canConnect) {
                 val bleDevice = rxBleClient.getBleDevice(info.first!!)
                 observeConnectionState(bleDevice)
                 establishConnection(bleDevice)
             }

            /*Handler(Looper.getMainLooper()).postDelayed(Runnable {
                // Your Code
                if (BluetoothState.get().deviceState === DeviceState.SCANNING){
                    val bleDevice = rxBleClient.getBleDevice(info.first!!)
                    observeConnectionState(bleDevice)
                    establishConnection(bleDevice)
                }
                runnable?.let { handler!!.postDelayed(it, 10000) }
            }, 15000)*/

//            handler = Handler()
            handler = Handler(Looper.getMainLooper())
            runnable = Runnable {
                if (BluetoothState.get().deviceState === DeviceState.SCANNING){
                    val bleDevice = rxBleClient.getBleDevice(info.first!!)
                    observeConnectionState(bleDevice)
                    establishConnection(bleDevice)
                }
                runnable?.let { handler!!.postDelayed(it, 10000) }
            }
            handler!!.postDelayed(runnable!!, 15000)

            /*handler = Handler()
            runnable = Runnable {
                if (canConnect && isReconnecting) {
                    isReconnecting = false
                    val bleDevice = rxBleClient.getBleDevice(info.first!!)
                    observeConnectionState(bleDevice)
                    establishConnection(bleDevice)
                } else {
                    if (!canConnect) {
                        BluetoothLog.writeLog(TAG, "Bluetooth is disabled")
                    }
                }
                handler!!.postDelayed(runnable, 10000)
            }
            handler!!.postDelayed(runnable, 15000)*/

        }


    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBusManager.instance().unRegister(this)
        stopConnection()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAlarmStarted(event: FirmwareUpdateEvent) {
        deviceController?.update(event.firmware)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAlarmStarted(event: AlarmStartEvent) {
        if (event.error == null) {
            deviceController?.alarmStarted()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    fun onAlarmDisabled(event: AlarmStopEvent) {
        if (event.error == null) {
            deviceController?.alarmStopped()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGuardianJoined(event: GuardianJoinedEvent) {
        deviceController?.guardianJoined(event.name)
    }

    @Subscribe
    fun onLogout() {
        EventBusManager.instance().unRegister(this)
        stopSelf()
    }

    private fun establishConnection(bleDevice: RxBleDevice) {
        BluetoothLog.writeLog(TAG, "Establishing connection")

        BluetoothState.get().deviceState = DeviceState.CONNECTING
        EventBusManager.instance().postEvent(DeviceConnectionStateChangedEvent(false))

        connectionSubscription = bleDevice.establishConnection(false).subscribe({ rxBleConnection ->
            initControllers(bleDevice, rxBleConnection)
        }, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
//        isReconnecting = false
    }

    private fun stopConnection(resetDevice: Boolean = false) {
        BluetoothLog.writeLog(TAG, "Stopping connection")

        BluetoothState.get().deviceState = DeviceState.DISCONNECTING
        EventBusManager.instance().postEvent(DeviceConnectionStateChangedEvent(false))

        stateSubscription?.unsubscribe()
        stateSubscription = null
        destroyControllers(resetDevice).doAfterTerminate {
            connectionSubscription?.let { subscription ->
                BluetoothLog.writeLog(TAG, "Device was reset")
                subscription.unsubscribe()

                BluetoothState.get().deviceState = DeviceState.DISCONNECTED
                EventBusManager.instance().postEvent(DeviceConnectionStateChangedEvent(false))
            }
            connectionSubscription = null
        }.subscribe({}, { throwable -> BluetoothLog.writeThrowable(TAG, throwable) })
    }

    private fun scanDevices(macAddress: String) {
//        if (isBt) {
            BluetoothLog.writeLog(TAG, "Searching for device")
//        }else{
//            BluetoothLog.writeLog(TAG, "Bluetooth is disabled")
//        }

        BluetoothState.get().deviceState = DeviceState.SCANNING
        EventBusManager.instance().postEvent(DeviceConnectionStateChangedEvent(false))

        rxBleClient.scanBleDevices(ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES).build(),
                ScanFilter.Builder().setDeviceAddress(macAddress).build()
        ).takeUntil(BluetoothState.get().observableMacAddress().filter { address ->
            TextUtils.isEmpty(address) || macAddress != address
        }).take(1).observeOn(AndroidSchedulers.mainThread()).subscribe({ scanResult ->
            BluetoothLog.writeLog(TAG, "${scanResult.bleDevice.name} found trying to connect")
            observeConnectionState(scanResult.bleDevice)
            establishConnection(scanResult.bleDevice)
        }, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
    }

    private fun initControllers(bleDevice: RxBleDevice, bleConnection: RxBleConnection) {
        BluetoothLog.writeLog(TAG, "Initializing controllers")
        deviceController = if (BluetoothState.get().deviceType == DeviceType.SAFELET) {
            SafeletController(applicationContext, bleDevice, bleConnection)
        } else {
            SmartBandController(applicationContext, bleDevice, bleConnection)
        }
        deviceInfoController = DeviceInfoController(applicationContext, bleConnection)
    }

    private fun destroyControllers(resetDevice: Boolean): Observable<Boolean> {
        val result = if (resetDevice) {
            deviceController?.reset() ?: Observable.just(true)
        } else {
            Observable.just(true)
        }
        deviceController = null
        deviceInfoController = null
        return result
    }

    private fun observeConnectionState(bleDevice: RxBleDevice) {
        stateSubscription = bleDevice.observeConnectionStateChanges().distinct().subscribe({ connectionState ->
            if (connectionState == RxBleConnection.RxBleConnectionState.DISCONNECTED) {
                BluetoothLog.writeLog(TAG, "Disconnected scanning for device")

                stopConnection()

                startService(BluetoothLogService.getUploadBluetoothLogIntent(this))

                val reconnectState = BehaviorSubject.create<Boolean>(BluetoothState.get().isReconnectEnabled)
                BluetoothState.get().observableReconnectState().subscribe(reconnectState)
                reconnectState.subscribe { isReconnectEnabled ->
                    if (isReconnectEnabled) {
//                        isReconnecting = true
                        BluetoothLog.writeLog(TAG, "Reconnect enabled")
                        scanDevices(bleDevice.macAddress)
                    } else {
//                        isReconnecting = false
                        BluetoothLog.writeLog(TAG, "Waiting for reconnect to be enabled")
                    }
                }
            }
        }, { throwable ->
            BluetoothLog.writeThrowable(TAG, throwable)
        })
    }

    companion object {
        private val TAG = BluetoothConnectionService::class.java.simpleName
    }
}
