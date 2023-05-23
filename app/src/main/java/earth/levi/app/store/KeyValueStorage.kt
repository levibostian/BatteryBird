package earth.levi.app.store

import android.content.SharedPreferences
import earth.levi.app.DiGraph
import earth.levi.app.android.ShownNotification
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val DiGraph.keyValueStorage: KeyValueStorage
    get() = KeyValueStorage(sharedPreferences)

open class KeyValueStorage(private val sharedPreferences: SharedPreferences) {

    enum class Keys {
        NotificationShown;
    }
}

inline fun <reified T> SharedPreferences.Editor.putSerializable(key: String, value: T): SharedPreferences.Editor {
    return putString(key, Json.encodeToString(value))
}

inline fun <reified T> SharedPreferences.getSerializable(key: String): T? {
    val stringValue = getString(key, null) ?: return null

    return Json.decodeFromString(stringValue)
}