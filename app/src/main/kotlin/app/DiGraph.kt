package app

import android.app.Activity
import android.app.Application
import android.app.NotificationManager
import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import earth.levi.batterybird.store.Database
import earth.levi.batterybird.store.DatabaseStore
import earth.levi.batterybird.store.DriverFactory
import earth.levi.batterybird.store.createDatabase
import java.lang.ref.WeakReference

class DiGraph(
    val bluetoothManager: BluetoothManager,
    val notificationManager: NotificationManager,
    val sharedPreferences: SharedPreferences,
    val database: DatabaseStore,
    private val applicationReference: WeakReference<Application>
) {

    // If DiGraph is still in memory, then the application must also be in memory since it's the object keeping the Digraph reference.
    // So it's safe to use !! here.
    val application: Application
        get() = applicationReference.get()!!

    constructor(application: Application): this(
        // construct dependencies that require context now to avoid holding a strong reference to a Context and cause memory leak
        bluetoothManager = application.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager,
        notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager,
        sharedPreferences = application.getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE),
        database = DatabaseStore(createDatabase(DriverFactory(application))),
        applicationReference = WeakReference(application)
    )

    val overrides: MutableMap<String, Any> = mutableMapOf()

    fun <DEP> override(dependency: Class<DEP>, value: DEP) {
        overrides[dependency.name] = value as Any
    }

    inline fun <reified DEP> override(): DEP? = overrides[DEP::class.java.name] as? DEP
}

// How to get a ViewModel instance from within an Activity.
// Example:
// private val bluetoothDevicesViewModel by viewModelDiGraph { bluetoothDevicesViewModel }
inline fun <reified VM : ViewModel> ComponentActivity.viewModelDiGraph(
    noinline createInstance: (DiGraph.() -> VM)
): Lazy<VM> {
    return viewModels {
        object : ViewModelProvider.Factory {
            override fun <T: ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return createInstance((application as MainApplication).diGraph) as T
            }
        }
    }
}

// How to get a ViewModel instance from within a Composable function.
// Example:
// val bluetoothDevicesViewModel: BluetoothDevicesViewModel = viewModelFromActivity()
// Depends on androidx.lifecycle:lifecycle-viewmodel-compose
//
// You don't need to provide a Factory to construct the ViewModel, because the ViewModel is already constructed by the Activity and this function's job is to get it from there.
@Composable
inline fun <reified VM : ViewModel> viewModelFromActivity(): VM {
    // Parameter of providing Activity is to add compatibility when the Composable is inside of a NavHost.
    // https://stackoverflow.com/a/68996426
    return androidx.lifecycle.viewmodel.compose.viewModel(LocalContext.current as ComponentActivity)
}

val Activity.diGraph: DiGraph
    get() = (application as MainApplication).diGraph

val Service.diGraph: DiGraph
    get() = (application as MainApplication).diGraph

// Designed to be called from a BroadcastReceiver or Worker.
val Context.digraph: DiGraph?
    get() = (this as? MainApplication)?.diGraph