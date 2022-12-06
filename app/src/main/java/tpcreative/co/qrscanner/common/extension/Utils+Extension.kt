package tpcreative.co.qrscanner.common.extension

import android.net.Uri
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.google.zxing.ResultPoint
import com.google.zxing.client.result.*
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Constant
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
            mString.append("TEL;${it.key}:${it.value}")
            mString.append("\n")
        }
    }

    mData.contact?.emails?.forEach {
        if (it.value.isNotEmpty()){
            mString.append("EMAIL;${it.key}:${it.value}")
            mString.append("\n")
        }
    }

    mData.contact?.addresses?.forEach {
        if (it.value.getValue().isNotEmpty() || it.value.address?.isNotEmpty() == true){
            mString.append("ADR;${it.key}:;;${it.value.street.orEmpty()};${it.value.city.orEmpty()};${it.value.region.orEmpty()};${it.value.postalCode.orEmpty()};${it.value.country.orEmpty()}")
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
                mAddressMap[mAddressType ?:"-$index"] = mAddressObject
            }
            mGeneral.contact?.addresses = mAddressMap

            val mPhoneMap: MutableMap<String, String> = mutableMapOf()
            mAddressBook?.phoneNumbers?.forEachIndexed { index, it ->
                val mPhoneType = mAddressBook.phoneTypes?.get(index)
                mPhoneMap[mPhoneType?:"-$index"] = it
            }
            mGeneral.contact?.phones = mPhoneMap


            val mEmailMap: MutableMap<String, String> = mutableMapOf()
            mAddressBook?.emails?.forEachIndexed { index, it ->
                val mEmailType = mAddressBook.emailTypes?.get(index)
                mEmailMap[mEmailType ?:"-$index"] = it
            }
            mGeneral.contact?.emails = mEmailMap

            val mURLList: MutableList<String> = mutableListOf()
            mAddressBook?.urLs?.forEachIndexed { index, it ->
                mURLList.add(it)
            }
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
                    save.createType = mData.createType?.name
                }
                ParsedResultType.EMAIL_ADDRESS -> {
                    save.code =
                        "MATMSG:TO:" + mData.email + ";SUB:" + mData.subject + ";BODY:" + mData.message + ";"
                    save.type = Constant.email
                    save.email = mData.email
                    save.subject = mData.subject
                    save.message = mData.message
                    save.createType = mData.createType?.name
                }
                ParsedResultType.PRODUCT -> {
                    save.code = mData.textProductIdISNB
                    save.type = Constant.barCode
                    save.textProductIdISNB = mData.textProductIdISNB
                    save.createType = mData.createType?.name
                    save.barcodeFormat = mData.barcodeFormat
                }
                ParsedResultType.URI -> {
                    save.code = mData.url
                    save.type = Constant.webSite
                    save.url = mData.url
                    save.createType = mData.createType?.name
                }
                ParsedResultType.WIFI -> {
                    save.code =
                        "WIFI:S:" + mData.ssId + ";T:" + mData.networkEncryption + ";P:" + mData.password + ";H:" + mData.hidden + ";"
                    save.type = Constant.wifi
                    save.ssId = mData.ssId
                    save.password = mData.password
                    save.networkEncryption = mData.networkEncryption
                    save.hidden = mData.hidden
                    save.createType = mData.createType?.name
                }
                ParsedResultType.GEO -> {
                    save.code = "geo:" + mData.lat + "," + mData.lon + "?q=" + mData.query + ""
                    save.type = Constant.location
                    save.lat = mData.lat
                    save.lon = mData.lon
                    save.query = mData.query
                    save.createType = mData.createType?.name
                }
                ParsedResultType.TEL -> {
                    save.code = "tel:" + mData.phone + ""
                    save.type = Constant.phoneNumber
                    save.phone = mData.phone
                    save.createType = mData.createType?.name
                }
                ParsedResultType.SMS -> {
                    save.code = "smsto:" + mData.phone + ":" + mData.message
                    save.type = Constant.sms
                    save.phone = mData.phone
                    save.message = mData.message
                    save.createType = mData.createType?.name
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
                    save.code = builder.toString()
                    save.type = Constant.calendar
                }
                ParsedResultType.ISBN -> {
                    save.code = mData.textProductIdISNB
                }
                else -> {
                    save.code = mData.textProductIdISNB
                    save.type = Constant.text

                    save.textProductIdISNB = mData.textProductIdISNB
                    save.createType = mData.createType?.name
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
                    create.fragmentType,
                    EnumAction.DO_ADVANCE,
                    R.drawable.baseline_location_on_white_48,
                    ConstantValue.ADVANCE,
                    create.favorite
                )
            )
            when (create.createType) {
                ParsedResultType.ADDRESSBOOK -> {
                    /*Put item to HashClipboard*/

                    history.hashClipboard?.set(ConstantKey.FULL_NAME, create.getNames())
                    create.contact?.addresses?.forEach {
                        history.hashClipboard?.put(
                            ConstantKey.ADDRESS + it.key,
                            it.value.getValue()
                        )
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

                    create.contact?.phones?.forEach {
                        history.navigationList?.add(
                            ItemNavigation(
                                create.createType,
                                it.key,
                                it.value,
                                create.fragmentType,
                                EnumAction.PHONE_ADDRESS_BOOK,
                                R.drawable.baseline_phone_white_48,
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
                                create.fragmentType,
                                EnumAction.EMAIL_ADDRESS_BOOK,
                                R.drawable.baseline_email_white_48,
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
                                create.fragmentType,
                                EnumAction.URL_ADDRESS_BOOK,
                                R.drawable.baseline_language_white_48,
                                it,
                                create.favorite
                            )
                        )
                    }
                    history.fullName = create.fullName
                    history.address = create.address
                    history.phone = create.phone
                    history.email = create.email
                    history.createType = create.createType?.name
                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            ConstantValue.NONE,
                            create.fragmentType,
                            EnumAction.Other,
                            R.drawable.baseline_perm_contact_calendar_white_48,
                            ConstantValue.ADDRESS_BOOK,
                            create.favorite
                        )
                    )
                    history.title = ConstantValue.ADDRESS_BOOK
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
                            create.fragmentType,
                            EnumAction.Other,
                            R.drawable.baseline_email_white_48,
                            create.email,
                            create.favorite
                        )
                    )
                    history.title = ConstantValue.EMAIL
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
                            ConstantValue.NONE,
                            create.fragmentType,
                            EnumAction.SEARCH,
                            R.drawable.baseline_search_white_48,
                            create.textProductIdISNB,
                            create.favorite
                        )
                    )
                    history.title = ConstantValue.PRODUCT
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
                            create.fragmentType,
                            EnumAction.SEARCH,
                            R.drawable.baseline_search_white_48,
                            create.url,
                            create.favorite
                        )
                    )
                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            ConstantValue.NONE,
                            create.fragmentType,
                            EnumAction.Other,
                            R.drawable.baseline_language_white_48,
                            create.url,
                            create.favorite
                        )
                    )
                    history.title = ConstantValue.WEBSITE
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
                            create.fragmentType,
                            EnumAction.Other,
                            R.drawable.baseline_network_wifi_white_48,
                            create.ssId,
                            create.favorite
                        )
                    )
                    history.title = ConstantValue.WIFI
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
                            create.fragmentType,
                            EnumAction.Other,
                            R.drawable.baseline_location_on_white_48,
                            create.query,
                            create.favorite
                        )
                    )
                    history.title = ConstantValue.LOCATION
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
                            create.fragmentType,
                            EnumAction.Other,
                            R.drawable.baseline_phone_white_48,
                            create.phone,
                            create.favorite
                        )
                    )
                    history.title = ConstantValue.TELEPHONE
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
                            create.fragmentType,
                            EnumAction.Other,
                            R.drawable.baseline_textsms_white_48,
                            create.phone,
                            create.favorite
                        )
                    )
                    history.title = ConstantValue.SMS
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
                            create.fragmentType,
                            EnumAction.Other,
                            R.drawable.baseline_event_white_48,
                            ConstantValue.CALENDAR,
                            create.favorite
                        )
                    )
                    history.title = ConstantValue.CALENDAR
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
                            ConstantValue.NONE,
                            create.fragmentType,
                            EnumAction.SEARCH,
                            R.drawable.baseline_search_white_48,
                            ConstantValue.SEARCH,
                            create.favorite
                        )
                    )
                    history.title = ConstantValue.ISBN
                }
                else -> {
                    //Text query
                    if (BarcodeFormat.QR_CODE == BarcodeFormat.valueOf(
                            create.barcodeFormat ?: BarcodeFormat.QR_CODE.name
                        )
                    ) {
                        history.navigationList?.add(
                            ItemNavigation(
                                create.createType,
                                ConstantValue.NONE,
                                ConstantValue.NONE,
                                create.fragmentType,
                                EnumAction.Other,
                                R.drawable.baseline_textsms_white_48,
                                ConstantValue.TEXT,
                                create.favorite
                            )
                        )
                    }
                    /*Put item to HashClipboard*/
                    history.hashClipboard?.set(ConstantKey.TEXT, create.textProductIdISNB)
                    history.textProductIdISNB = create.textProductIdISNB
                    history.createType = create.createType?.name
                    history.navigationList?.add(
                        ItemNavigation(
                            create.createType,
                            ConstantValue.NONE,
                            ConstantValue.NONE,
                            create.fragmentType,
                            EnumAction.SEARCH,
                            R.drawable.baseline_search_white_48,
                            ConstantValue.SEARCH,
                            create.favorite
                        )
                    )
                    history.title = ConstantValue.TEXT
                }
            }
            history.navigationList?.add(
                ItemNavigation(
                    create.createType,
                    ConstantValue.NONE,
                    ConstantValue.NONE,
                    create.fragmentType,
                    EnumAction.CLIPBOARD,
                    R.drawable.ic_baseline_content_copy_24,
                    ConstantValue.CLIPBOARD,
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
                    if (general.getPhones().isNotEmpty()){
                        mContent.append("\n")
                        mContent.append(general.getPhones())
                    }
                    if (general.getEmails().isNotEmpty()){
                        mContent.append("\n")
                        mContent.append(general.getEmails())

                    }
                    if (general.getAddresses().isNotEmpty()){
                        mContent.append("\n")
                        mContent.append(general.getAddresses())
                    }
                    if (general.getUrls().isNotEmpty()){
                        mContent.append("\n")
                        mContent.append(general.getUrls())
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

