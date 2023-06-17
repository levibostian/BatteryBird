package app.android

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import app.DiGraph
import app.work.BluetoothDeviceBatteryCheckWorker
import java.util.concurrent.TimeUnit

val DiGraph.workManager: WorkManager
    get() = WorkManager()

class WorkManager {
    fun runBluetoothDeviceBatteryCheck(context: Context) {
        androidx.work.WorkManager.getInstance(context).apply {
            val taskTag = BluetoothDeviceBatteryCheckWorker::class.java.simpleName

            cancelAllWorkByTag(taskTag)

            enqueue(OneTimeWorkRequestBuilder<BluetoothDeviceBatteryCheckWorker>()
                .addTag(taskTag)
                .build())
        }
    }

    fun schedulePeriodicBluetoothDeviceBatteryCheck(context: Context) {
        androidx.work.WorkManager.getInstance(context).apply {
            val taskTag = BluetoothDeviceBatteryCheckWorker::class.java.simpleName

            cancelAllWorkByTag(taskTag)

            enqueue(PeriodicWorkRequestBuilder<BluetoothDeviceBatteryCheckWorker>(15, TimeUnit.MINUTES)
                .addTag(taskTag)
                .build())
        }
    }
}