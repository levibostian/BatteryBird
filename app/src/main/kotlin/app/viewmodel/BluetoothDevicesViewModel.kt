package app.viewmodel

import android.app.Activity
import androidx.lifecycle.viewModelScope
import app.DiGraph
import app.android.Bluetooth
import app.android.AndroidNotifications
import app.android.WorkManager
import app.android.bluetooth
import app.android.androidNotifications
import app.android.workManager
import app.model.BluetoothDevice
import app.model.samples.Samples
import app.model.samples.bluetoothDevices
import app.store.BluetoothDevicesStore
import app.store.KeyValueStorage
import app.store.bluetoothDevicesStore
import app.store.keyValueStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

val DiGraph.bluetoothDevicesViewModel: BluetoothDevicesViewModel
    get() = BluetoothDevicesViewModel(bluetoothDevicesStore, workManager, keyValueStorage, bluetooth, androidNotifications)

class BluetoothDevicesViewModel(
    private val bluetoothDevicesStore: BluetoothDevicesStore,
    private val workManager: WorkManager,
    keyValueStorage: KeyValueStorage,
    bluetooth: Bluetooth,
    notifications: AndroidNotifications
    ): BaseViewModel(androidFeaturesUsedInViewModel = listOf(bluetooth, notifications), keyValueStorage) {

    // Set demo data so the app has something to show in the UI
    private var _pairedDevices = MutableStateFlow(Samples.bluetoothDevices)

    val observePairedDevices: StateFlow<List<BluetoothDevice>>
        get() = _pairedDevices

    init {
        startObservingPairedBluetoothDevices()
    }

    override fun updateMissingPermissions(activity: Activity) {
        super.updateMissingPermissions(activity)

        // Because the user might have just accepted the bluetooth connect permission, let's run the worker so we can show bluetooth devices in the UI right away.
        workManager.runBluetoothDeviceBatteryCheck(activity)
    }

    private fun startObservingPairedBluetoothDevices() {
        viewModelScope.launch {
            bluetoothDevicesStore.observePairedDevices.collect { currentlyPairedDevices ->
                currentlyPairedDevices?.let {
                    _pairedDevices.value = it
                }
            }
        }
    }

}