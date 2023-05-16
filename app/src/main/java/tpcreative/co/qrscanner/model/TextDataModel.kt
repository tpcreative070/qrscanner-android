package tpcreative.co.qrscanner.model

import tpcreative.co.qrscanner.common.Constant
import tpcreative.co.qrscanner.common.EnumFont
import vadiole.colorpicker.hexColor

class TextDataModel (val currentColor :String =  Constant.defaultColor.hexColor,
         val currentFont : String = EnumFont.roboto_regular.name,
         val currentBackgroundColor : String = Constant.defaultColor.hexColor,
         val currentText : String = "",
         val currentFontSize : Int = 90)  : java.io.Serializable{

}