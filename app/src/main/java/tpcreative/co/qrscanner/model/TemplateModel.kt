package tpcreative.co.qrscanner.model

import tpcreative.co.qrscanner.common.EnumIcon
import tpcreative.co.qrscanner.common.Utils

class TemplateModel(val id : String,val enumShape: EnumShape,val enumIcon : EnumIcon,val enumChangeDesignType : EnumChangeDesignType,val changeDesign : ChangeDesignModel) : java.io.Serializable,Comparable<TemplateModel> {

    override fun compareTo(other: TemplateModel) = compareValuesBy(this, other,
        { it.enumIcon },
        { it.enumIcon }
    )
}