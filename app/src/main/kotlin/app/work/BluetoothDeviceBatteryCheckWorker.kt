package app.work

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import app.DiGraph
import app.android.bluetooth
import app.android.id
import app.extensions.minutesToMillis
import app.extensions.now
import app.extensions.secondsToMillis
import app.log.logger
import app.notifications.notifications
import app.repository.bluetoothDevicesRepository
import app.store.bluetoothDevicesStore
import earth.levi.batterybird.BluetoothDeviceModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Long-running worker that never stops that gets the battery level of the bluetooth device and shows a notification if the battery is low
class BluetoothDeviceBatteryCheckWorker(context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {

    private val bluetooth = DiGraph.instance.bluetooth
    private val log = DiGraph.instance.logger
    private val notifications = DiGraph.instance.notifications
    private val bluetoothDevicesStore = DiGraph.instance.bluetoothDevicesStore
    private val bluetoothDevicesRepository = DiGraph.instance.bluetoothDevicesRepository

    private val pairedBluetoothDevices: kotlin.Result<List<BluetoothDeviceModel>>
        get() = bluetooth.getPairedDevices(applicationContext)

    override suspend fun doWork(): Result {
        log.debug("worker started", this)

        if (!bluetooth.canGetPairedDevices(applicationContext)) {
            log.debug("cannot get bluetooth devices so quitting early", this)
            return Result.success()
        }

        // This worker is a long-running worker that doesn't stop running.
        // You call setForeground() to tell WorkManager that you are a long-running task. It's required.
        val notificationForLongRunningJob = notifications.getBatteryMonitoringNotification(applicationContext)
        setForeground(ForegroundInfo(notificationForLongRunningJob.id, notificationForLongRunningJob))

        insertPairedDevicesIntoDB()
        checkBatteryLevels()

        log.debug("worker done. Returning result.", this)
        return Result.success() // Only call this function if worker determines that the bluetooth device got disconnected
    }

    private suspend fun insertPairedDevicesIntoDB() = withContext(Dispatchers.IO) {
        val pairedBluetoothDevices = pairedBluetoothDevices.getOrDefault(emptyList()) // using default value as an easy way to handle scenario where bluetooth permission revoked from settings. Keeps code running without throwing exception

        bluetoothDevicesStore.devices = pairedBluetoothDevices // inserts new devices into DB
    }

    @SuppressLint("MissingPermission") // we are not checking for bluetooth permission in this function, we do that in another class
    private suspend fun checkBatteryLevels() = withContext(Dispatchers.IO) {
        while (true) { // never stop running worker. only done when worker is cancelled
            log.debug("checking battery levels of devices", this@BluetoothDeviceBatteryCheckWorker)

            val bluetoothDevices = bluetoothDevicesStore.devices

            bluetoothDevices.forEach { bluetoothDevice ->
                log.debug("checking battery level of ${bluetoothDevice.name}", this@BluetoothDeviceBatteryCheckWorker)

                val batteryLevel = bluetoothDevicesRepository.updateBatteryLevel(applicationContext, bluetoothDevice)

                log.debug("got battery level of ${bluetoothDevice.name}: $batteryLevel",this@BluetoothDeviceBatteryCheckWorker)

                if (batteryLevel != null && batteryLevel <= 20) {
                    notifications.getBatteryLowNotification(applicationContext, bluetoothDevice, show = true)
                } else {
                    notifications.dismissBatteryLowNotification(applicationContext, bluetoothDevice)
                }
            }

            val timeBetweenChecks = 3.minutesToMillis()
            log.debug("sleeping for $timeBetweenChecks", this@BluetoothDeviceBatteryCheckWorker)
            Thread.sleep(timeBetweenChecks)
        }
    }
}