package tpcreative.co.qrscanner.common.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.zxing.BarcodeFormat
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.model.HistoryEntityModel

@Entity(tableName = "history")
class HistoryEntity {
    @PrimaryKey(autoGenerate = true)
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
    var text: String?
    var ssId: String?
    var hidden: Boolean?
    var password: String?
    var url: String?
    var createType: String?
    var networkEncryption: String?
    var createDatetime: String? = null
    var barcodeFormat: String?
    var favorite: Boolean?
    var updatedDateTime: String?

    /*content_type_barcode*/
    var contentUnique: String? = null

    /*sync data*/
    var isSynced : Boolean? = false
    var uuId: String? = null

    constructor(
            email: String?,
            subject: String?,
            message: String?,
            phone: String?,
            lat: Double,
            lon: Double,
            query: String?,
            title: String?,
            location: String?,
            description: String?,
            startEvent: String?,
            endEvent: String?,
            startEventMilliseconds: Long,
            endEventMilliseconds: Long,
            fullName: String?,
            address: String?,
            text: String?,
            ssId: String?,
            hidden: Boolean,
            password: String?,
            url: String?,
            createType: String?,
            networkEncryption: String?,
            createDatetime: String?, barcodeFormat: BarcodeFormat?, updatedDateTime: String?) {
        this.email = email
        this.subject = subject
        this.message = message
        this.phone = phone
        this.lat = lat
        this.lon = lon
        this.query = query
        this.title = title
        this.location = location
        this.description = description
        this.startEvent = startEvent
        this.endEvent = endEvent
        this.startEventMilliseconds = startEventMilliseconds
        this.endEventMilliseconds = endEventMilliseconds
        this.fullName = fullName
        this.address = address
        this.text = text
        this.ssId = ssId
        this.hidden = hidden
        this.password = password
        this.url = url
        this.createType = createType
        this.networkEncryption = networkEncryption
        this.createDatetime = createDatetime
        this.barcodeFormat = barcodeFormat?.name
        favorite = false
        this.updatedDateTime = updatedDateTime
    }

    constructor() {
        email = ""
        subject = ""
        message = ""
        phone = ""
        lat = 0.0
        lon = 0.0
        startEventMilliseconds = 0
        endEventMilliseconds = 0
        query = ""
        title = ""
        location = ""
        description = ""
        startEvent = ""
        endEvent = ""
        fullName = ""
        address = ""
        text = ""
        ssId = ""
        hidden = false
        password = ""
        url = ""
        createType = ""
        networkEncryption = ""
        barcodeFormat = BarcodeFormat.QR_CODE.name
        favorite = false
        updatedDateTime = Utils.getCurrentDateTimeSort()
    }

    constructor(item: HistoryEntityModel?) {
        id = item?.id
        email = item?.email
        subject = item?.subject
        message = item?.message
        phone = item?.phone
        lat = item?.lat
        lon = item?.lon
        query = item?.query
        title = item?.title
        location = item?.location
        description = item?.description
        startEvent = item?.startEvent
        endEvent = item?.endEvent
        startEventMilliseconds = item?.startEventMilliseconds
        endEventMilliseconds = item?.endEventMilliseconds
        fullName = item?.fullName
        address = item?.address
        text = item?.text
        ssId = item?.ssId
        hidden = item?.hidden
        password = item?.password
        url = item?.url
        createType = item?.createType
        networkEncryption = item?.networkEncryption
        createDatetime = item?.createDatetime
        barcodeFormat = item?.barcodeFormat
        favorite = item?.favorite
        updatedDateTime = item?.updatedDateTime
        contentUnique = item?.contentUnique
        isSynced = item?.isSynced
        uuId = item?.uuId
    }
}