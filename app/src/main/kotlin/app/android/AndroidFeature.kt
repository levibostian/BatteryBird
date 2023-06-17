package app.android

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import app.ui.type.RuntimePermission

interface AndroidFeature {
    fun getRequiredPermissions(): List<RuntimePermission>
}

abstract class AndroidFeatureImpl: AndroidFeature {

    // Each Android feature may require runtime permissions. They are returned in this function.
    abstract override fun getRequiredPermissions(): List<RuntimePermission>

    fun isPermissionGranted(permission: RuntimePermission, context: Context): Boolean {
        if (!permission.doesDeviceRequirePermission) return true
        return ContextCompat.checkSelfPermission(context, permission.string) == PackageManager.PERMISSION_GRANTED
    }

}