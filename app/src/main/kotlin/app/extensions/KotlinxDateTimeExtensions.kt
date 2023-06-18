package app.extensions

import android.text.format.DateUtils
import android.text.format.DateUtils.DAY_IN_MILLIS
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.text.format.DateUtils.WEEK_IN_MILLIS
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus

// convenient way to get Instant instance from anywhere.
// you might want to consider mocking the Instant so only use this for certain use cases.
fun now(): Instant = Clock.System.now()

fun Instant.minus(years: Int = 0,
                  months: Int = 0,
                  days: Int = 0,
                  hours: Int = 0,
                  minutes: Int = 0,
                  seconds: Int = 0): Instant = this.minus(DateTimePeriod(years, months, days, hours, minutes, seconds), TimeZone.currentSystemDefault())

fun Instant.toRelativeTimeSpanString(): String {
    val millisTimeAgo = this.toEpochMilliseconds()
    val millisNow = now().toEpochMilliseconds()

    DateUtils.getRelativeTimeSpanString(millisTimeAgo, millisNow, MINUTE_IN_MILLIS).toString().let { if (it.contains("ago")) return it }
    DateUtils.getRelativeTimeSpanString(millisTimeAgo, millisNow, DAY_IN_MILLIS).toString().let { if (it.contains("ago")) return it }
    return DateUtils.getRelativeTimeSpanString(millisTimeAgo, millisNow, WEEK_IN_MILLIS).toString()
}