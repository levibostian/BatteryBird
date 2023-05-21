package earth.levi.app.extensions

fun Int.secondsToMillis(): Long {
    return this.toLong() * 1000
}

