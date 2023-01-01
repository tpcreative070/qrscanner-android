package tpcreative.co.qrscanner.common.extension

import android.app.Activity
import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.graphics.drawable.Drawable
import android.net.*
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.afollestad.materialdialogs.MaterialDialog
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.google.zxing.aztec.AztecWriter
import com.google.zxing.client.result.*
import com.google.zxing.datamatrix.DataMatrixWriter
import com.google.zxing.oned.*
import com.google.zxing.pdf417.PDF417Writer
import kotlinx.android.synthetic.main.custom_spinner_item.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.ConstantKey
import tpcreative.co.qrscanner.common.ConstantValue
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.*
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.reflect.KClass


fun Utils.readVCF(uri: Uri): GeneralModel? {
    var mGeneral: GeneralModel? = null
    try {
        val inputStream: InputStream? =
            QRScannerApplication.getInstance().contentResolver.openInputStream(uri)
        val mParse = inputStream?.let { getBytesFromInputStream(it) }
        mGeneral = mParse?.decodeToString()?.let { onParseVCard(it) }
        inputStream?.close()
        return mGeneral
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return mGeneral
}

fun Utils.onCreateVCard(mData: GeneralModel): String {
    val mString = StringBuilder()
    val mSplitName = mData.contact?.fullName?.stringToMap()
    mString.append("BEGIN:VCARD")
    mString.append("\n")
    mString.append("VERSION:3.0")
    mString.append("\n")
    mString.append(
        "N:${mSplitName?.get(ConstantKey.LAST_NAME).orEmpty()};${mSplitName?.get(ConstantKey.FIRST_NAME).orEmpty()};${
            mSplitName?.get(
                ConstantKey.MIDDLE_NAME
            ).orEmpty()
        };;${mSplitName?.get(ConstantKey.SUFFIX_NAME).orEmpty()}"
    )
    mString.append("\n")
    mString.append("FN:${mData.contact?.fullName.orEmpty()}")
    mString.append("\n")
    mData.contact?.phones?.forEach {
        if (it.value.isNotEmpty()){
            if (it.key == ConstantValue.HOME || it.key == ConstantValue.WORK || it.key == ConstantValue.CELL){
                mString.append("TEL;TYPE=${it.key}:${it.value}")
            }else{
                mString.append("TEL:${it.value}")
            }
            mString.append("\n")
        }
    }

    mData.contact?.emails?.forEach {
        if (it.value.isNotEmpty()){
            if (it.key == ConstantValue.HOME || it.key == ConstantValue.WORK || it.key == ConstantValue.CELL){
                mString.append("EMAIL;TYPE=${it.key}:${it.value}")
            }else{
                mString.append("EMAIL:${it.value}")
            }
            mString.append("\n")
        }
    }

    mData.contact?.addresses?.forEach {
        if (it.value.getAddressValue().isNotEmpty() || it.value.address?.isNotEmpty() == true){
            if (it.key == ConstantValue.HOME || it.key == ConstantValue.WORK || it.key == ConstantValue.CELL){
                mString.append("ADR;TYPE=${it.key}:;;${it.value.street.orEmpty()};${it.value.city.orEmpty()};${it.value.region.orEmpty()};${it.value.postalCode.orEmpty()};${it.value.country.orEmpty()}")
            }else{
                mString.append("ADR:;;${it.value.street.orEmpty()};${it.value.city.orEmpty()};${it.value.region.orEmpty()};${it.value.postalCode.orEmpty()};${it.value.country.orEmpty()}")
            }
            mString.append("\n")
        }
    }

    mData.contact?.company?.let {
        if (it.isNotEmpty()) {
            mString.append("ORG:${it};")
            mString.append("\n")
        }

    }

    mData.contact?.jobTitle?.let {
        if (it.isNotEmpty()) {
            mString.append("TITLE:${it}")
            mString.append("\n")
        }
    }

    mData.contact?.urls?.forEach {
        mString.append("URL:${it}")
        mString.append("\n")
    }
    mData.contact?.note?.let {
        mString.append("NOTE:${it}")
        mString.append("\n")
    }
    mString.append("END:VCARD")
    return mString.toString()
}

fun Utils.onCreateMeCard(mData : GeneralModel) : String?{
    val mBuilder : StringBuilder = StringBuilder()
    mBuilder.append("MECARD:")
    mBuilder.append("N:${mData.contact?.fullName};")
    if (mData.contact?.phones?.values?.isNotEmpty() == true){
        mBuilder.append("TEL:${mData.contact?.phones?.values?.joinToString(",")};")
    }
    if (mData.contact?.emails?.values?.isNotEmpty() == true){
       mBuilder.append("EMAIL:${mData.contact?.emails?.values?.joinToString(",")};")
    }
    if (mData.contact?.addresses?.values?.isNotEmpty() == true){
        mBuilder.append("ADR:${mData.contact?.addresses?.values?.firstOrNull()?.address};")
    }
    if (mData.contact?.urls?.isNotEmpty()==true){
        mBuilder.append("URL:${mData.contact?.urls?.joinToString(",")};")
    }
    if (mData.contact?.note?.isNotEmpty()==true){
        mBuilder.append("NOTE:${mData.contact?.note};")
    }
    if (mData.contact?.birthday?.isNotEmpty()==true){
        mBuilder.append("BDAY:${mData.contact?.birthday};")
    }
    return mBuilder.toString()
}

@Throws(IOException::class)
fun getBytesFromInputStream(input: InputStream): ByteArray? {
    val os = ByteArrayOutputStream()
    val buffer = ByteArray(0xFFFF)
    var len = input.read(buffer)
    while (len != -1) {
        os.write(buffer, 0, len)
        len = input.read(buffer)
    }
    return os.toByteArray()
}


