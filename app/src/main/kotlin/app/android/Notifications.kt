package app.android

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import app.DiGraph
import app.R
import app.ui.type.RuntimePermission
import kotlinx.serialization.Serializable
import kotlin.random.Random

val DiGraph.notifications: Notifications
    get() = Notifications(notificationManager)

open class Notifications(val notificationManager: NotificationManager): AndroidFeatureImpl() {

    override fun getRequiredPermissions(): List<RuntimePermission> = listOf(RuntimePermission.Notifications)

    fun showNotification(notification: Notification) {
        val notificationId = notification.id
        val notificationTag = notification.tag

        if (notificationTag != null) {
            notificationManager.notify(notificationTag, notificationId, notification)
        } else {
            notificationManager.notify(notificationId, notification)
        }
    }

    fun cancel(notificationId: Int, notificationTag: String?) {
        notificationManager.cancel(notificationTag, notificationId)
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

    fun getBatteryMonitoringNotification(context: Context, show: Boolean = false) = getNotificationBuilder(context, Channels.BackgroundUpdatesDeviceBatteryLevels).apply {
        setContentTitle("Monitoring Bluetooth battery levels...")
        setSmallIcon(R.drawable.notification_small_monitoring)
        setGroup(Groups.DevicesBeingMonitored.name)
        setSortKey("aaaaaaa") // we want this notification to be displayed on top of group so set sort key to something that can't be beat lexicographically
        setOngoing(true)
        color = context.resources.getColor(R.color.monitoring_notification)
        setId(Groups.DevicesBeingMonitored.ordinal)
    }.build(showAfterBuild = show)

    fun getBatteryLowNotification(context: Context, deviceName: String, deviceHardwareAddress: String, batteryPercentage: Int, show: Boolean = false) = getNotificationBuilder(context, Channels.LowBattery).apply {
        setContentTitle("$deviceName needs charged")
        setContentText("Battery level $batteryPercentage%")
        setOngoing(true) // do not allow swiping away to accidentally swipe it. instead, we add a button to dismiss it.
        setOnlyAlertOnce(true) // only play sound once. if notification gets updated later, update content but no alert
        setGroup(Groups.LowBatteryDevices.name)
        setId(deviceHardwareAddress.hashCode()) // what makes the notification unique from other low battery notifications
        setTag(Groups.LowBatteryDevices.name)
        setSmallIcon(R.drawable.notification_small_low_battery)
        color = context.resources.getColor(R.color.battery_low_notification)
        addAction(android.R.drawable.ic_delete, "done", DismissNotificationService.getPendingIntent(context, id, tag))
    }.build(showAfterBuild = show)

    @Suppress("DEPRECATION") // use deprecated version if SDK version doesn't support channel id
    private fun getNotificationBuilder(context: Context, channel: Channels): NotificationCompat.Builder {
        val channelId = channel.channelId ?: return NotificationCompat.Builder(context)

        return NotificationCompat.Builder(context, channelId)
    }

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
            Random.nextInt(),
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