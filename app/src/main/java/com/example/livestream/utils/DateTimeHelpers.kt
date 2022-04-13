package com.example.livestream.utils

import java.text.SimpleDateFormat
import java.util.*


val offsetFromUTCInSeconds =
    (TimeZone.getDefault().rawOffset + TimeZone.getDefault().dstSavings) / 1000

/**
 * Converts unix timestamp in (UTC) seconds to readable string.
 * */
fun Long.toReadableDateTime(): String {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)
    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.US)

    val date = Date(this * 1000)

    return "${dateFormatter.format(date)}, ${timeFormatter.format(date).lowercase(Locale.US)}"
}
