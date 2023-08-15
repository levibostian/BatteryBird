package app.extensions

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import kotlinx.coroutines.Delay
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.delay

// Created first to ask for runtime permission in a Composable and needed to know how to to call showPermissionRationale() from a Composable.
// Library: https://github.com/google/accompanist/ permissions module does that but I wanted to see if I could avoid adding another dependency. So, read source to see how they do it.
fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("Permissions should be called in the context of an Activity")
}


suspend fun delaySeconds(seconds: Int) = delay(seconds * 1000L)