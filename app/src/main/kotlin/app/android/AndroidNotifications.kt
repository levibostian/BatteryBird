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
import app.ui.type.RuntimePermission
import kotlin.random.Random

val DiGraph.androidNotifications: AndroidNotifications
    get() = AndroidNotificationsImpl(notificationManager)

interface AndroidNotifications: AndroidFeature {
    fun showNotification(notification: Notification)
    fun cancel(notificationId: Int, notificationTag: String?)
    fun createChannels()

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
}

open class AndroidNotificationsImpl(val notificationManager: NotificationManager): AndroidFeatureImpl(), AndroidNotifications {

    override fun getRequiredPermissions(): List<RuntimePermission> = listOf(RuntimePermission.Notifications)

    override fun showNotification(notification: Notification) {
        val notificationId = notification.id
        val notificationTag = notification.tag

        if (notificationTag != null) {
            notificationManager.notify(notificationTag, notificationId, notification)
        } else {
            notificationManager.notify(notificationId, notification)
        }
    }

    override fun cancel(notificationId: Int, notificationTag: String?) {
        notificationManager.cancel(notificationTag, notificationId)
    }

    @SuppressLint("NewApi") // because notification channels returns null if OS level too low, we can ignore lint error
    override fun createChannels() {
        enumValues<AndroidNotifications.Channels.Groups>().mapNotNull { it.group }.forEach { channelGroup ->
            notificationManager.createNotificationChannelGroup(channelGroup)
        }
        enumValues<AndroidNotifications.Channels>().mapNotNull { it.channel }.forEach { channel ->
            notificationManager.createNotificationChannel(channel)
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

        DiGraph.instance.androidNotifications.cancel(notificationId, notificationTag)

        stopSelf()

        return START_STICKY_COMPATIBILITY
    }
}