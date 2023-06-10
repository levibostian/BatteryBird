package earth.levi.app.android

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import earth.levi.app.DiGraph
import earth.levi.app.log.Logger
import earth.levi.app.store.KeyValueStorage
import earth.levi.app.store.keyValueStorage
import earth.levi.app.ui.type.RuntimePermission

val DiGraph.bluetoothAdapter: BluetoothAdapter
    get() = bluetoothManager.adapter

val DiGraph.bluetooth: Bluetooth
    get() = Bluetooth(bluetoothAdapter)

class Bluetooth(private val systemBluetoothAdapter: BluetoothAdapter): AndroidFeature() {

    @SuppressLint("MissingPermission") // we check if permission granted inside of the function.
    fun getPairedDevices(context: Context): List<BluetoothDevice> {
        if (!isPermissionGranted(RuntimePermission.Bluetooth, context)) return emptyList()

        return systemBluetoothAdapter.bondedDevices.toList()
    }

    override fun getRequiredPermissions(): List<RuntimePermission> = listOf(RuntimePermission.Bluetooth)
}

// using systemapi function to get battery level. there is risk in using a non-public sdk function, however, logcat has not yet shown a warning from the android source code that the function is hidden and what alternative to use. Therefore, I think there is less risk involved in using at this time. Something to watch.
val BluetoothDevice.batteryLevel: Int?
    get() {
        val level = javaClass.getMethod("getBatteryLevel").invoke(this) as Int
        if (level < 0) return null
        return level
    }