fun Utils.onParseVCard(code: String?): GeneralModel? {
    var mGeneral: GeneralModel? = null
    Log("readVCF code ", code)
    try {
        val mResult = Result(code, code?.toByteArray(), null, BarcodeFormat.QR_CODE)
        val mParsed = ResultParser.parseResult(mResult)
        if (mParsed != null) {
            mGeneral = GeneralModel()
            val mAddressBook = mParsed as AddressBookParsedResult?
            val mContact = ContactModel()
            val mFullName = mAddressBook?.names?.firstOrNull()
            val mMap = mFullName?.stringToMap()
            val mFirstName: String? = mMap?.get(ConstantKey.FIRST_NAME)
            val mMiddleName: String? = mMap?.get(ConstantKey.MIDDLE_NAME)
            val mLastName: String? = mMap?.get(ConstantKey.LAST_NAME)
            val mSuffixesName =  mMap?.get(ConstantKey.SUFFIX_NAME)

            mContact.givenName = mFirstName
            mContact.middleName = mMiddleName
            mContact.familyName = mLastName
            mContact.suffixesName = mSuffixesName

            if (mContact.suffixesName?.isNotEmpty() == true && (mAddressBook?.names?.firstOrNull()?.length ?:0) > (mSuffixesName?.length ?:0)) {
                mContact.fullName =
                    "${mContact.givenName} ${mContact.middleName} ${mContact.familyName}, ${mContact.suffixesName}"
            } else {
                mContact.fullName =
                    "${mContact.givenName} ${mContact.middleName} ${mContact.familyName}"
            }
            val mOrganization = mAddressBook?.org?.split("\n")
            val mCompany = mOrganization?.firstOrNull()
            val mDepartment = mOrganization?.lastOrNull()
            val jobTitle = mAddressBook?.title


            mContact.company = mCompany
            mContact.department = mDepartment
            mContact.jobTitle = jobTitle

            mGeneral.contact = mContact

            val mAddressMap: MutableMap<String, AddressModel> = mutableMapOf()
            mAddressBook?.addresses?.forEachIndexed { index, it ->
                val mAddressType = mAddressBook.addressTypes?.get(index)
                val mData = it.split("\n").toMutableList()
                val mStreet = mData.firstOrNull()
                if (mData.size>0){
                    mData.removeAt(0)
                }
                val mCity = mData.firstOrNull()
                if (mData.size>0){
                    mData.removeAt(0)
                }
                val mRegion = mData.firstOrNull()
                if (mData.size>0){
                    mData.removeAt(0)
                }
                val mPostal = mData.firstOrNull()
                if (mData.size>0){
                    mData.removeAt(0)
                }
                val mCountry = mData.firstOrNull()
                val mAddressObject = AddressModel()
                mAddressObject.street = mStreet
                mAddressObject.city = mCity
                mAddressObject.region = mRegion
                mAddressObject.postalCode = mPostal
                mAddressObject.country = mCountry

                if (mAddressType.equals(ConstantValue.HOME) ||  mAddressType.equals(ConstantValue.WORK) ||mAddressType.equals(ConstantValue.CELL)){
                    mAddressMap["$mAddressType"] = mAddressObject
                }else{
                    mAddressMap["$mAddressType-$index"] = mAddressObject
                }
            }
            mGeneral.contact?.addresses = mAddressMap

            val mPhoneMap: MutableMap<String, String> = mutableMapOf()
            mAddressBook?.phoneNumbers?.forEachIndexed { index, it ->
                val mPhoneType = mAddressBook.phoneTypes?.get(index)
                if (mPhoneType.equals(ConstantValue.HOME) ||  mPhoneType.equals(ConstantValue.WORK) ||mPhoneType.equals(ConstantValue.CELL)){
                    mPhoneMap["$mPhoneType"] = it
                }else{
                    mPhoneMap["$mPhoneType-$index"] = it
                }
            }
            mGeneral.contact?.phones = mPhoneMap


            val mEmailMap: MutableMap<String, String> = mutableMapOf()
            mAddressBook?.emails?.forEachIndexed { index, it ->
                val mEmailType = mAddressBook.emailTypes?.get(index)
                if (mEmailType.equals(ConstantValue.HOME) ||  mEmailType.equals(ConstantValue.WORK) ||mEmailType.equals(ConstantValue.CELL)){
                    mEmailMap["$mEmailType"] = it
                }else{
                    mEmailMap["$mEmailType-$index"] = it
                }
            }
            mGeneral.contact?.emails = mEmailMap

            val mURLList: MutableList<String> = mutableListOf()
            mAddressBook?.urLs?.forEachIndexed { index, it ->
                mURLList.add(it)
            }

            mGeneral.contact?.note = mAddressBook?.note.orEmpty()
            mGeneral.contact?.birthday = mAddressBook?.birthday.orEmpty()
            mGeneral.contact?.nickname = mAddressBook?.nicknames?.firstOrNull()
            mGeneral.contact?.urls = mURLList
            mGeneral.code = code
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    Utils.Log(TAG,"vCard ${Gson().toJson(mGeneral)}")
    return mGeneral
}

fun Utils.onParseMeCard(mText: String?): GeneralModel? {

    var mGeneral: GeneralModel? = null
    try {

        val mResult = Result(mText, mText?.toByteArray(), null, BarcodeFormat.QR_CODE)
        val mParsed = ResultParser.parseResult(mResult)
        if (mParsed != null) {
            mGeneral = GeneralModel()
            val mContact = ContactModel()
            val mAddressBook = mParsed as AddressBookParsedResult?
            val mFullName = mAddressBook?.names?.firstOrNull()
            val mMap = mFullName?.stringToMap()
            val mFirstName: String? = mMap?.get(ConstantKey.FIRST_NAME)
            val mMiddleName: String? = mMap?.get(ConstantKey.MIDDLE_NAME)
            val mLastName: String? = mMap?.get(ConstantKey.LAST_NAME)
            val mSuffixesName =  mMap?.get(ConstantKey.SUFFIX_NAME)
            mContact.givenName = mFirstName
            mContact.middleName = mMiddleName
            mContact.familyName = mLastName
            mContact.suffixesName = mSuffixesName

            if (mContact.suffixesName?.isNotEmpty() == true && (mFullName?.length ?:0) > (mSuffixesName?.length ?:0)) {
                mContact.fullName =
                    "${mContact.givenName} ${mContact.middleName} ${mContact.familyName}, ${mContact.suffixesName}"
            } else {
                mContact.fullName =
                    "${mContact.givenName} ${mContact.middleName} ${mContact.familyName}"
            }

            mAddressBook?.addresses?.forEach {
                val mAddress = AddressModel()
                mAddress.address = it
                mContact.addresses?.set(it, mAddress)
            }

            mAddressBook?.emails?.forEach {
                mContact.emails?.set(it, it)
            }

            mAddressBook?.phoneNumbers?.forEach {
                mContact.phones?.set(it, it)
            }

            mAddressBook?.urLs?.forEach {
                mContact.urls?.add(it)
            }

            mContact.note = mAddressBook?.note
            mContact.birthday = mAddressBook?.birthday
            mContact.nickname = mAddressBook?.nicknames?.firstOrNull()
            mGeneral.contact = mContact
            mGeneral.code = mText
            Utils.Log(TAG, "Gson:? " + Gson().toJson(mAddressBook))
            Utils.Log(TAG, "Gson: " + Gson().toJson(mParsed))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    Utils.Log(TAG,"meCard ${Gson().toJson(mGeneral)}")
    return mGeneral
}


inline fun <reified T : Any, reified G : Any> Utils.onGeneralParse(data: G, clazz: KClass<T>): T {
    return when (clazz) {
        /*
       * Parse for display review
       *
       * */
        SaveModel::class -> {
            val mData = data as GeneralModel
            val save = SaveModel()
            when (mData.createType) {
                ParsedResultType.ADDRESSBOOK -> {
                    //Create vCard code == null requesting create a contact or edit on old and new version of contact
                    if (mData.code?.isNotEmpty() == true){
                        save.code = mData.code
                    }else{
                        if (isVcard(mData.code) || mData.code.isNullOrEmpty()) {
                            save.code = onCreateVCard(mData)
                        } else {
                            save.code = onCreateMeCard(mData)
                        }
                    }
                    save.type = onTranslateCreateType(mData.createType)
                    save.createType = mData.createType?.name
                    save.barcodeFormat = mData.barcodeFormat
                    save.onAddressBook(mData)
                }
                ParsedResultType.EMAIL_ADDRESS -> {
                    save.code =
                        "MATMSG:TO:" + mData.email + ";SUB:" + mData.subject + ";BODY:" + mData.message + ";"
                    save.type = onTranslateCreateType(mData.createType)
                    save.email = mData.email
                    save.subject = mData.subject
                    save.message = mData.message
                    save.createType = mData.createType?.name
                    save.barcodeFormat = mData.barcodeFormat
                }
                ParsedResultType.PRODUCT -> {
                    save.code = mData.textProductIdISNB
                    save.type = onTranslateCreateType(mData.createType)
                    save.textProductIdISNB = mData.textProductIdISNB
                    save.createType = mData.createType?.name
                    save.barcodeFormat = mData.barcodeFormat
                }
                ParsedResultType.URI -> {
                    save.code = mData.url
                    save.type = onTranslateCreateType(mData.createType)
                    save.url = mData.url
                    save.createType = mData.createType?.name
                    save.barcodeFormat = mData.barcodeFormat
                }
                ParsedResultType.WIFI -> {
                    save.code =
                        "WIFI:S:" + mData.ssId + ";T:" + mData.networkEncryption + ";P:" + mData.password + ";H:" + mData.hidden + ";"
                    save.type = onTranslateCreateType(mData.createType)
                    save.ssId = mData.ssId
                    save.password = mData.password
                    save.networkEncryption = mData.networkEncryption
                    save.hidden = mData.hidden
                    save.createType = mData.createType?.name
                    save.barcodeFormat = mData.barcodeFormat
                }
                ParsedResultType.GEO -> {
                    save.code = "geo:" + mData.lat + "," + mData.lon + "?q=" + mData.query + ""
                    save.type = onTranslateCreateType(mData.createType)
                    save.lat = mData.lat
                    save.lon = mData.lon
                    save.query = mData.query
                    save.createType = mData.createType?.name
                    save.barcodeFormat = mData.barcodeFormat
                }
                ParsedResultType.TEL -> {
                    save.code = "tel:" + mData.phone + ""
                    save.type = onTranslateCreateType(mData.createType)
                    save.phone = mData.phone
                    save.createType = mData.createType?.name
                    save.barcodeFormat = mData.barcodeFormat
                }
                ParsedResultType.SMS -> {
                    save.code = "smsto:" + mData.phone + ":" + mData.message
                    save.type = onTranslateCreateType(mData.createType)
                    save.phone = mData.phone
                    save.message = mData.message
                    save.createType = mData.createType?.name
                    save.barcodeFormat = mData.barcodeFormat
                }
                ParsedResultType.CALENDAR -> {
                    val builder = StringBuilder()
                    builder.append("BEGIN:VEVENT")
                    builder.append("\n")
                    builder.append("SUMMARY:" + mData.title)
                    builder.append("\n")
                    builder.append("DTSTART:" + mData.startEvent)
                    builder.append("\n")
                    builder.append("DTEND:" + mData.endEvent)
                    builder.append("\n")
                    builder.append("LOCATION:" + mData.location)
                    builder.append("\n")
                    builder.append("DESCRIPTION:" + mData.description)
                    builder.append("\n")
                    builder.append("END:VEVENT")
                    save.title = mData.title
                    save.startEvent = mData.startEvent
                    save.endEvent = mData.endEvent
                    save.startEventMilliseconds = mData.startEventMilliseconds
                    save.endEventMilliseconds = mData.endEventMilliseconds
                    save.location = mData.location
                    save.description = mData.description
                    save.createType = mData.createType?.name
                    save.barcodeFormat = mData.barcodeFormat
                    save.code = builder.toString()
                    save.type = onTranslateCreateType(mData.createType)
                }
                ParsedResultType.ISBN -> {
                    save.code = mData.textProductIdISNB
                    save.createType = mData.createType?.name
                    save.barcodeFormat = mData.barcodeFormat
                    save.type = onTranslateCreateType(mData.createType)
                    save.textProductIdISNB = mData.textProductIdISNB
                }
                else -> {
                    save.code = mData.textProductIdISNB
                    save.type = onTranslateCreateType(mData.createType)
                    save.textProductIdISNB = mData.textProductIdISNB
                    save.createType = mData.createType?.name
                    save.barcodeFormat = mData.barcodeFormat
                }
            }
            save as T
        }
        HistoryModel::class -> {
            val create = data as GeneralModel
            val history = HistoryModel()
            history.navigationList?.add(
                ItemNavigation(
                    create.createType,
                    ConstantValue.NONE,
                    ConstantValue.NONE,
                    create.barcodeFormat,
                    create.fragmentType,
                    EnumAction.DO_ADVANCE,
                    R.drawable.ic_note,
                    ConstantValue.ADVANCE,
                    create.favorite
                )
            )
            when (create.createType) {
                ParsedResultType.ADDRESSBOOK -> {
                    /*Put item to HashClipboard*/
                    history.hashClipboard?.set(ConstantKey.FULL_NAME, create.getNames())
                    create.contact?.addresses?.forEach {
                        if (it.value.getAddressValue().isEmpty()){
                            history.hashClipboard?.put(
                                ConstantKey.ADDRESS + it.key,
                                it.value.address
                            )
                        }else{
                            history.hashClipboard?.put(
                                ConstantKey.ADDRESS + it.key,
                                it.value.getAddressValue()
                            )
                        }
                    }
                    create.contact?.phones?.forEach {
                        history.hashClipboard?.put(ConstantKey.PHONE + it.key, it.value)
                    }
                    create.contact?.emails?.forEach {
                        history.hashClipboard?.put(ConstantKey.EMAIL + it.key, it.value)
                    }

                    create.contact?.urls?.forEach {
                        history.hashClipboard?.put(ConstantKey.URL + it, it)
                    }

                    if (create.getNote()?.isNotEmpty()==true){
                        history.hashClipboard?.put(ConstantKey.NOTE + create.getNote(),  create.getNote())
                    }

                    if (create.getBirthday()?.isNotEmpty()==true){
                        history.hashClipboard?.put(ConstantKey.BIRTHDAY + create.getBirthday(),  create.getBirthday())
                    }

                    create.contact?.phones?.forEach {
                        history.navigationList?.add(
                            ItemNavigation(
                                create.createType,
                                it.key,
                                it.value,
                                create.barcodeFormat,
                                create.fragmentType,
                                EnumAction.PHONE_ADDRESS_BOOK,
                                R.drawable.ic_phone,
                                it.value,
                                create.favorite
                            )
                        )
                    }

                    create.contact?.emails?.forEach {
                        history.navigationList?.add(
                            ItemNavigation(
                                create.createType,
                                it.key,
                                it.value,
                                create.barcodeFormat,
                                create.fragmentType,
                                EnumAction.EMAIL_ADDRESS_BOOK,
                                R.drawable.ic_email,
                                it.value,
                                create.favorite
                            )
                        )
                    }

                    create.contact?.urls?.forEach {
                        history.navigationList?.add(
                            ItemNavigation(
                                create.createType,
                                ConstantValue.NONE,
                                it,
                                create.barcodeFormat,
                                create.fragmentType,
                                EnumAction.URL_ADDRESS_BOOK,
                                R.drawable.ic_network,
                                it,
                                create.favorite
                            )
                        )
                    }

                    create.contact?.addresses?.forEach {
                        if (it.value.getAddressValue().isEmpty()){
                            history.navigationList?.add(
                                ItemNavigation(
                                    create.createType,
                                    ConstantValue.NONE,
                                    it.value.address ?:"",
                                    create.barcodeFormat,
                                    create.fragmentType,
                                    EnumAction.GEO_ADDRESS_BOOK,
                                    R.drawable.ic_direction,
                                    it.value.address,
                                    create.favorite
                                )
                            )
                        }else{
                            history.navigationList?.add(
                                ItemNavigation(
                                    create.createType,
                                    ConstantValue.NONE,
                                    it.value.getAddressValue(),
                                    create.barcodeFormat,
                                    create.fragmentType,
                                    EnumAction.GEO_ADDRESS_BOOK,
                                    R.drawable.ic_direction,
                                    it.value.getAddressValue(),
                                    create.favorite
                                )
                            )
                        }
                    }
                    history.onAddressBook(create)
                    history.createType = create.createType?.name
                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            ConstantValue.NONE,
                            create.barcodeFormat,
                            create.fragmentType,
                            EnumAction.Other,
                            R.drawable.ic_contact,
                            QRScannerApplication.getInstance().getString(R.string.contact),
                            create.favorite
                        )
                    )
                    history.titleDisplay = QRScannerApplication.getInstance().getString(R.string.contact)
                }
                ParsedResultType.EMAIL_ADDRESS -> {
                    /*Put item to HashClipboard*/
                    history.hashClipboard?.set(ConstantKey.EMAIL, create.email)
                    history.hashClipboard?.set(ConstantKey.SUBJECT, create.subject)
                    history.hashClipboard?.set(ConstantKey.MESSAGE, create.message)
                    history.email = create.email
                    history.subject = create.subject
                    history.message = create.message
                    history.createType = create.createType?.name
                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            ConstantValue.NONE,
                            create.barcodeFormat,
                            create.fragmentType,
                            EnumAction.Other,
                            R.drawable.ic_email,
                            create.email,
                            create.favorite
                        )
                    )
                    history.titleDisplay = ConstantValue.EMAIL
                }
                ParsedResultType.PRODUCT -> {
                    /*Put item to HashClipboard*/
                    history.hashClipboard?.set(ConstantKey.PRODUCT_ID, create.textProductIdISNB)
                    history.textProductIdISNB = create.textProductIdISNB
                    history.createType = create.createType?.name
                    history.barcodeFormat = create.barcodeFormat
                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            create.textProductIdISNB ?:"",
                            create.barcodeFormat,
                            create.fragmentType,
                            EnumAction.SEARCH_WEB,
                            R.drawable.ic_search,
                            QRScannerApplication.getInstance().getString(R.string.search_product_on_the_web),
                            create.favorite
                        )
                    )
                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            create.textProductIdISNB ?:"",
                            create.barcodeFormat,
                            create.fragmentType,
                            EnumAction.SEARCH_AMAZON,
                            R.drawable.ic_search_ecommerce,
                            QRScannerApplication.getInstance().getString(R.string.search_on_amazon_com),
                            create.favorite
                        )
                    )

                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            create.textProductIdISNB ?:"",
                            create.barcodeFormat,
                            create.fragmentType,
                            EnumAction.SEARCH_EBAY,
                            R.drawable.ic_search_ecommerce,
                            QRScannerApplication.getInstance().getString(R.string.search_on_ebay_com),
                            create.favorite
                        )
                    )
                    history.titleDisplay = QRScannerApplication.getInstance().getString(R.string.product_id)
                }
                ParsedResultType.URI -> {
                    /*Put item to HashClipboard*/
                    history.hashClipboard?.set(ConstantKey.URL, create.url)
                    history.url = create.url
                    history.createType = create.createType?.name
                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            ConstantValue.NONE,
                            create.barcodeFormat,
                            create.fragmentType,
                            EnumAction.SEARCH,
                            R.drawable.ic_search,
                            create.url,
                            create.favorite
                        )
                    )
                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            ConstantValue.NONE,
                            create.barcodeFormat,
                            create.fragmentType,
                            EnumAction.Other,
                            R.drawable.ic_network,
                            create.url,
                            create.favorite
                        )
                    )
                    history.titleDisplay = ConstantValue.WEBSITE
                    history.isRequestOpenBrowser = PrefsController.getBoolean(
                        QRScannerApplication.getInstance()
                            .getString(R.string.key_auto_navigate_to_browser), false
                    )
                }
                ParsedResultType.WIFI -> {
                    /*Put item to HashClipboard*/
                    history.hashClipboard?.set(ConstantKey.SSID, create.ssId)
                    history.hashClipboard?.set(ConstantKey.PASSWORD, create.password)
                    history.hashClipboard?.set(
                        ConstantKey.NETWORK_ENCRYPTION,
                        create.networkEncryption
                    )
                    history.hashClipboard?.set(
                        ConstantKey.HIDDEN,
                        if (create.hidden == true) "Yes" else "No"
                    )
                    history.ssId = create.ssId
                    history.password = create.password
                    history.networkEncryption = create.networkEncryption
                    history.hidden = create.hidden
                    history.createType = create.createType?.name
                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            ConstantValue.NONE,
                            create.barcodeFormat,
                            create.fragmentType,
                            EnumAction.Other,
                            R.drawable.ic_wifi,
                            create.ssId,
                            create.favorite
                        )
                    )
                    history.titleDisplay = ConstantValue.WIFI
                }
                ParsedResultType.GEO -> {
                    /*Put item to HashClipboard*/
                    history.hashClipboard?.set(ConstantKey.LAT, create.lat.toString() + "")
                    history.hashClipboard?.set(ConstantKey.LON, create.lon.toString() + "")
                    history.hashClipboard?.set(ConstantKey.QUERY, create.query)
                    history.lat = create.lat
                    history.lon = create.lon
                    history.query = create.query
                    history.createType = create.createType?.name
                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            ConstantValue.NONE,
                            create.barcodeFormat,
                            create.fragmentType,
                            EnumAction.Other,
                            R.drawable.ic_location,
                            "${create.lat},${create.lon}",
                            create.favorite
                        )
                    )
                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            "${create.query}",
                            create.barcodeFormat,
                            create.fragmentType,
                            EnumAction.GEO_ADDRESS_BOOK,
                            R.drawable.ic_direction,
                            "${create.query}",
                            create.favorite
                        )
                    )
                    history.titleDisplay = QRScannerApplication.getInstance().getString(R.string.location)
                }
                ParsedResultType.TEL -> {
                    /*Put item to HashClipboard*/
                    history.hashClipboard?.set(ConstantKey.PHONE, create.phone)
                    history.phone = create.phone
                    history.createType = create.createType?.name
                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            ConstantValue.NONE,
                            create.barcodeFormat,
                            create.fragmentType,
                            EnumAction.Other,
                            R.drawable.ic_phone,
                            create.phone,
                            create.favorite
                        )
                    )
                    history.titleDisplay = QRScannerApplication.getInstance().getString(R.string.telephone)
                }
                ParsedResultType.SMS -> {
                    /*Put item to HashClipboard*/
                    history.hashClipboard?.set(ConstantKey.PHONE, create.phone)
                    history.hashClipboard?.set(ConstantKey.MESSAGE, create.message)
                    history.phone = create.phone
                    history.message = create.message
                    history.createType = create.createType?.name
                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            ConstantValue.NONE,
                            create.barcodeFormat,
                            create.fragmentType,
                            EnumAction.Other,
                            R.drawable.ic_message,
                            create.phone,
                            create.favorite
                        )
                    )
                    history.titleDisplay = QRScannerApplication.getInstance().getString(R.string.message)
                }
                ParsedResultType.CALENDAR -> {
                    /*Put item to HashClipboard*/
                    history.hashClipboard?.set(ConstantKey.TITLE, create.title)
                    history.hashClipboard?.set(ConstantKey.LOCATION, create.location)
                    history.hashClipboard?.set(ConstantKey.DESCRIPTION, create.description)
                    history.hashClipboard?.set(
                        ConstantKey.START_EVENT_MILLISECONDS, Utils.getCurrentDatetimeEvent(
                            create.startEventMilliseconds
                                ?: 0
                        )
                    )
                    history.hashClipboard?.set(
                        ConstantKey.END_EVENT_MILLISECONDS, getCurrentDatetimeEvent(
                            create.endEventMilliseconds
                                ?: 0
                        )
                    )
                    history.title = create.title
                    history.location = create.location
                    history.description = create.description
                    history.startEvent = create.startEvent
                    history.endEvent = create.endEvent
                    history.startEventMilliseconds = create.startEventMilliseconds
                    history.endEventMilliseconds = create.endEventMilliseconds
                    history.createType = create.createType?.name
                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            ConstantValue.NONE,
                            create.barcodeFormat,
                            create.fragmentType,
                            EnumAction.Other,
                            R.drawable.ic_calender,
                            QRScannerApplication.getInstance().getString(R.string.event),
                            create.favorite
                        )
                    )
                    history.titleDisplay = QRScannerApplication.getInstance().getString(R.string.event)
                }
                ParsedResultType.ISBN -> {
                    /*Put item to HashClipboard*/
                    history.hashClipboard?.set(ConstantKey.ISBN, create.textProductIdISNB)
                    history.textProductIdISNB = create.textProductIdISNB
                    history.createType = create.createType?.name
                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            create.textProductIdISNB?:"",
                            create.barcodeFormat,
                            create.fragmentType,
                            EnumAction.SEARCH_WEB,
                            R.drawable.ic_search,
                            QRScannerApplication.getInstance().getString(R.string.search_product_on_the_web),
                            create.favorite
                        )
                    )

                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            create.textProductIdISNB ?:"",
                            create.barcodeFormat,
                            create.fragmentType,
                            EnumAction.SEARCH_AMAZON,
                            R.drawable.ic_search_ecommerce,
                            QRScannerApplication.getInstance().getString(R.string.search_on_amazon_com),
                            create.favorite
                        )
                    )

                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            create.textProductIdISNB ?:"",
                            create.barcodeFormat,
                            create.fragmentType,
                            EnumAction.SEARCH_EBAY,
                            R.drawable.ic_search_ecommerce,
                            QRScannerApplication.getInstance().getString(R.string.search_on_ebay_com),
                            create.favorite
                        )
                    )
                    history.titleDisplay = ConstantValue.ISBN
                }
                else -> {
                    /*Put item to HashClipboard*/
                    history.hashClipboard?.set(ConstantKey.TEXT, create.textProductIdISNB)
                    history.textProductIdISNB = create.textProductIdISNB
                    history.createType = create.createType?.name
                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            ConstantValue.NONE,
                            create.barcodeFormat,
                            create.fragmentType,
                            EnumAction.SEARCH,
                            R.drawable.ic_search,
                            QRScannerApplication.getInstance().getString(R.string.search),
                            create.favorite
                        )
                    )
                    history.titleDisplay = QRScannerApplication.getInstance().getString(R.string.text)
                }
            }
            history.navigationList?.add(
                ItemNavigation(
                    create.createType,
                    ConstantValue.NONE,
                    ConstantValue.NONE,
                    create.barcodeFormat,
                    create.fragmentType,
                    EnumAction.CLIPBOARD,
                    R.drawable.ic_copy,
                    QRScannerApplication.getInstance().getString(R.string.copy),
                    create.favorite
                )
            )
            history as T
        }

        /*
        * Parse for scan and view history
        *
        * */
        GeneralModel::class -> {
            val mData = GeneralModel()
            val mResult = data as Result
            val parsedResult = ResultParser.parseResult(mResult)
            mData.code = mResult.text
            when (parsedResult.type) {
                ParsedResultType.ADDRESSBOOK -> {
                    if (Utils.isVcard(mResult.text)){
                        val mVCard = Utils.onParseVCard(mResult.text)
                        Utils.Log(TAG,"Result vcard "+Gson().toJson(mVCard))
                        mData.contact = mVCard?.contact
                    }else{
                        val mMeCard = Utils.onParseMeCard(mResult.text)
                        mData.contact = mMeCard?.contact
                        Utils.Log(TAG,"Result mecard "+Gson().toJson(mMeCard))
                    }
                    mData.createType = ParsedResultType.ADDRESSBOOK
                    val mParsed = ResultParser.parseResult(mResult)
                    val mResultAddressBook = mParsed as AddressBookParsedResult?
                    mData.onAddressBook(mResultAddressBook)
                    Log(clazz::class.java, "Result address ${Gson().toJson(parsedResult)}")
                }
                ParsedResultType.EMAIL_ADDRESS -> {
                    mData.createType = ParsedResultType.EMAIL_ADDRESS
                    val emailAddress = parsedResult as EmailAddressParsedResult
                    mData.email = emailAddress.tos.firstOrNull()
                    mData.subject =
                        if (emailAddress.subject == null) "" else emailAddress.subject
                    mData.message = if (emailAddress.body == null) "" else emailAddress.body
                    Log(clazz::class.java, "Result address ${Gson().toJson(emailAddress)}")
                }
                ParsedResultType.PRODUCT -> {
                    mData.createType = ParsedResultType.PRODUCT
                    val productResult = parsedResult as ProductParsedResult
                    mData.textProductIdISNB =
                        if (productResult.productID == null) "" else productResult.productID
                    Log(clazz::class.java, "Product " + Gson().toJson(productResult))
                }
                ParsedResultType.URI -> {
                    mData.createType = ParsedResultType.URI
                    val urlResult = parsedResult as URIParsedResult
                    mData.url = if (urlResult.uri == null) "" else urlResult.uri
                }
                ParsedResultType.WIFI -> {
                    mData.createType = ParsedResultType.WIFI
                    val wifiResult = parsedResult as WifiParsedResult
                    mData.hidden = wifiResult.isHidden
                    mData.ssId = if (wifiResult.ssid == null) "" else wifiResult.ssid
                    mData.networkEncryption =
                        if (wifiResult.networkEncryption == null) "" else wifiResult.networkEncryption
                    mData.password =
                        if (wifiResult.password == null) "" else wifiResult.password
                    Log(
                        clazz::class.java,
                        "method : " + wifiResult.networkEncryption + " :" + wifiResult.phase2Method + " :" + wifiResult.password
                    )
                }
                ParsedResultType.GEO -> {
                    mData.createType = ParsedResultType.GEO
                    try {
                        val geoParsedResult = parsedResult as GeoParsedResult
                        mData.lat = geoParsedResult.latitude
                        mData.lon = geoParsedResult.longitude
                        mData.query = geoParsedResult.query
                        val strNew = mData.query?.replace("q=", "")
                        mData.query = strNew
                    } catch (e: Exception) {
                    }
                }
                ParsedResultType.TEL -> {
                    mData.createType = ParsedResultType.TEL
                    val telParsedResult = parsedResult as TelParsedResult
                    mData.phone = telParsedResult.number
                }
                ParsedResultType.SMS -> {
                    mData.createType = ParsedResultType.SMS
                    val smsParsedResult = parsedResult as SMSParsedResult
                    mData.phone = smsParsedResult.numbers?.firstOrNull()
                    mData.message =
                        if (smsParsedResult.body == null) "" else smsParsedResult.body
                }
                ParsedResultType.CALENDAR -> {
                    mData.createType = ParsedResultType.CALENDAR
                    val calendarParsedResult = parsedResult as CalendarParsedResult
                    val startTime = getCurrentDatetimeEvent(calendarParsedResult.startTimestamp)
                    val endTime = getCurrentDatetimeEvent(calendarParsedResult.endTimestamp)
                    mData.title =
                        if (calendarParsedResult.summary == null) "" else calendarParsedResult.summary
                    mData.description =
                        if (calendarParsedResult.description == null) "" else calendarParsedResult.description
                    mData.location =
                        if (calendarParsedResult.location == null) "" else calendarParsedResult.location
                    mData.startEvent = startTime
                    mData.endEvent = endTime
                    mData.startEventMilliseconds = calendarParsedResult.startTimestamp
                    mData.endEventMilliseconds = calendarParsedResult.endTimestamp
                    Log(clazz::class.java, "$startTime : $endTime")
                }
                ParsedResultType.ISBN -> {
                    mData.createType = ParsedResultType.ISBN
                    val isbParsedResult = parsedResult as ISBNParsedResult
                    mData.textProductIdISNB =
                        if (isbParsedResult.isbn == null) "" else isbParsedResult.isbn
                    Log(clazz::class.java, "Result filter " + Gson().toJson(isbParsedResult))
                }
                else -> try {
                    Log(clazz::class.java, "Default value")
                    mData.createType = ParsedResultType.TEXT
                    val textParsedResult = parsedResult as TextParsedResult
                    mData.textProductIdISNB =
                        if (textParsedResult.text == null) "" else textParsedResult.text
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            mData as T
        }
        /*
        * Display code
        *
        * */
        String::class -> {
            val general = data as GeneralModel
            val code: String?
            when (general.createType) {
                ParsedResultType.ADDRESSBOOK -> {
                    code = onCreateVCard(general)
                }
                ParsedResultType.EMAIL_ADDRESS -> {
                    code =
                        "MATMSG:TO:" + general.email + ";SUB:" + general.subject + ";BODY:" + general.message + ";"
                }
                ParsedResultType.PRODUCT -> {
                    code = general.textProductIdISNB
                }
                ParsedResultType.URI -> {
                    code = general.url
                }
                ParsedResultType.WIFI -> {
                    code =
                        "WIFI:S:" + general.ssId + ";T:" + general.password + ";P:" + general.networkEncryption + ";H:" + general.hidden + ";"
                }
                ParsedResultType.GEO -> {
                    code = "geo:" + general.lat + "," + general.lon + "?q=" + general.query + ""
                }
                ParsedResultType.TEL -> {
                    code = "tel:" + general.phone + ""
                }
                ParsedResultType.SMS -> {
                    code = "smsto:" + general.phone + ":" + general.message
                }
                ParsedResultType.CALENDAR -> {
                    val builder = StringBuilder()
                    builder.append("BEGIN:VEVENT")
                    builder.append("\n")
                    builder.append("SUMMARY:" + general.title)
                    builder.append("\n")
                    builder.append("DTSTART:" + general.startEvent)
                    builder.append("\n")
                    builder.append("DTEND:" + general.endEvent)
                    builder.append("\n")
                    builder.append("LOCATION:" + general.location)
                    builder.append("\n")
                    builder.append("DESCRIPTION:" + general.description)
                    builder.append("\n")
                    builder.append("END:VEVENT")
                    code = builder.toString()
                }
                ParsedResultType.ISBN -> {
                    code = general.textProductIdISNB
                }
                else -> {
                    code = general.textProductIdISNB
                }
            }
            code as T
        }

        /*Hashmap for result scanner*/
        HashMap::class -> {
            val general = data as GeneralModel
            val mMap = java.util.HashMap<String, String?>()
            val mContent = StringBuilder()
            when (general.createType) {
                ParsedResultType.ADDRESSBOOK -> {
                    if (general.getNames().isNotEmpty()){
                        mContent.append(general.getNames())
                    }
                    if (general.getPhones(ConstantValue.SEPARATORS_COMMA).isNotEmpty()){
                        mContent.append("\n")
                        mContent.append(general.getPhones(ConstantValue.SEPARATORS_COMMA))
                    }
                    if (general.getEmails(ConstantValue.SEPARATORS_COMMA).isNotEmpty()){
                        mContent.append("\n")
                        mContent.append(general.getEmails(ConstantValue.SEPARATORS_COMMA))

                    }
                    if (general.getAddresses(ConstantValue.SEPARATORS_COMMA).isNotEmpty()){
                        mContent.append("\n")
                        mContent.append(general.getAddresses(ConstantValue.SEPARATORS_COMMA))
                    }
                    if (general.getUrls(ConstantValue.SEPARATORS_COMMA).isNotEmpty()){
                        mContent.append("\n")
                        mContent.append(general.getUrls(ConstantValue.SEPARATORS_COMMA))
                    }

                    if (general.getNote()?.isNotEmpty() == true){
                        mContent.append("\n")
                        mContent.append(general.getNote())
                    }

                    if (general.getBirthday()?.isNotEmpty() == true){
                        mContent.append("\n")
                        mContent.append(general.getBirthday())
                    }

                    mMap[ConstantKey.CONTENT] = mContent.toString()
                    mMap[ConstantKey.BARCODE_FORMAT] =
                        general.barcodeFormat ?: BarcodeFormat.QR_CODE.name
                    mMap[ConstantKey.CREATED_DATETIME] =
                        getCurrentDateDisplay(general.updatedDateTime)
                }
                ParsedResultType.EMAIL_ADDRESS -> {
                    mContent.append(general.email)
                    mContent.append("\n")
                    mContent.append(general.subject)
                    mContent.append("\n")
                    mContent.append(general.message)
                    mMap[ConstantKey.CONTENT] = mContent.toString()
                    mMap[ConstantKey.BARCODE_FORMAT] =
                        general.barcodeFormat ?: BarcodeFormat.QR_CODE.name
                    mMap[ConstantKey.CREATED_DATETIME] =
                        getCurrentDateDisplay(general.updatedDateTime)
                }
                ParsedResultType.PRODUCT -> {
                    mContent.append(general.textProductIdISNB)
                    mMap[ConstantKey.CONTENT] = mContent.toString()
                    mMap[ConstantKey.BARCODE_FORMAT] =
                        general.barcodeFormat ?: BarcodeFormat.QR_CODE.name
                    mMap[ConstantKey.CREATED_DATETIME] =
                        getCurrentDateDisplay(general.updatedDateTime)
                }
                ParsedResultType.URI -> {
                    mContent.append(general.url)
                    mMap[ConstantKey.CONTENT] = mContent.toString()
                    mMap[ConstantKey.BARCODE_FORMAT] =
                        general.barcodeFormat ?: BarcodeFormat.QR_CODE.name
                    mMap[ConstantKey.CREATED_DATETIME] =
                        getCurrentDateDisplay(general.updatedDateTime)
                }
                ParsedResultType.WIFI -> {
                    mContent.append(general.ssId)
                    mContent.append("\n")
                    mContent.append(general.password)
                    mContent.append("\n")
                    mContent.append(general.networkEncryption)
                    mContent.append("\n")
                    mContent.append(general.hidden)
                    mMap[ConstantKey.CONTENT] = mContent.toString()
                    mMap[ConstantKey.BARCODE_FORMAT] =
                        general.barcodeFormat ?: BarcodeFormat.QR_CODE.name
                    mMap[ConstantKey.CREATED_DATETIME] =
                        getCurrentDateDisplay(general.updatedDateTime)
                }
                ParsedResultType.GEO -> {
                    mContent.append(general.lat)
                    mContent.append("\n")
                    mContent.append(general.lon)
                    mContent.append("\n")
                    mContent.append(general.query)
                    mMap[ConstantKey.CONTENT] = mContent.toString()
                    mMap[ConstantKey.BARCODE_FORMAT] =
                        general.barcodeFormat ?: BarcodeFormat.QR_CODE.name
                    mMap[ConstantKey.CREATED_DATETIME] =
                        getCurrentDateDisplay(general.updatedDateTime)
                }
                ParsedResultType.TEL -> {
                    mContent.append(general.phone)
                    mMap[ConstantKey.CONTENT] = mContent.toString()
                    mMap[ConstantKey.BARCODE_FORMAT] =
                        general.barcodeFormat ?: BarcodeFormat.QR_CODE.name
                    mMap[ConstantKey.CREATED_DATETIME] =
                        getCurrentDateDisplay(general.updatedDateTime)
                }
                ParsedResultType.SMS -> {
                    mContent.append(general.phone)
                    mContent.append("\n")
                    mContent.append(general.message)
                    mMap[ConstantKey.CONTENT] = mContent.toString()
                    mMap[ConstantKey.BARCODE_FORMAT] =
                        general.barcodeFormat ?: BarcodeFormat.QR_CODE.name
                    mMap[ConstantKey.CREATED_DATETIME] =
                        getCurrentDateDisplay(general.updatedDateTime)
                }
                ParsedResultType.CALENDAR -> {
                    mContent.append(general.title)
                    mContent.append("\n")
                    mContent.append(general.startEvent)
                    mContent.append("\n")
                    mContent.append(general.endEvent)
                    mContent.append("\n")
                    mContent.append(general.location)
                    mContent.append("\n")
                    mContent.append(general.description)
                    mMap[ConstantKey.CONTENT] = mContent.toString()
                    mMap[ConstantKey.BARCODE_FORMAT] =
                        general.barcodeFormat ?: BarcodeFormat.QR_CODE.name
                    mMap[ConstantKey.CREATED_DATETIME] =
                        getCurrentDateDisplay(general.updatedDateTime)
                }
                ParsedResultType.ISBN -> {
                    mContent.append(general.textProductIdISNB)
                    mMap[ConstantKey.CONTENT] = mContent.toString()
                    mMap[ConstantKey.BARCODE_FORMAT] =
                        general.barcodeFormat ?: BarcodeFormat.QR_CODE.name
                    mMap[ConstantKey.CREATED_DATETIME] =
                        getCurrentDateDisplay(general.updatedDateTime)
                }
                else -> {
                    mContent.append(general.textProductIdISNB)
                    mMap[ConstantKey.CONTENT] = mContent.toString()
                    mMap[ConstantKey.BARCODE_FORMAT] =
                        general.barcodeFormat ?: BarcodeFormat.QR_CODE.name
                    mMap[ConstantKey.CREATED_DATETIME] =
                        getCurrentDateDisplay(general.updatedDateTime)
                }
            }
            mMap as T
        }
        else -> SaveModel() as T
    }
}

