package earth.levi.app.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import earth.levi.app.log.Logger


class BluetoothDeviceMonitorService: Service() {
    override fun onBind(p0: Intent?): IBinder? {
        Logger.debug("service onBind")

        return null
    }
}