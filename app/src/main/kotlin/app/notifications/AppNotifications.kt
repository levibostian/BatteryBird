package app.notifications

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import app.DiGraph
import app.R
import app.android.IgnoreNotificationService
import app.android.AndroidNotifications
import app.android.id
import app.android.androidNotifications
import app.android.setId
import app.android.setTag
import app.android.tag
import earth.levi.batterybird.BluetoothDeviceModel

val DiGraph.notifications: AppNotifications
    get() = AppNotifications(androidNotifications)

class AppNotifications(private val androidNotifications: AndroidNotifications) {

    fun getBatteryLowNotification(context: Context, device: BluetoothDeviceModel): Notification {
        val notificationBuilder = getNotificationBuilder(context, AndroidNotifications.Channels.LowBattery).apply {
            setContentTitle("${device.name} needs charged")
            setContentText("Battery level ${device.batteryLevel}%")
            setOngoing(true) // do not allow swiping away to accidentally swipe it. instead, we add a button to dismiss it.
            setOnlyAlertOnce(true) // only play sound once. if notification gets updated later, update content but no alert
            setGroup(AndroidNotifications.Groups.LowBatteryDevices.name)
            setId(device.hardwareAddress.hashCode()) // what makes the notification unique from other low battery notifications
            setTag(AndroidNotifications.Groups.LowBatteryDevices.name)
            setSmallIcon(R.drawable.notification_small_low_battery)
            color = context.resources.getColor(R.color.battery_low_notification)
            addAction(
                android.R.drawable.ic_delete,
                "Ignore",
                IgnoreNotificationService.getPendingIntent(context, deviceHardwareAddress = device.hardwareAddress, notificationId = id, notificationTag = tag))
        }

        return notificationBuilder.build()
    }

    fun show(notification: Notification) {
        androidNotifications.showNotification(notification)
    }

    fun dismissBatteryLowNotification(context: Context, device: BluetoothDeviceModel) {
        getBatteryLowNotification(context, device).let { androidNotifications.cancel(it.id, it.tag) }
    }

    @Suppress("DEPRECATION") // use deprecated version if SDK version doesn't support channel id
    fun getNotificationBuilder(context: Context, channel: AndroidNotifications.Channels): NotificationCompat.Builder {
        val channelId = channel.channelId ?: return NotificationCompat.Builder(context)

        return NotificationCompat.Builder(context, channelId)
    }
}