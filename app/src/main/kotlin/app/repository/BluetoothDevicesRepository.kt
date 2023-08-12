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
import app.store.KeyValueStorage
import app.store.bluetoothDevicesStore
import app.store.keyValueStorage
import earth.levi.batterybird.BluetoothDeviceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface BluetoothDevicesRepository {
    suspend fun updateAllBatteryLevels(context: Context, updateNotifications: Boolean)
    suspend fun updateBatteryLevel(context: Context, device: BluetoothDeviceModel, updateNotifications: Boolean): Int?
}

val DiGraph.bluetoothDevicesRepository: BluetoothDevicesRepository
    get() = override() ?: BluetoothDevicesRepositoryImpl(bluetooth, bluetoothDevicesStore, notifications, keyValueStorage)

class BluetoothDevicesRepositoryImpl(
    private val bluetooth: Bluetooth,
    private val devicesStore: BluetoothDevicesStore,
    private val notifications: AppNotifications,
    private val keyValueStorage: KeyValueStorage): BluetoothDevicesRepository {

    override suspend fun updateAllBatteryLevels(context: Context, updateNotifications: Boolean) {
        insertPairedDevicesIntoDB(context) // sync system bluetooth devices with our DB to keep our app up-to-date with devices you may have added

        devicesStore.devices.forEach { device ->
            updateBatteryLevel(context, device, updateNotifications)
        }
    }

    override suspend fun updateBatteryLevel(context: Context, device: BluetoothDeviceModel, updateNotifications: Boolean): Int? = withContext(Dispatchers.IO) {
        val batteryLevel = bluetooth.getBatteryLevel(context, device)
        val lastTimeConnected = if (batteryLevel == null) null else now()

        devicesStore.devices = listOf(device.copy(batteryLevel = batteryLevel?.toLong(), lastTimeConnected = lastTimeConnected))

        if (updateNotifications)  {
            // To avoid annoying the user, there is an ignore action button added to notification. If pressed, we do not show the notification again
            // until th device charges again.
            // Check if we should ignore showing the notification.
            if (batteryLevel != null && batteryLevel <= 20 && !keyValueStorage.isLowBatteryAlertIgnoredForDevice(device)) {
                notifications.apply {
                    show(getBatteryLowNotification(context, device))
                }
            } else {
                // reset memory of alert being ignored so next time battery is low, notification shows by default.
                keyValueStorage.setLowBatteryAlertIgnoredForDevice(device, shouldIgnore = false)

                notifications.dismissBatteryLowNotification(context, device)
            }
        }

        batteryLevel
    }

    private suspend fun insertPairedDevicesIntoDB(context: Context) = withContext(Dispatchers.IO) {
        val pairedBluetoothDevices = bluetooth.getPairedDevices(context).getOrDefault(emptyList()) // using default value as an easy way to handle scenario where bluetooth permission revoked from settings. Keeps code running without throwing exception

        devicesStore.devices = pairedBluetoothDevices // inserts new devices into DB
    }

}