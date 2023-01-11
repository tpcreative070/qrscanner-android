package tpcreative.co.qrscanner.common.extension

import android.content.res.Configuration
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_scanner.*

fun Fragment.isLandscape():Boolean{
    val currentOrientation = resources.configuration.orientation
    return (currentOrientation == Configuration.ORIENTATION_LANDSCAPE)
}