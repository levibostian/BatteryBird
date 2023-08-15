package app.ui.widgets

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.ui.theme.ErrorMessageColor

@Composable
fun ErrorText(text: String, modifier: Modifier = Modifier) {
    Text(text = text, color = ErrorMessageColor, modifier = modifier)
}