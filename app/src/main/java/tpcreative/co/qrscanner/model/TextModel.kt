package tpcreative.co.qrscanner.model

import tpcreative.co.qrscanner.common.EnumIcon

class TextModel(val enumIcon: EnumIcon = EnumIcon.ic_qr_background, val type : EnumImage = EnumImage.QR_TEXT_BOTTOM,
                val enumChangeDesignType : EnumChangeDesignType = EnumChangeDesignType.NORMAL, val data : TextDataModel = TextDataModel())  : java.io.Serializable,Comparable<TextModel>{
    override fun compareTo(other: TextModel) = compareValuesBy(this, other,
        { it.type },
        { it.type }
    )
}