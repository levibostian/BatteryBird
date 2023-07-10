package app.ui

import android.content.Intent
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
import app.activity.DevicesRoute
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.runtime.CompositionLocalProvider

sealed class Screen(val route: String) {
    object Devices : Screen("devices")
    object AddDevice : Screen("addDevice")
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    deepLinkState: StateFlow<Intent?>,
    startDestination: String = Screen.Devices.route
) {
    val navController = rememberNavController()

    deepLinkState.collectAsState(null).value?.let {
        navController.handleDeepLink(it)
    }

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
        route = Screen.Devices.route,
        deepLinks = (getDeepLink("devices"))
    ) {
        DevicesRoute(onAddDeviceClicked = {
            navController.navigate(Screen.AddDevice.route)
        })
    }
}

internal fun NavGraphBuilder.addAddDeviceRoute(
    navController: NavHostController
) {
    composable(
        route = Screen.Devices.route,
        deepLinks = (getDeepLink("addDevice"))
    ) {
        // TODO: Add AddDeviceRoute
        //AddDeviceRoute(onBack = {
//            navController.navigateUp()
//        })
    }
}

fun getDeepLink(screen: String, arguments: String? = null): List<NavDeepLink> {
    return listOf(
        navDeepLink {
            uriPattern = "batterybird://$screen" + if (arguments != null) "?$arguments" else ""
        },
        navDeepLink {
            uriPattern = "https://batterybird.app/$screen" + if (arguments != null) "?$arguments" else ""
        }
    )
}