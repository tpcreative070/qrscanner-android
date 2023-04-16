package tpcreative.co.qrscanner.common.extension

import android.content.res.Resources
import androidx.core.content.ContextCompat
import tpcreative.co.qrscanner.common.services.QRScannerApplication

fun Int.toText() : String{
    return QRScannerApplication.getInstance().getString(this)
}

val Int.dp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Int.StringRes: String get() = QRScannerApplication.getInstance().getString(this)
val Int.StringHex: String get() = "#" + Integer.toHexString(ContextCompat.getColor(QRScannerApplication.getInstance(),this));
val Int.StringHexNoTransparency: String get() = String.format("#%06x", ContextCompat.getColor(QRScannerApplication.getInstance(), this) and 0xffffff)