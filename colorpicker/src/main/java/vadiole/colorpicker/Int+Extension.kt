package vadiole.colorpicker

import android.graphics.Color

val Int.hexColor get() = String.format("#%06X", 0xFFFFFF and this)

//val Int.hexWithAlphaColor get() = String.format("#%08X", 0xFFFFFFFF and this)

val Int.hexWithAlphaColor get() = java.lang.String.format("#%08X", -0x1 and this)

fun String.getRgbFromHex(): IntArray {
    val initColor = Color.parseColor(this)
    val r = Color.red(initColor)
    val g = Color.green(initColor)
    val b = Color.blue(initColor)
    return intArrayOf(r, g, b, )
}