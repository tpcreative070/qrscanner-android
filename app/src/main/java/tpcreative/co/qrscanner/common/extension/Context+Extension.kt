package tpcreative.co.qrscanner.common.extension

import android.content.Context
import android.content.res.Configuration
import android.util.DisplayMetrics




fun Context.isLandscape():Boolean{
    val currentOrientation = resources.configuration.orientation
    return (currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
}


fun Context.pxToDp(px : Float):Float{
    return px * resources.displayMetrics.density
}

fun Context.dpToPx_(dp: Float) : Float{
    return dp * resources.displayMetrics.density
}