fun Utils.isVcard(code: String?): Boolean {
    if (code?.isNotEmpty() == true) {
        if ((code.length) >= 11) {
            val mSplit = code.trim().substring(0, 11)
            if (mSplit.contentEquals(ConstantValue.VCard)) {
                return true
            }
        }
    }
    return false
}

fun Utils.getDisplay(mGeneral : GeneralModel) : String?{
    var mResult : String?
    if (mGeneral.createType == ParsedResultType.EMAIL_ADDRESS) {
        mResult = mGeneral.email
    } else if (mGeneral.createType == ParsedResultType.SMS) {
        mResult = mGeneral.phone
    } else if (mGeneral.createType == ParsedResultType.GEO) {
        mResult = mGeneral.query
    } else if (mGeneral.createType == ParsedResultType.CALENDAR) {
        mResult = mGeneral.title
    } else if (mGeneral.createType == ParsedResultType.ADDRESSBOOK) {
        mResult = mGeneral.getNames()
    } else if (mGeneral.createType == ParsedResultType.TEL) {
        mResult = mGeneral.phone
    } else if (mGeneral.createType == ParsedResultType.WIFI) {
        mResult = mGeneral.ssId
    } else if (mGeneral.createType == ParsedResultType.URI) {
        mResult = mGeneral.url
    } else {
        mResult = mGeneral.textProductIdISNB
    }
    mResult = if ((mResult?.length ?:0)>30){
        mResult?.substring(0,30)+"..."
    }else{
        mResult
    }
    return mResult
}

