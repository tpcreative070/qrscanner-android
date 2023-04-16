package tpcreative.co.qrscanner.model

import tpcreative.co.qrscanner.common.entities.DesignQREntity

class DesignQREntityModel {
    var id = 0
    var uuId: String?
    var uuIdQR : String?
    var codeDesign : String?
    var createDatetime: String? = ""
    var updatedDateTime: String? = ""

    constructor(){
        this.uuId = ""
        this.uuIdQR = ""
        this.codeDesign = ""
    }

    constructor(data : DesignQRModel?){
        this.uuId = data?.uuId
        this.uuIdQR = data?.uuIdQR
        this.codeDesign = data?.codeDesign
    }

    constructor(data : DesignQREntity){
        this.id = data.id
        this.uuId = data.uuId
        this.uuIdQR = data.uuIdQR
        this.codeDesign = data.codeDesign
    }

}