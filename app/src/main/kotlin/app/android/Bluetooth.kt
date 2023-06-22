package app.android

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import app.DiGraph
import app.extensions.now
import app.model.BluetoothDeviceModel
import app.model.samples.Samples
import app.model.samples.bluetoothDeviceModels
import app.ui.type.RuntimePermission

interface Bluetooth: AndroidFeature {
    fun getPairedDevices(context: Context): List<BluetoothDeviceModel>
}

val DiGraph.bluetoothAdapter: BluetoothAdapter
    get() = bluetoothManager.adapter

val DiGraph.bluetooth: Bluetooth
    get() = override() ?: BluetoothImpl()

open class BluetoothImpl: AndroidFeatureImpl(), Bluetooth {

    // Get this later instead of in the constructor. Getting the bluetooth adapter might return null so we want to try and retrieve it only after
    // we have done checks such as getting permission.
    private val systemBluetoothAdapter by lazy { DiGraph.instance.bluetoothAdapter }

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
    get() = BluetoothSamplesStub()

class BluetoothSamplesStub: BluetoothImpl() {

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