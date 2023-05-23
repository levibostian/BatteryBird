package earth.levi.app.android

import android.Manifest
import android.app.NotificationManager
import android.bluetooth.BluetoothDevice
import android.content.Context
import androidx.annotation.RequiresPermission
import earth.levi.app.DiGraph
import earth.levi.app.store.NotificationsStore
import earth.levi.app.store.notificationsStore

val DiGraph.bluetoothDeviceMonitoringNotifications: BluetoothDeviceMonitoringNotifications
    get() = BluetoothDeviceMonitoringNotifications(notificationManager, notificationsStore)

class BluetoothDeviceMonitoringNotifications(notificationManager: NotificationManager, notificationsStore: NotificationsStore) : Notifications(notificationManager, notificationsStore) {

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun updateDevicesMonitoredNotifications(context: Context, connectedDevicesWithBatteryLevel: List<Pair<BluetoothDevice, Int>>) {
        val pairedDevicesTags = connectedDevicesWithBatteryLevel.map { it.first.name }
        val existingDeviceMonitoringNotifications = shownNotifications.filter { activeNotification ->
            activeNotification.id == Groups.DevicesBeingMonitored.ordinal && activeNotification.tag != null
        }
        val devicesNoLongerBeingMonitored = existingDeviceMonitoringNotifications.toMutableList().filter { !pairedDevicesTags.contains(it.tag)  }

        devicesNoLongerBeingMonitored.forEach {
            cancel(it)
        }

        connectedDevicesWithBatteryLevel.forEach {
            getDeviceBatteryMonitoringNotification(context, it.first.name, it.second, show = true)
        }
    }

}