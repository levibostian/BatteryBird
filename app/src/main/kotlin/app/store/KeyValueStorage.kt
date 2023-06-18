package app.store

import android.content.SharedPreferences
import app.DiGraph
import app.extensions.toJsonString
import app.extensions.toObjectFromJsonString
import app.model.BluetoothDeviceModel
import app.ui.type.RuntimePermission
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val DiGraph.keyValueStorage: KeyValueStorage
    get() = KeyValueStorage(sharedPreferences)

open class KeyValueStorage(private val sharedPreferences: SharedPreferences) {

     enum class Keys {
        PairedBluetoothDevices,
        NotificationShown,
        HasNeverAskedForARuntimePermission
    }

    private val flowSharedPreferences: FlowSharedPreferences
        get() = FlowSharedPreferences(sharedPreferences)

    var pairedBluetoothDevices: List<BluetoothDeviceModel>
        set(newValue) {
            sharedPreferences.edit().putJson(Keys.PairedBluetoothDevices.name, newValue).commit()
        }
        get() = sharedPreferences.getFromJson(Keys.PairedBluetoothDevices.name) ?: emptyList()

    val observePairedBluetoothDevices: Flow<List<BluetoothDeviceModel>>
        get() = flowSharedPreferences.getString(Keys.PairedBluetoothDevices.name, "").asFlow()
            .map { string ->
                if (string.isBlank()) emptyList() else string.toObjectFromJsonString()
            }

    fun hasAskedForPermission(permission: RuntimePermission): Boolean = sharedPreferences.getBoolean("${Keys.HasNeverAskedForARuntimePermission.name}_${permission.string}", false)

    fun permissionHasBeenAsked(permission: RuntimePermission) {
        sharedPreferences.edit().putBoolean("${Keys.HasNeverAskedForARuntimePermission.name}_${permission.string}", true).commit()
    }
}

inline fun <reified T> SharedPreferences.Editor.putJson(key: String, value: T): SharedPreferences.Editor {
    return putString(key, value.toJsonString())
}

inline fun <reified T> SharedPreferences.getFromJson(key: String): T? {
    val stringValue = getString(key, null) ?: return null

    return stringValue.toObjectFromJsonString()
}