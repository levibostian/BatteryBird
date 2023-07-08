package app.android

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import app.DiGraph
import app.work.AssertBatteryCheckerRunningWorker
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

    // Because the battery check worker never stops, this periodic worker is just to make sure the battery check worker is running.
    // This increases the chances that our worker is running at all times.
    fun schedulePeriodicBluetoothDeviceBatteryCheck(context: Context) {
        androidx.work.WorkManager.getInstance(context).apply {
            val taskTag = AssertBatteryCheckerRunningWorker::class.java.simpleName

            cancelAllWorkByTag(taskTag)

            enqueue(PeriodicWorkRequestBuilder<AssertBatteryCheckerRunningWorker>(15, TimeUnit.MINUTES)
                .addTag(taskTag)
                .build())
        }
    }
}