package app.viewmodel

import android.app.Activity
import android.os.Build
import androidx.lifecycle.ViewModel
import app.android.AndroidFeature
import app.store.KeyValueStorage
import app.ui.type.RuntimePermission
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

// Class that all ViewModels extend in this app.
// Allows app to have extra functionality in it's ViewModels specific to this app.
abstract class BaseViewModel(override val androidFeaturesUsedInViewModel: List<AndroidFeature>, private val keyValueStorage: KeyValueStorage): ViewModel(), ViewModelUsingAndroidFeatures {

    private var _missingRuntimePermissions: MutableStateFlow<List<RuntimePermission>> = MutableStateFlow(emptyList())
    override val observeMissingPermissions: StateFlow<List<RuntimePermission>> = _missingRuntimePermissions

    // Call this function when your UI starts (onResume good place) and when user interacts with a permission pop-up so you should update permissions needed.
    override fun updateMissingPermissions(activity: Activity)  {
        _missingRuntimePermissions.value = androidFeaturesUsedInViewModel.flatMap {
            it.getRequiredPermissions().filter { permission ->
                // Determining if a permission is missing (meaning you should ask for permission in UI) is a little complex.
                // A permission pop-up can show up twice (maybe only once on some OS versions). A user must deny it twice before the OS does not show a pop-up anymore.
                // Android's docs suggest asking for permission without showing an explanation for the first pop-up and then show a UI explanation after denying the permission for the first time.
                // I don't like that experience as I want the app to open when you first install it and not be asked to login, not be asked for permission, etc.
                // Therefore, we keep track of the first time we ask for permission and then we ask the OS if we should show a UI explantion for a second time.

                val hasNeverShownPermissionPopup = !keyValueStorage.hasAskedForPermission(permission)

                val doesOSSuggestShowingExplanation = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    activity.shouldShowRequestPermissionRationale(permission.string)
                } else {
                    false
                }

                hasNeverShownPermissionPopup || doesOSSuggestShowingExplanation
            }
        }
    }

    // Call when you tell the OS to show the runtime permission pop-up
    override fun hasAskedForPermission(activity: Activity, permission: RuntimePermission) {
        keyValueStorage.permissionHasBeenAsked(permission)
    }

}