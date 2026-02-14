package com.nuda.nudaclient.extensions

import java.text.NumberFormat
import java.util.Locale

fun Int.toFormattedPrice() : String {
    return NumberFormat.getNumberInstance(Locale.KOREA).format(this) + "원"
}
