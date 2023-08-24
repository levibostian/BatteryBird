package earth.levi.batterybird.store

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import earth.levi.batterybird.BluetoothDeviceModel
import earth.levi.batterybird.BluetoothDeviceQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock.System.now
import kotlinx.datetime.Instant

class DatabaseStore(private val db: Database) {

    val observeBluetoothDevices: Flow<List<BluetoothDeviceModel>>
        get() = db.bluetoothDeviceQueries.getAll().asFlow().mapToList(Dispatchers.IO)

    fun insert(devices: List<BluetoothDeviceModel>) {
        devices.forEach {
            db.bluetoothDeviceQueries.insert(hardwareAddress = it.hardwareAddress, name = it.name)
        }
    }

    fun updateBatteryStatus(device: BluetoothDeviceModel, batteryLevel: Int?, isConnected: Boolean, lastTimeConnected: Instant?) {
        db.bluetoothDeviceQueries.updateBatteryStatus(isConnected = isConnected, batteryLevel = batteryLevel?.toLong(), lastTimeConnected = lastTimeConnected, name = "", hardwareAddress = device.hardwareAddress)
    }

    fun updateNotificationBatteryLevel(device: BluetoothDeviceModel, notificationBatteryLevel: Int?) {
        db.bluetoothDeviceQueries.updateNotificationBatteryLevel(notificationBatteryLevel?.toLong(), device.hardwareAddress)
    }

    fun updateName(device: BluetoothDeviceModel, name: String) {
        db.bluetoothDeviceQueries.updateName(name, device.hardwareAddress)
    }

    fun deleteAll() {
        db.transaction {
            db.bluetoothDeviceQueries.deleteAll()
        }
    }

    fun getDevices(): List<BluetoothDeviceModel> = db.bluetoothDeviceQueries.getAll().executeAsList()
}