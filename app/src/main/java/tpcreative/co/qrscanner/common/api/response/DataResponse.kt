package tpcreative.co.qrscanner.common.api.response

import tpcreative.co.qrscanner.common.api.request.CheckoutRequest
import java.io.Serializable

class DataResponse : Serializable {
    var checkout: CheckoutRequest? = null
}