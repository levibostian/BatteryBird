package app.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import app.diGraph
import app.repository.BluetoothDevicesRepository
import app.repository.bluetoothDevicesRepositoryStub

class DemoActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.data?.let { deepLink -> handleDeepLink(deepLink) }
    }

    // batterybird://demo
    private fun handleDeepLink(deepLink: Uri) {
        diGraph.override(BluetoothDevicesRepository::class.java, diGraph.bluetoothDevicesRepositoryStub)

        startActivity(MainActivity.getIntent(this))
        finish()
    }
}