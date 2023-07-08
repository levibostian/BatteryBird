package app.extensions

fun Int.secondsToMillis(): Long {
    return this.toLong() * 1000
}

fun Int.minutesToMillis(): Long {
    return (this * 60).secondsToMillis()
}

fun Double.secondsToMillis(): Long {
    return this.toLong() * 1000
}

