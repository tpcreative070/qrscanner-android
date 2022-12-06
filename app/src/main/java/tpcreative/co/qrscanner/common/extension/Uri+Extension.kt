package tpcreative.co.qrscanner.common.extension

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import tpcreative.co.qrscanner.common.ConstantValue
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.model.AddressModel
import tpcreative.co.qrscanner.model.ContactModel

@SuppressLint("Range")
fun Uri.onParseContact(context: Context) : ContactModel{
    val mContact = ContactModel()
    val cursor: Cursor? = this.let {
        context.contentResolver
            .query(it, null, null, null, null)
    }
    cursor?.let { mCursor ->
        // Double-check that you
        // actually got results
        if (mCursor.count == 0) return@let

        // Pull out the first column
        // of the first row of data
        // that is your contact's name
        if (mCursor.moveToFirst()){

            // To get number - runtime permission is mandatory.
            val id: String = mCursor.getString(mCursor.getColumnIndex(ContactsContract.Contacts._ID))

            val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id.toLong())
            val dataUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Data.CONTENT_DIRECTORY)

            /*Name area*/
            val mNameStructure: Cursor? = context.contentResolver.query(
                dataUri,
                null,
                ContactsContract.Contacts.Data.MIMETYPE + "=?", arrayOf(
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE),
                null
            )
            while (mNameStructure?.moveToNext() == true) {
                val firstName = mNameStructure.getString(mNameStructure.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                val lastName = mNameStructure.getString(mNameStructure.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))

                val middleName = mNameStructure.getString(mNameStructure.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME))

                val mSuffixesName = mNameStructure.getString(mNameStructure.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredName.SUFFIX))

                val mFullName = mNameStructure.getString(mNameStructure.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredName.FULL_NAME_STYLE))

                mContact.givenName = firstName
                mContact.middleName = middleName
                mContact.middleName = middleName
                mContact.familyName  = lastName
                mContact.suffixesName = mSuffixesName
                mContact.fullName = mFullName
            }
            mNameStructure?.close()

            /*Organization area*/
            val mStructuredOrganization: Cursor? = context.contentResolver.query(
                dataUri,
                null,
                ContactsContract.Contacts.Data.MIMETYPE + "=?", arrayOf(
                    ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
                null
            )
            while (mStructuredOrganization?.moveToNext() == true) {
                val companyName = mStructuredOrganization.getString(mStructuredOrganization.getColumnIndex(
                    ContactsContract.CommonDataKinds.Organization.COMPANY))
                val departmentName = mStructuredOrganization.getString(mStructuredOrganization.getColumnIndex(
                    ContactsContract.CommonDataKinds.Organization.DEPARTMENT))
                val titleName = mStructuredOrganization.getString(mStructuredOrganization.getColumnIndex(
                    ContactsContract.CommonDataKinds.Organization.TITLE))
                Utils.Log("onParseContact","Organization 1 $companyName Organization 2 $titleName")
                mContact.company = companyName
                mContact.department = departmentName
                mContact.jobTitle = titleName
            }
            mStructuredOrganization?.close()


            /*Email area*/
            val mStructuredEmail: Cursor? = context.contentResolver.query(
                dataUri,
                null,
                ContactsContract.Contacts.Data.MIMETYPE + "=?", arrayOf(
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE),
                null
            )
            while (mStructuredEmail?.moveToNext() == true) {
                val typeName = mStructuredEmail.getString(mStructuredEmail.getColumnIndex(
                    ContactsContract.CommonDataKinds.Email.TYPE))
                val addressName = mStructuredEmail.getString(mStructuredEmail.getColumnIndex(
                    ContactsContract.CommonDataKinds.Email.ADDRESS))

                var mResultType : String? = ""
                if (typeName == "${ContactsContract.CommonDataKinds.Email.TYPE_HOME}"){
                    mResultType = ConstantValue.HOME
                }else if (typeName == "${ContactsContract.CommonDataKinds.Email.TYPE_WORK}"){
                    mResultType = ConstantValue.WORK
                }else if (typeName == "${ContactsContract.CommonDataKinds.Email.TYPE_MOBILE}"){
                    mResultType = ConstantValue.MOBILE
                }else{
                    mResultType = ConstantValue.OTHER
                }
                Utils.Log("onParseContact","Email 1 $mResultType Email 2 $addressName")
                mContact.emails?.set(mResultType, addressName)
            }
            mStructuredEmail?.close()


            /*Postal area*/
            val mStructuredPostal: Cursor? = context.contentResolver.query(
                dataUri,
                null,
                ContactsContract.Contacts.Data.MIMETYPE + "=?", arrayOf(
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE),
                null
            )
            while (mStructuredPostal?.moveToNext() == true) {
                val typeName = mStructuredPostal.getString(mStructuredPostal.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredPostal.TYPE))
                val addressName = mStructuredPostal.getString(mStructuredPostal.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS))
                val streetName = mStructuredPostal.getString(mStructuredPostal.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredPostal.STREET))
                val cityName = mStructuredPostal.getString(mStructuredPostal.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredPostal.CITY))
                val regionName = mStructuredPostal.getString(mStructuredPostal.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredPostal.REGION))
                val postalName = mStructuredPostal.getString(mStructuredPostal.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE))
                val countryName = mStructuredPostal.getString(mStructuredPostal.getColumnIndex(
                    ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY))
                var mResultType : String? = ""
                if (typeName == "${ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME}"){
                    mResultType = ConstantValue.HOME
                }else if (typeName == "${ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK}"){
                    mResultType = ConstantValue.WORK
                }else{
                    mResultType = ConstantValue.OTHER
                }
                Utils.Log("onParseContact","Postal 1 $mResultType Postal 2 $postalName")
                val mContactOfAddress = AddressModel()
                mContactOfAddress.address = addressName
                mContactOfAddress.street = streetName
                mContactOfAddress.city = cityName
                mContactOfAddress.region = regionName
                mContactOfAddress.postalCode = postalName
                mContactOfAddress.country = countryName
                mContact.addresses?.set(mResultType, mContactOfAddress)
            }
            mStructuredPostal?.close()

            /*Phone area*/
            val mStructuredPhone: Cursor? = context.contentResolver.query(
                dataUri,
                null,
                ContactsContract.Contacts.Data.MIMETYPE + "=?", arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE),
                null
            )
            while (mStructuredPhone?.moveToNext() == true) {
                val typeName = mStructuredPhone.getString(mStructuredPhone.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.TYPE))
                val phoneName = mStructuredPhone.getString(mStructuredPhone.getColumnIndex(
                    ContactsContract.CommonDataKinds.Phone.NUMBER))
                var mResultType : String? = ""
                if (typeName == "${ContactsContract.CommonDataKinds.Phone.TYPE_HOME}"){
                    mResultType = ConstantValue.HOME
                }else if (typeName == "${ContactsContract.CommonDataKinds.Phone.TYPE_WORK}"){
                    mResultType = ConstantValue.WORK
                }else if (typeName == "${ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE}"){
                    mResultType = ConstantValue.CELL
                }else{
                    mResultType = ConstantValue.OTHER
                }
                Utils.Log("onParseContact","Phone 1 $mResultType Phone 2 $phoneName")
                mContact.phones?.set(mResultType, phoneName)
            }
            mStructuredPhone?.close()

            /*Website area*/
            val mStructuredURL: Cursor? = context.contentResolver.query(
                dataUri,
                null,
                ContactsContract.Contacts.Data.MIMETYPE + "=?", arrayOf(
                    ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE),
                null
            )
            while (mStructuredURL?.moveToNext() == true) {
                val urlName = mStructuredURL.getString(mStructuredURL.getColumnIndex(
                    ContactsContract.CommonDataKinds.Website.URL))
                Utils.Log("onParseContact","URL 2 $urlName")
                mContact.urls?.add(urlName)
            }
            mStructuredURL?.close()
            mCursor.close()
        }
    }
    cursor?.close()
    return mContact
}