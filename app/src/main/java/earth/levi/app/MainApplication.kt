package earth.levi.app

import android.app.Application
import earth.levi.app.android.Notifications
import earth.levi.app.android.notifications


class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        DiGraph.initialize(this)

        DiGraph.instance.notifications.createChannels()
    }
}