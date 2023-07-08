package app.android

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import app.DiGraph
import app.extensions.now
import app.model.samples.Samples
import app.model.samples.bluetoothDeviceModels
import app.ui.type.RuntimePermission
import earth.levi.batterybird.BluetoothDeviceModel

interface Bluetooth: AndroidFeature {
    fun canGetPairedDevices(context: Context): Boolean
    // Gets list of paired devices (or empty list if none), or Error if permissions not yet accepted
    fun getPairedDevices(context: Context): Result<List<BluetoothDeviceModel>>
    // if system bluetooth is on or not
    val isBluetoothOn: Boolean
}

val DiGraph.bluetoothAdapter: BluetoothAdapter
    get() = bluetoothManager.adapter

val DiGraph.bluetooth: Bluetooth
    get() = override() ?: BluetoothImpl()

open class BluetoothImpl: AndroidFeatureImpl(), Bluetooth {

    // Get this later instead of in the constructor. Getting the bluetooth adapter might return null so we want to try and retrieve it only after
    // we have done checks such as getting permission.
    private val systemBluetoothAdapter by lazy { DiGraph.instance.bluetoothAdapter }

    override fun canGetPairedDevices(context: Context): Boolean = areAllPermissionsGranted(context)

    @SuppressLint("MissingPermission") // we check if permission granted inside of the function.
    override fun getPairedDevices(context: Context): Result<List<BluetoothDeviceModel>> {
        if (!canGetPairedDevices(context)) return Result.failure(BluetoothPermissionsNotAccepted())

        // Note: If bluetooth is off on device, bondedDevices will return empty list.
        return Result.success(systemBluetoothAdapter.bondedDevices.toList().map { pairedDevice ->
            val isDeviceConnected = pairedDevice.batteryLevel != null

            BluetoothDeviceModel(
                hardwareAddress = pairedDevice.address,
                name = pairedDevice.name,
                batteryLevel = pairedDevice.batteryLevel?.toLong(),
                isConnected = isDeviceConnected,
                lastTimeConnected = if (isDeviceConnected) now() else null
            )
        })
    }

    override val isBluetoothOn: Boolean
        get() = systemBluetoothAdapter.isEnabled

    override fun getRequiredPermissions(): List<RuntimePermission> = listOf(RuntimePermission.Bluetooth)
}

val DiGraph.bluetoothStub: Bluetooth
    get() = BluetoothSamplesStub()

class BluetoothSamplesStub: BluetoothImpl() {

    var samplePairedDevices: List<BluetoothDeviceModel> = Samples.bluetoothDeviceModels

    override fun canGetPairedDevices(context: Context): Boolean = true

    override fun getPairedDevices(context: Context): Result<List<BluetoothDeviceModel>> = Result.success(samplePairedDevices)

}

// using systemapi function to get battery level. there is risk in using a non-public sdk function, however, logcat has not yet shown a warning from the android source code that the function is hidden and what alternative to use. Therefore, I think there is less risk involved in using at this time. Something to watch.
val BluetoothDevice.batteryLevel: Int?
    get() {
        val level = javaClass.getMethod("getBatteryLevel").invoke(this) as Int
        if (level < 0) return null
        return level
    }

class BluetoothPermissionsNotAccepted: Throwable()