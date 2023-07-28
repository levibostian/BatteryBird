package app.ui

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDeepLink
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import app.DiGraph
import app.ui.screens.AddDevice
import app.ui.screens.AddDeviceScreen
import app.ui.screens.DevicesList
import app.ui.widgets.TopAppBar
import app.viewModelFromActivity
import app.viewmodel.BluetoothDevicesViewModel
import app.viewmodel.bluetoothDevicesViewModel

sealed class Screen(val route: String) {
    object Devices : Screen("devices")
    object AddDevice : Screen("addDevice")
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Devices.route
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        addDevicesRoute(navController)
        addAddDeviceRoute(navController)
    }
}

internal fun NavGraphBuilder.addDevicesRoute(
    navController: NavHostController
) {
    composable(
        route = Screen.Devices.route
    ) {
        DevicesList(onAddDeviceClicked = {
            navController.navigate(Screen.AddDevice.route)
        })
    }
}

internal fun NavGraphBuilder.addAddDeviceRoute(
    navController: NavHostController
) {
    composable(
        route = Screen.AddDevice.route
    ) {
        AddDeviceScreen(navController = navController)
    }
}