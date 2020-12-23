package tpcreative.co.qrscanner.model

import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.entities.SaveEntity

class SaveEntityModel {
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

    constructor(item: SaveEntity?) {
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
        contentUniqueForUpdatedTime = item.contentUnique + "" + updatedDateTime
        isSynced = item.isSynced
        uuId = item.uuId
    }

    constructor(item: SaveModel?) {
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
        contentUnique = Utils.getCodeContentByGenerate(item)
        contentUniqueForUpdatedTime = contentUnique + "" + updatedDateTime
        isSynced = item.isSynced
        uuId = item.uuId
    }
}