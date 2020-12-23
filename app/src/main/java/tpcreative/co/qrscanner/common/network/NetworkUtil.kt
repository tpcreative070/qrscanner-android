package tpcreative.co.qrscanner.common.network

import android.content.Context
import android.net.ConnectivityManager

object NetworkUtil {
    var TYPE_WIFI = 1
    var TYPE_MOBILE = 2
    var TYPE_NOT_CONNECTED = 0
    private val IP: String? = "google.com.vn"
    fun getConnectivityStatus(context: Context?): Int {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = cm.activeNetworkInfo
        if (null != activeNetwork) {
            if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) return TYPE_WIFI
            if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) return TYPE_MOBILE
        }
        return TYPE_NOT_CONNECTED
    }

    fun pingIpAddress(context: Context?): Boolean {
        val CManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val NInfo = CManager.activeNetworkInfo
        return if (NInfo != null && NInfo.isConnectedOrConnecting) {
            false
        } else true
    }
}