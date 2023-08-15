package app.service

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.DiGraph
import app.android.workManager
import app.digraph
import app.log.logger

class BluetoothDeviceMonitorBroadcastReceiver:  BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val diGraph = context.digraph ?: return

        val log = diGraph.logger
        val workManager = diGraph.workManager

        log.debug("bluetooth device monitor broadcast receiver onReceive. action: ${intent.action}, extras: ${intent.extras.toString()}", this)

        // update battery levels immediately when a broadcast is received so the newly connected or disconnected device gets the correct status in the UI.
        workManager.runDeviceBatteryCheckOnce(context)
    }
}