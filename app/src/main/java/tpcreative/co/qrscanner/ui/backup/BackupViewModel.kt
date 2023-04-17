package tpcreative.co.qrscanner.ui.backup

import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.EmptyModel
import tpcreative.co.qrscanner.model.EnumScreens
import tpcreative.co.qrscanner.ui.filecolor.ChangeFileColorViewModel
import tpcreative.co.qrscanner.viewmodel.BaseViewModel

class BackupViewModel : BaseViewModel<EmptyModel>() {
    fun doShowAds(callback : (Boolean)->Unit){
        if (QRScannerApplication.getInstance().isLiveAds()) {
            callback.invoke(true)
        } else {
            callback.invoke(false)
        }
    }

    companion object {
        private val TAG = ChangeFileColorViewModel::class.java.simpleName
    }
}