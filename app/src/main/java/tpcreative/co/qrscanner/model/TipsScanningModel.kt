package tpcreative.co.qrscanner.model

import androidx.annotation.DrawableRes
import com.google.zxing.BarcodeFormat
import java.io.Serializable

class TipsScanningModel (val enumAction: EnumAction, @DrawableRes var icon : Int, @DrawableRes var iconStatus: Int, var tintColor : Int) :
    Serializable {
}