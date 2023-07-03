package app.store

import androidx.room.Database
import androidx.room.RoomDatabase
import app.model.BluetoothDeviceModel
import app.store.dao.BluetoothDevicesDao

@Database(entities = [BluetoothDeviceModel::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bluetoothDeviceDao(): BluetoothDevicesDao
}
