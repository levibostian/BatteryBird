package app

import android.app.Application
import app.android.androidNotifications


class MainApplication: Application() {

    lateinit var diGraph: DiGraph

    override fun onCreate() {
        super.onCreate()

        diGraph = DiGraph(this).apply {
            androidNotifications.createChannels()
        }
    }
}