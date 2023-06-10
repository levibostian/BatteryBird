package earth.levi.app.model.samples

import earth.levi.app.extensions.minus
import earth.levi.app.extensions.now
import earth.levi.app.model.BluetoothDevice
import earth.levi.app.model.BluetoothDeviceDemo

val Samples.bluetoothDevices: List<BluetoothDevice>
    get() = listOf(
        BluetoothDeviceDemo("3F:23:BC:5A:92:23", "Workout headphones", 90, lastTimeConnected = null),
        BluetoothDeviceDemo("76:8X:36:JK:LA:N3", "Dining room speakers", 20, lastTimeConnected = now().minus(days = 1, hours = 20, minutes = 5)),
        BluetoothDeviceDemo("VN:4J:BC:HJ:73:B5", "Home office headphones", 100, lastTimeConnected = now().minus(days = 12, hours = 3, minutes = 35)),
        BluetoothDeviceDemo("WX:1Z:2E:DG:08:83", "Pods", 40, lastTimeConnected = now().minus(days = 12, hours = 3, minutes = 35)),
        BluetoothDeviceDemo("7V:OX:7C:IB:MN:17", "Music ear buds", 10, lastTimeConnected = now().minus(days = 12, hours = 3, minutes = 35)),
    )