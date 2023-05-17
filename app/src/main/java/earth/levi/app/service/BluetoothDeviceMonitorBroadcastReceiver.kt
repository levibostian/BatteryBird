package earth.levi.app.service

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import earth.levi.app.log.Logger

class BluetoothDeviceMonitorBroadcastReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Logger.debug("bluetooth device monitor broadcast receiver onReceive")

        when (intent.action) {
            "android.bluetooth.device.action.ACL_DISCONNECTED" -> {
                // check if device is connected to any bluetooth devices that have a battery percentage attached (not a car). if no, stop service. we dont need it.
            }
            "android.bluetooth.device.action.ACL_CONNECTED" -> {
                // Start long-running service monitoring bluetooth devices. checking the battery status every few minutes.
            }
        }

        // Get the bluetooth device from the intent
        val bluetoothDevice: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.extras?.getParcelable("android.bluetooth.device.extra.DEVICE", BluetoothDevice::class.java)
        } else {
            intent.extras?.getParcelable("android.bluetooth.device.extra.DEVICE")
        }

        context.startService(Intent(context, BluetoothDeviceMonitorService::class.java))
    }

}