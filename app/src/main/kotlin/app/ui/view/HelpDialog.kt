package app.ui.view

import android.content.res.Configuration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HelpDialog(title: String = "", text: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = { onDismiss() })
            { Text(text = "OK") }
        },
        title = { if (text.isNotBlank()) Text(text = title) },
        text = { Text(text = text) }
    )
}

@Preview(showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun HelpDialogPreview() {
    HelpDialog(text = "This is a preview of the HelpDialog.") {}
}