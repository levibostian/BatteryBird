package app.extensions

fun Int.secondsToMillis(): Long {
    return this.toLong() * 1000
}

fun Double.secondsToMillis(): Long {
    return this.toLong() * 1000
}

