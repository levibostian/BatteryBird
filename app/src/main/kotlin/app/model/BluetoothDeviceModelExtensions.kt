package app.model

import earth.levi.batterybird.BluetoothDeviceModel

val BluetoothDeviceModel.notificationBatteryLevelOrDefault: Int
    get() = notificationBatteryLevel?.toInt() ?: 20