package tpcreative.co.qrscanner.common.api.response

import com.google.gson.Gson
import tpcreative.co.qrscanner.model.Version
import java.io.Serializable

open class BaseResponse : Serializable {
    var message: String? = null
    var error = false
    var nextPage: String? = null
    var version: Version? = null
    fun toFormResponse(): String? {
        return Gson().toJson(this)
    }
}