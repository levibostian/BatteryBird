package earth.levi.app.android

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import androidx.annotation.RequiresPermission
import earth.levi.app.DiGraph
import earth.levi.app.model.BluetoothDevice
import earth.levi.app.store.NotificationsStore
import earth.levi.app.store.notificationsStore

val DiGraph.bluetoothDeviceMonitoringNotifications: BluetoothDeviceMonitoringNotifications
    get() = BluetoothDeviceMonitoringNotifications(notificationManager, notificationsStore)

class BluetoothDeviceMonitoringNotifications(notificationManager: NotificationManager, notificationsStore: NotificationsStore) : Notifications(notificationManager, notificationsStore) {

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun updateDevicesMonitoredNotifications(context: Context, connectedDevices: List<BluetoothDevice>) {
        val pairedDevicesTags = connectedDevices.map { it.name }
        val existingDeviceMonitoringNotifications = shownNotifications.filter { activeNotification ->
            activeNotification.id == Groups.DevicesBeingMonitored.ordinal && activeNotification.tag != null
        }
        val devicesNoLongerBeingMonitored = existingDeviceMonitoringNotifications.toMutableList().filter { !pairedDevicesTags.contains(it.tag)  }

        devicesNoLongerBeingMonitored.forEach {
            cancel(it)
        }

        connectedDevices.forEach {
            getDeviceBatteryMonitoringNotification(context, it.name, it.batteryLevel, show = true)
        }
    }

}