package earth.levi.app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import earth.levi.app.android.bluetooth
import earth.levi.app.android.notifications
import earth.levi.app.log.Logger
import earth.levi.app.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    private val bluetooth = DiGraph.instance.bluetooth
    private val notifications = DiGraph.instance.notifications

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
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
    AppTheme {
        Greeting("Android")
    }
}