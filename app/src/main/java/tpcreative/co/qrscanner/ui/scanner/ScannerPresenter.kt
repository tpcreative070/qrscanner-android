package tpcreative.co.qrscanner.ui.scanner

import androidx.fragment.app.Fragment
import com.google.zxing.client.result.ParsedResultType
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.controller.PrefsController
import tpcreative.co.qrscanner.common.presenter.Presenter
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import java.util.*

class ScannerPresenter : Presenter<ScannerView?>() {
    protected var mFragment: MutableList<Fragment?>?
    protected var hashClipboard: HashMap<Any?, String?>? = HashMap()
    protected var hashClipboardResult: HashMap<Any?, String?>? = HashMap()
    protected var stringBuilderClipboard: StringBuilder? = StringBuilder()
    protected var mCount = 0
    protected var history: HistoryModel? = HistoryModel()
    fun updateValue(mCount: Int) {
        val view = view()
        this.mCount = this.mCount + mCount
        view.updateValue(QRScannerApplication.Companion.getInstance().getString(R.string.total) + ": " + this.mCount)
    }

    fun doSaveItems(mCreate: Create?) {
        when (mCreate.createType) {
            ParsedResultType.ADDRESSBOOK -> {
                /*Put item to HashClipboard*/hashClipboard["fullName"] = mCreate.fullName
                hashClipboard["address"] = mCreate.address
                hashClipboard["phone"] = mCreate.phone
                hashClipboard["email"] = mCreate.email
                history = HistoryModel()
                history.fullName = mCreate.fullName
                history.address = mCreate.address
                history.phone = mCreate.phone
                history.email = mCreate.email
                history.createType = mCreate.createType.name
                onShowUI(mCreate)
            }
            ParsedResultType.EMAIL_ADDRESS -> {
                /*Put item to HashClipboard*/hashClipboard["email"] = mCreate.email
                hashClipboard["subject"] = mCreate.subject
                hashClipboard["message"] = mCreate.message
                history = HistoryModel()
                history.email = mCreate.email
                history.subject = mCreate.subject
                history.message = mCreate.message
                history.createType = mCreate.createType.name
                onShowUI(mCreate)
            }
            ParsedResultType.PRODUCT -> {
                /*Put item to HashClipboard*/hashClipboard["productId"] = mCreate.productId
                history = HistoryModel()
                history.text = mCreate.productId
                history.createType = mCreate.createType.name
                onShowUI(mCreate)
            }
            ParsedResultType.URI -> {
                /*Put item to HashClipboard*/hashClipboard["url"] = mCreate.url
                history = HistoryModel()
                history.url = mCreate.url
                history.createType = mCreate.createType.name
                onShowUI(mCreate)
            }
            ParsedResultType.WIFI -> {
                /*Put item to HashClipboard*/hashClipboard["ssId"] = mCreate.ssId
                hashClipboard["password"] = mCreate.password
                hashClipboard["networkEncryption"] = mCreate.networkEncryption
                hashClipboard["hidden"] = if (mCreate.hidden) "Yes" else "No"
                history = HistoryModel()
                history.ssId = mCreate.ssId
                history.password = mCreate.password
                history.networkEncryption = mCreate.networkEncryption
                history.hidden = mCreate.hidden
                history.createType = mCreate.createType.name
                onShowUI(mCreate)
            }
            ParsedResultType.GEO -> {

                /*Put item to HashClipboard*/hashClipboard["lat"] = mCreate.lat.toString() + ""
                hashClipboard["lon"] = mCreate.lon.toString() + ""
                hashClipboard["query"] = mCreate.query
                history = HistoryModel()
                history.lat = mCreate.lat
                history.lon = mCreate.lon
                history.query = mCreate.query
                history.createType = mCreate.createType.name
                onShowUI(mCreate)
            }
            ParsedResultType.TEL -> {
                /*Put item to HashClipboard*/hashClipboard["phone"] = mCreate.phone
                history = HistoryModel()
                history.phone = mCreate.phone
                history.createType = mCreate.createType.name
                onShowUI(mCreate)
            }
            ParsedResultType.SMS -> {
                /*Put item to HashClipboard*/hashClipboard["phone"] = mCreate.phone
                hashClipboard["message"] = mCreate.message
                history = HistoryModel()
                history.phone = mCreate.phone
                history.message = mCreate.message
                history.createType = mCreate.createType.name
                onShowUI(mCreate)
            }
            ParsedResultType.CALENDAR -> {
                /*Put item to HashClipboard*/hashClipboard["title"] = mCreate.title
                hashClipboard["location"] = mCreate.location
                hashClipboard["description"] = mCreate.description
                hashClipboard["startEventMilliseconds"] = Utils.convertMillisecondsToDateTime(mCreate.startEventMilliseconds)
                hashClipboard["endEventMilliseconds"] = Utils.convertMillisecondsToDateTime(mCreate.endEventMilliseconds)
                history = HistoryModel()
                history.title = mCreate.title
                history.location = mCreate.location
                history.description = mCreate.description
                history.startEvent = mCreate.startEvent
                history.endEvent = mCreate.endEvent
                history.startEventMilliseconds = mCreate.startEventMilliseconds
                history.endEventMilliseconds = mCreate.endEventMilliseconds
                history.createType = mCreate.createType.name
                onShowUI(mCreate)
            }
            ParsedResultType.ISBN -> {
                /*Put item to HashClipboard*/hashClipboard["ISBN"] = mCreate.ISBN
                history = HistoryModel()
                history.text = mCreate.ISBN
                history.createType = mCreate.createType.name
                onShowUI(mCreate)
            }
            else -> {
                /*Put item to HashClipboard*/hashClipboard["text"] = mCreate.text
                history = HistoryModel()
                history.text = mCreate.text
                history.createType = mCreate.createType.name
                onShowUI(mCreate)
            }
        }
        try {
            val autoCopy = PrefsController.getBoolean(QRScannerApplication.Companion.getInstance().getString(R.string.key_copy_to_clipboard), false)
            if (autoCopy) {
                Utils.copyToClipboard(getResult(hashClipboard))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun doRefreshView() {
        val view = view()
        mCount = 0
        view.doRefreshView()
    }

    private fun onShowUI(create: Create?) {
        /*Adding new columns*/
        history.barcodeFormat = create.barcodeFormat
        history.favorite = create.favorite
        val time = Utils.getCurrentDateTimeSort()
        history.createDatetime = time
        history.updatedDateTime = time
        SQLiteHelper.onInsert(history)
        HistorySingleton.Companion.getInstance().reloadData()
    }

    private fun getResult(value: HashMap<Any?, String?>?): String? {
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

    init {
        mFragment = ArrayList()
    }
}