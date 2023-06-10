package earth.levi.app

import android.app.Application
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import earth.levi.app.android.Notifications
import earth.levi.app.android.notifications
import earth.levi.app.android.workManager
import earth.levi.app.work.BluetoothDeviceBatteryCheckWorker
import java.util.concurrent.TimeUnit


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