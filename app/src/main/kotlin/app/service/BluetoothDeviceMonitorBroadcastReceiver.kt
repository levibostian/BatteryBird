package app.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.DiGraph
import app.android.workManager
import app.log.logger
import app.repository.bluetoothDevicesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BluetoothDeviceMonitorBroadcastReceiver:  BroadcastReceiver() {

    private val log by lazy { DiGraph.instance.logger }
    private val workManager by lazy { DiGraph.instance.workManager }

    override fun onReceive(context: Context, intent: Intent) {
        log.debug("bluetooth device monitor broadcast receiver onReceive. action: ${intent.action}, extras: ${intent.extras.toString()}", this)

        // update battery levels immediately when a broadcast is received so the newly connected or disconnected device gets the correct status in the UI.
        workManager.runDeviceBatteryCheckOnce(context)
    }
}