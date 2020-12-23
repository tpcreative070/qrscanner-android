package tpcreative.co.qrscanner.model

import com.google.gson.Gson
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import java.io.Serializable

class Author : Serializable {
    var version: Version? = null
    var access_token: String? = null
    var isConnectedToGoogleDrive = false
    var email: String? = null
    fun getAuthorInfo(): Author? {
        try {
            val value = PrefsController.getString(QRScannerApplication.Companion.getInstance().getString(R.string.key_author), null)
            if (value != null) {
                val author = Gson().fromJson(value, Author::class.java)
                if (author != null) {
                    return author
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Author()
    }

    companion object {
        private var instance: Author? = null
        private val TAG = Author::class.java.simpleName
        fun getInstance(): Author? {
            if (instance == null) {
                instance = Author()
            }
            return instance
        }
    }
}