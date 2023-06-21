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

    private val pairedBluetoothDevices: List<BluetoothDeviceModel>
        get() = bluetooth.getPairedDevices(applicationContext)

    override suspend fun doWork(): Result {
        // don't run worker if there are no devices currently connected.
        // this check allows us to run this worker many times even if we think there might not be any devices connected and it will not show a notification in the UI and annoy the user.
        if (pairedBluetoothDevices.isEmpty()) return Result.success()

        // This worker is a long-running worker that runs X number of minutes while the bluetooth device is connected.
        // You call setForeground() to tell WorkManager that you are a long-running task.
        val notificationForLongRunningJob = notifications.getBatteryMonitoringNotification(applicationContext)
        setForeground(ForegroundInfo(notificationForLongRunningJob.id, notificationForLongRunningJob))

        // Now we can begin the work!
        // Keep looping this function without returning
        Thread.sleep(1.secondsToMillis()) // sometimes bluetooth device battery info isn't available right away. sleep to give OS a second to finish connecting to bluetooth device
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
                bluetoothDevicesStore.pairedDevices = pairedBluetoothDevices

                pairedBluetoothDevices.forEach { bluetoothDevice ->
                    atLeastOneBluetoothDeviceConnected = true // if there is a battery level read, we can assume the device is connected

                    log.debug("device: ${bluetoothDevice.name}, battery: ${bluetoothDevice.batteryLevel}", this)

                    if (bluetoothDevice.batteryLevel <= 20) {
                        notifications.getBatteryLowNotification(applicationContext, bluetoothDevice, show = true)
                    } else {
                        notifications.dismissBatteryLowNotification(applicationContext, bluetoothDevice)
                    }
                }

                Thread.sleep(10.secondsToMillis()) // TODO: reset
            }
        }
    }
}