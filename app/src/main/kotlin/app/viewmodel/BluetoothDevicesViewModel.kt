package app.viewmodel

import android.app.Activity
import android.content.Context
import androidx.lifecycle.viewModelScope
import app.DiGraph
import app.android.Bluetooth
import app.android.AndroidNotifications
import app.android.WorkManager
import app.android.bluetooth
import app.android.androidNotifications
import app.android.workManager
import app.extensions.delaySeconds
import app.extensions.now
import app.extensions.toRelativeTimeSpanString
import app.model.samples.Samples
import app.model.samples.bluetoothDevices
import app.repository.BluetoothDevicesRepository
import app.repository.bluetoothDevicesRepository
import app.store.BluetoothDevicesStore
import app.store.KeyValueStorage
import app.store.bluetoothDevicesStore
import app.store.keyValueStorage
import earth.levi.batterybird.BluetoothDeviceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

val DiGraph.bluetoothDevicesViewModel: BluetoothDevicesViewModel
    get() = BluetoothDevicesViewModel(bluetoothDevicesStore, bluetoothDevicesRepository, keyValueStorage, bluetooth, androidNotifications)

class BluetoothDevicesViewModel(
    private val bluetoothDevicesStore: BluetoothDevicesStore,
    private val bluetoothDevicesRepository: BluetoothDevicesRepository,
    keyValueStorage: KeyValueStorage,
    bluetooth: Bluetooth,
    notifications: AndroidNotifications
    ): BaseViewModel(androidFeaturesUsedInViewModel = listOf(bluetooth, notifications), keyValueStorage) {

    // Set demo data so the app has something to show in the UI
    private var _pairedDevices = MutableStateFlow(Samples.bluetoothDevices)
    private var _isDemoMode = MutableStateFlow(true)

    val isDemoMode: StateFlow<Boolean>
        get() = _isDemoMode

    val observePairedDevices: StateFlow<List<BluetoothDeviceModel>>
        get() = _pairedDevices

    init {
        startObservingPairedBluetoothDevices()
    }

    override fun updateMissingPermissions(activity: Activity) {
        super.updateMissingPermissions(activity)

        // Because the user might have just accepted the bluetooth connect permission, let's update battery levels so we can show bluetooth devices in the UI right away.
        viewModelScope.launch(Dispatchers.IO) {
            bluetoothDevicesRepository.updateAllBatteryLevels(activity, updateNotifications = true)
        }
    }

    // Call from UI to continuously update the battery levels while the UI is visible.
    fun updateBatteryLevels(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            bluetoothDevicesRepository.updateAllBatteryLevels(context, updateNotifications = true)

            delaySeconds(30)
        }
    }

    fun manuallyAddBluetoothDevice(context: Context, hardwareAddress: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val model = BluetoothDeviceModel(
                hardwareAddress = hardwareAddress,
                name = "Added device, $hardwareAddress",
                isConnected = false,
                batteryLevel = null,
                lastTimeConnected = null
            )

            bluetoothDevicesStore.manuallyAddDevice(model)
            bluetoothDevicesRepository.updateBatteryLevel(context, model, updateNotifications = true) // update battery level right away so we can show it in the UI after adding
        }
    }

    private fun startObservingPairedBluetoothDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            bluetoothDevicesStore.observePairedDevices.collect { pairedDevices ->
                _pairedDevices.value = pairedDevices.ifEmpty { Samples.bluetoothDevices }
                _isDemoMode.value = pairedDevices.isEmpty()
            }
        }
    }

}