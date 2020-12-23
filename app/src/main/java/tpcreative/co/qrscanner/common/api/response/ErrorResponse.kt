package tpcreative.co.qrscanner.common.api.response

import java.io.Serializable

class ErrorResponse : Serializable {
    var code = 0
    var message: String? = null
}