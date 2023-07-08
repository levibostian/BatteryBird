package earth.levi.batterybird.store

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import earth.levi.batterybird.BluetoothDeviceModel
import earth.levi.batterybird.BluetoothDeviceQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

class DatabaseStore(private val db: Database) {

    var bluetoothDevices: List<BluetoothDeviceModel>
        get() = db.bluetoothDeviceQueries.getAll().executeAsList()
        set(newValue) = newValue.forEach {
            db.bluetoothDeviceQueries.insertOrReplace(hardwareAddress = it.hardwareAddress, name = it.name, batteryLevel = it.batteryLevel, isConnected = it.isConnected, lastTimeConnected = it.lastTimeConnected)
        }

    val observeBluetoothDevices: Flow<List<BluetoothDeviceModel>>
        get() = db.bluetoothDeviceQueries.getAll().asFlow().mapToList(Dispatchers.IO)

    fun deleteAll() {
        db.transaction {
            db.bluetoothDeviceQueries.deleteAll()
        }
    }
}