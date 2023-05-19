package tpcreative.co.qrscanner.common.entities

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import tpcreative.co.qrscanner.model.DesignQREntityModel

@Entity("design_qr",indices = [Index(value = ["uuIdQR"], unique = true)])
//@Entity("design_qr")
class DesignQREntity {
    @PrimaryKey(autoGenerate = true)
    var id = 0
    var uuId: String?
    var uuIdQR : String?
    var codeDesign : String?
    var createDatetime: String?
    var updatedDateTime: String?
    constructor(){
        this.uuId = ""
        this.uuIdQR = ""
        this.codeDesign = ""
        this.createDatetime = ""
        this.updatedDateTime = ""
    }

    constructor(data : DesignQREntityModel?){
        this.id = data?.id ?:0
        this.uuId = data?.uuId ?: ""
        this.uuIdQR = data?.uuIdQR ?:""
        this.codeDesign = data?.codeDesign
        this.createDatetime = data?.createDatetime
        this.updatedDateTime = data?.updatedDateTime
    }
}