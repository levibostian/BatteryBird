package app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.DiGraph
import app.android.workManager
import app.log.logger

class BluetoothDeviceMonitorBroadcastReceiver: BroadcastReceiver() {

    private val log = DiGraph.instance.logger
    private val workManager = DiGraph.instance.workManager

    override fun onReceive(context: Context, intent: Intent) {
        log.debug("bluetooth device monitor broadcast receiver onReceive. action: ${intent.action}, extras: ${intent.extras.toString()}", this)

        workManager.runBluetoothDeviceBatteryCheck(context)
    }
}