fun Utils.onTranslateCreateType(type : ParsedResultType?) : String {
    when(type){
        ParsedResultType.PRODUCT ->{
            return QRScannerApplication.getInstance().getString(R.string.product_id)
        }
        ParsedResultType.EMAIL_ADDRESS ->{
            return QRScannerApplication.getInstance().getString(R.string.email)
        }
        ParsedResultType.SMS ->{
            return QRScannerApplication.getInstance().getString(R.string.message)
        }
        ParsedResultType.GEO ->{
            return QRScannerApplication.getInstance().getString(R.string.location)
        }
        ParsedResultType.ADDRESSBOOK ->{
            return QRScannerApplication.getInstance().getString(R.string.contact)
        }
        ParsedResultType.CALENDAR ->{
            return QRScannerApplication.getInstance().getString(R.string.calendar)
        }
        ParsedResultType.TEL ->{
            return QRScannerApplication.getInstance().getString(R.string.telephone)
        }
        ParsedResultType.WIFI ->{
            return QRScannerApplication.getInstance().getString(R.string.wifi)
        }
        ParsedResultType.URI ->{
            return QRScannerApplication.getInstance().getString(R.string.website)
        }
        ParsedResultType.ISBN ->{
            return ConstantValue.ISBN
        }
        else -> {return QRScannerApplication.getInstance().getString(R.string.text)}
    }
}

