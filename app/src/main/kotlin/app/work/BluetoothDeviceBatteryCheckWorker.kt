package app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.extensions.secondsToMillis
import app.getDiGraph
import app.log.Logger
import app.log.logger
import app.repository.BluetoothDevicesRepository
import app.repository.bluetoothDevicesRepository
import kotlinx.coroutines.delay

class BluetoothDeviceBatteryCheckWorker(context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {

    private lateinit var log: Logger
    private lateinit var bluetoothDevicesRepository: BluetoothDevicesRepository

    override suspend fun doWork(): Result {
        getDiGraph(applicationContext).also { diGraph ->
            log = diGraph.logger
            bluetoothDevicesRepository = diGraph.bluetoothDevicesRepository
        }

        log.debug("bluetooth device checker worker started", this)

        delay(2.secondsToMillis()) // hack. Sleep before checking battery levels. This is mostly for when a device just connected and we try to get the battery level for it immediately. Some devices only work by getting the battery level from the private Android OS call. Without this delay, the Android OS call returns null and then GATT is used which can cause issues (such as Sony WH-1000XM5 being disconnected when using GATT). Add this delay to make sure that the Android OS call will return the battery level.

        bluetoothDevicesRepository.updateAllBatteryLevels(applicationContext, updateNotifications = true)

        return Result.success()
    }
}