package app.viewmodel

import android.app.Activity
import app.android.AndroidFeature
import app.ui.type.RuntimePermission
import kotlinx.coroutines.flow.StateFlow

interface ViewModelUsingAndroidFeatures {
    val androidFeaturesUsedInViewModel: List<AndroidFeature>

    val observeMissingPermissions: StateFlow<List<RuntimePermission>>
    fun updateMissingPermissions(activity: Activity) // updates the observe Flow value

    fun hasAskedForPermission(activity: Activity, permission: RuntimePermission)
}