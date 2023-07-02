package app.work

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import app.DiGraph
import app.android.bluetooth
import app.android.id
import app.extensions.secondsToMillis
import app.log.logger
import app.model.BluetoothDeviceModel
import app.notifications.notifications
import app.store.bluetoothDevicesStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BluetoothDeviceBatteryCheckWorker(context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {

    private val bluetooth = DiGraph.instance.bluetooth
    private val log = DiGraph.instance.logger
    private val notifications = DiGraph.instance.notifications
    private val bluetoothDevicesStore = DiGraph.instance.bluetoothDevicesStore

    private val pairedBluetoothDevices: kotlin.Result<List<BluetoothDeviceModel>>
        get() = bluetooth.getPairedDevices(applicationContext)

    override suspend fun doWork(): Result {
        if (!bluetooth.canGetPairedDevices(applicationContext)) {
            log.debug("cannot get bluetooth devices so quitting early", this)
            return Result.success()
        }

        // This worker is a long-running worker that runs X number of minutes while the bluetooth device is connected.
        // You call setForeground() to tell WorkManager that you are a long-running task.
        val notificationForLongRunningJob = notifications.getBatteryMonitoringNotification(applicationContext)
        setForeground(ForegroundInfo(notificationForLongRunningJob.id, notificationForLongRunningJob))

        // Now we can begin the work!
        // Keep looping this function without returning
        Thread.sleep(0.5.secondsToMillis()) // sometimes bluetooth device battery info isn't available right away. sleep to give OS a second to finish connecting to bluetooth device
        checkBatteryLevels()

        log.debug("worker done. Returning result.", this)
        return Result.success() // Only call this function if worker determines that the bluetooth device got disconnected
    }

    @SuppressLint("MissingPermission") // we are not checking for bluetooth permission in this function, we do that in another class
    private suspend fun checkBatteryLevels() = withContext(Dispatchers.IO) {
        val pairedBluetoothDevices = pairedBluetoothDevices.getOrDefault(emptyList()) // using default value as an easy way to handle scenario where bluetooth permission revoked from settings. Keeps code running without throwing exception

        bluetoothDevicesStore.pairedDevices = pairedBluetoothDevices

        pairedBluetoothDevices.forEach { bluetoothDevice ->
            if (bluetoothDevice.batteryLevel <= 20) {
                notifications.getBatteryLowNotification(applicationContext, bluetoothDevice, show = true)
            } else {
                notifications.dismissBatteryLowNotification(applicationContext, bluetoothDevice)
            }
        }

        Thread.sleep(90.secondsToMillis())
    }
}