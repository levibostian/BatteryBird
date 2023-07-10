package app

import android.app.NotificationManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import earth.levi.batterybird.store.Database
import earth.levi.batterybird.store.DatabaseStore
import earth.levi.batterybird.store.DriverFactory
import earth.levi.batterybird.store.createDatabase

class DiGraph(
    val bluetoothManager: BluetoothManager,
    val notificationManager: NotificationManager,
    val sharedPreferences: SharedPreferences,
    val database: DatabaseStore
) {
    companion object {
        lateinit var instance: DiGraph

        fun initialize(context: Context) {
            instance = DiGraph(
                bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager,
                notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager,
                sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE),
                database = DatabaseStore(createDatabase(DriverFactory(context)))
            )
        }
    }

    val overrides: MutableMap<String, Any> = mutableMapOf()

    fun <DEP> override(dependency: Class<DEP>, value: DEP) {
        overrides[dependency.name] = value as Any
    }

    inline fun <reified DEP> override(): DEP? = overrides[DEP::class.java.name] as? DEP
}

inline fun <reified VM : ViewModel> ComponentActivity.viewModelDiGraph(
    noinline createInstance: (DiGraph.() -> VM)
): Lazy<VM> {
    return viewModels {
        object : ViewModelProvider.Factory {
            override fun <T: ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return createInstance(DiGraph.instance) as T
            }
        }
    }
}

// Composable version of viewModel() to construct instances of ViewModels inside of @Composable functions (such as Screens).
// It finds the nearest lifecycle owner and creates a ViewModel scoped to that lifecycle. You can provide a lifecycle owner.
// Depends on androidx.lifecycle:lifecycle-viewmodel-compose
@Composable
inline fun <reified VM : ViewModel> DiGraph.viewModel(
    noinline createInstance: (DiGraph.() -> VM)
): VM {
    // see viewModelStoreOwner parameter in androidx.lifecycle.viewmodel.compose.viewModel for more info on overriding the lifecycle owner
    return androidx.lifecycle.viewmodel.compose.viewModel(initializer = {
        createInstance()
    })
}