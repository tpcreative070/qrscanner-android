package tpcreative.co.qrscanner.model

import java.io.Serializable

class ContactModel  : Serializable {
    var givenName : String? = null
    var familyName : String? = null
    var suffixesName : String? = null
    var middleName : String? = null
    var fullName : String? = null
    var jobTitle : String? = null
    var department : String? = null
    var company : String? = null
    var phones : MutableMap<String,String>? = null
    var addresses : MutableMap<String,AddressModel>? = null
    var emails : MutableMap<String,String>? = null
    var urls : MutableList<String>? = null
    var note : String? = null
    var birthday : String? = null
    var nickname : String? = null

    init {
        phones = mutableMapOf()
        addresses = mutableMapOf()
        emails = mutableMapOf()
        urls = mutableListOf()
    }
}