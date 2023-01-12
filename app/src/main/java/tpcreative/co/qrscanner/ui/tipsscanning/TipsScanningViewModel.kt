package tpcreative.co.qrscanner.ui.tipsscanning

import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.viewmodel.BaseViewModel

class TipsScanningViewModel : BaseViewModel<EmptyModel>() {
    var mList: MutableList<TipsScanningModel> = mutableListOf()

    fun getData()  = liveData(Dispatchers.Main){
        mList.clear()
        mList.add(
            TipsScanningModel(
                EnumAction.DEGREE_0,
                R.drawable.ic_ean_8_show_number,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )
        mList.add(
            TipsScanningModel(
                EnumAction.DEGREE_90,
                R.drawable.ic_ean_8_show_number_90_degree,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )
        mList.add(
            TipsScanningModel(
                EnumAction.DEGREE_270,
                R.drawable.ic_ean_8_show_number_270_degree,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )
        mList.add(
            TipsScanningModel(
                EnumAction.OTHER_ORIENTATION,
                R.drawable.ic_ean_8_show_number_90_degree,
                R.drawable.ic_close,
                R.color.red_dark)
        )
        mList.add(
            TipsScanningModel(
                EnumAction.SHADOW,
                R.drawable.ic_ean_8_show_number,
                R.drawable.ic_close,
                R.color.red_dark)
        )
        mList.add(
            TipsScanningModel(
                EnumAction.TOO_CLOSE_BLURRY,
                R.drawable.barcode_ean8_blurry_new,
                R.drawable.ic_close,
                R.color.red_dark)
        )
        mList.add(
            TipsScanningModel(
                EnumAction.LED_WHEN_DARK,
                R.drawable.ic_ean_8_show_number,
                R.drawable.baseline_check_white_48,
                R.color.material_green_a700)
        )

        mList.add(
            TipsScanningModel(
                EnumAction.LOW_CONTRAST,
                R.drawable.ic_ean_8_show_number,
                R.drawable.ic_close,
                R.color.red_dark)
        )
        emit(mList)
    }

    companion object {
        private val TAG = TipsScanningViewModel::class.java.simpleName
    }
}