package app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.DiGraph
import app.android.workManager
import app.log.logger

class BluetoothDeviceMonitorBroadcastReceiver: BroadcastReceiver() {

    private val log by lazy { DiGraph.instance.logger }
    private val workManager by lazy { DiGraph.instance.workManager }

    override fun onReceive(context: Context, intent: Intent) {
        log.debug("bluetooth device monitor broadcast receiver onReceive. action: ${intent.action}, extras: ${intent.extras.toString()}", this)

        // Run the battery checker when device connects or disconnects so we keep the state of devices as accurate as possible. If a worker is already running, we want to cancel it and rerun a new one so we check levels now instead of waiting for next cycle of checking in the existing worker.
        workManager.runBluetoothDeviceBatteryCheck(context)
    }
}