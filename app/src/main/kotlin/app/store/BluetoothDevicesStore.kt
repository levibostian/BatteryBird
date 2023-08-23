package app.store

import app.DiGraph
import app.model.samples.Samples
import app.model.samples.bluetoothDeviceModels
import earth.levi.batterybird.BluetoothDeviceModel
import earth.levi.batterybird.store.DatabaseStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

interface BluetoothDevicesStore {
    var devices: List<BluetoothDeviceModel>
    val observePairedDevices: Flow<List<BluetoothDeviceModel>>
    fun manuallyAddDevice(bluetoothDevice: BluetoothDeviceModel)
    fun updateDevice(bluetoothDevice: BluetoothDeviceModel)
}

val DiGraph.bluetoothDevicesStore: BluetoothDevicesStore
    get() = override() ?: BluetoothDevicesStoreImpl(database)

open class BluetoothDevicesStoreImpl(private val database: DatabaseStore): BluetoothDevicesStore {

    override var devices: List<BluetoothDeviceModel>
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

    override fun updateDevice(bluetoothDevice: BluetoothDeviceModel) {
        database.bluetoothDevices = listOf(bluetoothDevice) // this will update or insert device in DB without modifying other values in DB. The function name looks weird, I understand
    }

    override fun manuallyAddDevice(bluetoothDevice: BluetoothDeviceModel) {
        database.bluetoothDevices = database.bluetoothDevices + bluetoothDevice
    }

    override val observePairedDevices: Flow<List<BluetoothDeviceModel>>
        get() = database.observeBluetoothDevices
}

val DiGraph.bluetoothDevicesStoreStub: BluetoothDevicesStore
    get() = BluetoothDevicesStoreStub(database)

class BluetoothDevicesStoreStub(database: DatabaseStore): BluetoothDevicesStoreImpl(database) {

    override var devices: List<BluetoothDeviceModel>
        get() = Samples.bluetoothDeviceModels
        set(value) {}

    override val observePairedDevices: Flow<List<BluetoothDeviceModel>>
        get() = flow { emit(Samples.bluetoothDeviceModels) }

}