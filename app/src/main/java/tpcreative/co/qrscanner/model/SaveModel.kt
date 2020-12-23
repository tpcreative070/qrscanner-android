package tpcreative.co.qrscanner.model

import com.google.zxing.BarcodeFormat
import tpcreative.co.qrscanner.common.Utils
import java.io.Serializable

class SaveModel : Serializable {
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
    var createDatetime: String?
    var barcodeFormat: String?
    var favorite: Boolean
    var updatedDateTime: String?

    /*content_type_barcode*/
    var contentUnique: String?
    var contentUniqueForUpdatedTime: String?

    /*sync data*/
    var isSynced: Boolean
    var uuId: String?

    /*Custom fields*/
    var typeCategories: TypeCategories? = null
    private var isChecked = false
    private var isDeleted = false

    constructor() {
        email = ""
        subject = ""
        message = ""
        phone = ""
        lat = 0.0
        lon = 0.0
        query = ""
        title = ""
        location = ""
        description = ""
        startEvent = ""
        endEvent = ""
        startEventMilliseconds = 0
        endEventMilliseconds = 0
        fullName = ""
        address = ""
        text = ""
        ssId = ""
        hidden = false
        password = ""
        url = ""
        createType = ""
        networkEncryption = ""
        typeCategories = TypeCategories()
        barcodeFormat = BarcodeFormat.QR_CODE.name
        favorite = false
        val time = Utils.getCurrentDateTimeSort()
        createDatetime = time
        updatedDateTime = time
        contentUnique = ""
        contentUniqueForUpdatedTime = ""
        isSynced = false
        uuId = Utils.getUUId()
    }

    constructor(item: SaveEntityModel?) {
        id = item.id
        email = item.email
        subject = item.subject
        message = item.message
        phone = item.phone
        lat = item.lat
        lon = item.lon
        query = item.query
        title = item.title
        location = item.location
        description = item.description
        startEvent = item.startEvent
        endEvent = item.endEvent
        startEventMilliseconds = item.startEventMilliseconds
        endEventMilliseconds = item.endEventMilliseconds
        fullName = item.fullName
        address = item.address
        text = item.text
        ssId = item.ssId
        hidden = item.hidden
        password = item.password
        url = item.url
        createType = item.createType
        networkEncryption = item.networkEncryption
        createDatetime = item.createDatetime
        barcodeFormat = item.barcodeFormat
        favorite = item.favorite
        updatedDateTime = item.updatedDateTime
        contentUnique = item.contentUnique
        contentUniqueForUpdatedTime = item.contentUniqueForUpdatedTime
        isSynced = item.isSynced
        uuId = item.uuId
    }

    constructor(item: SaveModel?, isSynced: Boolean) {
        id = item.id
        email = item.email
        subject = item.subject
        message = item.message
        phone = item.phone
        lat = item.lat
        lon = item.lon
        query = item.query
        title = item.title
        location = item.location
        description = item.description
        startEvent = item.startEvent
        endEvent = item.endEvent
        startEventMilliseconds = item.startEventMilliseconds
        endEventMilliseconds = item.endEventMilliseconds
        fullName = item.fullName
        address = item.address
        text = item.text
        ssId = item.ssId
        hidden = item.hidden
        password = item.password
        url = item.url
        createType = item.createType
        networkEncryption = item.networkEncryption
        createDatetime = item.createDatetime
        barcodeFormat = item.barcodeFormat
        favorite = item.favorite
        updatedDateTime = item.updatedDateTime
        contentUnique = item.contentUnique
        contentUniqueForUpdatedTime = item.contentUniqueForUpdatedTime
        this.isSynced = isSynced
        uuId = item.uuId
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
        return id
    }

    fun setId(id: Int) {
        this.id = id
    }

    fun getCategoryId(): Int {
        return typeCategories.getId()
    }

    fun getCategoryName(): String? {
        return typeCategories.getType()
    }
}