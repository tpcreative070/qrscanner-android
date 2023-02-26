package tpcreative.co.qrscanner.ui.filecolor
import androidx.lifecycle.liveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.EmptyModel
import tpcreative.co.qrscanner.model.Theme
import tpcreative.co.qrscanner.viewmodel.BaseViewModel

class ChangeFileColorViewModel : BaseViewModel<EmptyModel>() {
    var mList: MutableList<Theme> = mutableListOf()
    var mTheme: Theme? = null
    fun getData(myCallback: (list: MutableList<Theme>) -> Unit){
        mList = Theme.getInstance()!!.getList()
        mTheme = Theme.getInstance()?.getThemeInfo()
        if (mTheme != null) {
            for (i in mList.indices) {
                mList[i].isCheck = mTheme?.getId() == mList[i].getId()
            }
        }
        Utils.Log(TAG, "Value :" + Gson().toJson(mList))
        myCallback.invoke(mList)
    }

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