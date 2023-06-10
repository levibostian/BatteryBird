package earth.levi.app.viewmodel

import android.app.Activity
import android.content.Context
import earth.levi.app.android.AndroidFeature
import earth.levi.app.ui.type.RuntimePermission
import kotlinx.coroutines.flow.StateFlow

interface ViewModelUsingAndroidFeatures {
    val androidFeaturesUsedInViewModel: List<AndroidFeature>

    val observeMissingPermissions: StateFlow<List<RuntimePermission>>
    fun updateMissingPermissions(activity: Activity) // updates the observe Flow value

    fun hasAskedForPermission(activity: Activity, permission: RuntimePermission)
}