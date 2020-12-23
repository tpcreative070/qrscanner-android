package tpcreative.co.qrscanner.ui.scannerresult

import android.util.Log
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.model.Create
import java.util.*

class ScannerResultPresenter : Presenter<ScannerResultView?>() {
    var result: Create?
    var hashClipboard: HashMap<Any?, String?>? = HashMap()
    var hashClipboardResult: HashMap<Any?, String?>? = HashMap()
    protected var stringBuilderClipboard: StringBuilder? = StringBuilder()
    var mListItemNavigation: MutableList<ItemNavigation?>?
    fun doShowAds() {
        val view: ScannerResultView = view()
        if (!Utils.isPremium() && Utils.isLiveAds()) {
            view.doShowAds(true)
        } else {
            view.doShowAds(false)
        }
    }

    fun getIntent(activity: Activity?) {
        val view: ScannerResultView = view()
        val bundle: Bundle = activity.getIntent().getExtras()
        val data = bundle.get(QRScannerApplication.Companion.getInstance().getString(R.string.key_data)) as Create
        if (data != null) {
            result = data
        }
        view.setView()
        if (BuildConfig.DEBUG) {
            Log.d(TAG, Gson().toJson(result))
        }
    }

    fun getResult(value: HashMap<Any?, String?>?): String? {
        stringBuilderClipboard = StringBuilder()
        if (value != null && value.size > 0) {
            var i = 1
            for ((_, value1) in value) {
                if (i == value.size) {
                    stringBuilderClipboard.append(value1)
                } else {
                    stringBuilderClipboard.append(value1)
                    stringBuilderClipboard.append("\n")
                }
                i += 1
            }
            return stringBuilderClipboard.toString()
        }
        return ""
    }

    companion object {
        private val TAG = ScannerResultPresenter::class.java.simpleName
    }

    init {
        result = Create()
        mListItemNavigation = ArrayList<ItemNavigation?>()
    }
}