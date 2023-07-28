package app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import app.R
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign.Companion.Center
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import app.ui.widgets.ErrorText
import app.ui.widgets.TopAppBar
import app.viewModelFromActivity
import app.viewmodel.BluetoothDevicesViewModel

// This Composable doesn't have a Preview when I wish it did. If I could figure out an easy way to provide ViewModel instances with @Preview, then this could be done.
@Composable
fun AddDeviceScreen(navController: NavHostController) {
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar("Add device", navController) },
    ) { contentPadding ->

        Box(Modifier.padding(contentPadding.calculateTopPadding())) {
            val bluetoothDevicesViewModel: BluetoothDevicesViewModel = viewModelFromActivity()

            AddDevice(modifier = Modifier.padding(top = 20.dp), onAddDevice = { hardwareAddress ->
                bluetoothDevicesViewModel.manuallyAddBluetoothDevice(context, hardwareAddress)

                navController.navigateUp()
            })
        }
    }
}

@Composable
fun AddDevice(modifier: Modifier = Modifier, onAddDevice: (String) -> Unit) {
    var hardwareAddressText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(modifier.fillMaxSize().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Don't see a Bluetooth device listed in the app? Let's add it!", fontSize = 20.sp, fontWeight = Bold, textAlign = Center, modifier = Modifier.padding(vertical = 12.dp))
        Text(text = "Some Bluetooth devices use an app instead the system settings to connect. This means that ${stringResource(id = R.string.app_name)} cannot display the battery level of that device.\n\nTo get around this, we can add the device into ${stringResource(id = R.string.app_name)} manually.")

        BluetoothHardwareAddressTextField(modifier = Modifier.padding(vertical = 20.dp)) {
            hardwareAddressText = it
        }

        ErrorText(text = errorMessage)

        Button(onClick = {
            // validate that the text is a valid MAC address
            val regex = Regex("([0-9A-Fa-f]{2}:){5}[0-9A-Fa-f]{2}")
            if (!regex.matches(hardwareAddressText)) {
                errorMessage = "The Bluetooth hardware address entered is not a valid address. Expected format: 00:00:00:00:00:00"
                return@Button
            }

            onAddDevice(hardwareAddressText)
        }) {
            Text(text = "Add device")
        }
    }
}

@Composable
fun BluetoothHardwareAddressTextField(modifier: Modifier = Modifier, onTextChanged: (String) -> Unit) {
    var text: TextFieldValue by remember { mutableStateOf(TextFieldValue()) }

    TextField(
        modifier = modifier,
        value = text,
        singleLine = true,
        supportingText = { Text("Expected format 00:00:00:00:00:00") },
        onValueChange = { newTextValue ->
            text = newTextValue

            onTextChanged(text.text)
        },
        placeholder = { Text("00:00:00:00:00:00") }
    )
}

@Preview(showBackground = true)
@Composable
fun AddDevicePreview() {
    AddDevice {}
}