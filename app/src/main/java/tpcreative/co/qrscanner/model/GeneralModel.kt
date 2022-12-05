package tpcreative.co.qrscanner.model
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.google.zxing.ResultPoint
import com.google.zxing.client.result.ParsedResultType
import com.google.zxing.client.result.ResultParser
import tpcreative.co.qrscanner.common.ConstantValue
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.isVcard
import tpcreative.co.qrscanner.common.extension.onGeneralParse
import tpcreative.co.qrscanner.common.extension.onParseMeCard
import tpcreative.co.qrscanner.common.extension.onParseVCard
import java.io.Serializable

class GeneralModel : Serializable {
    var id: Int
    var email: String?
    var subject: String?
    var message: String?
    var phone: String?
    var lat: Double
    var lon: Double
    var query: String?
    var title: String?
    var location: String?
    var description: String?
    var startEvent: String?
    var startEventMilliseconds: Long
    var endEventMilliseconds: Long
    var endEvent: String?
    var fullName: String?
    var address: String?
    var textProductIdISNB: String?
    var ssId: String?
    var hidden: Boolean
    var password: String?
    var url: String?
    var barcodeFormat: String?
    var favorite: Boolean
    var updatedDateTime: String?
    var createdDateTime: String? = null
    var createType: ParsedResultType?
    var networkEncryption: String? = null
    var fragmentType: EnumFragmentType?
    var enumImplement: EnumImplement?
    var noted: String? = null


    /*10:39 28/11/2022
     * Using code filed in able to solve address book and email type
     * Display to view
    * */
     var code : String?
    var navigationList : MutableList<ItemNavigation>?
    var hashClipboard: HashMap<Any?, String?>?
    var contact : ContactModel? = null

    /*Sync data*/
    var isSynced = false
    var uuId: String? = null

    constructor() {
        this.id = 0
        this.email = ""
        this.subject = ""
        this.message = ""
        this.phone = ""
        this.lat = 0.0
        this.lon = 0.0
        this.startEventMilliseconds = 0
        this.endEventMilliseconds = 0
        this.query = ""
        this.title = ""
        this.location = ""
        this.description = ""
        this.startEvent = ""
        this.endEvent = ""
        this.fullName = ""
        this.address = ""
        this.textProductIdISNB = ""
        this.ssId = ""
        this.password = ""
        this.url = ""
        this.hidden = false
        this.createType = ParsedResultType.TEXT
        this.fragmentType = EnumFragmentType.CREATE
        this.enumImplement = EnumImplement.CREATE
        this.barcodeFormat = BarcodeFormat.QR_CODE.name
        this.favorite = false
        this.updatedDateTime = Utils.getCurrentDateTimeSort()
        this.createdDateTime = Utils.getCurrentDateTimeSort()
        this.enumImplement =  EnumImplement.CREATE
        this.isSynced =  false
        this.uuId = Utils.getUUId()
        this.code = ""
        this.navigationList = mutableListOf()
        this.hashClipboard = hashMapOf()
        this.contact = ContactModel()
        this.noted = ""
    }

    //Send request from saver
    constructor(mSave: SaveModel, mFragmentType : EnumFragmentType, mImplement : EnumImplement)  {
        this.id = mSave.id ?: 0
        this.email = mSave.email
        this.subject = mSave.subject
        this.message = mSave.message
        this.phone = mSave.phone
        this.lat = mSave.lat ?: 0.0
        this.lon = mSave.lon ?: 0.0
        this.startEventMilliseconds = mSave.startEventMilliseconds ?: 0
        this.endEventMilliseconds = mSave.endEventMilliseconds ?: 0
        this.query = mSave.query
        this.title = mSave.title
        this.location = mSave.location
        this.description = mSave.description
        this.startEvent = mSave.startEvent
        this.endEvent = mSave.endEvent
        this.fullName = mSave.fullName
        this.address = mSave.address
        this.textProductIdISNB = mSave.textProductIdISNB
        this.ssId = mSave.ssId
        this.password = mSave.password
        this.url = mSave.url
        this.hidden = mSave.hidden ?: false
        this.createType = ParsedResultType.valueOf(mSave.createType ?: ParsedResultType.TEXT.name)
        this.fragmentType = mFragmentType
        this.barcodeFormat = mSave.barcodeFormat
        this.favorite = mSave.favorite ?: false
        this.updatedDateTime = Utils.getCurrentDateTimeSort()
        this.createdDateTime = mSave.createdDatetime
        this.enumImplement = mImplement
        this.isSynced = true && mSave.isSynced ?: false
        this.uuId = mSave.uuId
        this.code = mSave.code
        this.navigationList = mutableListOf()
        this.hashClipboard = hashMapOf()
        this.contact = ContactModel()
        this.noted = mSave.noted

        /*Exception*/
        if (mSave.createType.equals(ParsedResultType.ADDRESSBOOK.name, ignoreCase = true)) {
            if (Utils.isVcard(mSave.code)){
                val mGeneral = Utils.onParseVCard(mSave.code ?:"")
                this.contact = mGeneral?.contact
            }else{
                val mGeneral = Utils.onParseMeCard(mSave.code ?:"")
                this.contact = mGeneral?.contact
            }
        }
    }

