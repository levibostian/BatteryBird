package app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFFF17300)
val Secondary = Color(0xFF4CB944)

// Status bar
val StatusBar = Color(0xFF0B132B)
val StatusBarDark = Color.Black

// Battery levels
val BatteryLevelHigh = Color(0xFF4CB944)
val BatteryLevelMedium = Color(0xFFF17300)
val BatteryLevelLow = Color(0xFF92140C)
val BatteryLevelUnknown = Color(0xFF6D6D6D) // when device has not been connected yet
val BatteryLevelTrackLight = Color(0xFFCCCCCC)
val BatteryLevelTrackDark = Color(0xFF494949)

// UI elements
val ErrorMessageColor = Color(0xFFF17300)

@Composable
fun ButtonDefaults.helpButtonColor(
    darkTheme: Boolean = isSystemInDarkTheme(),
) = ButtonDefaults.buttonColors(
    containerColor = Color.LightGray,
    contentColor = if (darkTheme) Color.Black else Color.White
)