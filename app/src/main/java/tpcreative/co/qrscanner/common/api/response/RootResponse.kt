package tpcreative.co.qrscanner.common.api.response

import java.io.Serializable

class RootResponse : BaseResponse(), Serializable {
    var data: DataResponse? = null
}