    //Send request from history
    constructor(mHistory: HistoryModel, mFragmentType : EnumFragmentType, mImplement : EnumImplement)  {
        this.id = mHistory.id ?: 0
        this.email = mHistory.email
        this.subject = mHistory.subject
        this.message = mHistory.message
        this.phone = mHistory.phone
        this.lat = mHistory.lat ?: 0.0
        this.lon = mHistory.lon ?: 0.0
        this.startEventMilliseconds = mHistory.startEventMilliseconds ?: 0
        this.endEventMilliseconds = mHistory.endEventMilliseconds ?: 0
        this.query = mHistory.query
        this.title = mHistory.title
        this.location = mHistory.location
        this.description = mHistory.description
        this.startEvent = mHistory.startEvent
        this.endEvent = mHistory.endEvent
        this.fullName = mHistory.fullName
        this.address = mHistory.address
        this.textProductIdISNB = mHistory.textProductIdISNB
        this.ssId = mHistory.ssId
        this.password = mHistory.password
        this.url = mHistory.url
        this.hidden = mHistory.hidden ?: false
        this.createType = ParsedResultType.valueOf(mHistory.createType ?: ParsedResultType.TEXT.name)
        this.fragmentType = mFragmentType
        this.barcodeFormat = mHistory.barcodeFormat
        this.favorite = mHistory.favorite ?: false
        this.updatedDateTime = Utils.getCurrentDateTimeSort()
        this.createdDateTime = mHistory.createdDatetime
        this.enumImplement = mImplement
        this.isSynced = true && mHistory.isSynced ?: false
        this.uuId = mHistory.uuId
        this.code = mHistory.code
        this.navigationList = mutableListOf()
        this.hashClipboard = hashMapOf()
        this.contact = ContactModel()
        this.noted = mHistory.noted

        /*Exception*/

        if (mHistory.createType.equals(ParsedResultType.ADDRESSBOOK.name, ignoreCase = true)) {
            if (Utils.isVcard(mHistory.code)){
                val mGeneral = Utils.onParseVCard(mHistory.code ?:"")
                this.contact = mGeneral?.contact
            }else{
                val mGeneral = Utils.onParseMeCard(mHistory.code ?:"")
                this.contact = mGeneral?.contact
            }
        }
    }


    //Init constructor for History
    constructor(mHistory: HistoryModel)  {
        this.id = mHistory.id ?: 0
        this.email = mHistory.email
        this.subject = mHistory.subject
        this.message = mHistory.message
        this.phone = mHistory.phone
        this.lat = mHistory.lat ?: 0.0
        this.lon = mHistory.lon ?: 0.0
        this.startEventMilliseconds = mHistory.startEventMilliseconds ?: 0
        this.endEventMilliseconds = mHistory.endEventMilliseconds ?: 0
        this.query = mHistory.query
        this.title = mHistory.title
        this.location = mHistory.location
        this.description = mHistory.description
        this.startEvent = mHistory.startEvent
        this.endEvent = mHistory.endEvent
        this.fullName = mHistory.fullName
        this.address = mHistory.address
        this.textProductIdISNB = mHistory.textProductIdISNB
        this.ssId = mHistory.ssId
        this.password = mHistory.password
        this.url = mHistory.url
        this.hidden = mHistory.hidden ?: false
        this.createType = ParsedResultType.valueOf(mHistory.createType ?: ParsedResultType.TEXT.name)
        this.fragmentType = EnumFragmentType.NONE
        this.barcodeFormat = mHistory.barcodeFormat
        this.favorite = mHistory.favorite ?: false
        this.updatedDateTime = Utils.getCurrentDateTimeSort()
        this.createdDateTime = mHistory.createdDatetime
        this.enumImplement = EnumImplement.NONE
        this.isSynced = true && mHistory.isSynced ?: false
        this.uuId = mHistory.uuId
        this.code = mHistory.code
        this.navigationList = mutableListOf()
        this.hashClipboard = hashMapOf()
        this.contact = ContactModel()
        /*Immigration from mecard to vcard*/
        if (this.fullName?.isNotEmpty()==true){
            this.contact?.fullName = this.fullName
        }
        if (this.address?.isNotEmpty() == true){
            val mAddress = AddressModel()
            mAddress.address = this.address
            mAddress.street = this.address
            this.contact?.addresses?.set(ConstantValue.HOME,mAddress)
        }
        this.phone?.let {
            this.contact?.phones?.set(ConstantValue.HOME,it)
        }

        this.email?.let {
            this.contact?.emails?.set(ConstantValue.HOME,it)
        }

        this.noted = mHistory.noted
    }

