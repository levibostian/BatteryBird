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

        // Broadcast receiver's job is to make sure that battery level worker is running. This is a helpful way to make sure that the worker is running.
        workManager.runBluetoothDeviceBatteryCheck(context)
    }
}