package tpcreative.co.qrscanner.common.extension

import tpcreative.co.qrscanner.common.services.QRScannerApplication

fun Int.toText() : String{
    return QRScannerApplication.getInstance().getString(this)
}