package tpcreative.co.qrscanner.common.extension

import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.model.GeneralModel
import tpcreative.co.qrscanner.model.SaveModel

fun SaveModel.onAddressBook(mGeneral : GeneralModel){
    if (Utils.isVcard(this.code)){
        this.address = mGeneral.contact?.addresses?.values?.firstOrNull()?.getAddressValue()
    }else{
        this.address = mGeneral.contact?.addresses?.values?.firstOrNull()?.address
    }
    this.fullName = mGeneral.contact?.fullName
    this.email = mGeneral.contact?.emails?.values?.firstOrNull()
    this.phone = mGeneral.contact?.phones?.values?.firstOrNull()
}