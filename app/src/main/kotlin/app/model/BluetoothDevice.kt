package app.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

interface BluetoothDevice {
    val hardwareAddress: String // unique ID for the bluetooth device
    val name: String
    val batteryLevel: Int
    val isDemo: Boolean
    val lastTimeConnected: Instant? // if null, it's connected now
}

// Used in data store
@Serializable
data class BluetoothDeviceModel(
    override val hardwareAddress: String,
    override val name: String,
    override val batteryLevel: Int,
    override val lastTimeConnected: Instant?
) : BluetoothDevice {
    override val isDemo: Boolean = false
}

// Used to show a demo of devices in UI
data class BluetoothDeviceDemo(
    override val hardwareAddress: String,
    override val name: String,
    override val batteryLevel: Int,
    override val lastTimeConnected: Instant?
): BluetoothDevice {
    override val isDemo: Boolean = true
}