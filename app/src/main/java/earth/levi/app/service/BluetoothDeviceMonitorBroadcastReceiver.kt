package earth.levi.app.service

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import earth.levi.app.DiGraph
import earth.levi.app.android.bluetooth
import earth.levi.app.android.workManager
import earth.levi.app.log.Logger
import earth.levi.app.log.logger
import earth.levi.app.work.BluetoothDeviceBatteryCheckWorker

class BluetoothDeviceMonitorBroadcastReceiver: BroadcastReceiver() {

    private val log = DiGraph.instance.logger
    private val workManager = DiGraph.instance.workManager

    override fun onReceive(context: Context, intent: Intent) {
        log.debug("bluetooth device monitor broadcast receiver onReceive. action: ${intent.action}, extras: ${intent.extras.toString()}", this)

        workManager.runBluetoothDeviceBatteryCheck(context)
    }
}