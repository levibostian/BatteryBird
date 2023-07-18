package app.store

import app.DiGraph
import earth.levi.batterybird.BluetoothDeviceModel
import earth.levi.batterybird.store.Database
import earth.levi.batterybird.store.DatabaseStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface BluetoothDevicesStore {
    var pairedDevices: List<BluetoothDeviceModel>
    val observePairedDevices: Flow<List<BluetoothDeviceModel>>
    fun manuallyAddDevice(bluetoothDevice: BluetoothDeviceModel)
}

val DiGraph.bluetoothDevicesStore: BluetoothDevicesStore
    get() = BluetoothDevicesStoreImpl(database)

class BluetoothDevicesStoreImpl(private val database: DatabaseStore): BluetoothDevicesStore {

    override var pairedDevices: List<BluetoothDeviceModel>
        get() = database.bluetoothDevices
        set(newValue) {
            // if newValue is empty, it's probably to indicate that bluetooth is off. We rely on it never giving us empty so that we can update the status of all of the devices easily by overriding new values.
            // if empty, we need to at least update the connected status for all devices to not connected.
            if (newValue.isEmpty()) {
                database.bluetoothDevices = database.bluetoothDevices.map { it.copy(isConnected = false) }
            } else {
                database.bluetoothDevices = newValue
            }
        }

    override fun manuallyAddDevice(bluetoothDevice: BluetoothDeviceModel) {
        database.bluetoothDevices = database.bluetoothDevices + bluetoothDevice
    }

    override val observePairedDevices: Flow<List<BluetoothDeviceModel>>
        get() = database.observeBluetoothDevices

}