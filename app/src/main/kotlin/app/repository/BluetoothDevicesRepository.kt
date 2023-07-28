package app.repository

import android.content.Context
import app.DiGraph
import app.android.Bluetooth
import app.android.bluetooth
import app.extensions.now
import app.store.BluetoothDevicesStore
import app.store.bluetoothDevicesStore
import earth.levi.batterybird.BluetoothDeviceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface BluetoothDevicesRepository {
    suspend fun updateBatteryLevel(context: Context, device: BluetoothDeviceModel): Int?
}

val DiGraph.bluetoothDevicesRepository: BluetoothDevicesRepository
    get() = override() ?: BluetoothDevicesRepositoryImpl(bluetooth, bluetoothDevicesStore)

class BluetoothDevicesRepositoryImpl(private val bluetooth: Bluetooth, private val devicesStore: BluetoothDevicesStore): BluetoothDevicesRepository {

    override suspend fun updateBatteryLevel(context: Context, device: BluetoothDeviceModel): Int? = withContext(Dispatchers.IO) {
        val batteryLevel = bluetooth.getBatteryLevel(context, device)
        val lastTimeConnected = if (batteryLevel == null) null else now()

        devicesStore.devices = listOf(device.copy(batteryLevel = batteryLevel?.toLong(), lastTimeConnected = lastTimeConnected))

        batteryLevel
    }

}