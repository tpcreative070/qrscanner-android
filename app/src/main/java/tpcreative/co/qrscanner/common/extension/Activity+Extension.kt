package tpcreative.co.qrscanner.common.extension

import android.app.Activity
import android.content.res.Configuration

fun Activity.isPortrait() : Boolean{
    try {
        val orientation = this.resources?.configuration?.orientation
        return orientation == Configuration.ORIENTATION_PORTRAIT
    }catch (e : Exception){
        e.printStackTrace()
    }
    return false
}