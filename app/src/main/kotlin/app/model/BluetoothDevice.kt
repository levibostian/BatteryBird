package app.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

interface BluetoothDevice {
    val hardwareAddress: String // unique ID for the bluetooth device
    val name: String
    val batteryLevel: Int? // some devices do not broadcast a GATT battery service therefore, we cannot get a battery level for it
    val isDemo: Boolean
    val lastTimeConnected: Instant? // if null, it's connected now
}

// Used in data store
@Serializable
@Entity
data class BluetoothDeviceModel(
    @PrimaryKey override val hardwareAddress: String,
    override val name: String,
    override val batteryLevel: Int?,
    override val lastTimeConnected: Instant?
) : BluetoothDevice {
    override val isDemo: Boolean = false
}

// Used to show a demo of devices in UI
data class BluetoothDeviceDemo(
    override val hardwareAddress: String,
    override val name: String,
    override val batteryLevel: Int?,
    override val lastTimeConnected: Instant?
): BluetoothDevice {
    override val isDemo: Boolean = true
}