fun Utils.validBarcode(code :String,barcodeFormat : BarcodeFormat) : Boolean{
    try {
        when(barcodeFormat){
            BarcodeFormat.EAN_8 ->{
                val mData = EAN8Writer().encode(code)
                if (mData!=null){
                    return true
                }
            }
            BarcodeFormat.EAN_13 ->{
                val mData = EAN13Writer().encode(code)
                if (mData!=null){
                    return true
                }
            }
            BarcodeFormat.UPC_A->{
                val mData = UPCAWriter().encode(code,barcodeFormat,200,200)
                if (mData!=null){
                    return true
                }
            }
            BarcodeFormat.UPC_E->{
                val mData = UPCEWriter().encode(code)
                if (mData!=null){
                    return true
                }
            }
            BarcodeFormat.ITF->{
                val mData = ITFWriter().encode(code)
                if (mData!=null){
                    return true
                }
            }
            BarcodeFormat.CODABAR ->{
                val mData = CodaBarWriter().encode(code)
                if (mData!=null){
                    return true
                }
            }
            BarcodeFormat.DATA_MATRIX ->{
                val mData = DataMatrixWriter().encode(code,barcodeFormat,200,200)
                if (mData!=null){
                    return true
                }
            }
            BarcodeFormat.PDF_417 ->{
                val mData = PDF417Writer().encode(code,barcodeFormat,200,200)
                if (mData!=null){
                    return true
                }
            }
            BarcodeFormat.AZTEC ->{
                val mData = AztecWriter().encode(code,barcodeFormat,200,200)
                if (mData!=null){
                    return true
                }
            }

            BarcodeFormat.CODE_128 ->{
                val mData = Code128Writer().encode(code,barcodeFormat,200,200)
                if (mData!=null){
                    return true
                }
            }
            BarcodeFormat.CODE_39 ->{
                val mData = Code39Writer().encode(code,barcodeFormat,200,200)
                if (mData!=null){
                    return true
                }
            }
            BarcodeFormat.CODE_93 ->{
                val mData = Code93Writer().encode(code,barcodeFormat,200,200)
                if (mData!=null){
                    return true
                }
            }
            else -> {}
        }
    }catch (e : Exception){
        e.printStackTrace()
        return false
    }
    return false
}

