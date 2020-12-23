package tpcreative.co.qrscanner.model

import tpcreative.co.qrscanner.common.api.response.BaseResponseDrive
import tpcreative.co.qrscanner.common.api.response.DriveResponse
import java.io.Serializable

class DriveAbout : BaseResponseDrive(), Serializable {
    var inAppUsed: Long = 0
    var user: DriveUser? = null
    var storageQuota: StorageQuota? = null

    /*Create folder*/ /*Drive api queries*/
    var files: MutableList<DriveResponse?>? = null
}