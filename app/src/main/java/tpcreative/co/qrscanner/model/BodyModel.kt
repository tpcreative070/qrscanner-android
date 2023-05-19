package tpcreative.co.qrscanner.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import tpcreative.co.qrscanner.common.EnumIcon

class BodyModel(val enumIcon: EnumIcon, var isSelected : Boolean,val tintColorHex : String?, val enumChangeDesignType: EnumChangeDesignType,val enumBody : EnumBody) : java.io.Serializable,Comparable<BodyModel> {
    override fun compareTo(other: BodyModel) = compareValuesBy(this, other,
        { it.enumIcon },
        { it.enumIcon }
    )
}