package app.store

import app.DiGraph
import app.model.BluetoothDeviceModel
import kotlinx.coroutines.flow.Flow

// We want to store bluetooth devices to app storage so that we keep a history of devices. We can then display historical data in the app's UI.
interface BluetoothDevicesStore {
    var pairedDevices: List<BluetoothDeviceModel>
    val observePairedDevices: Flow<List<BluetoothDeviceModel>>
}

val DiGraph.bluetoothDevicesStore: BluetoothDevicesStore
    get() = BluetoothDevicesStoreImpl(keyValueStorage)

class BluetoothDevicesStoreImpl(private val keyValueStorage: KeyValueStorage): BluetoothDevicesStore {

    override var pairedDevices: List<BluetoothDeviceModel>
        get() = keyValueStorage.pairedBluetoothDevices
        set(value) {
            keyValueStorage.pairedBluetoothDevices = value
        }

    override val observePairedDevices: Flow<List<BluetoothDeviceModel>>
        get() = keyValueStorage.observePairedBluetoothDevices

}