package app.android

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import app.DiGraph
import app.work.BluetoothDeviceBatteryCheckWorker
import java.util.concurrent.TimeUnit

val DiGraph.workManager: WorkManager
    get() = WorkManager()

class WorkManager {

    fun schedulePeriodicBluetoothDeviceBatteryCheck(context: Context) {
        runDeviceBatteryCheck(context, isPeriodic = true)
    }

    fun runDeviceBatteryCheckOnce(context: Context) {
        runDeviceBatteryCheck(context, isPeriodic = false)
    }

    private fun runDeviceBatteryCheck(context: Context, isPeriodic: Boolean) {
        androidx.work.WorkManager.getInstance(context).apply {
            val taskTag = BluetoothDeviceBatteryCheckWorker::class.java.simpleName

            // If there is a bug in the battery check worker where the job does not finish, we want to cancel the job and start a new one. Otherwise, we run the risk of the job never succeeding and a new one never starting to replace it.
            cancelAllWorkByTag(taskTag)

            if (isPeriodic) {
                enqueue(PeriodicWorkRequestBuilder<BluetoothDeviceBatteryCheckWorker>(15, TimeUnit.MINUTES)
                    .addTag(taskTag)
                    .build())
            } else {
                enqueue(OneTimeWorkRequestBuilder<BluetoothDeviceBatteryCheckWorker>()
                    .addTag(taskTag)
                    .build())
            }
        }
    }
}