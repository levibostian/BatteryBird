package app.model.samples

import app.extensions.minus
import app.extensions.now
import earth.levi.batterybird.BluetoothDeviceModel

val Samples.bluetoothDevices: List<BluetoothDeviceModel>
    get() = listOf(
        BluetoothDeviceModel("3F:23:BC:5A:92:23", "Workout headphones", 90, isConnected = true, notificationBatteryLevel = 10, lastTimeConnected = null),
        BluetoothDeviceModel("76:8X:36:JK:LA:N3", "Dining room speakers", 20, isConnected = false, notificationBatteryLevel = null, lastTimeConnected = now().minus(days = 1, hours = 20, minutes = 5)),
        BluetoothDeviceModel("VN:4J:BC:HJ:73:B5", "Home office headphones", 100, isConnected = true, notificationBatteryLevel = 30, lastTimeConnected = null),
        BluetoothDeviceModel("WX:1Z:2E:DG:08:83", "Pods", 40, isConnected = false, notificationBatteryLevel = null, lastTimeConnected = now().minus(days = 12, hours = 3, minutes = 35)),
        BluetoothDeviceModel("7V:OX:7C:IB:MN:17", "Music ear buds", null, isConnected = false, notificationBatteryLevel = null, lastTimeConnected = null),
    )

val Samples.bluetoothDeviceModels: List<BluetoothDeviceModel>
    get() = bluetoothDevices