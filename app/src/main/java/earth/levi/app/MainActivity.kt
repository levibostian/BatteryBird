package earth.levi.app

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.format.DateUtils
import android.text.format.DateUtils.DAY_IN_MILLIS
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.text.format.DateUtils.WEEK_IN_MILLIS
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ChipColors
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.core.app.ActivityCompat
import earth.levi.app.android.bluetooth
import earth.levi.app.android.notifications
import earth.levi.app.log.Logger
import earth.levi.app.ui.theme.AppTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import earth.levi.app.extensions.now
import earth.levi.app.extensions.supportEmailIntent
import earth.levi.app.extensions.systemBluetoothSettingsIntent
import earth.levi.app.extensions.toRelativeTimeSpanString
import earth.levi.app.model.BluetoothDevice
import earth.levi.app.model.BluetoothDeviceDemo
import earth.levi.app.model.samples.Samples
import earth.levi.app.model.samples.bluetoothDevices
import earth.levi.app.ui.theme.BatteryLevelHigh
import earth.levi.app.ui.theme.BatteryLevelLow
import earth.levi.app.ui.theme.BatteryLevelMedium
import earth.levi.app.ui.theme.BatteryLevelTrackDark
import earth.levi.app.ui.theme.BatteryLevelTrackLight
import earth.levi.app.viewmodel.BluetoothDevicesViewModel
import earth.levi.app.viewmodel.bluetoothDevicesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {

    private val bluetoothDevicesViewModel by viewModelDiGraph { DiGraph.instance.bluetoothDevicesViewModel }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MainActivityComposable(bluetoothDevicesViewModel, toolbarBluetoothSettingsOnClick = {
                startActivity(systemBluetoothSettingsIntent())
            }, contactUsOnClick = {
                startActivity(supportEmailIntent())
            })

            // TODO: show a CTA view for bluetooth permission
            // TODO: show a CTA view for notification permissione
        }
    }

//        // required to get runtime permission in order to get broadcast receiver notifications if bluetooth device connected or not.
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            Button(onClick = {
//                requestPermissions(arrayOf(bluetooth.getPairedDevicesPermission), 0)
//            }) {
//                Text(text = "bluetooth permission")
//            }
//        }
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            Button(onClick = {
//                requestPermissions(arrayOf(notifications.showNotificationPermission), 1)
//            }) {
//                Text(text = "notification permission")
//            }
}

@Composable
fun MainActivityComposable(bluetoothDevicesViewModel: BluetoothDevicesViewModel, toolbarBluetoothSettingsOnClick: () -> Unit, contactUsOnClick: () -> Unit) {
    val bluetoothDevices = bluetoothDevicesViewModel.observePairedDevices.collectAsState()

    MainActivityScreen(
        bluetoothDevices = bluetoothDevices.value,
        toolbarBluetoothSettingsOnClick,
        contactUsOnClick
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainActivityScreen(bluetoothDevices: List<BluetoothDevice>, toolbarBluetoothSettingsOnClick: () -> Unit, contactUsOnClick: () -> Unit) {
    AppTheme {
        Scaffold(
            topBar = { BluetoothDevicesTopAppBar(
                systemBluetoothSettingsOnClick = toolbarBluetoothSettingsOnClick,
                contactUsOnClick = contactUsOnClick)
            }
        ) { contentPadding ->
            Column(Modifier.padding(top = contentPadding.calculateTopPadding())) {
                LazyColumn {
                    items(bluetoothDevices, key = { it.hardwareAddress }) {
                        Row(Modifier.padding(10.dp)) {
                            BluetoothBatteryProgressBar(it.batteryLevel, modifier = Modifier
                                .fillMaxHeight()
                                .align(Alignment.CenterVertically)
                                .padding(end = 10.dp))

                            Column {
                                Row {
                                    Text(text = it.name)
                                    IsDemoView(isDemo = it.isDemo, modifier = Modifier
                                        .align(Alignment.CenterVertically)
                                        .padding(start = 6.dp))
                                }

                                DeviceLastConnectedText(it.lastTimeConnected)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DeviceLastConnectedText(lastConnected: Instant?) {
    val text = lastConnected?.toRelativeTimeSpanString() ?: "Connected"
    Text(text = text, color = Color.Gray, fontSize = 12.sp)
}

@Composable
fun IsDemoView(isDemo: Boolean, modifier: Modifier = Modifier) {
    if (!isDemo) return

    Text(text = "(Demo)", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, modifier = modifier)
}

@Composable
fun BluetoothBatteryProgressBar(batteryLevel: Int, modifier: Modifier) {
    val color: Color = when {
        batteryLevel <= 20 -> BatteryLevelLow
        batteryLevel <= 40 -> BatteryLevelMedium
        else -> BatteryLevelHigh
    }

    Box(modifier.size(50.dp)) {
        CircularProgressIndicator(
            batteryLevel / 100f,
            color = color,
            strokeCap = StrokeCap.Round,
            modifier = Modifier.fillMaxSize(),
            trackColor = if (isSystemInDarkTheme()) BatteryLevelTrackDark else BatteryLevelTrackLight
        )
        Row(
            Modifier
                .align(Alignment.Center)
                .padding(start = 4.dp)) {
            Text(text = "$batteryLevel", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
            Text(text = "%", fontWeight = FontWeight.Bold, fontSize = 8.sp, color = color, modifier = Modifier
                .align(Alignment.Bottom)
                .padding(bottom = 3.dp))
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BluetoothDevicesTopAppBar(systemBluetoothSettingsOnClick: () -> Unit, contactUsOnClick: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = { Text("Devices") },
        actions = {
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "open menu")
                }
                MiscMenuOptionsDropdown(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }, systemBluetoothSettingsOnClick = systemBluetoothSettingsOnClick, contactUsOnClick = contactUsOnClick)
            }
        }
    )
}

@Composable
fun MiscMenuOptionsDropdown(expanded: Boolean, onDismissRequest: () -> Unit, systemBluetoothSettingsOnClick: () -> Unit, contactUsOnClick: () -> Unit) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        DropdownMenuItem(text = { Text("System Bluetooth Settings") }, onClick = systemBluetoothSettingsOnClick)
        DropdownMenuItem(text = { Text("Contact Us") }, onClick = contactUsOnClick)
    }
}

@Composable
@Preview(device = "id:pixel_5")
@Preview(device = "id:pixel_5",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
fun MainActivityScreenPhonePreview() {
    MainActivityScreen(bluetoothDevices = Samples.bluetoothDevices, {}) {}
}