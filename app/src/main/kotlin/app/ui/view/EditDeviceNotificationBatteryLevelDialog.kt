package app.ui.view


import android.content.res.Configuration
import android.widget.ProgressBar
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
fun EditDeviceNotificationBatteryLevelDialog(
    currentDeviceNotificationBatteryLevel: Int,
    done: (Int?) -> Unit
) {
    var newValue by remember { mutableIntStateOf(currentDeviceNotificationBatteryLevel) }

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
                    text = "What battery percentage would you like to be notified at for this device?",
                    style = MaterialTheme.typography.titleMedium
                )

                Row {
                    Text(text = "$newValue%", modifier = Modifier.padding(top = 14.dp))
                    Slider(value = newValue.toFloat(), valueRange = 0f..100f, onValueChange = {
                        newValue = it.toInt()
                    })
                }

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
                        done(newValue)
                    }) {
                        Text(text = "Change level")
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
fun EditDeviceNotificationBatteryLevelDialogPreview() {
    EditDeviceNotificationBatteryLevelDialog(20) {}
}
