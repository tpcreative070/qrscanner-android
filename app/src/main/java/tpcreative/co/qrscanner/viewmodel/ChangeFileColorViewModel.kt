package tpcreative.co.qrscanner.viewmodel
import androidx.lifecycle.liveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.model.EmptyModel
import tpcreative.co.qrscanner.model.Theme

class ChangeFileColorViewModel : BaseViewModel<EmptyModel>() {
    var mList: MutableList<Theme> = mutableListOf()
    var mTheme: Theme? = null
    fun getData()  = liveData(Dispatchers.Main){
        mList = Theme.getInstance()!!.getList()
        mTheme = Theme.getInstance()?.getThemeInfo()
        if (mTheme != null) {
            for (i in mList.indices) {
                mList[i].isCheck = mTheme?.getId() == mList[i].getId()
            }
        }
        Utils.Log(TAG, "Value :" + Gson().toJson(mList))
        //view.onSuccessful("Successful", EnumStatus.SHOW_DATA)
        emit(mList)
    }

    companion object {
        private val TAG = ChangeFileColorViewModel::class.java.simpleName
    }


}