package app.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import app.DiGraph
import app.android.Bluetooth
import app.android.bluetoothStub
import app.android.workManager

class DemoActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.data?.let { deepLink -> handleDeepLink(deepLink) }
    }

    // batterybird://demo?show_low_battery_notification=true
    private fun handleDeepLink(deepLink: Uri) {
        DiGraph.instance.override(Bluetooth::class.java, DiGraph.instance.bluetoothStub)

        if (deepLink.getQueryParameter("show_low_battery_notification") != null) {
            DiGraph.instance.workManager.runBluetoothDeviceBatteryCheck(this)
        }

        startActivity(MainActivity.getIntent(this))
        finish()
    }
}