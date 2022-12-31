package tpcreative.co.qrscanner.ui.supportedcode

import androidx.lifecycle.liveData
import com.google.zxing.BarcodeFormat
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.model.EmptyModel
import tpcreative.co.qrscanner.model.EnumAction
import tpcreative.co.qrscanner.model.SupportedCodeModel
import tpcreative.co.qrscanner.ui.filecolor.ChangeFileColorViewModel
import tpcreative.co.qrscanner.viewmodel.BaseViewModel

class SupportedCodeViewModel : BaseViewModel<EmptyModel>() {
    var mList: MutableList<SupportedCodeModel> = mutableListOf()

    fun getData()  = liveData(Dispatchers.Main){
        mList.clear()
        mList.add(
            SupportedCodeModel(
                BarcodeFormat.QR_CODE,
                EnumAction.SUPPORTED_CODES,
                "123",
                R.drawable.ic_qr_code,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )
        mList.add(
            SupportedCodeModel(
                BarcodeFormat.EAN_13,
                EnumAction.SUPPORTED_CODES,
                "1234567890128",
                R.drawable.ic_ean_13,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )
        mList.add(
            SupportedCodeModel(
                BarcodeFormat.EAN_8,
                EnumAction.SUPPORTED_CODES,
                "12345670",
                R.drawable.ic_ean_8,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )
        mList.add(
            SupportedCodeModel(
                BarcodeFormat.EAN_8,
                EnumAction.EAN_5,
                "12345670",
                R.drawable.ic_ean_5,
                R.drawable.ic_close,
                R.color.red_dark)
        )
        mList.add(
            SupportedCodeModel(
                BarcodeFormat.ITF,
                EnumAction.SUPPORTED_CODES,
                "123457",
                R.drawable.ic_itf,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )
        mList.add(
            SupportedCodeModel(
                BarcodeFormat.UPC_A,
                EnumAction.SUPPORTED_CODES,
                "123456789012",
                R.drawable.ic_upc_a_13,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )
        mList.add(
            SupportedCodeModel(
                BarcodeFormat.UPC_E,
                EnumAction.SUPPORTED_CODES,
                "01234565",
                R.drawable.ic_upc_e_8,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )

        mList.add(
            SupportedCodeModel(
                BarcodeFormat.CODABAR,
                EnumAction.SUPPORTED_CODES,
                "20012345678909",
                R.drawable.ic_codabar,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )
        mList.add(
            SupportedCodeModel(
                BarcodeFormat.CODE_39,
                EnumAction.CODE_25,
                "123",
                R.drawable.ic_code_25,
                R.drawable.ic_close,
                R.color.red_dark)
        )
        mList.add(
            SupportedCodeModel(
                BarcodeFormat.CODE_39,
                EnumAction.SUPPORTED_CODES,
                "123",
                R.drawable.ic_code_39,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )
        mList.add(
            SupportedCodeModel(
                BarcodeFormat.CODE_93,
                EnumAction.SUPPORTED_CODES,
                "123",
                R.drawable.ic_code_93,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )
        mList.add(
            SupportedCodeModel(
                BarcodeFormat.CODE_128,
                EnumAction.SUPPORTED_CODES,
                "123",
                R.drawable.ic_code_128,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )
        mList.add(
            SupportedCodeModel(
                BarcodeFormat.RSS_14,
                EnumAction.SUPPORTED_CODES,
                "20012345678909",
                R.drawable.ic_databar,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )
        mList.add(
            SupportedCodeModel(
                BarcodeFormat.AZTEC,
                EnumAction.SUPPORTED_CODES,
                "123",
                R.drawable.ic_aztec,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )
        mList.add(
            SupportedCodeModel(
                BarcodeFormat.DATA_MATRIX,
                EnumAction.SUPPORTED_CODES,
                "123",
                R.drawable.ic_data_matrix,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )
        mList.add(
            SupportedCodeModel(
                BarcodeFormat.PDF_417,
                EnumAction.SUPPORTED_CODES,
                "123",
                R.drawable.ic_pdf_417,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )
        emit(mList)
    }

    companion object {
        private val TAG = ChangeFileColorViewModel::class.java.simpleName
    }
}