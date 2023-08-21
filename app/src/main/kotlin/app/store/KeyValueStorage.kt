package app.store

import android.content.SharedPreferences
import app.DiGraph
import app.extensions.now
import app.ui.type.RuntimePermission
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import earth.levi.batterybird.BluetoothDeviceModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant

val DiGraph.keyValueStorage: KeyValueStorage
    get() = KeyValueStorage(sharedPreferences)

open class KeyValueStorage(private val sharedPreferences: SharedPreferences) {

    private val flowSharedPreferences = FlowSharedPreferences(sharedPreferences)

     enum class Keys {
         LowBatteryAlertForDeviceSent,
         HasNeverAskedForARuntimePermission,
         TimeAllDeviceBatteryLevelsUpdated,
    }

    fun hasAskedForPermission(permission: RuntimePermission): Boolean = sharedPreferences.getBoolean("${Keys.HasNeverAskedForARuntimePermission.name}_${permission.string}", false)

    fun permissionHasBeenAsked(permission: RuntimePermission) {
        sharedPreferences.edit().putBoolean("${Keys.HasNeverAskedForARuntimePermission.name}_${permission.string}", true).commit()
    }

    fun setLowBatteryAlertIgnoredForDevice(device: BluetoothDeviceModel, shouldIgnore: Boolean = true) {
        setLowBatteryAlertIgnoredForDevice(device.hardwareAddress, shouldIgnore)
    }

    fun setLowBatteryAlertIgnoredForDevice(hardwareAddress: String, shouldIgnore: Boolean = true) {
        sharedPreferences.edit().putBoolean("${Keys.LowBatteryAlertForDeviceSent}_${hardwareAddress}", shouldIgnore).commit()
    }

    fun isLowBatteryAlertIgnoredForDevice(device: BluetoothDeviceModel): Boolean {
        return sharedPreferences.getBoolean("${Keys.LowBatteryAlertForDeviceSent}_${device.hardwareAddress}", false)
    }

    fun allDeviceBatteryLevelsUpdated() {
        sharedPreferences.edit().putLong(Keys.TimeAllDeviceBatteryLevelsUpdated.name, now().epochSeconds).commit()
    }

    val observeLastTimeAllDevicesBatteryLevelUpdated: Flow<Instant?>
        get() = flowSharedPreferences.getLong(Keys.TimeAllDeviceBatteryLevelsUpdated.name, -1L).asFlow().map { seconds ->
            if (seconds == -1L) return@map null

            Instant.fromEpochSeconds(seconds)
        }

    fun deleteAll() {
        sharedPreferences.edit().clear().commit()
    }
}