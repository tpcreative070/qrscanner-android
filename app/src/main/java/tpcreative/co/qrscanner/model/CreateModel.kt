package tpcreative.co.qrscanner.model
import com.google.zxing.client.result.ParsedResultType
import tpcreative.co.qrscanner.common.Utils
import java.io.Serializable

class CreateModel : Serializable {
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
    var text: String?
    var productId: String? = null
    var ISBN: String? = null
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

    /*Sync data*/
    var isSynced = false
    var uuId: String? = null

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
        password = ""
        url = ""
        hidden = false
        createType = ParsedResultType.TEXT
        fragmentType = EnumFragmentType.SCANNER
        enumImplement = EnumImplement.VIEW
        id = 0
        barcodeFormat = ""
        favorite = false
        updatedDateTime = Utils.getCurrentDateTimeSort()
    }

    constructor(save: SaveModel?) {
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
        password = ""
        url = ""
        hidden = false
        createType = ParsedResultType.TEXT
        fragmentType = EnumFragmentType.CREATE
        enumImplement = EnumImplement.CREATE
        id = 0
        barcodeFormat = ""
        favorite = false
        updatedDateTime = Utils.getCurrentDateTimeSort()
        createdDateTime = if (save != null) save.createDatetime else Utils.getCurrentDateTimeSort()
        enumImplement = if (save != null) EnumImplement.EDIT else EnumImplement.CREATE
        id = save?.id ?: 0
        isSynced = save != null && save.isSynced ?: false
        uuId = if (save != null) save.uuId else Utils.getUUId()
        uuId = if (save != null) save.uuId else Utils.getUUId()
    }
}