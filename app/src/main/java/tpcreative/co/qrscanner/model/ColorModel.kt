package tpcreative.co.qrscanner.model

import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

class ColorModel( @DrawableRes
                  val icon : Int,
    @ColorRes val tint : Int,
val type : EnumImage,
var isSelected : Boolean,
var mapColor : HashMap<EnumImage,String>) : java.io.Serializable {

}