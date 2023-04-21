package tpcreative.co.qrscanner.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import tpcreative.co.qrscanner.common.EnumIcon

class LogoModel (@DrawableRes val icon: Int, val enumIcon: EnumIcon, var isSelected : Boolean, @ColorRes val tint : Int, val typeIcon : EnumTypeIcon, val enumChangeDesignType: EnumChangeDesignType, var enumShape: EnumShape)  : java.io.Serializable{
}