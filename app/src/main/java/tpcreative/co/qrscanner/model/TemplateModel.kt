package tpcreative.co.qrscanner.model

import tpcreative.co.qrscanner.common.EnumIcon

class TemplateModel(val enumIcon : EnumIcon) : java.io.Serializable,Comparable<TemplateModel> {
    override fun compareTo(other: TemplateModel) = compareValuesBy(this, other,
        { it.enumIcon },
        { it.enumIcon }
    )
}