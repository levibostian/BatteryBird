package app.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.isDebugInspectorInfoEnabled
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import app.DiGraph
import app.android.bluetooth
import app.android.workManager
import app.extensions.findActivity
import app.extensions.supportEmailIntent
import app.extensions.systemBluetoothSettingsIntent
import app.extensions.toRelativeTimeSpanString
import app.model.samples.Samples
import app.model.samples.bluetoothDevices
import app.ui.AppNavGraph
import app.ui.theme.AppTheme
import app.ui.theme.BatteryLevelHigh
import app.ui.theme.BatteryLevelLow
import app.ui.theme.BatteryLevelMedium
import app.ui.theme.BatteryLevelTrackDark
import app.ui.theme.BatteryLevelTrackLight
import app.ui.theme.Primary
import app.ui.type.AnyCTA
import app.ui.type.ButtonCTA
import app.ui.type.RuntimePermission
import app.ui.type.RuntimePermissionCTA
import app.ui.widgets.ClickableText
import app.viewModel
import app.viewModelDiGraph
import app.viewmodel.BluetoothDevicesViewModel
import app.viewmodel.bluetoothDevicesViewModel
import earth.levi.batterybird.BluetoothDeviceModel
import kotlinx.datetime.Instant

@Composable
fun DevicesList(onAddDeviceClicked: () -> Unit) {
    val bluetoothDevicesViewModel = DiGraph.instance.viewModel { bluetoothDevicesViewModel }

    val bluetoothDevices = bluetoothDevicesViewModel.observePairedDevices.collectAsState()
    val isDemoMode = bluetoothDevicesViewModel.isDemoMode.collectAsState()
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // user responded to permission request
        bluetoothDevicesViewModel.updateMissingPermissions(context.findActivity()) // get the next permission, if any
    }

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

    DevicesList(
        bluetoothDevices = bluetoothDevices.value,
        isDemoMode = isDemoMode.value,
        setupCTAs = missingPermissionCtas,
        ctaActionOnClick = { ctaClicked ->
            when (ctaClicked) {
                is RuntimePermissionCTA -> {
                    bluetoothDevicesViewModel.hasAskedForPermission(context.findActivity(), ctaClicked.permission)
                    launcher.launch(ctaClicked.permission.string)
                }
            }
        },
        toolbarBluetoothSettingsOnClick = {
            context.startActivity(systemBluetoothSettingsIntent())
        },
        contactUsOnClick = {
            context.startActivity(supportEmailIntent())
        },
        onAddDeviceClicked = onAddDeviceClicked
    )
}

@Composable
fun DevicesList(bluetoothDevices: List<BluetoothDeviceModel>,
                isDemoMode: Boolean,
                setupCTAs: List<AnyCTA>,
                ctaActionOnClick: (AnyCTA) -> Unit,
                toolbarBluetoothSettingsOnClick: () -> Unit,
                contactUsOnClick: () -> Unit,
                onAddDeviceClicked: () -> Unit) {
    Scaffold(
        topBar = { BluetoothDevicesTopAppBar(
            systemBluetoothSettingsOnClick = toolbarBluetoothSettingsOnClick,
            contactUsOnClick = contactUsOnClick
        )}
    ) { contentPadding ->
        Column(
            Modifier
                .padding(top = contentPadding.calculateTopPadding())
                .fillMaxSize()) {

            Box(Modifier.weight(1f)) {
                if (bluetoothDevices.isEmpty()) {
                    ListEmptyView(openBluetoothSettingsOnClick = toolbarBluetoothSettingsOnClick)
                } else {
                    BluetoothDevicesList(bluetoothDevices = bluetoothDevices, isDemoMode = isDemoMode, onAddDeviceClicked = onAddDeviceClicked)
                }
            }

            setupCTAs.firstOrNull()?.let { cta ->
                CTAView(cta = cta, onClick = ctaActionOnClick, modifier = Modifier.padding(vertical = 20.dp, horizontal = 20.dp))
            }
        }
    }
}

@Composable
fun BluetoothDevicesList(bluetoothDevices: List<BluetoothDeviceModel>, isDemoMode: Boolean, onAddDeviceClicked: () -> Unit) {
    Column {
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
                            IsDemoView(isDemo = isDemoMode, modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(start = 6.dp))
                        }

                        DeviceLastConnectedText(it)
                    }
                }
            }
        }

        ManuallyAddDeviceView(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp)) {
            onAddDeviceClicked()
        }
    }
}

@Composable
fun ManuallyAddDeviceView(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth()) {
        ClickableText(
            modifier
                .fillMaxWidth()
                .align(Alignment.Center), textAlign = TextAlign.Center, text = "+ Add device", color = Color.DarkGray, onClick = onClick)
    }
}

@Composable
fun ListEmptyView(openBluetoothSettingsOnClick: () -> Unit) {
    Box {
        BluetoothDevicesList(bluetoothDevices = Samples.bluetoothDevices, isDemoMode = true, onAddDeviceClicked = {})

        CTAView(
            cta = ButtonCTA(
                title = "No Bluetooth devices found",
                description = "Looks like either you don't have any Bluetooth devices paired or your Bluetooth is turned off. \n\nI suggest going into the Bluetooth settings to pair a Bluetooth device or turn on Bluetooth.",
                actionTitle = "Open Bluetooth settings"
            ),
            onClick = { openBluetoothSettingsOnClick() },
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp)
        )
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
fun DeviceLastConnectedText(device: BluetoothDeviceModel) {
    val isDeviceConnected = device.isConnected
    val hasDeviceEverBeenConnected = device.lastTimeConnected != null

    val text = when {
        isDeviceConnected -> "Connected"
        hasDeviceEverBeenConnected -> "Last connected ${device.lastTimeConnected?.toRelativeTimeSpanString()}"
        else -> "Connect device to get battery level"
    }

    Text(text = text, color = Color.Gray, fontSize = 12.sp)
}

@Composable
fun IsDemoView(isDemo: Boolean, modifier: Modifier = Modifier) {
    if (!isDemo) return

    Text(text = "(Demo)", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, modifier = modifier)
}

@Composable
fun BluetoothBatteryProgressBar(batteryLevel: Long?, modifier: Modifier) {
    val color: Color = when {
        batteryLevel == null -> Color.Gray
        batteryLevel <= 20 -> BatteryLevelLow
        batteryLevel <= 40 -> BatteryLevelMedium
        else -> BatteryLevelHigh
    }

    Box(modifier.size(50.dp)) {
        if (batteryLevel != null) {
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
fun DevicesListPhonePreview() {
    DevicesList(
        bluetoothDevices = Samples.bluetoothDevices,
        isDemoMode = true,
        setupCTAs = listOf(RuntimePermissionCTA.sample), {},  {}, {}, {})
}

@Composable
@Preview(device = "id:pixel_5")
@Preview(device = "id:pixel_5",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
fun DevicesListEmptyViewPreview() {
    DevicesList(
        bluetoothDevices = emptyList(),
        isDemoMode = true,
        setupCTAs =  listOf(RuntimePermissionCTA.sample), {},  {}, {}, {})
}