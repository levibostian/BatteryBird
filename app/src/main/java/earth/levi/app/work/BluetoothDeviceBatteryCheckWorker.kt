package earth.levi.app.work

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import earth.levi.app.DiGraph
import earth.levi.app.android.batteryLevel
import earth.levi.app.android.bluetooth
import earth.levi.app.android.bluetoothDeviceMonitoringNotifications
import earth.levi.app.android.id
import earth.levi.app.android.notifications
import earth.levi.app.extensions.now
import earth.levi.app.extensions.secondsToMillis
import earth.levi.app.log.logger
import earth.levi.app.model.BluetoothDevice
import earth.levi.app.model.BluetoothDeviceModel
import earth.levi.app.store.bluetoothDevicesStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

class BluetoothDeviceBatteryCheckWorker(context: Context, workerParameters: WorkerParameters): CoroutineWorker(context, workerParameters) {

    private val bluetooth = DiGraph.instance.bluetooth
    private val log = DiGraph.instance.logger
    private val notifications = DiGraph.instance.notifications
    private val bluetoothDeviceMonitoringNotifications = DiGraph.instance.bluetoothDeviceMonitoringNotifications
    private val bluetoothDevicesStore = DiGraph.instance.bluetoothDevicesStore

    override suspend fun doWork(): Result {
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
                val pairedAndroidBluetoothDevices = bluetooth.getPairedDevices(applicationContext) // paired doesn't mean they are connected! Get battery level to verify they are connected
                val pairedBluetoothDevices = pairedAndroidBluetoothDevices.mapNotNull { pairedDevice ->
                    val batteryLevelOfDevice = pairedDevice.batteryLevel ?: return@mapNotNull null // if Android OS doesn't give a battery level, we don't care about that device. Ignore it.

                    BluetoothDeviceModel(
                        hardwareAddress = pairedDevice.address,
                        name = pairedDevice.name,
                        batteryLevel = batteryLevelOfDevice,
                        lastTimeConnected = now()
                    )
                }

                bluetoothDeviceMonitoringNotifications.updateDevicesMonitoredNotifications(applicationContext, pairedBluetoothDevices)
                bluetoothDevicesStore.pairedDevices = pairedBluetoothDevices

                pairedBluetoothDevices.forEach { bluetoothDevice ->
                    atLeastOneBluetoothDeviceConnected = true // if there is a battery level read, we can assume the device is connected

                    log.debug("device: ${bluetoothDevice.name}, battery: ${bluetoothDevice.batteryLevel}", this)

                    if (bluetoothDevice.batteryLevel <= 20) {
                        notifications.getBatteryLowNotification(applicationContext, bluetoothDevice.name, bluetoothDevice.batteryLevel, show = true)
                    }
                }

                Thread.sleep(30.secondsToMillis())
            }
        }
    }
}