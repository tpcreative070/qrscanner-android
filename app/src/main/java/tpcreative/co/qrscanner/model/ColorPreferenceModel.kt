package tpcreative.co.qrscanner.model

class ColorPreferenceModel(val hexColor : String,val milliSeconds : Long,var isSelected : Boolean = false) : java.io.Serializable,Comparable<ColorPreferenceModel> {
    override fun compareTo(other: ColorPreferenceModel) = compareValuesBy(this, other,
        { it.milliSeconds },
        { it.milliSeconds }
    )
}