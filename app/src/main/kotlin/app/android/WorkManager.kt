package app.android

import android.content.Context
import androidx.work.ExistingWorkPolicy
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
            // We only want 1 job to be running at a time. If one already running, skip this request.
            val taskTag = BluetoothDeviceBatteryCheckWorker::class.java.simpleName

            // TODO: test this works.
            // it seems that we can put the workmanager into a state where this job never runs again.
            // I think that if there is a bug in the blueooth suspend getbattery function, it will never run again.
            // maybe we stop using a long runner job and just make it periodic?
            // or, every so often, we do cancel the job and re-schedule it.

            // maybe when app is in background, 15 minute updates is good enough. But when app is in foreground, we want to check more often.
            enqueueUniqueWork(taskTag, ExistingWorkPolicy.KEEP, OneTimeWorkRequestBuilder<BluetoothDeviceBatteryCheckWorker>()
                .build())
        }
    }

    // Because the battery check worker never stops, this periodic worker is just to make sure the battery check worker is running.
    // This increases the chances that our worker is running at all times.
    fun schedulePeriodicBluetoothDeviceBatteryCheck(context: Context) {
        androidx.work.WorkManager.getInstance(context).apply {
            val taskTag = AssertBatteryCheckerRunningWorker::class.java.simpleName

            enqueue(PeriodicWorkRequestBuilder<AssertBatteryCheckerRunningWorker>(15, TimeUnit.MINUTES)
                .addTag(taskTag)
                .build())
        }
    }
}