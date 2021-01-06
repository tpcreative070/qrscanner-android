package tpcreative.co.qrscanner.common.services
import android.content.*
import android.net.ConnectivityManager
import tpcreative.co.qrscanner.common.network.NetworkUtil

class QRScannerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent) {
        val action: String? = intent.action
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION, ignoreCase = true)) {
            connectivityReceiverListener?.onNetworkConnectionChanged(!NetworkUtil.pingIpAddress(QRScannerApplication.getInstance()))
        }
        if (action.equals(Intent.ACTION_SCREEN_OFF, ignoreCase = true)) {
            connectivityReceiverListener?.onActionScreenOff()
        }
    }

    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
        fun onActionScreenOff()
    }

    companion object {
        val TAG = QRScannerReceiver::class.java.simpleName
        var connectivityReceiverListener: ConnectivityReceiverListener? = null
        fun isConnected(): Boolean {
            return !NetworkUtil.pingIpAddress(QRScannerApplication.getInstance())
        }
    }
}