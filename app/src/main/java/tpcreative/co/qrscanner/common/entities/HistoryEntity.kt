package tpcreative.co.qrscanner.common.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.zxing.BarcodeFormat
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.model.HistoryEntityModel

@Entity(tableName = "history")
class HistoryEntity {
    @PrimaryKey(autoGenerate = true)
    var id = 0
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
    var endEvent: String?
    var startEventMilliseconds: Long
    var endEventMilliseconds: Long
    var fullName: String?
    var address: String?
    var text: String?
    var ssId: String?
    var hidden: Boolean
    var password: String?
    var url: String?
    var createType: String?
    var networkEncryption: String?
    var createDatetime: String? = ""
    var barcodeFormat: String?
    var favorite: Boolean = false
    var updatedDateTime: String?

    /*content_type_barcode*/
    var contentUnique: String? = ""

    /*sync data*/
    var isSynced : Boolean = false
    var uuId: String? = ""
    var noted : String? = ""

    /*10:39 28/11/2022
    * Using code filed in able to solve address book and email type
    * Display to view
   * */
    var code : String? = ""

    constructor() {
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
        this.text = ""
        this.ssId = ""
        this.hidden = false
        this.password = ""
        this.url = ""
        this.createType = ""
        this.networkEncryption = ""
        this.barcodeFormat = BarcodeFormat.QR_CODE.name
        this.favorite = false
        this.updatedDateTime = Utils.getCurrentDateTimeSort() ?: ""
        this.noted = ""
        this.code = ""
    }

    constructor(item: HistoryEntityModel?) {
        this.id = item?.id ?: 0
        this.email = item?.email ?: ""
        this.subject = item?.subject ?: ""
        this.message = item?.message ?: ""
        this.phone = item?.phone ?: ""
        this.lat = item?.lat ?: 0.0
        this.lon = item?.lon ?: 0.0
        this.query = item?.query ?: ""
        this.title = item?.title ?: ""
        this.location = item?.location ?: ""
        this.description = item?.description ?: ""
        this.startEvent = item?.startEvent ?: ""
        this.endEvent = item?.endEvent ?: ""
        this.startEventMilliseconds = item?.startEventMilliseconds ?: 0
        this.endEventMilliseconds = item?.endEventMilliseconds ?: 0
        this.fullName = item?.fullName ?: ""
        this.address = item?.address ?: ""
        this.text = item?.text ?: ""
        this.ssId = item?.ssId ?: ""
        this.hidden = item?.hidden ?: false
        this.password = item?.password ?: ""
        this.url = item?.url ?: ""
        this.createType = item?.createType ?: ""
        this.networkEncryption = item?.networkEncryption ?: ""
        this.createDatetime = item?.createDatetime ?: ""
        this.barcodeFormat = item?.barcodeFormat ?: ""
        this.favorite = item?.favorite ?: favorite
        this.updatedDateTime = item?.updatedDateTime ?: ""
        this.contentUnique = item?.contentUnique ?: ""
        this.isSynced = item?.isSynced ?: false
        this.uuId = item?.uuId ?: ""
        this.noted = item?.noted ?: ""
        this.code = item?.code ?:""
    }
}