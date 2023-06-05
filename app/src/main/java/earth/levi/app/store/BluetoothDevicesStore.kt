package earth.levi.app.store

import android.content.SharedPreferences
import com.fredporciuncula.flow.preferences.FlowSharedPreferences
import earth.levi.app.DiGraph
import earth.levi.app.model.BluetoothDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

interface BluetoothDevicesStore {
    var pairedDevices: List<BluetoothDevice>
    val observePairedDevices: Flow<List<BluetoothDevice>>
}

val DiGraph.bluetoothDevicesStore: BluetoothDevicesStore
    get() = BluetoothDevicesStoreImpl(keyValueStorage)

class BluetoothDevicesStoreImpl(private val keyValueStorage: KeyValueStorage): BluetoothDevicesStore {

    override var pairedDevices: List<BluetoothDevice>
        get() = keyValueStorage.pairedBluetoothDevices
        set(value) {
            keyValueStorage.pairedBluetoothDevices = value
        }

    override val observePairedDevices: Flow<List<BluetoothDevice>>
        get() = keyValueStorage.observePairedBluetoothDevices

}