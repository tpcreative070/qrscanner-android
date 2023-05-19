package tpcreative.co.qrscanner.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import tpcreative.co.qrscanner.common.EnumIcon

class PositionMarkerModel(val enumIcon: EnumIcon, var isSelected : Boolean,val tintColorHex : String?, val enumChangeDesignType: EnumChangeDesignType,val enumPositionMarker : EnumPositionMarker) : java.io.Serializable,Comparable<PositionMarkerModel> {
    override fun compareTo(other: PositionMarkerModel) = compareValuesBy(this, other,
        { it.enumIcon },
        { it.enumIcon }
    )
}