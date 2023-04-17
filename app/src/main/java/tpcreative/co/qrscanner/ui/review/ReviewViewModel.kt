package tpcreative.co.qrscanner.ui.review
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.liveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.findImageName
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.common.extension.toJson
import tpcreative.co.qrscanner.common.extension.toObject
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.viewmodel.BaseViewModel

class ReviewViewModel : BaseViewModel<ItemNavigation>() {
    val TAG = this::class.java.name
    var create: GeneralModel = GeneralModel()
    var isSharedIntent : Boolean = false
    fun getIntent(activity: Activity?,callback: (result: Boolean) -> Unit)  {
        val bundle: Bundle? = activity?.intent?.extras
        val action = activity?.intent?.action
        if (action != Intent.ACTION_SEND){
            Utils.Log(TAG,"type $bundle")
            val data  = activity?.intent?.serializable(QRScannerApplication.getInstance().getString(R.string.key_data),GeneralModel::class.java)
            if (data != null) {
                create = data
                Utils.Log(TAG,Gson().toJson(create))
                callback.invoke(true)
            }
        }
    }

    fun doShowAds() = liveData(Dispatchers.Main) {
        if (QRScannerApplication.getInstance().isLiveAds()) {
            emit(true)
        } else {
            emit(false)
        }
    }

    fun getTakeNote(id : Int?) : String? {
        val mItem = SQLiteHelper.getSaveItemById(id)
        return mItem?.noted
    }

    fun getFavorite(id : Int?) : Boolean? {
        val mItem = SQLiteHelper.getSaveItemById(id)
        return mItem?.favorite
    }

    fun updateId(uuId : String?){
        if (create.id == 0){
            val mModel = SQLiteHelper.getItemByUUIdOfHistory(uuId)
            create.fragmentType = EnumFragmentType.HISTORY
            create.noted = ""
            create.id = mModel?.id ?: 0
        }
    }

    fun onDeleteChangeDesign(data : GeneralModel?){
        data?.uuId?.findImageName()?.delete()
        val mData = SQLiteHelper.getDesignQR(create.uuId)
        Utils.Log(TAG,"Data change design requesting delete ${mData?.toJson()}")
        SQLiteHelper.onDelete(mData)
    }
}