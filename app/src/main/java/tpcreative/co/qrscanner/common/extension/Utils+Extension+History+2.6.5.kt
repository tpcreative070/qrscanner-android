package tpcreative.co.qrscanner.common.extension

import tpcreative.co.qrscanner.model.GeneralModel
import tpcreative.co.qrscanner.model.HistoryModel
import tpcreative.co.qrscanner.model.SaveModel

fun HistoryModel.onAddressBook(mGeneral : GeneralModel){
    this.address = mGeneral.address
    this.fullName = mGeneral.fullName
    this.email = mGeneral.email
    this.phone = mGeneral.phone
}