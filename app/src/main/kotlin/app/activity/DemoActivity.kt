package app.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import app.DiGraph
import app.android.Bluetooth
import app.android.workManager
import app.diGraph
import app.store.BluetoothDevicesStore
import app.store.bluetoothDevicesStoreStub

class DemoActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.data?.let { deepLink -> handleDeepLink(deepLink) }
    }

    // batterybird://demo
    private fun handleDeepLink(deepLink: Uri) {
        diGraph.override(BluetoothDevicesStore::class.java, diGraph.bluetoothDevicesStoreStub)

        startActivity(MainActivity.getIntent(this))
        finish()
    }
}