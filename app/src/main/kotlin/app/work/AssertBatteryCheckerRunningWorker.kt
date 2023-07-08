package app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import app.DiGraph
import app.android.workManager

class AssertBatteryCheckerRunningWorker(context: Context, workerParameters: WorkerParameters): Worker(context, workerParameters) {

    private val workManager = DiGraph.instance.workManager

    override fun doWork(): Result {
        workManager.runBluetoothDeviceBatteryCheck(context = applicationContext)

        return Result.success()
    }
}