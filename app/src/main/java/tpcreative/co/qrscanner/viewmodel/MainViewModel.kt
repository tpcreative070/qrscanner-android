package tpcreative.co.qrscanner.viewmodel
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.EmptyModel

class MainViewModel : BaseViewModel<EmptyModel>() {
    var isPremium = false
    fun doShowAds()  = liveData(Dispatchers.Main){
        if (!Utils.isPremium() && QRScannerApplication.getInstance().isLiveAds()) {
            emit(true)
        } else {
            emit(false)
        }
    }
}