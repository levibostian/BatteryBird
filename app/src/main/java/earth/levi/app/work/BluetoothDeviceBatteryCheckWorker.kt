package earth.levi.app.work

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import earth.levi.app.DiGraph
import earth.levi.app.R
import earth.levi.app.android.Notifications
import earth.levi.app.android.batteryLevel
import earth.levi.app.android.bluetooth
import earth.levi.app.android.id
import earth.levi.app.android.notifications
import earth.levi.app.android.show
import earth.levi.app.extensions.secondsToMillis
import earth.levi.app.log.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BluetoothDeviceBatteryCheckWorker(context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {

    private val bluetooth = DiGraph.instance.bluetooth
    private val log = DiGraph.instance.logger
    private val notifications = DiGraph.instance.notifications

    override suspend fun doWork(): Result {
        // This worker is a long-running worker that runs X number of minutes while the bluetooth device is connected.
        // You call setForeground() to tell WorkManager that you are a long-running task.
        val notificationForLongRunningJob = notifications.getBatteryMonitoringNotification(applicationContext, WorkManager.getInstance(applicationContext).createCancelPendingIntent(id))
        setForeground(ForegroundInfo(notificationForLongRunningJob.id, notificationForLongRunningJob))

        // Now we can begin the work!
        // Keep looping this function without returning
        Thread.sleep(3.secondsToMillis()) // sometimes bluetooth device battery info isn't available right away. sleep to give OS a second to finish connecting to bluetooth device
        checkBatteryLevels()

        log.debug("doWork done. Returning result.", this)
        return Result.success() // Only call this function if worker determines that the bluetooth device got disconnected
    }

    @SuppressLint("MissingPermission") // we are not checking for bluetooth permission in this function, we do that in another class
    private suspend fun checkBatteryLevels() {
        var atLeastOneBluetoothDeviceConnected = true

        while (atLeastOneBluetoothDeviceConnected) {
            log.debug("checking battery levels for all devices.", this)

            atLeastOneBluetoothDeviceConnected = false // changes to true if a device found to be paired

            withContext(Dispatchers.IO) {
                val pairedDevices = bluetooth.getPairedDevices(applicationContext)

                pairedDevices.forEach { pairedDevice ->
                    pairedDevice.batteryLevel?.let { batteryLevelOfDevice ->
                        atLeastOneBluetoothDeviceConnected = true // if there is a battery level read, we can assume the device is connected

                        log.debug("device: ${pairedDevice.name}, battery: $batteryLevelOfDevice", this)

                        if (batteryLevelOfDevice <= 20) {
                            notifications.getBatteryLowNotification(applicationContext, pairedDevice.name, batteryLevelOfDevice).show(notifications)
                        }
                    }
                }

                Thread.sleep(5.secondsToMillis())
            }
        }
    }
}