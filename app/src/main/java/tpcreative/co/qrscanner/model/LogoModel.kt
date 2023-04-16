package tpcreative.co.qrscanner.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

class LogoModel (@DrawableRes val icon: Int, val title: String, val enumView: EnumView, var isSelected : Boolean, @ColorRes val tint : Int, val isRequestIcon : Boolean)  :java.io.Serializable {

}