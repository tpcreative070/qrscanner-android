package tpcreative.co.qrscanner.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import tpcreative.co.qrscanner.common.EnumIcon

class LogoModel (val enumIcon: EnumIcon, var isSelected : Boolean,val isSupportedBGColor : Boolean,val isSupportedFGColor: Boolean, val tintColorHex : String?, val typeIcon : EnumTypeIcon, val enumChangeDesignType: EnumChangeDesignType, var enumShape: EnumShape)  : java.io.Serializable,Comparable<LogoModel>{
    override fun compareTo(other: LogoModel) = compareValuesBy(this, other,
        { it.enumIcon },
        { it.enumIcon }
    )
}