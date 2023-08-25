package app.viewmodel

import android.app.Activity
import android.app.Application
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
import app.log.Logger
import app.log.logger
import app.model.samples.Samples
import app.model.samples.bluetoothDevices
import app.repository.BluetoothDevicesRepository
import app.repository.bluetoothDevicesRepository
import app.store.KeyValueStorage
import app.store.keyValueStorage
import earth.levi.batterybird.BluetoothDeviceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlin.math.log

val DiGraph.bluetoothDevicesViewModel: BluetoothDevicesViewModel
    get() = BluetoothDevicesViewModel(bluetoothDevicesRepository, logger, application, keyValueStorage, bluetooth, androidNotifications)

class BluetoothDevicesViewModel(
    private val bluetoothDevicesRepository: BluetoothDevicesRepository,
    private val logger: Logger,
    application: Application,
    val keyValueStorage: KeyValueStorage,
    bluetooth: Bluetooth,
    notifications: AndroidNotifications
    ): BaseViewModel(application, androidFeaturesUsedInViewModel = listOf(bluetooth, notifications), keyValueStorage) {

    // Set demo data so the app has something to show in the UI
    private var _pairedDevices = MutableStateFlow(Samples.bluetoothDevices)
    private var _isDemoMode = MutableStateFlow(true)
    private var _observeLastTimeAllDevicesBatteryLevelUpdated: MutableStateFlow<Instant?> = MutableStateFlow(null)

    val isDemoMode: StateFlow<Boolean>
        get() = _isDemoMode

    val observePairedDevices: StateFlow<List<BluetoothDeviceModel>>
        get() = _pairedDevices

    val observeLastTimeAllDevicesBatteryLevelUpdated: StateFlow<Instant?>
        get() = _observeLastTimeAllDevicesBatteryLevelUpdated

    init {
        startObservingPairedBluetoothDevices()
        startObservingLastTimeAllDevicesBatteryLevelUpdated()
    }

    override fun updateMissingPermissions(activity: Activity) {
        super.updateMissingPermissions(activity)

        // Because the user might have just accepted the bluetooth connect permission, let's update battery levels so we can show bluetooth devices in the UI right away.
        updateBatteryLevels()
    }

    fun updateBatteryLevels() {
        viewModelScope.launch(Dispatchers.IO) {
            bluetoothDevicesRepository.updateAllBatteryLevels(getApplication(), updateNotifications = true)
        }
    }

    fun manuallyAddBluetoothDevice(hardwareAddress: String) {
        val hardwareAddress = hardwareAddress.trim().uppercase()

        viewModelScope.launch(Dispatchers.IO) {
            val model = BluetoothDeviceModel(
                hardwareAddress = hardwareAddress,
                name = "Added device, $hardwareAddress",
                isConnected = false,
                batteryLevel = null,
                notificationBatteryLevel = null,
                lastTimeConnected = null
            )

            bluetoothDevicesRepository.manuallyAddDevice(model)
            updateBatteryLevels() // update battery level right away so we can show it in the UI after adding
        }
    }

    private fun startObservingPairedBluetoothDevices() {
        viewModelScope.launch(Dispatchers.IO) {
            bluetoothDevicesRepository.observePairedDevices.collect { pairedDevices ->
                _pairedDevices.value = pairedDevices.ifEmpty { Samples.bluetoothDevices }
                _isDemoMode.value = pairedDevices.isEmpty()
            }
        }
    }

    private fun startObservingLastTimeAllDevicesBatteryLevelUpdated() {
        viewModelScope.launch(Dispatchers.IO) {
            keyValueStorage.observeLastTimeAllDevicesBatteryLevelUpdated.collect { lastTimeAllDevicesBatteryLevelUpdated ->
                _observeLastTimeAllDevicesBatteryLevelUpdated.value = lastTimeAllDevicesBatteryLevelUpdated
            }
        }
    }

    fun updateDeviceName(deviceToEditName: BluetoothDeviceModel, newName: String) = viewModelScope.launch {
        bluetoothDevicesRepository.updateName(deviceToEditName, newName)
    }

    fun updateDeviceNotificationBatteryLevel(device: BluetoothDeviceModel, newLevel: Int) = viewModelScope.launch {
        bluetoothDevicesRepository.updateNotificationBatteryLevel(device, newLevel)
    }

}