    //Init constructor for Saver
    constructor(mSave: SaveModel)  {
        this.id = mSave.id ?: 0
        this.email = mSave.email
        this.subject = mSave.subject
        this.message = mSave.message
        this.phone = mSave.phone
        this.lat = mSave.lat ?: 0.0
        this.lon = mSave.lon ?: 0.0
        this.startEventMilliseconds = mSave.startEventMilliseconds ?: 0
        this.endEventMilliseconds = mSave.endEventMilliseconds ?: 0
        this.query = mSave.query
        this.title = mSave.title
        this.location = mSave.location
        this.description = mSave.description
        this.startEvent = mSave.startEvent
        this.endEvent = mSave.endEvent
        this.fullName = mSave.fullName
        this.address = mSave.address
        this.textProductIdISNB = mSave.textProductIdISNB
        this.ssId = mSave.ssId
        this.password = mSave.password
        this.url = mSave.url
        this.hidden = mSave.hidden ?: false
        this.createType = ParsedResultType.valueOf(mSave.createType ?: ParsedResultType.TEXT.name)
        this.fragmentType = EnumFragmentType.NONE
        this.barcodeFormat = mSave.barcodeFormat
        this.favorite = mSave.favorite ?: false
        this.updatedDateTime = Utils.getCurrentDateTimeSort()
        this.createdDateTime = mSave.createdDatetime
        this.enumImplement = EnumImplement.NONE
        this.isSynced = true && mSave.isSynced ?: false
        this.uuId = mSave.uuId
        this.code = mSave.code
        this.navigationList = mutableListOf()
        this.hashClipboard = hashMapOf()
        this.contact = ContactModel()
        /*Immigration from mecard to vcard*/
        if (this.fullName?.isNotEmpty()==true){
            this.contact?.fullName = this.fullName
        }
        if (this.address?.isNotEmpty() == true){
            val mAddress = AddressModel()
            mAddress.address = this.address
            mAddress.street = this.address
            this.contact?.addresses?.set(ConstantValue.HOME,mAddress)
        }
        this.phone?.let {
            this.contact?.phones?.set(ConstantValue.HOME,it)
        }

        this.email?.let {
            this.contact?.emails?.set(ConstantValue.HOME,it)
        }

        this.noted = mSave.noted
    }

    //Request create, edit and view
    constructor(mGeneral: GeneralModel?)  {
        var save = GeneralModel()
        mGeneral?.let {
            save = it
        }
        this.id = save.id
        this.email = save.email
        this.subject = save.subject
        this.message = save.message
        this.phone = save.phone
        this.lat = save.lat
        this.lon = save.lon
        this.startEventMilliseconds = save.startEventMilliseconds
        this.endEventMilliseconds = save.endEventMilliseconds
        this.query = save.query
        this.title = save.title
        this.location = save.location
        this.description = save.description
        this.startEvent = save.startEvent
        this.endEvent = save.endEvent
        this.fullName = save.fullName
        this.address = save.address
        this.textProductIdISNB = save.textProductIdISNB
        this.ssId = save.ssId
        this.password = save.password
        this.url = save.url
        this.hidden = save.hidden
        this.createType = save.createType
        this.fragmentType = save.fragmentType
        this.barcodeFormat = save.barcodeFormat
        this.favorite = save.favorite
        this.updatedDateTime = Utils.getCurrentDateTimeSort()
        this.createdDateTime = save.createdDateTime
        this.enumImplement = save.enumImplement
        this.isSynced = true && save.isSynced
        this.uuId = save.uuId
        this.code = save.code
        this.navigationList = mutableListOf()
        this.hashClipboard = hashMapOf()
        this.contact = ContactModel()
        this.noted = save.noted

        /*Exception*/
        val mResultPoint = Array(1) { i -> ResultPoint(0F, 0F) }
        val mResult  = Result(this.code,this.code?.toByteArray(),mResultPoint,BarcodeFormat.valueOf(this.barcodeFormat ?: BarcodeFormat.QR_CODE.name))
        val parsedResult = ResultParser.parseResult(mResult)
        if ((this.code?.isNotEmpty()==true) && parsedResult!=null){
            val general = Utils.onGeneralParse(mResult,GeneralModel::class)
            this.contact = mGeneral?.contact
            this.navigationList = general.navigationList
            this.hashClipboard = general.hashClipboard
        }
    }

