package tpcreative.co.qrscanner.common.extension

import android.content.res.Configuration
import androidx.fragment.app.Fragment

fun Fragment.isPortrait():Boolean{
    try {
        val currentOrientation = resources.configuration.orientation
        return (currentOrientation == Configuration.ORIENTATION_PORTRAIT)
    }catch (e : Exception){
        e.printStackTrace()
        return true
    }
}