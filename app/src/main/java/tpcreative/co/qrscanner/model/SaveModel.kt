package tpcreative.co.qrscanner.model
import com.google.gson.annotations.SerializedName
import com.google.zxing.BarcodeFormat
import com.google.zxing.client.result.ParsedResultType
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.ConstantValue
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.isVcard
import tpcreative.co.qrscanner.common.extension.onParseMeCard
import tpcreative.co.qrscanner.common.extension.onParseVCard
import tpcreative.co.qrscanner.common.extension.onTranslateCreateType
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import java.io.Serializable

class SaveModel : Serializable {
    var id : Int? = 0
    var email: String?
    var subject: String?
    var message: String?
    var phone: String?
    var lat: Double?
    var lon: Double?
    var query: String?
    var title: String?
    var location: String?
    var description: String?
    var startEvent: String?
    var endEvent: String?
    var startEventMilliseconds: Long?
    var endEventMilliseconds: Long?
    var fullName: String?
    var address: String?
    @SerializedName("text")
    var textProductIdISNB: String?
    /*Only using to get data from json
    * Will be removed several next months
    * */
    @SerializedName("textProductIdISNB")
    var text: String? = null
    var ssId: String?
    var hidden: Boolean?
    var password: String?
    var url: String?
    var createType: String?
    var networkEncryption: String?
    var createDatetime: String?
    var barcodeFormat: String?
    var favorite: Boolean?
    var updatedDateTime: String?

    /*content_type_barcode*/
    var contentUnique: String?
    var contentUniqueForUpdatedTime: String?

    /*sync data*/
    var isSynced: Boolean?
    var uuId: String?
    var noted: String?

    /*10:39 28/11/2022
     * Using code filed in able to solve address book and email type
     * Display to view
    * */
    var code : String?

    /*20:39 09/12/2022 Added noted and favorite*/
    var hiddenDatetime: String?

    /*Display on review*/
    var type : String?
    /*Display on review*/
    var titleDisplay : String? = null

    /*Custom fields*/
    var typeCategories: TypeCategories? = null
    private var isChecked = false
    private var isDeleted = false

    constructor() {
        this.email = ""
        this.subject = ""
        this.message = ""
        this.phone = ""
        this.lat = 0.0
        this.lon = 0.0
        this.query = ""
        this.title = ""
        this.location = ""
        this.description = ""
        this.startEvent = ""
        this.endEvent = ""
        this.startEventMilliseconds = 0
        this.endEventMilliseconds = 0
        this.fullName = ""
        this.address = ""
        this.textProductIdISNB = ""
        this.ssId = ""
        this.hidden = false
        this.password = ""
        this.url = ""
        this.createType = ""
        this.networkEncryption = ""
        this.typeCategories = TypeCategories()
        this.barcodeFormat = BarcodeFormat.QR_CODE.name
        this.favorite = false
        val time = Utils.getCurrentDateTimeSort()
        this.createDatetime = time
        this.updatedDateTime = time
        this.hiddenDatetime = time
        this.contentUnique = ""
        this.contentUniqueForUpdatedTime = ""
        this.isSynced = false
        this.uuId = Utils.getUUId()
        this.noted = ""
        this.code = ""
        this.type = ""
    }

    constructor(item: SaveEntityModel?) {
        this.id = item?.id
        this.email = item?.email
        this.subject = item?.subject
        this.message = item?.message
        this.phone = item?.phone
        this.lat = item?.lat
        this.lon = item?.lon
        this.query = item?.query
        this.title = item?.title
        this.location = item?.location
        this.description = item?.description
        this.startEvent = item?.startEvent
        this.endEvent = item?.endEvent
        this.startEventMilliseconds = item?.startEventMilliseconds
        this.endEventMilliseconds = item?.endEventMilliseconds
        this.fullName = item?.fullName
        this.address = item?.address
        this.textProductIdISNB = item?.text
        this.ssId = item?.ssId
        this.hidden = item?.hidden
        this.password = item?.password
        this.url = item?.url
        this.createType = item?.createType
        this.networkEncryption = item?.networkEncryption
        this.createDatetime = item?.createDatetime
        this.hiddenDatetime = item?.hiddenDatetime
        this.barcodeFormat = item?.barcodeFormat
        this.favorite = item?.favorite
        this.updatedDateTime = item?.updatedDateTime
        this.contentUnique = item?.contentUnique
        this.contentUniqueForUpdatedTime = item?.contentUniqueForUpdatedTime
        this.isSynced = item?.isSynced
        this.uuId = item?.uuId
        this.noted = item?.noted
        this.code = item?.code
        this.type = ""
    }

    constructor(item: SaveModel?, isSynced: Boolean) {
        this.id = item?.id
        this.email = item?.email
        this.subject = item?.subject
        this.message = item?.message
        this.phone = item?.phone
        this.lat = item?.lat
        this.lon = item?.lon
        this.query = item?.query
        this.title = item?.title
        this.location = item?.location
        this.description = item?.description
        this.startEvent = item?.startEvent
        this.endEvent = item?.endEvent
        this.startEventMilliseconds = item?.startEventMilliseconds
        this.endEventMilliseconds = item?.endEventMilliseconds
        this.fullName = item?.fullName
        this.address = item?.address
        this.textProductIdISNB = item?.textProductIdISNB
        this.ssId = item?.ssId
        this.hidden = item?.hidden
        this.password = item?.password
        this.url = item?.url
        this.createType = item?.createType
        this.networkEncryption = item?.networkEncryption
        this.createDatetime = item?.createDatetime
        this.hiddenDatetime = item?.hiddenDatetime
        this.barcodeFormat = item?.barcodeFormat
        this.favorite = item?.favorite
        this.updatedDateTime = item?.updatedDateTime
        this.contentUnique = item?.contentUnique
        this.contentUniqueForUpdatedTime = item?.contentUniqueForUpdatedTime
        this.isSynced = isSynced
        this.uuId = item?.uuId
        this.noted = item?.noted
        this.code = item?.code
        this.type = ""
    }

