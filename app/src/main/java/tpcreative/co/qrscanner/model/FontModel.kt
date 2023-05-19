package tpcreative.co.qrscanner.model

import tpcreative.co.qrscanner.common.EnumFont

class FontModel(val enumFontSize: EnumFontSize = EnumFontSize.SMALL,val name : String  = EnumFont.roboto_regular.name,val enumFont: EnumFont = EnumFont.roboto_regular,val fontSize : Int = 20,val fontName : String = EnumFont.roboto_regular.name,val enumChangeDesignType: EnumChangeDesignType = EnumChangeDesignType.NORMAL,var isSelected : Boolean = false): java.io.Serializable,Comparable<FontModel> {
    override fun compareTo(other: FontModel) = compareValuesBy(this, other,
        { it.name },
        { it.name }
    )
}