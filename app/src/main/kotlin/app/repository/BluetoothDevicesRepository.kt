package app.repository

import android.app.Notification
import android.content.Context
import app.DiGraph
import app.android.Bluetooth
import app.android.bluetooth
import app.extensions.now
import app.notifications.AppNotifications
import app.notifications.notifications
import app.store.BluetoothDevicesStore
import app.store.bluetoothDevicesStore
import earth.levi.batterybird.BluetoothDeviceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface BluetoothDevicesRepository {
    suspend fun updateAllBatteryLevels(context: Context, updateNotifications: Boolean)
    suspend fun updateBatteryLevel(context: Context, device: BluetoothDeviceModel): Int?
}

val DiGraph.bluetoothDevicesRepository: BluetoothDevicesRepository
    get() = override() ?: BluetoothDevicesRepositoryImpl(bluetooth, bluetoothDevicesStore, notifications)

class BluetoothDevicesRepositoryImpl(
    private val bluetooth: Bluetooth,
    private val devicesStore: BluetoothDevicesStore,
    private val notifications: AppNotifications): BluetoothDevicesRepository {

    override suspend fun updateAllBatteryLevels(context: Context, updateNotifications: Boolean) {
        insertPairedDevicesIntoDB(context) // sync system bluetooth devices with our DB to keep our app up-to-date with devices you may have added

        devicesStore.devices.forEach { device ->
            val batteryLevel = updateBatteryLevel(context, device)

            if (updateNotifications)  {
                if (batteryLevel != null && batteryLevel <= 20) {
                    notifications.getBatteryLowNotification(context, device, show = true)
                } else {
                    notifications.dismissBatteryLowNotification(context, device)
                }
            }
        }
    }

    override suspend fun updateBatteryLevel(context: Context, device: BluetoothDeviceModel): Int? = withContext(Dispatchers.IO) {
        val batteryLevel = bluetooth.getBatteryLevel(context, device)
        val lastTimeConnected = if (batteryLevel == null) null else now()

        devicesStore.devices = listOf(device.copy(batteryLevel = batteryLevel?.toLong(), lastTimeConnected = lastTimeConnected))

        batteryLevel
    }

    private suspend fun insertPairedDevicesIntoDB(context: Context) = withContext(Dispatchers.IO) {
        val pairedBluetoothDevices = bluetooth.getPairedDevices(context).getOrDefault(emptyList()) // using default value as an easy way to handle scenario where bluetooth permission revoked from settings. Keeps code running without throwing exception

        devicesStore.devices = pairedBluetoothDevices // inserts new devices into DB
    }

}