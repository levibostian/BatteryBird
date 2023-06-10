package earth.levi.app.android

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import earth.levi.app.store.KeyValueStorage
import earth.levi.app.ui.type.RuntimePermission

abstract class AndroidFeature {

    // Each Android feature may require runtime permissions. They are returned in this function.
    abstract fun getRequiredPermissions(): List<RuntimePermission>

    fun isPermissionGranted(permission: RuntimePermission, context: Context): Boolean {
        if (!permission.doesDeviceRequirePermission) return true
        return ContextCompat.checkSelfPermission(context, permission.string) == PackageManager.PERMISSION_GRANTED
    }

}