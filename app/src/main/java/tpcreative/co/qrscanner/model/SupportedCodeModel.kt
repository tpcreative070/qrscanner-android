package tpcreative.co.qrscanner.model

import androidx.annotation.DrawableRes
import com.google.zxing.BarcodeFormat
import java.io.Serializable

class SupportedCodeModel (var barcodeFormat: BarcodeFormat, val code : String,@DrawableRes var icon : Int, @DrawableRes var iconStatus: Int,var tintColor : Int) :
    Serializable {
}