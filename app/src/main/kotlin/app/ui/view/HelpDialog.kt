package app.ui.view

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

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