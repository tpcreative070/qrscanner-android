package tpcreative.co.qrscanner.ui.help

import androidx.lifecycle.liveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.EmptyModel
import tpcreative.co.qrscanner.model.EnumAction
import tpcreative.co.qrscanner.model.HelpModel
import tpcreative.co.qrscanner.model.Theme
import tpcreative.co.qrscanner.ui.filecolor.ChangeFileColorViewModel
import tpcreative.co.qrscanner.viewmodel.BaseViewModel

class HelpViewModel : BaseViewModel<EmptyModel>() {
    var mList: MutableList<HelpModel> = mutableListOf()
    fun getData()  = liveData(Dispatchers.Main){
        mList.clear()
        mList.add(HelpModel(R.drawable.baseline_check_white_48,R.color.colorAccent,EnumAction.SUPPORTED_CODES,QRScannerApplication.getInstance().getString(R.string.supported_codes)))
        mList.add(HelpModel(R.drawable.ic_tips,R.color.colorAccent,EnumAction.TIPS_SCANNING,QRScannerApplication.getInstance().getString(R.string.tips_for_scanning)))
        mList.add(HelpModel(R.drawable.ic_youtube,R.color.colorAccent,EnumAction.GUIDES_VIDEO,QRScannerApplication.getInstance().getString(R.string.guides_video)))
        mList.add(HelpModel(R.drawable.ic_email,R.color.grey_light,EnumAction.SEND_US_AN_EMAIL,QRScannerApplication.getInstance().getString(R.string.send_us_an_email)))
        emit(mList)
    }

    companion object {
        private val TAG = ChangeFileColorViewModel::class.java.simpleName
    }
}