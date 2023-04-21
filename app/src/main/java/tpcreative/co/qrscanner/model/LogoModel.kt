package tpcreative.co.qrscanner.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import tpcreative.co.qrscanner.common.EnumIcon
import tpcreative.co.qrscanner.ui.changedesign.fragment.LogoFragment

class LogoModel (@DrawableRes val icon: Int, val enumIcon: EnumIcon, var isSelected : Boolean, @ColorRes val tint : Int, val isRequestIcon : Boolean,val enumChangeDesignType: EnumChangeDesignType,var enumShape: EnumShape)  : java.io.Serializable{
}