    fun getAddresses() : String{
        val mStringBuilder : StringBuilder = StringBuilder()
        if (createType == ParsedResultType.ADDRESSBOOK) {
            if (Utils.isVcard(this.code)){
                val mParsedVcard =  Utils.onParseVCard(this.code ?:"")
                mParsedVcard?.contact?.addresses?.forEach {
                    if (it.value.getValue().isNotEmpty()){
                        mStringBuilder.append(it.value.getValue()+", ")
                    }
                }
            }else{
                val mParsedVcard =  Utils.onParseMeCard(this.code ?:"")
                mParsedVcard?.contact?.addresses?.forEach {
                    if (it.value.address?.isNotEmpty()==true){
                        mStringBuilder.append(it.value.address+", ")
                    }
                }
            }
        }else{
            mStringBuilder.append(this.address)
        }
        return mStringBuilder.toString().trimEnd()
    }

    fun getPhones() : String{
        val mStringBuilder : StringBuilder = StringBuilder()
        if (createType == ParsedResultType.ADDRESSBOOK) {
            if (Utils.isVcard(this.code)){
                val mParsedVcard =  Utils.onParseVCard(this.code ?:"")
                mParsedVcard?.contact?.phones?.forEach {
                    if (it.value.isNotEmpty()){
                        mStringBuilder.append(it.value+", ")
                    }
                }
            }else{
                val mParsedVcard =  Utils.onParseMeCard(this.code ?:"")
                mParsedVcard?.contact?.phones?.forEach {
                    if (it.value.isNotEmpty()){
                        mStringBuilder.append(it.value+", ")
                    }
                }
            }
        }else{
            mStringBuilder.append(this.phone)
        }
        return mStringBuilder.toString().trimEnd()
    }

    fun getEmails() : String {
        val mStringBuilder : StringBuilder = StringBuilder()
        if (createType == ParsedResultType.ADDRESSBOOK) {
            if (Utils.isVcard(this.code)){
                val mParsedVcard =  Utils.onParseVCard(this.code ?:"")
                mParsedVcard?.contact?.emails?.forEach {
                    if (it.value.isNotEmpty()){
                        mStringBuilder.append(it.value+", ")
                    }
                }
            }else{
                val mParsedVcard =  Utils.onParseMeCard(this.code ?:"")
                mParsedVcard?.contact?.emails?.forEach {
                    if (it.value.isNotEmpty()){
                        mStringBuilder.append(it.value+", ")
                    }
                }
            }
        }else{
            mStringBuilder.append(this.email)
        }
        return mStringBuilder.toString().trimEnd()
    }

    fun getUrls() : String{
        val mStringBuilder : StringBuilder = StringBuilder()
        if (createType == ParsedResultType.ADDRESSBOOK) {
            if (Utils.isVcard(this.code)){
                val mParsedVcard =  Utils.onParseVCard(this.code ?:"")
                mParsedVcard?.contact?.urls?.forEach {
                    if (it.isNotEmpty()){
                        mStringBuilder.append("$it, ")
                    }
                }
            }else{
                val mParsedVcard =  Utils.onParseMeCard(this.code ?:"")
                mParsedVcard?.contact?.urls?.forEach {
                    if (it.isNotEmpty()){
                        mStringBuilder.append("$it, ")
                    }
                }
            }
        }else{
            mStringBuilder.append(url)
        }
        return mStringBuilder.toString().trimEnd()
    }

    fun getNames() : String{
        val mString : String? = if (createType == ParsedResultType.ADDRESSBOOK) {
            if (Utils.isVcard(this.code)){
                val mParsedVcard =  Utils.onParseVCard(this.code ?:"")
                mParsedVcard?.contact?.fullName
            }else{
                val mParsedVcard =  Utils.onParseMeCard(this.code ?:"")
                mParsedVcard?.contact?.fullName
            }
        }else{
            this.fullName
        }
        return mString.toString()
    }
}