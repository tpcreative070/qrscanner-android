package tpcreative.co.qrscanner.viewmodel
import android.text.InputFilter
import android.widget.EditText
import androidx.lifecycle.liveData
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.getString
import tpcreative.co.qrscanner.model.EmptyModel
import tpcreative.co.qrscanner.model.FormatTypeModel
import tpcreative.co.qrscanner.model.QRCodeType

class GenerateViewModel : BaseViewModel<EmptyModel>(){
    var mList: MutableList<QRCodeType> = mutableListOf()
    var mBarcodeFormat: MutableList<FormatTypeModel> = mutableListOf()
    var mType: BarcodeFormat? = BarcodeFormat.EAN_13
    var mLength = 13
    var isPremium = false
    fun getDataList() = liveData(Dispatchers.Main) {
        mList.clear()
        if (Utils.isPremium()) {
            mList.add(QRCodeType("0", getString(R.string.barcode), R.drawable.ic_barcode))
        }
        mList.add(QRCodeType("1", getString(R.string.email), R.drawable.baseline_email_white_48))
        mList.add(QRCodeType("2", getString(R.string.message), R.drawable.baseline_textsms_white_48))
        mList.add(QRCodeType("3", getString(R.string.location), R.drawable.baseline_location_on_white_48))
        mList.add(QRCodeType("4", getString(R.string.event), R.drawable.baseline_event_white_48))
        mList.add(QRCodeType("5", getString(R.string.contact), R.drawable.baseline_perm_contact_calendar_white_48))
        mList.add(QRCodeType("6", getString(R.string.telephone), R.drawable.baseline_phone_white_48))
        mList.add(QRCodeType("7", getString(R.string.text), R.drawable.baseline_text_format_white_48))
        mList.add(QRCodeType("8", getString(R.string.wifi), R.drawable.baseline_network_wifi_white_48))
        mList.add(QRCodeType("9", getString(R.string.url), R.drawable.baseline_language_white_48))
        emit(mList)
    }

    fun getBarcodeFormat() = liveData(Dispatchers.Main) {
        mBarcodeFormat.add(FormatTypeModel(BarcodeFormat.EAN_13.name, "EAN 13"))
        mBarcodeFormat.add(FormatTypeModel(BarcodeFormat.EAN_8.name, "EAN 8"))
        emit(mBarcodeFormat)
    }

    fun doInitView() = liveData(Dispatchers.Main){
        emit(true)
    }

    fun doSetMaxLength(is13: Boolean, editText: EditText?) {
        if (is13) {
            val filterArray = arrayOfNulls<InputFilter?>(1)
            filterArray[0] = InputFilter.LengthFilter(13)
            editText?.setFilters(filterArray)
            mLength = 13
        } else {
            val filterArray = arrayOfNulls<InputFilter?>(1)
            filterArray[0] = InputFilter.LengthFilter(8)
            editText?.filters = filterArray
            mLength = 8
        }
    }


}