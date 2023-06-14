package earth.levi.app.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import earth.levi.app.ui.theme.AppTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import earth.levi.app.DiGraph
import earth.levi.app.extensions.supportEmailIntent
import earth.levi.app.extensions.systemBluetoothSettingsIntent
import earth.levi.app.extensions.toRelativeTimeSpanString
import earth.levi.app.model.BluetoothDevice
import earth.levi.app.model.samples.Samples
import earth.levi.app.model.samples.bluetoothDevices
import earth.levi.app.ui.theme.BatteryLevelHigh
import earth.levi.app.ui.theme.BatteryLevelLow
import earth.levi.app.ui.theme.BatteryLevelMedium
import earth.levi.app.ui.theme.BatteryLevelTrackDark
import earth.levi.app.ui.theme.BatteryLevelTrackLight
import earth.levi.app.ui.type.AnyCTA
import earth.levi.app.ui.type.RuntimePermission
import earth.levi.app.ui.type.RuntimePermissionCTA
import earth.levi.app.viewModelDiGraph
import earth.levi.app.viewmodel.BluetoothDevicesViewModel
import earth.levi.app.viewmodel.bluetoothDevicesViewModel
import kotlinx.datetime.Instant

class MainActivity : ComponentActivity() {

    companion object {
        fun getIntent(context: Context) = Intent(context, MainActivity::class.java)
    }

    private val bluetoothDevicesViewModel by viewModelDiGraph { DiGraph.instance.bluetoothDevicesViewModel }

    private lateinit var onActivityResultPermission: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onActivityResultPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // user responded to permission request
            bluetoothDevicesViewModel.updateMissingPermissions(this) // get the next permission, if any
        }

        setContent {
            MainActivityComposable(bluetoothDevicesViewModel, toolbarBluetoothSettingsOnClick = {
                startActivity(systemBluetoothSettingsIntent())
            }, contactUsOnClick = {
                startActivity(supportEmailIntent())
            }, setupCtaOnClick = { ctaClicked ->
                when (ctaClicked) {
                    is RuntimePermissionCTA -> {
                        onActivityResultPermission.launch(ctaClicked.permission.string)
                        bluetoothDevicesViewModel.hasAskedForPermission(this, ctaClicked.permission)
                    }
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()

        bluetoothDevicesViewModel.updateMissingPermissions(this)
    }
}

@Composable
fun MainActivityComposable(bluetoothDevicesViewModel: BluetoothDevicesViewModel, toolbarBluetoothSettingsOnClick: () -> Unit, contactUsOnClick: () -> Unit, setupCtaOnClick: (AnyCTA) -> Unit) {
    val bluetoothDevices = bluetoothDevicesViewModel.observePairedDevices.collectAsState()
    val missingPermissionCtas = bluetoothDevicesViewModel.observeMissingPermissions.collectAsState().value.map { missingPermission ->
        RuntimePermissionCTA(
            title = when (missingPermission) {
                RuntimePermission.Bluetooth -> "View your list of Bluetooth devices"
                RuntimePermission.Notifications -> "Low battery reminders"
            },
            description = when (missingPermission) {
                RuntimePermission.Bluetooth -> "App requires permission to Bluetooth to begin monitoring Bluetooth device battery levels. Until then, you will see these demo devices 😉."
                RuntimePermission.Notifications -> "Receive notifications to remind you to charge your Bluetooth devices when they have a low battery."
            },
            actionTitle = when (missingPermission) {
                RuntimePermission.Bluetooth -> "Accept Bluetooth permission"
                RuntimePermission.Notifications -> "Accept notifications permission"
            },
            permission = missingPermission
        )
    }

    MainActivityScreen(
        bluetoothDevices = bluetoothDevices.value,
        setupCTAs = missingPermissionCtas,
        setupCtaOnClick,
        toolbarBluetoothSettingsOnClick,
        contactUsOnClick
    )
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainActivityScreen(
    bluetoothDevices: List<BluetoothDevice>,
    setupCTAs: List<AnyCTA>,
    ctaActionOnClick: (AnyCTA) -> Unit,
    toolbarBluetoothSettingsOnClick: () -> Unit,
    contactUsOnClick: () -> Unit
) {
    AppTheme {
        Scaffold(
            topBar = { BluetoothDevicesTopAppBar(
                systemBluetoothSettingsOnClick = toolbarBluetoothSettingsOnClick,
                contactUsOnClick = contactUsOnClick)
            }
        ) { contentPadding ->
            Box(Modifier.padding(top = contentPadding.calculateTopPadding()).fillMaxSize()) {
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

                setupCTAs.firstOrNull()?.let { cta ->
                    CTAView(cta = cta, onClick = ctaActionOnClick,
                        modifier = Modifier.zIndex(1f).align(Alignment.BottomCenter).padding(vertical = 20.dp, horizontal = 20.dp))
                }
            }
        }
    }
}

@Composable
fun CTAView(cta: AnyCTA, onClick: (AnyCTA) -> Unit, modifier: Modifier = Modifier) {
    Card(modifier.shadow(12.dp)) {
        Column(Modifier.padding(vertical = 10.dp, horizontal = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = cta.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(text = cta.description, modifier = Modifier.padding(10.dp))
            Button(onClick = { onClick(cta) }) {
                Text(text = cta.actionTitle)
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
    MainActivityScreen(
        bluetoothDevices = Samples.bluetoothDevices,
        setupCTAs = listOf(RuntimePermissionCTA.sample), {},  {}, {})
}