package earth.levi.app

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import earth.levi.app.android.bluetooth
import earth.levi.app.android.notifications
import earth.levi.app.log.Logger
import earth.levi.app.ui.theme.BluetoothBatteryAlertTheme

class MainActivity : ComponentActivity() {

    private val bluetooth = DiGraph.instance.bluetooth
    private val notifications = DiGraph.instance.notifications

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BluetoothBatteryAlertTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Column {
                        Greeting("Android")

                        // required to get runtime permission in order to get broadcast receiver notifications if bluetooth device connected or not.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            Button(onClick = {
                                requestPermissions(arrayOf(bluetooth.getPairedDevicesPermission), 0)
                            }) {
                                Text(text = "bluetooth permission")
                            }
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Button(onClick = {
                                requestPermissions(arrayOf(notifications.showNotificationPermission), 1)
                            }) {
                                Text(text = "notification permission")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    BluetoothBatteryAlertTheme {
        Greeting("Android")
    }
}