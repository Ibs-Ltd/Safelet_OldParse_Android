package global

import android.bluetooth.BluetoothAdapter
import android.text.TextUtils
import com.safelet.android.global.ApplicationSafelet
import com.safelet.android.global.PreferencesManager
import models.enums.DeviceState
import models.enums.DeviceType
import rx.Observable
import rx.subjects.PublishSubject
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class BluetoothState private constructor() {

    // Variables
    private val deviceBatteryLevel = AtomicInteger(0)
    private val reconnectEnabled = AtomicBoolean(true)
    private val deviceUpdatePercentage = AtomicInteger(0)
    private val macAddressSubject = PublishSubject.create<String?>()
    private val deviceTypeSubject = PublishSubject.create<DeviceType?>()
    private val reconnectEnabledSubject = PublishSubject.create<Boolean>()

    val isBluetoothEnabled: Boolean
        get() {
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            return bluetoothAdapter != null && bluetoothAdapter.isEnabled
        }

    val isDevicePaired: Boolean
        get() = !TextUtils.isEmpty(macAddress)

    var deviceState: DeviceState = DeviceState.DISCONNECTED

    var isReconnectEnabled: Boolean
        get() = reconnectEnabled.get()
        set(value) {
            if (reconnectEnabled.getAndSet(value) != value) {
                reconnectEnabledSubject.onNext(value)
            }
        }

    var macAddress: String?
        get() = PreferencesManager.instance(ApplicationSafelet.getContext()).getString(PREF_VALUE_MAC_ADDRESS, null)
        set(value) {
            if (value != macAddress) {
                macAddressSubject.onNext(value)
            }
            if (value == null) {
                deviceType = null
            }
        }

    var deviceType: DeviceType?
        get() {
            val deviceType = PreferencesManager.instance(ApplicationSafelet.getContext()).getString(PREF_VALUE_DEVICE_TYPE, null)
            return if (TextUtils.isEmpty(deviceType)) {
                null
            } else {
                DeviceType.valueOf(deviceType)
            }
        }
        set(value) {
            deviceTypeSubject.onNext(value)
        }

    var batteryLevel: Int
        get() = deviceBatteryLevel.get()
        set(value) {
            deviceBatteryLevel.set(value)
        }

    var updatePercentage: Int
        get() = deviceUpdatePercentage.get()
        set(value) {
            deviceUpdatePercentage.set(value)
        }

    fun reset() {
        macAddressSubject.onNext(null)
        deviceTypeSubject.onNext(null)
    }

    fun observableReconnectState(): Observable<Boolean> {
        return reconnectEnabledSubject.asObservable()
    }

    fun observableMacAddress(): Observable<String?> {
        return macAddressSubject.asObservable()
    }

    fun observableDeviceType(): Observable<DeviceType?> {
        return deviceTypeSubject.asObservable()
    }

    init {
        observableMacAddress().subscribe({ address ->
            PreferencesManager.instance(ApplicationSafelet.getContext()).setString(PREF_VALUE_MAC_ADDRESS, address)
        })
        observableDeviceType().subscribe({ deviceType ->
            PreferencesManager.instance(ApplicationSafelet.getContext()).setString(PREF_VALUE_DEVICE_TYPE, deviceType?.name)
        })
    }

    companion object {

        private val PREF_VALUE_MAC_ADDRESS = "macAddress"
        private val PREF_VALUE_DEVICE_TYPE = "deviceType"

        // Singleton pattern objects
        private var instance: BluetoothState? = null

        @JvmStatic
        fun get(): BluetoothState {
            if (instance == null) {
                instance = BluetoothState()
            }
            return instance!!
        }
    }
}
