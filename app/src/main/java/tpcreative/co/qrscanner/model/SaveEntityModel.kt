package tpcreative.co.qrscanner.model
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.entities.SaveEntity

class SaveEntityModel {
    var id: Int? = 0
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
    var noted : String?

    /*10:39 28/11/2022
      * Using code filed in able to solve address book and email type
      * Display to view
    * */
    var code : String?

    /*20:39 09/12/2022 Added noted and favorite*/
    var hiddenDatetime: String?

    constructor(item: SaveEntity?) {
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
        this.text = item?.text
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
        this.contentUniqueForUpdatedTime = item?.contentUnique + "" + updatedDateTime
        this.isSynced = item?.isSynced
        this.uuId = item?.uuId
        this.noted = item?.noted
        this.code = item?.code
    }

    constructor(item: SaveModel?) {
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
        this.text = item?.textProductIdISNB
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
        this.contentUnique = Utils.getCodeContentByGenerate(item)
        this.contentUniqueForUpdatedTime = contentUnique + "" + updatedDateTime
        this.isSynced = item?.isSynced
        this.uuId = item?.uuId
        this.noted = item?.noted
        this.code = item?.code
    }
}