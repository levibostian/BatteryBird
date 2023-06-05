package earth.levi.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import earth.levi.app.DiGraph
import earth.levi.app.model.BluetoothDevice
import earth.levi.app.model.samples.Samples
import earth.levi.app.model.samples.bluetoothDevices
import earth.levi.app.store.BluetoothDevicesStore
import earth.levi.app.store.bluetoothDevicesStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

val DiGraph.bluetoothDevicesViewModel: BluetoothDevicesViewModel
    get() = BluetoothDevicesViewModel(bluetoothDevicesStore)

class BluetoothDevicesViewModel(private val bluetoothDevicesStore: BluetoothDevicesStore): ViewModel() {

    // Set demo data so the app has something to show in the UI
    private var _pairedDevices = MutableStateFlow(Samples.bluetoothDevices)

    val observePairedDevices: StateFlow<List<BluetoothDevice>>
        get() = _pairedDevices

    init {
        startObservingPairedBluetoothDevices()
    }

    private fun startObservingPairedBluetoothDevices() {
        viewModelScope.launch {
            bluetoothDevicesStore.observePairedDevices.collect { currentlyPairedDevices ->
                if (currentlyPairedDevices.isEmpty()) return@collect // continue to use demo data until currently paired devices list is not empty

                _pairedDevices.value = currentlyPairedDevices
            }
        }
    }

}