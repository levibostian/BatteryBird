package earth.levi.app.store

import android.content.SharedPreferences
import earth.levi.app.DiGraph
import earth.levi.app.android.ShownNotification
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val DiGraph.notificationsStore: NotificationsStore
    get() = NotificationsStore(sharedPreferences)

class NotificationsStore(val sharedPreferences: SharedPreferences): KeyValueStorage(sharedPreferences) {

    // Android's NotificationManager has a property activeNotifications that tells you what notifications are shown on the device. But it's only available API >= 23.
    // We keep track of what notifications are shown and hidden ourselves for compatibility reasons.
    fun notificationShown(notification: ShownNotification) {
        val existingNotificationsShown = this.notificationsShown.toMutableList()

        existingNotificationsShown.add(notification)

        sharedPreferences.edit().putJson(Keys.NotificationShown.name, existingNotificationsShown).commit()
    }

    val notificationsShown: List<ShownNotification>
        get() = sharedPreferences.getFromJson(Keys.NotificationShown.name) ?: emptyList()

    fun removeNotificationShown(notification: ShownNotification) {
        val existingNotificationsShown = this.notificationsShown.toMutableList()

        existingNotificationsShown.remove(notification)

        sharedPreferences.edit().putJson(Keys.NotificationShown.name, existingNotificationsShown).commit()
    }
}