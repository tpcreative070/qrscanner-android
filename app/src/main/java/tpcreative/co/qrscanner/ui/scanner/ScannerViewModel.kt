package tpcreative.co.qrscanner.ui.scanner
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.HistorySingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.extension.onGeneralParse
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.GeneralModel
import tpcreative.co.qrscanner.model.EmptyModel
import tpcreative.co.qrscanner.model.HistoryModel
import tpcreative.co.qrscanner.viewmodel.BaseViewModel
import java.util.HashMap

class ScannerViewModel : BaseViewModel<EmptyModel>(){
    var hashClipboard: HashMap<Any?, String?>? = HashMap()
    var stringBuilderClipboard: StringBuilder? = StringBuilder()
    var mCount = 0
    var history: HistoryModel? = HistoryModel()
    var isRequestDone : Boolean = false
    var isRequestSettings : Boolean = false
    var isAnyPermissionPermanentlyDenied : Boolean = false
    var isRequiredStartService : Boolean = false
    var isLight : Boolean = false
    var zoom : Float = 0F


    fun updateValue(mValue: Int, callback: (result: Int?) -> Unit){
        mCount += mValue
        Utils.setCountContinueScan(mCount)
        callback.invoke(mCount)
    }

    fun doSaveItems(mCreate: GeneralModel?) {
        mCreate?.let {
            history = Utils.onGeneralParse(mCreate,HistoryModel::class)
            history?.code = mCreate.code
            history?.hashClipboard?.let {
                hashClipboard = history?.hashClipboard
            }
            try {
                onShowUI(it)
                val autoCopy = PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_copy_to_clipboard), false)
                if (autoCopy) {
                    Utils.copyToClipboard(getResult(hashClipboard))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun doRefreshView() = liveData(Dispatchers.Main) {
        mCount = 0
        emit(mCount)
    }

    private fun onShowUI(create: GeneralModel?) {
        /*Adding new columns*/
        history?.barcodeFormat = create?.barcodeFormat
        history?.favorite = create?.favorite
        val time = Utils.getCurrentDateTimeSort()
        history?.createDatetime = time
        history?.updatedDateTime = time
        SQLiteHelper.onInsert(history)
        HistorySingleton.getInstance()?.reloadData()
    }

    private fun getResult(value: HashMap<Any?, String?>?): String {
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

    fun isBarCode(type : String?) : Boolean{
        return !type.isNullOrEmpty()
    }
}