fun Utils.onBarCodeId(format: String?) : Drawable?{
    var mDraw : Drawable? = null
    if (format.isNullOrEmpty()){
        mDraw =  ContextCompat.getDrawable(QRScannerApplication.getInstance(),R.drawable.ic_qrcode)
    }else{
        val mFormat = BarcodeFormat.valueOf(format)
        when(mFormat){
            BarcodeFormat.QR_CODE ->{
                mDraw =  ContextCompat.getDrawable(QRScannerApplication.getInstance(),R.drawable.ic_qrcode)
            }
            BarcodeFormat.DATA_MATRIX->{
                mDraw =  ContextCompat.getDrawable(QRScannerApplication.getInstance(),R.drawable.ic_qrcode)
            }
            BarcodeFormat.PDF_417->{
                mDraw =  ContextCompat.getDrawable(QRScannerApplication.getInstance(),R.drawable.ic_barcode)
            }
            BarcodeFormat.AZTEC->{
                mDraw =  ContextCompat.getDrawable(QRScannerApplication.getInstance(),R.drawable.ic_qrcode)
            }
            BarcodeFormat.EAN_13->{
                mDraw =  ContextCompat.getDrawable(QRScannerApplication.getInstance(),R.drawable.ic_barcode)
            }
            BarcodeFormat.EAN_8->{
                mDraw =  ContextCompat.getDrawable(QRScannerApplication.getInstance(),R.drawable.ic_barcode)
            }
            BarcodeFormat.UPC_E->{
                mDraw =  ContextCompat.getDrawable(QRScannerApplication.getInstance(),R.drawable.ic_barcode)
            }
            BarcodeFormat.UPC_A->{
                mDraw =  ContextCompat.getDrawable(QRScannerApplication.getInstance(),R.drawable.ic_barcode)
            }
            BarcodeFormat.CODE_128->{
                mDraw =  ContextCompat.getDrawable(QRScannerApplication.getInstance(),R.drawable.ic_barcode)
            }
            BarcodeFormat.CODE_39->{
                mDraw =  ContextCompat.getDrawable(QRScannerApplication.getInstance(),R.drawable.ic_barcode)
            }
            BarcodeFormat.CODE_93->{
                mDraw =  ContextCompat.getDrawable(QRScannerApplication.getInstance(),R.drawable.ic_barcode)
            }
            BarcodeFormat.CODABAR->{
                mDraw =  ContextCompat.getDrawable(QRScannerApplication.getInstance(),R.drawable.ic_barcode)
            }
            BarcodeFormat.ITF->{
                mDraw =  ContextCompat.getDrawable(QRScannerApplication.getInstance(),R.drawable.ic_barcode)
            }
            else -> {
                mDraw =  ContextCompat.getDrawable(QRScannerApplication.getInstance(),R.drawable.ic_barcode)
            }
        }
    }
    return mDraw
}

