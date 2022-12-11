package tpcreative.co.qrscanner.viewmodel
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.lifecycle.liveData
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import kotlinx.android.synthetic.main.activity_barcode.*
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.getString
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.*

class GenerateViewModel : BaseViewModel<EmptyModel>(){
    var mList: MutableList<QRCodeType> = mutableListOf()
    var mBarcodeFormat: MutableList<FormatTypeModel> = mutableListOf()
    var mType: BarcodeFormat? = BarcodeFormat.EAN_13
    var mLength = 13
    var isPremium = false
    val TAG = this::class.java.simpleName
    fun getDataList() = liveData(Dispatchers.Main) {
        mList.clear()
        mList.add(QRCodeType("0", getString(R.string.barcode), R.drawable.ic_barcode))
        mList.add(QRCodeType("1", getString(R.string.email), R.drawable.baseline_email_white_48))
        mList.add(QRCodeType("2", getString(R.string.message), R.drawable.baseline_textsms_white_48))
        mList.add(QRCodeType("3", getString(R.string.location), R.drawable.baseline_location_on_white_48))
        mList.add(QRCodeType("4", getString(R.string.event), R.drawable.baseline_event_white_48))
        mList.add(QRCodeType("5", getString(R.string.contact), R.drawable.baseline_perm_contact_calendar_white_48))
        mList.add(QRCodeType("6", getString(R.string.telephone), R.drawable.baseline_phone_white_48))
        mList.add(QRCodeType("7", getString(R.string.text), R.drawable.baseline_text_format_white_48))
        mList.add(QRCodeType("8", getString(R.string.wifi), R.drawable.baseline_network_wifi_white_48))
        mList.add(QRCodeType("9", getString(R.string.website), R.drawable.baseline_language_white_48))
        emit(mList)
    }

    fun getBarcodeFormat() = liveData(Dispatchers.Main) {
        mBarcodeFormat.add(FormatTypeModel(BarcodeFormat.EAN_8.name, "EAN 8"))
        mBarcodeFormat.add(FormatTypeModel(BarcodeFormat.EAN_13.name, "EAN 13"))
        mBarcodeFormat.add(FormatTypeModel(BarcodeFormat.UPC_A.name, "UPC A"))
        mBarcodeFormat.add(FormatTypeModel(BarcodeFormat.UPC_E.name, "UPC E"))
        mBarcodeFormat.add(FormatTypeModel(BarcodeFormat.CODABAR.name, "CodaBar"))
        mBarcodeFormat.add(FormatTypeModel(BarcodeFormat.DATA_MATRIX.name, "Data Matrix"))
        mBarcodeFormat.add(FormatTypeModel(BarcodeFormat.PDF_417.name, "PDF 417"))
        mBarcodeFormat.add(FormatTypeModel(BarcodeFormat.AZTEC.name, "Aztec"))
        mBarcodeFormat.add(FormatTypeModel(BarcodeFormat.CODE_128.name, "Code 128"))
        mBarcodeFormat.add(FormatTypeModel(BarcodeFormat.CODE_39.name, "Code 39"))
        mBarcodeFormat.add(FormatTypeModel(BarcodeFormat.CODE_93.name, "Code 93"))
        mBarcodeFormat.add(FormatTypeModel(BarcodeFormat.ITF.name, "ITF"))
        emit(mBarcodeFormat)
    }

    fun doInitView() = liveData(Dispatchers.Main){
        emit(true)
    }

    fun doSetMaxLength(type : BarcodeFormat, editText: EditText?) {
        editText?.text?.clear()
        editText?.inputType = InputType.TYPE_NUMBER_FLAG_SIGNED + InputType.TYPE_CLASS_NUMBER
        editText?.isSingleLine = true
        editText?.imeOptions = EditorInfo.IME_ACTION_DONE
        when(type){
            BarcodeFormat.EAN_13 ->{
                val filterArray = arrayOfNulls<InputFilter?>(1)
                filterArray[0] = InputFilter.LengthFilter(13)
                editText?.filters = filterArray
                mLength = 13
            }
            BarcodeFormat.EAN_8 ->{
                val filterArray = arrayOfNulls<InputFilter?>(1)
                filterArray[0] = InputFilter.LengthFilter(8)
                editText?.filters = filterArray
                mLength = 8
            }
            BarcodeFormat.UPC_E ->{
                val filterArray = arrayOfNulls<InputFilter?>(1)
                filterArray[0] = InputFilter.LengthFilter(8)
                editText?.filters = filterArray
                mLength = 8
            }
            BarcodeFormat.UPC_A ->{
                val filterArray = arrayOfNulls<InputFilter?>(1)
                filterArray[0] = InputFilter.LengthFilter(12)
                editText?.filters = filterArray
                mLength = 12
            }
            BarcodeFormat.CODABAR ->{
                val filterArray = arrayOfNulls<InputFilter?>(1)
                filterArray[0] = InputFilter.LengthFilter(40)
                editText?.filters = filterArray
                mLength = 40
            }
            BarcodeFormat.ITF ->{
                val filterArray = arrayOfNulls<InputFilter?>(1)
                filterArray[0] = InputFilter.LengthFilter(40)
                editText?.filters = filterArray
                mLength = 50
            }
            BarcodeFormat.CODE_39 ->{
                val filterArray = arrayOfNulls<InputFilter?>(2)
                filterArray[0] = InputFilter.LengthFilter(50)
                filterArray[1] = InputFilter.AllCaps()
                editText?.filters = filterArray
                editText?.isSingleLine = true
                editText?.imeOptions = EditorInfo.IME_ACTION_DONE
                editText?.inputType = InputType.TYPE_CLASS_TEXT
                mLength = 50
            }
            BarcodeFormat.CODE_93 ->{
                val filterArray = arrayOfNulls<InputFilter?>(2)
                filterArray[0] = InputFilter.LengthFilter(50)
                filterArray[1] = InputFilter.AllCaps()
                editText?.filters = filterArray
                editText?.isSingleLine = true
                editText?.imeOptions = EditorInfo.IME_ACTION_DONE
                editText?.inputType = InputType.TYPE_CLASS_TEXT
                mLength = 50
            }
            else -> {
                val filterArray = arrayOfNulls<InputFilter?>(1)
                filterArray[0] = InputFilter.LengthFilter(50)
                editText?.filters = filterArray
                editText?.isSingleLine = true
                editText?.imeOptions = EditorInfo.IME_ACTION_DONE
                editText?.inputType = InputType.TYPE_CLASS_TEXT
                mLength = 50
                Utils.Log(this::class.java.simpleName,"Nothing")
            }
        }
    }

    fun isText(mValue : String?) : Boolean{
        return !mValue.isNullOrEmpty()
    }

    fun doShowAds() = liveData(Dispatchers.Main) {
        if (QRScannerApplication.getInstance().isLiveAds()) {
            emit(true)
        } else {
            emit(false)
        }
    }

    fun getIntent(activity: Activity?) = liveData(Dispatchers.Main)  {
        val mData = activity?.intent?.serializable(getString(R.string.key_data), GeneralModel::class.java)
        if (mData != null) {
            emit(mData)
        } else {
            emit(null)
            Utils.Log(TAG, "Data is null")
        }
    }
}