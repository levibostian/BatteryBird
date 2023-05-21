package earth.levi.app

import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.core.content.getSystemService


class DiGraph(
    val bluetoothManager: BluetoothManager,
    val notificationManager: NotificationManager
) {
    companion object {
        lateinit var instance: DiGraph

        fun initialize(context: Context) {
            instance = DiGraph(
                bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager,
                notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            )
        }
    }
}