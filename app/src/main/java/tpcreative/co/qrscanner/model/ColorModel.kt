package tpcreative.co.qrscanner.model

import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

class ColorModel(@DrawableRes
                  val icon : Int,
                  val tintColorHex : String?,
val type : EnumImage,
var isSelected : Boolean,
var mapColor : HashMap<EnumImage,String>) : java.io.Serializable, Comparable<ColorModel> {
    override fun compareTo(other: ColorModel) = compareValuesBy(this, other,
        { it.type },
        { it.type }
    )
}