package app.notifications

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import app.DiGraph
import app.R
import app.android.DismissNotificationService
import app.android.AndroidNotifications
import app.android.id
import app.android.androidNotifications
import app.android.setId
import app.android.setTag
import app.android.tag
import app.store.KeyValueStorage
import app.store.keyValueStorage
import earth.levi.batterybird.BluetoothDeviceModel

val DiGraph.notifications: AppNotifications
    get() = AppNotifications(keyValueStorage, androidNotifications)

class AppNotifications(private val keyValueStorage: KeyValueStorage, private val androidNotifications: AndroidNotifications) {

    fun getBatteryLowNotification(context: Context, device: BluetoothDeviceModel, show: Boolean = false): Notification {
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
            addAction(android.R.drawable.ic_delete, "done", DismissNotificationService.getPendingIntent(context, id, tag))
        }

        // we dont want to annoy user by pressing done button and notification comes back over and over.
        // only show an alert once and then we can show again next time battery is low
        if (keyValueStorage.hasLowBatteryAlertBeenSentForDevice(device)) return notificationBuilder.build(showAfterBuild = false)

        if (show) keyValueStorage.setLowBatteryAlertSentForDevice(device, alertSent = true)

        return notificationBuilder.build(showAfterBuild = show)
    }

    fun dismissBatteryLowNotification(context: Context, device: BluetoothDeviceModel) {
        getBatteryLowNotification(context, device, show = false).let { androidNotifications.cancel(it.id, it.tag) }

        // reset memory of alert being sent so next time battery is low, we can reset
        keyValueStorage.setLowBatteryAlertSentForDevice(device, alertSent = false)
    }

    fun NotificationCompat.Builder.build(showAfterBuild: Boolean): Notification {
        return build().also { notification ->
            if (showAfterBuild) this@AppNotifications.androidNotifications.showNotification(notification)
        }
    }

    @Suppress("DEPRECATION") // use deprecated version if SDK version doesn't support channel id
    fun getNotificationBuilder(context: Context, channel: AndroidNotifications.Channels): NotificationCompat.Builder {
        val channelId = channel.channelId ?: return NotificationCompat.Builder(context)

        return NotificationCompat.Builder(context, channelId)
    }

}