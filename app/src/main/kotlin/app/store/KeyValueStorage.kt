package app.store

import android.content.SharedPreferences
import app.DiGraph
import app.ui.type.RuntimePermission
import earth.levi.batterybird.BluetoothDeviceModel

val DiGraph.keyValueStorage: KeyValueStorage
    get() = KeyValueStorage(sharedPreferences)

open class KeyValueStorage(private val sharedPreferences: SharedPreferences) {

     enum class Keys {
         LowBatteryAlertForDeviceSent,
         HasNeverAskedForARuntimePermission
    }

    fun hasAskedForPermission(permission: RuntimePermission): Boolean = sharedPreferences.getBoolean("${Keys.HasNeverAskedForARuntimePermission.name}_${permission.string}", false)

    fun permissionHasBeenAsked(permission: RuntimePermission) {
        sharedPreferences.edit().putBoolean("${Keys.HasNeverAskedForARuntimePermission.name}_${permission.string}", true).commit()
    }

    fun setLowBatteryAlertSentForDevice(device: BluetoothDeviceModel, alertSent: Boolean = true) {
        sharedPreferences.edit().putBoolean("${Keys.LowBatteryAlertForDeviceSent}_${device.hardwareAddress}", alertSent).commit()
    }

    fun hasLowBatteryAlertBeenSentForDevice(device: BluetoothDeviceModel): Boolean {
        return sharedPreferences.getBoolean("${Keys.LowBatteryAlertForDeviceSent}_${device.hardwareAddress}", false)
    }

    fun deleteAll() {
        sharedPreferences.edit().clear().commit()
    }
}