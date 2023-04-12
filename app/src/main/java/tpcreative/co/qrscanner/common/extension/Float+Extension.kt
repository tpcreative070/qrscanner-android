package tpcreative.co.qrscanner.common.extension

import android.content.res.Resources

fun Float.avoidNAN() : Float{
    if (this.isNaN()){
        return 0F
    }
    return this
}

val Float.dp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Float.px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()