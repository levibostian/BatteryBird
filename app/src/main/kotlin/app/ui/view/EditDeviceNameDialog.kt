package app.ui.view


import android.content.res.Configuration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDeviceNameDialog(
    currentDeviceName: String,
    done: (String?) -> Unit
) {
    var newNameValue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { done(null) },
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Surface(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Change the name of your device within the app.",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Current name: $currentDeviceName",
                    modifier = Modifier.padding(top = 8.dp)
                )

                TextField(
                    placeholder = { Text(text = "Workout headphones") },
                    value = newNameValue,
                    onValueChange = { newNameValue = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(onClick = { done(null) }) {
                        Text(text = "Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(onClick = {
                        done(newNameValue)
                    }) {
                        Text(text = "Change name")
                    }
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun EditableTextDialogPreview() {
    EditDeviceNameDialog("Current device name") {}
}
