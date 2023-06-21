package app

import android.app.Application
import app.android.androidNotifications


class MainApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        DiGraph.initialize(this)

        DiGraph.instance.androidNotifications.createChannels()
    }
}