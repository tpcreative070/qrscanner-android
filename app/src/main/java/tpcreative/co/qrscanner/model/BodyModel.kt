package tpcreative.co.qrscanner.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import tpcreative.co.qrscanner.common.EnumIcon

class BodyModel(@DrawableRes val icon: Int, val enumIcon: EnumIcon, var isSelected : Boolean, @ColorRes val tint : Int, val enumChangeDesignType: EnumChangeDesignType,val enumBody : EnumBody) : java.io.Serializable,Comparable<BodyModel> {
    override fun compareTo(other: BodyModel) = compareValuesBy(this, other,
        { it.enumIcon },
        { it.enumIcon }
    )
}