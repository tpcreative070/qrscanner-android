package tpcreative.co.qrscanner.common.extension

import com.google.zxing.client.result.AddressBookParsedResult
import tpcreative.co.qrscanner.model.GeneralModel

fun GeneralModel.onAddressBook(parsed : AddressBookParsedResult?){
    this.address = parsed?.addresses?.firstOrNull()
    this.fullName = parsed?.names?.firstOrNull()
    this.email = parsed?.emails?.firstOrNull()
    this.phone = parsed?.phoneNumbers?.firstOrNull()
}