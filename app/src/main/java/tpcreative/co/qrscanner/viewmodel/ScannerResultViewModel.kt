package tpcreative.co.qrscanner.viewmodel
import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.Create
import tpcreative.co.qrscanner.model.ItemNavigation
import java.util.HashMap

class ScannerResultViewModel : BaseViewModel<ItemNavigation>() {

    override val dataList: MutableList<ItemNavigation>
        get() = super.dataList
    var result: Create?
    var hashClipboard: HashMap<Any?, String?> = HashMap()
    var hashClipboardResult: HashMap<Any?, String?>? = HashMap()
    protected var stringBuilderClipboard: StringBuilder? = StringBuilder()
    val mListNavigation : MutableList<ItemNavigation> = mutableListOf()

    fun getIntent(activity: Activity?)  = liveData(Dispatchers.Main){
        val bundle: Bundle? = activity?.intent?.extras
        val data = bundle?.get(QRScannerApplication.getInstance().getString(R.string.key_data)) as Create
        result = data
        emit(true)
    }

    fun doShowAds() = liveData(Dispatchers.Main) {
        if (!Utils.isPremium() && QRScannerApplication.getInstance().isLiveAds()) {
            emit(true)
        } else {
           emit(false)
        }
    }

    fun doShowAudienceAds() = liveData(Dispatchers.Main){
        if (!Utils.isPremium() && QRScannerApplication.getInstance().isLiveAds()) {
            emit(true)
        } else {
            emit(false)
        }
    }

    fun getResult(value: HashMap<Any?, String?>?): String {
        stringBuilderClipboard = StringBuilder()
        if (value != null && value.size > 0) {
            var i = 1
            for ((_, value1) in value) {
                if (i == value.size) {
                    stringBuilderClipboard?.append(value1)
                } else {
                    stringBuilderClipboard?.append(value1)
                    stringBuilderClipboard?.append("\n")
                }
                i += 1
            }
            return stringBuilderClipboard.toString()
        }
        return ""
    }

    init {
        result = Create()
    }
}