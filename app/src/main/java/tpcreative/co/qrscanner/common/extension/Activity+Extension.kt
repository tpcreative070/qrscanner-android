package tpcreative.co.qrscanner.common.extension

import android.app.Activity
import android.content.res.Configuration

fun Activity.isLandscape() : Boolean{
    try {
        val orientation = this.resources?.configuration?.orientation
        return orientation == Configuration.ORIENTATION_LANDSCAPE
    }catch (e : Exception){
        e.printStackTrace()
    }
    return false
}