fun Utils.setDisplayLatTimeSyncedCompletely(){
    PrefsController.putString(QRScannerApplication.getInstance().getString(R.string.key_last_time_synced_completely),
        getCurrentDateTimeSort()
    )
}

private fun Utils.getDisplayLatTimeSyncedCompletely() : String?{
    return PrefsController.getString(QRScannerApplication.getInstance().getString(R.string.key_last_time_synced_completely),
        getCurrentDateTimeSort()
    )
}

fun Utils.onDisplayLatTimeSyncedCompletely(): String{
    return String.format(QRScannerApplication.getInstance().getString(R.string.synced_data_last_time_updated),
        getCurrentDateDisplay(getDisplayLatTimeSyncedCompletely())
    )
}


@RequiresApi(Build.VERSION_CODES.Q)
fun Utils.connectWifi(activity: Activity, id:String, password: String) {
    try {
        val suggestion = WifiNetworkSuggestion.Builder()
            .setSsid(id)
            .setWpa2Passphrase(password)
            .setIsAppInteractionRequired(true) // Optional (Needs location permission)
            .build();
        val suggestionsList = listOf(suggestion);
        val wifiManager =
            activity.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager;
        val status = wifiManager.addNetworkSuggestions(suggestionsList);
        if (status != WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
            // do error handling here
            Log(TAG, "error connection")
        }
    }catch (e : Exception){
        e.printStackTrace()
    }
    // Optional (Wait for post connection broadcast to one of your suggestions)
//    val intentFilter = IntentFilter(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION);
//    val broadcastReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context, intent: Intent) {
//            if (!intent.action.equals(WifiManager.ACTION_WIFI_NETWORK_SUGGESTION_POST_CONNECTION)) {
//                return;
//            }
//            // do post connect processing here
//        }
//    };
//    activity.registerReceiver(broadcastReceiver, intentFilter);
}


