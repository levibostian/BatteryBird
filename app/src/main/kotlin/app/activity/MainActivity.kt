package app.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import app.DiGraph
import app.android.workManager
import app.ui.AppNavGraph
import app.ui.theme.AppTheme
import app.viewModelDiGraph
import app.viewmodel.bluetoothDevicesViewModel

class MainActivity : ComponentActivity() {

    companion object {
        fun getIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    private val bluetoothDevicesViewModel by viewModelDiGraph { bluetoothDevicesViewModel }
    private val workManager by lazy { DiGraph.instance.workManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

// TODO: confirm these things
// the bluetooth VM is shared between activity and devices list composable
// the job in workmanager is scheduled in the activity