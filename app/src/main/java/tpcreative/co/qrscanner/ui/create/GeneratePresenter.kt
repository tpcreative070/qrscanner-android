package tpcreative.co.qrscanner.ui.create

import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.zxing.BarcodeFormat
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.presenter.Presenter
import tpcreative.co.qrscanner.model.FormatTypeModel
import tpcreative.co.qrscanner.model.QRCodeType
import java.util.*

class GeneratePresenter : Presenter<GenerateView?>() {
    var mList: MutableList<QRCodeType?>?
    protected var mFragment: MutableList<Fragment?>?
    var mBarcodeFormat: MutableList<FormatTypeModel?>?
    var mType: BarcodeFormat? = BarcodeFormat.EAN_13
    var mLength = 13
    var isPremium = false
    fun setList() {
        val view = view()
        mList.clear()
        if (Utils.isPremium()) {
            mList.add(QRCodeType("0", view.getContext().getString(R.string.barcode), R.drawable.ic_barcode))
        }
        mList.add(QRCodeType("1", view.getContext().getString(R.string.email), R.drawable.baseline_email_white_48))
        mList.add(QRCodeType("2", view.getContext().getString(R.string.message), R.drawable.baseline_textsms_white_48))
        mList.add(QRCodeType("3", view.getContext().getString(R.string.location), R.drawable.baseline_location_on_white_48))
        mList.add(QRCodeType("4", view.getContext().getString(R.string.event), R.drawable.baseline_event_white_48))
        mList.add(QRCodeType("5", view.getContext().getString(R.string.contact), R.drawable.baseline_perm_contact_calendar_white_48))
        mList.add(QRCodeType("6", view.getContext().getString(R.string.telephone), R.drawable.baseline_phone_white_48))
        mList.add(QRCodeType("7", view.getContext().getString(R.string.text), R.drawable.baseline_text_format_white_48))
        mList.add(QRCodeType("8", view.getContext().getString(R.string.wifi), R.drawable.baseline_network_wifi_white_48))
        mList.add(QRCodeType("9", view.getContext().getString(R.string.url), R.drawable.baseline_language_white_48))
        view.onSetView()
    }

    fun getBarcodeFormat() {
        val view = view()
        mBarcodeFormat.add(FormatTypeModel(BarcodeFormat.EAN_13.name, "EAN 13"))
        mBarcodeFormat.add(FormatTypeModel(BarcodeFormat.EAN_8.name, "EAN 8"))
        view.onSetView()
    }

    fun doInitView() {
        val view = view()
        view.onInitView()
    }

    fun doSetMaxLength(is13: Boolean, editText: EditText?) {
        if (is13) {
            val filterArray = arrayOfNulls<InputFilter?>(1)
            filterArray[0] = LengthFilter(13)
            editText.setFilters(filterArray)
            mLength = 13
        } else {
            val filterArray = arrayOfNulls<InputFilter?>(1)
            filterArray[0] = LengthFilter(8)
            editText.setFilters(filterArray)
            mLength = 8
        }
    }

    init {
        mList = ArrayList()
        mFragment = ArrayList()
        mBarcodeFormat = ArrayList()
    }
}