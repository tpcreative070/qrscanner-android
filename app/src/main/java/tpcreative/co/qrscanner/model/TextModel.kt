package tpcreative.co.qrscanner.model

import tpcreative.co.qrscanner.common.EnumIcon

class TextModel(val text : String, val tintColorHex : String?, val fontName :String, val color : String, val fontSize : String, val enumIcon: EnumIcon, val type : EnumImage,
                val enumChangeDesignType : EnumChangeDesignType,)  : java.io.Serializable,Comparable<TextModel>{
    override fun compareTo(other: TextModel) = compareValuesBy(this, other,
        { it.type },
        { it.type }
    )
}