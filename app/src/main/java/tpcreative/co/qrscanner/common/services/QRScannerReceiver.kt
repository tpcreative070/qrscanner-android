package tpcreative.co.qrscanner.common.services

import android.content.*
import android.net.ConnectivityManager
import android.util.Log

class QRScannerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive")
        val action = intent.getAction()
        /*In the case status of network changed and then updating status for app(Connected/Disconnect)*/if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION, ignoreCase = true)) {
            val cm = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            val isConnected = (activeNetwork != null
                    && activeNetwork.isConnected)
            if (connectivityReceiverListener != null) {
                connectivityReceiverListener.onNetworkConnectionChanged(isConnected)
            }
        }
    }

    interface ConnectivityReceiverListener {
        open fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    companion object {
        val TAG = QRScannerReceiver::class.java.simpleName
        var connectivityReceiverListener: ConnectivityReceiverListener? = null
        fun isConnected(): Boolean {
            val cm = QRScannerApplication.Companion.getInstance().getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetworkInfo
            return (activeNetwork != null
                    && activeNetwork.isConnectedOrConnecting)
        }
    }
}