package tpcreative.co.qrscanner.common.api.request

import com.google.gson.Gson

class BaseRequest {
    fun toFormRequest(): String? {
        return Gson().toJson(this)
    }
}