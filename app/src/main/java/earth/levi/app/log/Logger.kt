package earth.levi.app.log

import android.util.Log
import earth.levi.app.BuildConfig

object Logger {
    private val tag = "[${BuildConfig.APPLICATION_ID}]"

    fun debug(message: String) {
        Log.d(tag, message)
    }
}