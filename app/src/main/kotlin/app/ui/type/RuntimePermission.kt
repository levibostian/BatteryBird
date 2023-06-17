package app.ui.type

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build

// A data type that maps to Android runtime permissions. This is a list of all permissions that app uses.
// We created this data type so the UI can have a finite list of permissions that the app asks for so the UI can create
// CTA messages to try and get the permission accepted.
sealed class RuntimePermission {
    abstract val string: String // Only call this *after* checking if device requires permission or you could crash app
    abstract val doesDeviceRequirePermission: Boolean // Specify what api level the runtime permission was introduced. We do not ask for permission if we don't have to.

    object Bluetooth : RuntimePermission() {
        override val doesDeviceRequirePermission: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        @SuppressLint("InlinedApi") override val string: String = Manifest.permission.BLUETOOTH_CONNECT
    }

    object Notifications: RuntimePermission() {
        override val doesDeviceRequirePermission: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
        @SuppressLint("InlinedApi") override val string: String = Manifest.permission.POST_NOTIFICATIONS
    }
}