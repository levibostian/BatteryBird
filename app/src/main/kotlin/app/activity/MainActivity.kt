package app.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import app.DiGraph
import app.android.workManager
import app.diGraph
import app.ui.AppNavGraph
import app.ui.theme.AppTheme
import app.viewModelDiGraph
import app.viewmodel.bluetoothDevicesViewModel

class MainActivity : ComponentActivity() {

    companion object {
        fun getIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    // initialize properties late for tests to override properties before they are used
    private val bluetoothDevicesViewModel by viewModelDiGraph { bluetoothDevicesViewModel }
    private val workManager by lazy { diGraph.workManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            AppTheme {
                AppNavGraph()
            }
        }

        // app on startup, start the scheduled worker
        // This code is here and not in the application to make our automated tests easier to write
        workManager.schedulePeriodicBluetoothDeviceBatteryCheck(this)
    }

    override fun onResume() {
        super.onResume()

        bluetoothDevicesViewModel.updateMissingPermissions(this)
    }
}