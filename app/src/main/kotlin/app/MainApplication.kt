package app

import android.app.Application
import app.android.notifications
import app.android.workManager


class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        DiGraph.initialize(this)

        DiGraph.instance.notifications.createChannels()

        // Run Bluetooth worker when the app is not open so we can check the status of devices in case BroadcastReceiver did not get triggered.
        // The worker should begin running a long-running task only if a device is currently connected.
        DiGraph.instance.workManager.schedulePeriodicBluetoothDeviceBatteryCheck(this)
    }
}