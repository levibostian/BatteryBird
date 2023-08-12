package app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.DiGraph
import app.digraph
import app.log.logger
import app.repository.bluetoothDevicesRepository

class BluetoothDeviceBatteryCheckWorker(context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {

    private val log by lazy { context.digraph?.logger }
    private val bluetoothDevicesRepository by lazy { context.digraph?.bluetoothDevicesRepository }

    override suspend fun doWork(): Result {
        log?.debug("bluetooth device checker worker started", this)

        bluetoothDevicesRepository?.updateAllBatteryLevels(applicationContext, updateNotifications = true)

        return Result.success()
    }
}