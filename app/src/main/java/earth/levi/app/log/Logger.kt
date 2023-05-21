package earth.levi.app.log

import android.util.Log
import earth.levi.app.DiGraph
import kotlin.reflect.KClass

val DiGraph.logger: Logger
    get() = Logger()

class Logger {
    private val tag = "[bluetooth-battery]" //"[${BuildConfig.APPLICATION_ID}]"

    fun <T: Any> debug(message: String, caller: T) {
        Log.d(tag, getMessage(message, caller))
    }

    private fun <T: Any> getMessage(message: String, caller: T): String {
        return listOfNotNull(caller.javaClass.simpleName, message).joinToString(separator = " ")
    }
}