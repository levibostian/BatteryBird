package earth.levi.app

import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.getSystemService


class DiGraph(
    val bluetoothManager: BluetoothManager,
    val notificationManager: NotificationManager,
    val sharedPreferences: SharedPreferences
) {
    companion object {
        lateinit var instance: DiGraph

        fun initialize(context: Context) {
            instance = DiGraph(
                bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager,
                notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager,
                sharedPreferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE)
            )
        }
    }
}