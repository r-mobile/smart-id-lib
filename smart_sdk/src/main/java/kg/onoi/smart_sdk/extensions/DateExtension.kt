package kg.onoi.smart_sdk.extensions

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


fun Date.toFormattedString(pattern: String = "dd.MM.yyyy") = SimpleDateFormat(pattern).format(this)

fun Long.toMinuteSecondTime(): String {
    return String.format("%02d:%02d",
        TimeUnit.MILLISECONDS.toMinutes(this),
        TimeUnit.MILLISECONDS.toSeconds(this) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this)))
}