    fun isChecked(): Boolean {
        return isChecked
    }

    fun setChecked(checked: Boolean) {
        isChecked = checked
    }

    fun isDeleted(): Boolean {
        return isDeleted
    }

    fun setDeleted(deleted: Boolean) {
        isDeleted = deleted
    }

    fun getId(): Int {
        return id ?: 0
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun getCategoryId(): Int {
        return typeCategories?.getId() ?: 0
    }

    fun getCategoryName(): String? {
        try {
            return Utils.onTranslateCreateType(ParsedResultType.valueOf(typeCategories?.getType() ?: ParsedResultType.TEXT.name))
        }catch (e : Exception){
            return QRScannerApplication.getInstance().getString(R.string.favorite)
        }
    }

    fun getUpdatedTimeToMilliseconds(): Long{
        return Utils.getMilliseconds(updatedDateTime)
    }

    private fun isContentNoted() : Boolean {
        return !noted.isNullOrBlank()
    }

    fun getDisplay() : String?{
        if (isContentNoted()){
            return noted ?: ""
        }
        val mResult : String?
        if (createType == ParsedResultType.EMAIL_ADDRESS.name) {
            mResult = email
        } else if (createType == ParsedResultType.SMS.name) {
            mResult = message
        } else if (createType == ParsedResultType.GEO.name) {
            mResult = lat.toString() + "," + lon + "(" + query + ")"
        } else if (createType == ParsedResultType.CALENDAR.name) {
            mResult = title
        } else if (createType == ParsedResultType.ADDRESSBOOK.name) {
            mResult = getNames()
        } else if (createType == ParsedResultType.TEL.name) {
            mResult = phone
        } else if (createType == ParsedResultType.WIFI.name) {
            mResult = ssId
        } else if (createType == ParsedResultType.URI.name) {
            mResult = url
        } else {
            mResult = textProductIdISNB
        }
        return  mResult
    }


    fun getAddresses(separators : String?) : String{
        val mStringBuilder : StringBuilder = StringBuilder()
        if (createType == ParsedResultType.ADDRESSBOOK.name) {
            if (Utils.isVcard(this.code)){
                val mParsedVcard =  Utils.onParseVCard(this.code ?:"")
                //     mParsedVcard?.contact?.addresses?.values?.toList()?.joinToString(",")
                mStringBuilder.append(mParsedVcard?.contact?.addresses?.values?.toList()
                    ?.joinToString(separators?:", ") { it.getAddressValue()})
            }else{
                val mParsedVcard =  Utils.onParseMeCard(this.code ?:"")
                mStringBuilder.append(mParsedVcard?.contact?.addresses?.values?.toList()
                    ?.joinToString(separators?:", ") { it.address.orEmpty() })
            }
        }else{
            mStringBuilder.append(this.address)
        }
        return mStringBuilder.toString()
    }

    fun getPhones(separators : String?) : String{
        val mStringBuilder : StringBuilder = StringBuilder()
        if (createType == ParsedResultType.ADDRESSBOOK.name) {
            if (Utils.isVcard(this.code)){
                val mParsedVcard =  Utils.onParseVCard(this.code ?:"")
                mStringBuilder.append( mParsedVcard?.contact?.phones?.values?.toList()?.joinToString(separators?:", "))
            }else{
                val mParsedVcard =  Utils.onParseMeCard(this.code ?:"")
                mStringBuilder.append(mParsedVcard?.contact?.phones?.values?.toList()?.joinToString(separators?:", "))
            }
        }else{
            mStringBuilder.append(this.phone)
        }
        return mStringBuilder.toString()
    }

    fun getEmails(separators : String?) : String {
        val mStringBuilder : StringBuilder = StringBuilder()
        if (createType == ParsedResultType.ADDRESSBOOK.name) {
            if (Utils.isVcard(this.code)){
                val mParsedVcard =  Utils.onParseVCard(this.code ?:"")
                mStringBuilder.append(mParsedVcard?.contact?.emails?.values?.toList()?.joinToString(separators?:", "))
            }else{
                val mParsedVcard =  Utils.onParseMeCard(this.code ?:"")
                mStringBuilder.append(mParsedVcard?.contact?.emails?.values?.toList()?.joinToString(separators?:", "))
            }
        }else{
            mStringBuilder.append(this.email)
        }
        return mStringBuilder.toString()
    }

    fun getUrls(separators : String?) : String{
        val mStringBuilder : StringBuilder = StringBuilder()
        if (createType == ParsedResultType.ADDRESSBOOK.name) {
            if (Utils.isVcard(this.code)){
                val mParsedVcard =  Utils.onParseVCard(this.code ?:"")
                mStringBuilder.append(mParsedVcard?.contact?.urls?.joinToString(separators?:", "))
            }else{
                val mParsedVcard =  Utils.onParseMeCard(this.code ?:"")
                mStringBuilder.append(mParsedVcard?.contact?.urls?.joinToString(separators?:", "))
            }
        }else{
            mStringBuilder.append(url)
        }
        return mStringBuilder.toString()
    }

    fun getNames() : String{
        val mString : String? = if (createType == ParsedResultType.ADDRESSBOOK.name) {
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