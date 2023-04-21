package tpcreative.co.qrscanner.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import tpcreative.co.qrscanner.common.EnumIcon

class ChangeDesignCategoryModel(@DrawableRes val icon: Int, val title: String, val enumView: EnumView, @ColorRes val tint : Int, val enumIcon: EnumIcon)  :java.io.Serializable {

}