package tpcreative.co.qrscanner.viewmodel
import androidx.lifecycle.liveData
import com.google.zxing.client.result.ParsedResultType
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.HistorySingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.CreateModel
import tpcreative.co.qrscanner.model.EmptyModel
import tpcreative.co.qrscanner.model.HistoryModel
import java.util.HashMap

class ScannerViewModel : BaseViewModel<EmptyModel>(){
    var hashClipboard: HashMap<Any?, String?>? = HashMap()
    var hashClipboardResult: HashMap<Any?, String?>? = HashMap()
    var stringBuilderClipboard: StringBuilder? = StringBuilder()
    var mCount = 0
    var history: HistoryModel? = HistoryModel()


    fun updateValue(mValue: Int) = liveData(Dispatchers.Main){
        mCount += mValue
        emit(QRScannerApplication.getInstance().getString(R.string.total) + ": " + mCount)
    }

    fun doSaveItems(mCreate: CreateModel?) {
        when (mCreate?.createType) {
            ParsedResultType.ADDRESSBOOK -> {
                /*Put item to HashClipboard*/
                hashClipboard?.set("fullName", mCreate.fullName)
                hashClipboard?.set("address", mCreate.address)
                hashClipboard?.set("phone", mCreate.phone)
                hashClipboard?.set("email", mCreate.email)
                history = HistoryModel()
                history?.fullName = mCreate.fullName
                history?.address = mCreate.address
                history?.phone = mCreate.phone
                history?.email = mCreate.email
                history?.createType = mCreate.createType?.name
                onShowUI(mCreate)
            }
            ParsedResultType.EMAIL_ADDRESS -> {
                /*Put item to HashClipboard*/
                hashClipboard?.set("email", mCreate.email)
                hashClipboard?.set("subject", mCreate.subject)
                hashClipboard?.set("message", mCreate.message)
                history = HistoryModel()
                history?.email = mCreate.email
                history?.subject = mCreate.subject
                history?.message = mCreate.message
                history?.createType = mCreate.createType?.name
                onShowUI(mCreate)
            }
            ParsedResultType.PRODUCT -> {
                /*Put item to HashClipboard*/
                hashClipboard?.set("productId", mCreate.productId)
                history = HistoryModel()
                history?.text = mCreate.productId
                history?.createType = mCreate.createType?.name
                onShowUI(mCreate)
            }
            ParsedResultType.URI -> {
                /*Put item to HashClipboard*/
                hashClipboard?.set("url", mCreate.url)
                history = HistoryModel()
                history?.url = mCreate.url
                history?.createType = mCreate.createType?.name
                onShowUI(mCreate)
            }
            ParsedResultType.WIFI -> {
                /*Put item to HashClipboard*/
                hashClipboard?.set("ssId", mCreate.ssId)
                hashClipboard?.set("password", mCreate.password)
                hashClipboard?.set("networkEncryption", mCreate.networkEncryption)
                hashClipboard?.set("hidden", if (mCreate.hidden) "Yes" else "No")
                history = HistoryModel()
                history?.ssId = mCreate.ssId
                history?.password = mCreate.password
                history?.networkEncryption = mCreate.networkEncryption
                history?.hidden = mCreate.hidden
                history?.createType = mCreate.createType?.name
                onShowUI(mCreate)
            }
            ParsedResultType.GEO -> {
                /*Put item to HashClipboard*/
                hashClipboard?.set("lat", mCreate.lat.toString() + "")
                hashClipboard?.set("lon", mCreate.lon.toString() + "")
                hashClipboard?.set("query", mCreate.query)
                history = HistoryModel()
                history?.lat = mCreate.lat
                history?.lon = mCreate.lon
                history?.query = mCreate.query
                history?.createType = mCreate.createType?.name
                onShowUI(mCreate)
            }
            ParsedResultType.TEL -> {
                /*Put item to HashClipboard*/
                hashClipboard?.set("phone", mCreate.phone)
                history = HistoryModel()
                history?.phone = mCreate.phone
                history?.createType = mCreate.createType?.name
                onShowUI(mCreate)
            }
            ParsedResultType.SMS -> {
                /*Put item to HashClipboard*/
                hashClipboard?.set("phone", mCreate.phone)
                hashClipboard?.set("message", mCreate.message)
                history = HistoryModel()
                history?.phone = mCreate.phone
                history?.message = mCreate.message
                history?.createType = mCreate.createType?.name
                onShowUI(mCreate)
            }
            ParsedResultType.CALENDAR -> {
                /*Put item to HashClipboard*/
                hashClipboard?.set("title", mCreate.title)
                hashClipboard?.set("location", mCreate.location)
                hashClipboard?.set("description", mCreate.description)
                hashClipboard?.set("startEventMilliseconds", Utils.convertMillisecondsToDateTime(mCreate.startEventMilliseconds))
                hashClipboard?.set("endEventMilliseconds", Utils.convertMillisecondsToDateTime(mCreate.endEventMilliseconds))
                history = HistoryModel()
                history?.title = mCreate.title
                history?.location = mCreate.location
                history?.description = mCreate.description
                history?.startEvent = mCreate.startEvent
                history?.endEvent = mCreate.endEvent
                history?.startEventMilliseconds = mCreate.startEventMilliseconds
                history?.endEventMilliseconds = mCreate.endEventMilliseconds
                history?.createType = mCreate.createType?.name
                onShowUI(mCreate)
            }
            ParsedResultType.ISBN -> {
                /*Put item to HashClipboard*/
                hashClipboard?.set("ISBN", mCreate.ISBN)
                history = HistoryModel()
                history?.text = mCreate.ISBN
                history?.createType = mCreate.createType?.name
                onShowUI(mCreate)
            }
            else -> {
                /*Put item to HashClipboard*/
                hashClipboard?.set("text", mCreate?.text)
                history = HistoryModel()
                history?.text = mCreate?.text
                history?.createType = mCreate?.createType?.name
                onShowUI(mCreate)
            }
        }
        try {
            val autoCopy = PrefsController.getBoolean(QRScannerApplication.getInstance().getString(R.string.key_copy_to_clipboard), false)
            if (autoCopy) {
                Utils.copyToClipboard(getResult(hashClipboard))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun doRefreshView() = liveData(Dispatchers.Main) {
        mCount = 0
        emit(mCount)
    }

    private fun onShowUI(create: CreateModel?) {
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