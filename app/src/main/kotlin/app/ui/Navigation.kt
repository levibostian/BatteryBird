package app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.ui.view.AddDeviceScreen
import app.ui.view.DevicesList

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