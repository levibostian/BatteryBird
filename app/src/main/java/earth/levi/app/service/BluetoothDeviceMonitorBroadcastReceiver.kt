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
            "android.bluetooth.device.action.ACL_DISCONNECTED" -> {}
            "android.bluetooth.device.action.ACL_CONNECTED" -> {}
        }
        
        // intent.action == android.bluetooth.device.action.ACL_DISCONNECTED, a bluetooth device disconnected.
        val bluetoothDevice: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.extras?.getParcelable("android.bluetooth.device.extra.DEVICE", BluetoothDevice::class.java)
        } else {
            intent.extras?.getParcelable("android.bluetooth.device.extra.DEVICE")
        }

        context.startService(Intent(context, BluetoothDeviceMonitorService::class.java))
    }

}