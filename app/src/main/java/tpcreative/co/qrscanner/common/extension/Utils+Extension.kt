package tpcreative.co.qrscanner.common.extension

import android.net.Uri
import com.github.mangstadt.vinnie.io.SyntaxRules.vcard
import ezvcard.Ezvcard
import ezvcard.VCard
import ezvcard.parameter.TelephoneType
import ezvcard.property.Address
import ezvcard.property.Email
import ezvcard.property.Telephone
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.SaveModel
import java.io.InputStream


fun Utils.readVCF(uri : Uri) : SaveModel {
    val mResult  = SaveModel()
    try {
        val inputStream: InputStream? = QRScannerApplication.getInstance().contentResolver.openInputStream(uri)
        val vCards: VCard = Ezvcard.parse(inputStream).first()
        val mName = vCards.formattedName.value
        var mPhone : String? = null
        for (tel : Telephone in vCards.telephoneNumbers){
            if (tel.text!=null){
                mPhone = tel.text
            }
        }

        var mAddress : String? =  null
        for (address : Address in vCards.addresses){
            if (address.streetAddressFull!=null){
                mAddress = address.streetAddressFull
            }
        }

        var mEmail : String? = null
        for (address : Email in vCards.emails){
            if (address.value!=null){
                mEmail = address.value
            }
        }

        mResult.fullName = mName
        mResult.phone = mPhone
        mResult.email = mEmail
        mResult.address = mAddress
        inputStream?.close()
        return mResult
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return mResult
}