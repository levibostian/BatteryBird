package app.extensions

fun Int.secondsToMillis(): Long {
    return this.toLong() * 1000
}

