package tpcreative.co.qrscanner.common.extension

import android.content.res.Resources
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorLong
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.services.QRScannerApplication


fun Int.toText() : String{
    return QRScannerApplication.getInstance().getString(this)
}

val Int.dp: Int get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Int.stringRes: String get() = QRScannerApplication.getInstance().getString(this)
val Int.stringHex: String get() = String.format(
    "#%08x",
    ContextCompat.getColor(QRScannerApplication.getInstance(), this) and 0xffffffff.toInt()
)
val Int.stringHexNoTransparency: String get() = String.format("#%06x", ContextCompat.getColor(QRScannerApplication.getInstance(), this) and 0xffffff)

fun Int.toColorLong() : Long{
    return try {
        this.toColorLong()
    }
    catch (e : Exception){
        R.color.white.toColorLong()
    }
}