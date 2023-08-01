package app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import app.DiGraph
import app.log.logger
import app.repository.bluetoothDevicesRepository

class BluetoothDeviceBatteryCheckWorker(context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {

    private val log = DiGraph.instance.logger
    private val bluetoothDevicesRepository = DiGraph.instance.bluetoothDevicesRepository

    override suspend fun doWork(): Result {
        log.debug("worker started", this)

        bluetoothDevicesRepository.updateAllBatteryLevels(applicationContext, updateNotifications = true)

        return Result.success()
    }
}