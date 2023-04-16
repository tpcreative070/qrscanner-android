package tpcreative.co.qrscanner.model

import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.entities.DesignQREntity

class DesignQRModel : java.io.Serializable {
    var id = 0
    var uuId: String?
    var uuIdQR : String?
    var codeDesign : String?
    var createDatetime: String? = ""
    var updatedDateTime: String? = ""

    constructor(){
        this.uuId = Utils.getUUId()
        this.uuIdQR = ""
        this.codeDesign = ""
        this.createDatetime = Utils.getCurrentDateTimeSort()
        this.updatedDateTime =  Utils.getCurrentDateTimeSort()
    }

    constructor(data : DesignQREntityModel?){
        this.id = data?.id ?:0
        this.uuId = data?.uuId
        this.uuIdQR = data?.uuIdQR
        this.codeDesign = data?.codeDesign
        this.createDatetime = data?.createDatetime
        this.updatedDateTime = data?.updatedDateTime
    }
}