package tpcreative.co.qrscanner.common.api.response

import java.io.Serializable

open class BaseResponseDrive : Serializable {
    var error: ErrorResponse? = null
}