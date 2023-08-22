package app.ui.view

import android.content.res.Configuration
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.End
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.R
import app.extensions.findActivity
import app.extensions.minus
import app.extensions.now
import app.extensions.relativeTimeFlow
import app.extensions.supportEmailIntent
import app.extensions.systemBluetoothSettingsIntent
import app.extensions.toRelativeTimeSpanString
import app.model.samples.Samples
import app.model.samples.bluetoothDevices
import app.ui.theme.BatteryLevelHigh
import app.ui.theme.BatteryLevelLow
import app.ui.theme.BatteryLevelMedium
import app.ui.theme.BatteryLevelTrackDark
import app.ui.theme.BatteryLevelTrackLight
import app.ui.theme.BatteryLevelUnknown
import app.ui.type.AnyCTA
import app.ui.type.ButtonCTA
import app.ui.type.RuntimePermission
import app.ui.type.RuntimePermissionCTA
import app.ui.widgets.ClickableText
import app.viewModelFromActivity
import app.viewmodel.BluetoothDevicesViewModel
import earth.levi.batterybird.BluetoothDeviceModel
import kotlinx.datetime.Instant

@Composable
fun DevicesList(onAddDeviceClicked: () -> Unit) {
    val bluetoothDevicesViewModel: BluetoothDevicesViewModel = viewModelFromActivity()

    val bluetoothDevices = bluetoothDevicesViewModel.observePairedDevices.collectAsState()
    val isDemoMode = bluetoothDevicesViewModel.isDemoMode.collectAsState()
    val lastTimeAllDeviceBatteryLevelsUpdated = bluetoothDevicesViewModel.observeLastTimeAllDevicesBatteryLevelUpdated.collectAsState()
    val context = LocalContext.current

    var deviceToEditName: BluetoothDeviceModel? by remember { mutableStateOf(null) }

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
        lastTimeAllDeviceBatteryLevelsUpdated = lastTimeAllDeviceBatteryLevelsUpdated.value,
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
        onAddDeviceClicked = onAddDeviceClicked,
        onDeviceNameClicked = {
            deviceToEditName = it
        }
    )

    deviceToEditName?.let {
        EditDeviceNameDialog(
            currentDeviceName = it.name,
            done = { newName ->
                deviceToEditName = null
                val newName = newName ?: return@EditDeviceNameDialog
                bluetoothDevicesViewModel.updateDeviceName(it, newName)
            }
        )
    }
}

@Composable
fun DevicesList(bluetoothDevices: List<BluetoothDeviceModel>,
                lastTimeAllDeviceBatteryLevelsUpdated: Instant?,
                isDemoMode: Boolean,
                setupCTAs: List<AnyCTA>,
                ctaActionOnClick: (AnyCTA) -> Unit,
                toolbarBluetoothSettingsOnClick: () -> Unit,
                contactUsOnClick: () -> Unit,
                onAddDeviceClicked: () -> Unit,
                onDeviceNameClicked: (BluetoothDeviceModel) -> Unit) {

    val humanReadableLastTimeAllDeviceBatteryLevelsUpdated = lastTimeAllDeviceBatteryLevelsUpdated?.relativeTimeFlow()?.collectAsState(initial = null)

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

            if (!isDemoMode && humanReadableLastTimeAllDeviceBatteryLevelsUpdated != null) {
                Text(text = "Battery levels updated: ${humanReadableLastTimeAllDeviceBatteryLevelsUpdated.value}", modifier = Modifier
                    .align(End)
                    .padding(end = 10.dp))
            }

            Column {
                Box(Modifier.weight(1f)) {
                    if (bluetoothDevices.isEmpty()) {
                        ListEmptyView(openBluetoothSettingsOnClick = toolbarBluetoothSettingsOnClick)
                    } else {
                        BluetoothDevicesList(bluetoothDevices = bluetoothDevices, isDemoMode = isDemoMode, onAddDeviceClicked = onAddDeviceClicked, onDeviceNameClicked = onDeviceNameClicked)
                    }
                }

                setupCTAs.firstOrNull()?.let { cta ->
                    CTAView(cta = cta, onClick = ctaActionOnClick, modifier = Modifier.padding(vertical = 20.dp, horizontal = 20.dp))
                }
            }
        }
    }
}

@Composable
fun BluetoothDevicesList(bluetoothDevices: List<BluetoothDeviceModel>, isDemoMode: Boolean, onAddDeviceClicked: () -> Unit, onDeviceNameClicked: (BluetoothDeviceModel) -> Unit) {
    Column {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 140.dp),
            contentPadding = PaddingValues(5.dp)
        ) {
            items(bluetoothDevices, key = { it.hardwareAddress }) {
                Box(Modifier.padding(5.dp)) {
                    // make the card have padding for all children on the inside of it.
                    Card(Modifier.fillMaxSize()) {
                        Box(
                            Modifier
                                .padding(top = 10.dp)
                                .align(Alignment.CenterHorizontally)) {
                            BluetoothBatteryProgressBar(it.batteryLevel, modifier = Modifier
                                .size(80.dp))
                        }

                        Text(
                            modifier = Modifier
                                .padding(5.dp)
                                .align(Alignment.CenterHorizontally)
                                .clickable {
                                    onDeviceNameClicked(it)
                                },
                            text = it.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            minLines = 2
                        )
                    }

                    if (isDemoMode) {
                        IsDemoView(isDemo = isDemoMode, modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(all = 5.dp))
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
        BluetoothDevicesList(bluetoothDevices = Samples.bluetoothDevices, isDemoMode = true, onAddDeviceClicked = {}, onDeviceNameClicked = {})

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
fun IsDemoView(isDemo: Boolean, modifier: Modifier = Modifier) {
    if (!isDemo) return

    Text(text = "(Demo)", color = MaterialTheme.colorScheme.primary, fontSize = 10.sp, modifier = modifier)
}

@Composable
fun BluetoothBatteryProgressBar(batteryLevel: Long?, modifier: Modifier) {
    val heightOfView = 50.dp

    val color: Color = when {
        batteryLevel == null -> BatteryLevelUnknown
        batteryLevel <= 20 -> BatteryLevelLow
        batteryLevel <= 40 -> BatteryLevelMedium
        else -> BatteryLevelHigh
    }

    if (batteryLevel == null) {
        Column(
            modifier
                .height(heightOfView)
                .padding(top = 20.dp)) {
            Image(painter = painterResource(id =  R.drawable.not_connected_icon), contentDescription = "", modifier = Modifier
                .align(CenterHorizontally)
                .size(18.dp))
            Text(text = "Connect device to get battery level", fontSize = 10.sp, color = color, modifier = Modifier.align(
                CenterHorizontally))
        }
        return
    }

    Box(modifier.size(heightOfView)) {
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
fun DevicesListPhonePreview() {
    DevicesList(
        bluetoothDevices = Samples.bluetoothDevices,
        lastTimeAllDeviceBatteryLevelsUpdated = now().minus(minutes = 1),
        isDemoMode = true,
        setupCTAs = listOf(RuntimePermissionCTA.sample), {},  {}, {}, {}, {})
}

@Composable
@Preview(device = "id:pixel_5")
@Preview(device = "id:pixel_5",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
fun DevicesListEmptyViewPreview() {
    DevicesList(
        bluetoothDevices = emptyList(),
        lastTimeAllDeviceBatteryLevelsUpdated = now().minus(minutes = 1),
        isDemoMode = true,
        setupCTAs =  listOf(RuntimePermissionCTA.sample), {},  {}, {}, {}, {})
}