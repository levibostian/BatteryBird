package earth.levi.app.extensions

import android.app.Activity
import android.content.Intent
import android.net.Uri

fun Activity.systemBluetoothSettingsIntent() = Intent().apply {
    action = android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
}

// https://developer.android.com/guide/components/intents-common.html#Email
fun Activity.supportEmailIntent() = Intent(Intent.ACTION_SEND).apply {
    data = Uri.parse("mailto:"); // only email apps should handle this
    putExtra(Intent.EXTRA_EMAIL, "batterybird@curiosityio.com")
    putExtra(Intent.EXTRA_SUBJECT, "Support")
    putExtra(Intent.EXTRA_TEXT, "What can I help you with? Enter your question or comment here.")
    // TODO: attach log file
}