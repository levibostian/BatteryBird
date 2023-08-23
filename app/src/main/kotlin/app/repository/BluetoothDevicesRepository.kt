package app.repository

import android.app.Notification
import android.content.Context
import app.DiGraph
import app.android.Bluetooth
import app.android.bluetooth
import app.extensions.now
import app.model.notificationBatteryLevelOrDefault
import app.notifications.AppNotifications
import app.notifications.notifications
import app.store.BluetoothDevicesStore
import app.store.KeyValueStorage
import app.store.bluetoothDevicesStore
import app.store.keyValueStorage
import earth.levi.batterybird.BluetoothDeviceModel
import earth.levi.batterybird.store.DatabaseStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

interface BluetoothDevicesRepository {
    suspend fun updateAllBatteryLevels(context: Context, updateNotifications: Boolean)
    suspend fun updateBatteryLevel(context: Context, device: BluetoothDeviceModel, updateNotifications: Boolean): Int?
    suspend fun updateNotificationBatteryLevel(device: BluetoothDeviceModel, notificationBatteryLevel: Int?)
}

val DiGraph.bluetoothDevicesRepository: BluetoothDevicesRepository
    get() = override() ?: BluetoothDevicesRepositoryImpl(bluetooth, bluetoothDevicesStore, database, notifications, keyValueStorage)

class BluetoothDevicesRepositoryImpl(
    private val bluetooth: Bluetooth,
    private val devicesStore: BluetoothDevicesStore,
    private val database: DatabaseStore,
    private val notifications: AppNotifications,
    private val keyValueStorage: KeyValueStorage): BluetoothDevicesRepository {

    override suspend fun updateAllBatteryLevels(context: Context, updateNotifications: Boolean) {
        insertPairedDevicesIntoDB(context) // sync system bluetooth devices with our DB to keep our app up-to-date with devices you may have added

        devicesStore.devices.forEach { device ->
            updateBatteryLevel(context, device, updateNotifications)
        }

        keyValueStorage.allDeviceBatteryLevelsUpdated()
    }

    override suspend fun updateBatteryLevel(context: Context, device: BluetoothDeviceModel, updateNotifications: Boolean): Int? = withContext(Dispatchers.IO) {
        val newBatteryLevelIfDeviceConnected = bluetooth.getBatteryLevel(context, device)
        val cachedBatteryLevel = device.batteryLevel?.toInt()

        val lastTimeConnected = if (newBatteryLevelIfDeviceConnected == null) null else now()

        // update the device in local store with the new battery level
        devicesStore.devices = listOf(device.copy(batteryLevel = newBatteryLevelIfDeviceConnected?.toLong(), isConnected = bluetooth.isDeviceConnected(device), lastTimeConnected = lastTimeConnected))

        if (updateNotifications)  {
            // to make the app more reliable in making sure low battery notifications are shown, we use the cache as well as new battery level to cover the use case of: app killed, device battery low but device not connected, app started again.
            // Reproduce:
            // * Run app in android studio
            // * Connect device with low battery
            // * Re-run app in android studio. The app restarts with no notifications shown. This function tries to re-show the notifications.
            val batteryLevel: Int? = newBatteryLevelIfDeviceConnected ?: cachedBatteryLevel

            if (batteryLevel != null && batteryLevel <= device.notificationBatteryLevelOrDefault) { // repository sets default notification battery level
                // To avoid annoying the user, there is an ignore action button added to notification. If pressed, we do not show the notification again
                // until th device charges again. Check if we should ignore showing the notification.
                if (!keyValueStorage.isLowBatteryAlertIgnoredForDevice(device)) {
                    notifications.apply {
                        show(getBatteryLowNotification(context, device))
                    }
                }
            } else {
                // Important: Only run this function if the battery level is not low.

                // reset memory of alert being ignored so next time battery is low, notification shows by default.
                keyValueStorage.setLowBatteryAlertIgnoredForDevice(device, shouldIgnore = false)

                notifications.dismissBatteryLowNotification(context, device)
            }
        }

        newBatteryLevelIfDeviceConnected
    }

    override suspend fun updateNotificationBatteryLevel(
        device: BluetoothDeviceModel,
        notificationBatteryLevel: Int?
    ) {
        database.updateNotificationBatteryLevel(device, notificationBatteryLevel)
    }

    private suspend fun insertPairedDevicesIntoDB(context: Context) = withContext(Dispatchers.IO) {
        val pairedBluetoothDevices = bluetooth.getPairedDevices(context).getOrDefault(emptyList()) // using default value as an easy way to handle scenario where bluetooth permission revoked from settings. Keeps code running without throwing exception

        devicesStore.devices = pairedBluetoothDevices // inserts new devices into DB
    }

}