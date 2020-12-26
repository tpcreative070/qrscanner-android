package tpcreative.co.qrscanner.common.view.crop
import android.util.Log

internal object Log {
    private val TAG: String? = "android-crop"
    fun e(msg: String?) {
        Log.e(TAG, "$msg")
    }

    fun e(msg: String?, e: Throwable?) {
        Log.e(TAG, msg, e)
    }
}