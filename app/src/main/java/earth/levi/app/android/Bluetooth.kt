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

    // TODO: now that I have AndroidFeature which is a handy way to check API level of the feature AND if permission has been granted, refactor this class to remove
    // all of the "if SDK level ..." checks and annotations.

    val getPairedDevicesPermission = Manifest.permission.BLUETOOTH_CONNECT

    fun getPairedDevices(context: Context): List<BluetoothDevice> {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val havePermissionToGetSystemBluetoothDevices = ContextCompat.checkSelfPermission(context, getPairedDevicesPermission) == PackageManager.PERMISSION_GRANTED
            if (!havePermissionToGetSystemBluetoothDevices) return emptyList()
        }

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