fun Utils.connectWifiOnOldVersion(context: Context,ssId : String,key: String){
    val wifiConfig = WifiConfiguration()
    wifiConfig.SSID = String.format("\"%s\"", ssId)
    wifiConfig.preSharedKey = String.format("\"%s\"", key)
    val wifiManager = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager?
    wifiManager?.isWifiEnabled = true
    val netId = wifiManager?.addNetwork(wifiConfig)
    wifiManager?.disconnect()
    netId?.let {
        wifiManager.enableNetwork(it, true)
        val isConnectionSuccessful = wifiManager.reconnect()
        if (isConnectionSuccessful) {
            Log(TAG,"connection successful")
        } else {
            Log(TAG,"invalid credential")
        }
    }
}

fun Utils.checkingWifiEnable(context: Context) : Boolean{
    val wifiManager = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager?
    return wifiManager?.isWifiEnabled ?: false
}

fun Utils.alert(context: Context,mTitle : String?= null, mMessage: String, callback : ()->Unit?){
    MaterialDialog(context).show {
        message(text = mMessage)
        title( text = mTitle ?: "")
        positiveButton(res = R.string.ok){
            callback.invoke()
        }
    }
}

fun Utils.onFormatBarcodeDisplay(barcodeFormat: BarcodeFormat, enumAction: EnumAction) : String{
    if (enumAction == EnumAction.EAN_5){
        return "EAN-5"
    }
    if (enumAction == EnumAction.CODE_25){
        return "Code 25"
    }
    when(barcodeFormat){
        BarcodeFormat.QR_CODE ->{
            return "QR"
        }
        BarcodeFormat.EAN_13 ->{
            return "EAN-13"
        }
        BarcodeFormat.EAN_8 ->{
            return "EAN-8"
        }
        BarcodeFormat.ITF ->{
            return "ITF"
        }
        BarcodeFormat.UPC_A ->{
            return "UPC-A"
        }
        BarcodeFormat.UPC_E ->{
            return "UPC-E"
        }
        BarcodeFormat.CODABAR ->{
            return "Codabar"
        }
        BarcodeFormat.CODE_39 ->{
            return "CODE-39"
        }
        BarcodeFormat.CODE_93 ->{
            return "CODE-93"
        }
        BarcodeFormat.CODE_128 ->{
            return "CODE-122"
        }
        BarcodeFormat.RSS_14 ->{
            return "RSS-14"
        }
        BarcodeFormat.AZTEC ->{
            return "AZTEC"
        }
        BarcodeFormat.DATA_MATRIX ->{
            return "Data Matrix"
        }
        BarcodeFormat.PDF_417 ->{
            return "PDF-417"
        }
        else -> {}
    }
    return  ""
}


fun Utils.onFormatBarcodeDisplay(enumAction: EnumAction) : String{
    return when(enumAction){
        EnumAction.DEGREE_0 ->{
            R.string.orientation_0.toText()
        }
        EnumAction.DEGREE_90 ->{
            R.string.orientation_90.toText()
        }
        EnumAction.DEGREE_270 ->{
            R.string.orientation_270.toText()
        }
        EnumAction.OTHER_ORIENTATION ->{
            R.string.other_orientation.toText()
        }
        EnumAction.SHADOW ->{
            R.string.light_or_shadow.toText()
        }
        EnumAction.TOO_CLOSE_BLURRY ->{
            R.string.too_close_blurry.toText()
        }
        EnumAction.LED_WHEN_DARK->{
            R.string.led_when_dark.toText()
        }
        EnumAction.LOW_CONTRAST->{
            R.string.low_contrast.toText()
        }
        else -> {
            ""
        }
    }
}
fun Utils.calculateNoOfColumns(
    context: Context,
    columnWidthDp: Float
): Int { // For example columnWidthdp=180
    val displayMetrics = context.resources.displayMetrics
    val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
    return (screenWidthDp / columnWidthDp + 0.5).toInt()
}

fun Utils.getString() : Context{
    return QRScannerApplication.getInstance()
}

