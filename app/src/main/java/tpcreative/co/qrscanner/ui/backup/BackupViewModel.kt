package tpcreative.co.qrscanner.ui.backup

import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.EmptyModel
import tpcreative.co.qrscanner.ui.filecolor.ChangeFileColorViewModel
import tpcreative.co.qrscanner.viewmodel.BaseViewModel

class BackupViewModel : BaseViewModel<EmptyModel>() {
    fun doShowAds() = liveData(Dispatchers.Main) {
        if (QRScannerApplication.getInstance().isLiveAds() && !Utils.isHiddenAds()) {
            emit(true)
        } else {
            emit(false)
        }
    }

    companion object {
        private val TAG = ChangeFileColorViewModel::class.java.simpleName
    }
}