package tpcreative.co.qrscanner.common.extension

import android.content.Context
import tpcreative.co.qrscanner.common.services.QRScannerApplication

fun Int.toText() : String{
    return QRScannerApplication.getInstance().getString(this)
}

fun Int.pxToDp():Float{
    return this * QRScannerApplication.getInstance().resources.displayMetrics.density
}

fun Int.dpToPx() : Float{
    return this * QRScannerApplication.getInstance().resources.displayMetrics.density
}