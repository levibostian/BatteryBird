package app.activity

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import app.DiGraph
import app.android.Bluetooth
import app.android.bluetoothStub
import app.android.workManager
import app.diGraph

class DemoActivity: Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.data?.let { deepLink -> handleDeepLink(deepLink) }
    }

    // batterybird://demo
    private fun handleDeepLink(deepLink: Uri) {
        diGraph.override(Bluetooth::class.java, diGraph.bluetoothStub)

        startActivity(MainActivity.getIntent(this))
        finish()
    }
}