package earth.levi.app.android

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import earth.levi.app.DiGraph
import earth.levi.app.R
import earth.levi.app.store.KeyValueStorage
import earth.levi.app.store.NotificationsStore
import earth.levi.app.store.keyValueStorage
import earth.levi.app.store.notificationsStore
import kotlinx.serialization.Serializable

val DiGraph.notifications: Notifications
    get() = Notifications(notificationManager, notificationsStore)

open class Notifications(val notificationManager: NotificationManager, val notificationsStore: NotificationsStore) {

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val showNotificationPermission = Manifest.permission.POST_NOTIFICATIONS

    fun showNotification(notification: Notification) {
        val notificationId = notification.id
        val notificationTag = notification.tag

        if (notificationTag != null) {
            notificationManager.notify(notificationTag, notificationId, notification)
        } else {
            notificationManager.notify(notificationId, notification)
        }

        notificationsStore.notificationShown(ShownNotification(notificationId, notificationTag))
    }

    val shownNotifications: List<ShownNotification>
        get() = notificationsStore.notificationsShown

    fun cancel(shownNotification: ShownNotification) {
        this.cancel(shownNotification.id, shownNotification.tag)
    }

    fun cancel(notificationId: Int, notificationTag: String?) {
        notificationManager.cancel(notificationTag, notificationId)
        notificationsStore.removeNotificationShown(ShownNotification(notificationId, notificationTag))
    }

    @SuppressLint("NewApi") // because notification channels returns null if OS level too low, we can ignore lint error
    fun createChannels() {
        enumValues<Channels.Groups>().mapNotNull { it.group }.forEach { channelGroup ->
            notificationManager.createNotificationChannelGroup(channelGroup)
        }
        enumValues<Channels>().mapNotNull { it.channel }.forEach { channel ->
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getBatteryMonitoringNotification(context: Context, cancelIntent: PendingIntent, show: Boolean = false) = NotificationCompat.Builder(context).apply {
        Channels.BackgroundUpdatesDeviceBatteryLevels.channelId?.let { setChannelId(it) }
        setContentTitle("Monitoring battery levels...")
        setSmallIcon(R.drawable.ic_launcher_foreground)
        setGroup(Groups.DevicesBeingMonitored.name)
        setSortKey("aaaaaaa") // we want this notification to be displayed on top of group so set sort key to something that can't be beat lexicographically
        setOngoing(true)
        setId(Groups.DevicesBeingMonitored.ordinal)
        // Add the cancel action to the notification which can be used to cancel the worker
        addAction(android.R.drawable.ic_delete, "cancel", cancelIntent)
    }.build(showAfterBuild = show)

    fun getDeviceBatteryMonitoringNotification(context: Context, deviceName: String, batteryPercentage: Int, show: Boolean = false) = NotificationCompat.Builder(context).apply {
        Channels.BackgroundUpdatesDeviceBatteryLevels.channelId?.let { setChannelId(it) }
        setContentTitle("$deviceName battery being monitored...")
        setContentText("Current battery: $batteryPercentage%")
        setSmallIcon(R.drawable.ic_launcher_foreground)
        setGroup(Groups.DevicesBeingMonitored.name)
        setOngoing(true)
        setId(Groups.DevicesBeingMonitored.ordinal)
        setTag(deviceName)
    }.build(showAfterBuild = show)

    fun getBatteryLowNotification(context: Context, deviceName: String, batteryPercentage: Int, show: Boolean = false) = NotificationCompat.Builder(context).apply {
        Channels.LowBattery.channelId?.let { setChannelId(it) }
        setContentTitle("Bluetooth device battery low")
        setContentText("$deviceName battery level $batteryPercentage%")
        setOngoing(true) // do not allow swiping away to accidentally swipe it. instead, we add a button to dismiss it.
        setOnlyAlertOnce(true) // only play sound once. if notification gets updated later, update content but no alert
        setGroup(Groups.LowBatteryDevices.name)
        setId(Groups.LowBatteryDevices.ordinal)
        setTag(deviceName)
        setSmallIcon(R.drawable.ic_launcher_foreground)
        addAction(android.R.drawable.ic_delete, "done", DismissNotificationService.getPendingIntent(context, id, tag))
    }.build(showAfterBuild = show)

    enum class Groups { // You can group notifications together in tray that are related.
        LowBatteryDevices,
        DevicesBeingMonitored;
    }

    enum class Channels {
        LowBattery,
        BackgroundUpdatesDeviceBatteryLevels;

        companion object {
            val doesAndroidSupportChannels: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
        }

        val channelId: String?
            @SuppressLint("NewApi") // as long as we check OS version, ignore lint
            get() {
                return if (!doesAndroidSupportChannels) null else channel?.id
            }

        val channel: NotificationChannel?
            @SuppressLint("NewApi") // as long as we check OS version, ignore lint
            get() {
                if (!doesAndroidSupportChannels) return null

                return when (this) {
                    LowBattery -> NotificationChannel("low-battery", "Low battery alert", NotificationManager.IMPORTANCE_DEFAULT).apply {
                        description = "A bluetooth device has a low battery and should be charged."
                        group = Groups.Alerts.group!!.id
                    }
                    // Careful when setting importance for this channel since we use it for background workers. The OS does not allow importance "min" with background services, for example.
                    BackgroundUpdatesDeviceBatteryLevels -> NotificationChannel("monitor-bluetooth-devices", "Monitor connected Bluetooth devices", NotificationManager.IMPORTANCE_LOW).apply {
                        description = "Notification indicating that the app is continuously monitoring all connected Bluetooth devices and when the battery levels change."
                        group = Groups.Functionality.group!!.id
                    }
                }
            }

        enum class Groups {
            Alerts,
            Functionality;

            val group: NotificationChannelGroup?
                @SuppressLint("NewApi") // as long as we check OS version, ignore lint
                get() {
                    if (!doesAndroidSupportChannels) return null

                    return when (this) {
                        Alerts -> NotificationChannelGroup("alerts",  "Alerts")
                        Functionality -> NotificationChannelGroup("functionality", "App functionality")
                    }
                }
        }
    }

    fun NotificationCompat.Builder.build(showAfterBuild: Boolean): Notification {
        return build().also { notification ->
            if (showAfterBuild) this@Notifications.showNotification(notification)
        }
    }
}

@Serializable
data class ShownNotification(val id: Int, val tag: String?)

fun NotificationCompat.Builder.setId(value: Int) {
    addExtras(Bundle().apply { putInt("id", value) })
}

val Notification.id: Int
    get() = extras.getInt("id")

val NotificationCompat.Builder.id: Int
    get() = extras.getInt("id")

fun NotificationCompat.Builder.setTag(value: String) {
    addExtras(Bundle().apply { putString("tag", value) })
}

val NotificationCompat.Builder.tag: String?
    get() = extras.getString("tag")

val Notification.tag: String?
    get() = extras.getString("tag")

class DismissNotificationService: Service() {
    companion object {
        private val notificationIdBundleKey = "id"
        private val notificationTagBundleKey = "tag"

        fun getPendingIntent(context: Context, notificationId: Int, notificationTag: String?) = PendingIntent.getService(
            context,
            0,
            Intent(context, DismissNotificationService::class.java).putExtra(notificationIdBundleKey, notificationId).putExtra(notificationTagBundleKey, notificationTag),
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notificationId = intent.extras!!.getInt(notificationIdBundleKey)
        val notificationTag = intent.extras!!.getString(notificationTagBundleKey)

        DiGraph.instance.notifications.cancel(notificationId, notificationTag)

        stopSelf()

        return START_STICKY_COMPATIBILITY
    }
}