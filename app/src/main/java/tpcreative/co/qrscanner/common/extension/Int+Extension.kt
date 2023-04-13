package tpcreative.co.qrscanner.common.extension

import android.content.res.Resources
import tpcreative.co.qrscanner.common.services.QRScannerApplication

fun Int.toText() : String{
    return QRScannerApplication.getInstance().getString(this)
}

val Int.dp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()