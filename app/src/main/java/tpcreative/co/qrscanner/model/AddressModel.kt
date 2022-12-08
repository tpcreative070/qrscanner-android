package tpcreative.co.qrscanner.model

import java.io.Serializable

class AddressModel : Serializable {
    var address : String? = null
    var street : String? = null
    var city : String? = null
    var region : String? = null
    var postalCode : String? = null
    var country : String? = null

    fun getAddressValue() : String{
        val mStringBuilder = StringBuilder()
        if (street?.isNotEmpty() == true){
            mStringBuilder.append("$street")
        }
        if (city?.isNotEmpty() == true){
            mStringBuilder.append(", ")
            mStringBuilder.append("$city")
        }
        if (region?.isNotEmpty() == true){
            mStringBuilder.append(", ")
            mStringBuilder.append("$region")
        }

        if (postalCode?.isNotEmpty()== true){
            mStringBuilder.append(", ")
            mStringBuilder.append("$postalCode")
        }

        if (country?.isNotEmpty()==true){
            mStringBuilder.append(", ")
            mStringBuilder.append("$country")
        }
        return mStringBuilder.toString()
    }
}