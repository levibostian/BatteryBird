package earth.levi.app.android

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import earth.levi.app.DiGraph
import earth.levi.app.extensions.now
import earth.levi.app.model.BluetoothDeviceModel
import earth.levi.app.model.samples.Samples
import earth.levi.app.model.samples.bluetoothDeviceModels
import earth.levi.app.model.samples.bluetoothDevices
import earth.levi.app.ui.type.RuntimePermission

interface Bluetooth: AndroidFeature {
    fun getPairedDevices(context: Context): List<BluetoothDeviceModel>
}

val DiGraph.bluetoothAdapter: BluetoothAdapter
    get() = bluetoothManager.adapter

val DiGraph.bluetooth: Bluetooth
    get() = override() ?: BluetoothImpl(bluetoothAdapter)

open class BluetoothImpl(private val systemBluetoothAdapter: BluetoothAdapter): AndroidFeatureImpl(), Bluetooth {

    @SuppressLint("MissingPermission") // we check if permission granted inside of the function.
    override fun getPairedDevices(context: Context): List<BluetoothDeviceModel> {
        if (!isPermissionGranted(RuntimePermission.Bluetooth, context)) return emptyList()

        return systemBluetoothAdapter.bondedDevices.toList().mapNotNull { pairedDevice ->
            val batteryLevelOfDevice = pairedDevice.batteryLevel ?: return@mapNotNull null // if Android OS doesn't give a battery level, we don't care about that device. Ignore it.

            BluetoothDeviceModel(
                hardwareAddress = pairedDevice.address,
                name = pairedDevice.name,
                batteryLevel = batteryLevelOfDevice,
                lastTimeConnected = now()
            )
        }
    }

    override fun getRequiredPermissions(): List<RuntimePermission> = listOf(RuntimePermission.Bluetooth)
}

val DiGraph.bluetoothStub: Bluetooth
    get() = BluetoothSamplesStub(bluetoothAdapter)

class BluetoothSamplesStub(systemBluetoothAdapter: BluetoothAdapter): BluetoothImpl(systemBluetoothAdapter) {

    var samplePairedDevices: List<BluetoothDeviceModel> = Samples.bluetoothDeviceModels

    override fun getPairedDevices(context: Context): List<BluetoothDeviceModel> = samplePairedDevices

}

// using systemapi function to get battery level. there is risk in using a non-public sdk function, however, logcat has not yet shown a warning from the android source code that the function is hidden and what alternative to use. Therefore, I think there is less risk involved in using at this time. Something to watch.
val BluetoothDevice.batteryLevel: Int?
    get() {
        val level = javaClass.getMethod("getBatteryLevel").invoke(this) as Int
        if (level < 0) return null
        return level
    }