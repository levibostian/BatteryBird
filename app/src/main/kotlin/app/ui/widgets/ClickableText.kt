package app.ui.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ClickableText(modifier: Modifier = Modifier, text: String, textAlign: TextAlign? = null, color: Color = Color.Unspecified, onClick: () -> Unit) {
    Text(text = text, modifier = modifier.clickable(onClick = onClick).padding(10.dp), fontSize = 18.sp, color = color, textAlign = textAlign)
}

@Preview
@Composable
fun ClickableTextPreview() {
    ClickableText(text = "Click me") {
        // Do something
    }
}
