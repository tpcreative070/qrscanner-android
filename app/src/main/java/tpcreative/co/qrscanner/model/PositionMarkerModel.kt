package tpcreative.co.qrscanner.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import tpcreative.co.qrscanner.common.EnumIcon

class PositionMarkerModel(@DrawableRes val icon: Int, val enumIcon: EnumIcon, var isSelected : Boolean, @ColorRes val tint : Int, val enumChangeDesignType: EnumChangeDesignType) : java.io.Serializable,Comparable<PositionMarkerModel> {
    override fun compareTo(other: PositionMarkerModel) = compareValuesBy(this, other,
        { it.enumIcon },
        { it.enumIcon }
    )
}