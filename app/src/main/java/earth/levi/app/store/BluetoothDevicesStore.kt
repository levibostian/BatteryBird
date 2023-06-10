package earth.levi.app.store

import android.content.SharedPreferences
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import earth.levi.app.DiGraph
import earth.levi.app.model.BluetoothDevice
import earth.levi.app.model.BluetoothDeviceModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

// We want to store bluetooth devices to app storage so that we keep a history of devices. We can then display historical data in the app's UI.
interface BluetoothDevicesStore {
    var pairedDevices: List<BluetoothDeviceModel>
    val observePairedDevices: Flow<List<BluetoothDeviceModel>>
}

val DiGraph.bluetoothDevicesStore: BluetoothDevicesStore
    get() = BluetoothDevicesStoreImpl(keyValueStorage)

class BluetoothDevicesStoreImpl(private val keyValueStorage: KeyValueStorage): BluetoothDevicesStore {

    override var pairedDevices: List<BluetoothDeviceModel>
        get() = keyValueStorage.pairedBluetoothDevices
        set(value) {
            keyValueStorage.pairedBluetoothDevices = value
        }

    override val observePairedDevices: Flow<List<BluetoothDeviceModel>>
        get() = keyValueStorage.observePairedBluetoothDevices

}