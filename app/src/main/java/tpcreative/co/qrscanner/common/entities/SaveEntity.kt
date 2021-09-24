package tpcreative.co.qrscanner.common.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.zxing.BarcodeFormat
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.model.SaveEntityModel
import java.io.Serializable

@Entity(tableName = "save")
class SaveEntity : Serializable {
    @PrimaryKey(autoGenerate = true)
    var id  = 0
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
    var favorite: Boolean
    var updatedDateTime: String?

    /*content_type_barcode*/
    var contentUnique: String? = ""

    /*sync data*/
    var isSynced : Boolean = false
    var uuId: String? = ""
    var noted : String? = ""

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
        barcodeFormat = BarcodeFormat.QR_CODE.name
        favorite = false
        updatedDateTime = Utils.getCurrentDateTimeSort() ?: ""
        noted  = ""
    }

    constructor(item: SaveEntityModel?) {
        id = item?.id ?: 0
        email = item?.email ?: ""
        subject = item?.subject ?: ""
        message = item?.message ?: ""
        phone = item?.phone ?: ""
        lat = item?.lat ?: 0.0
        lon = item?.lon ?: 0.0
        query = item?.query ?: ""
        title = item?.title ?: ""
        location = item?.location ?: ""
        description = item?.description ?: ""
        startEvent = item?.startEvent ?: ""
        endEvent = item?.endEvent ?: ""
        startEventMilliseconds = item?.startEventMilliseconds ?: 0
        endEventMilliseconds = item?.endEventMilliseconds ?: 0
        fullName = item?.fullName ?: ""
        address = item?.address ?: ""
        text = item?.text ?: ""
        ssId = item?.ssId ?: ""
        hidden = item?.hidden ?: false
        password = item?.password  ?: ""
        url = item?.url ?: ""
        createType = item?.createType ?: ""
        networkEncryption = item?.networkEncryption ?: ""
        createDatetime = item?.createDatetime ?: ""
        barcodeFormat = item?.barcodeFormat ?: ""
        favorite = item?.favorite ?: false
        updatedDateTime = item?.updatedDateTime ?: ""
        contentUnique = item?.contentUnique ?: ""
        isSynced = item?.isSynced ?: false
        uuId = item?.uuId ?: ""
        noted = item?.noted ?: ""
    }
}