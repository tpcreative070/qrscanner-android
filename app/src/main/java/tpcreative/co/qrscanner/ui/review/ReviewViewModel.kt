package tpcreative.co.qrscanner.ui.review
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.lifecycle.liveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.CreateModel
import tpcreative.co.qrscanner.model.ItemNavigation
import tpcreative.co.qrscanner.viewmodel.BaseViewModel

class ReviewViewModel : BaseViewModel<ItemNavigation>() {
    val TAG = this::class.java.name
    var create: CreateModel = CreateModel()
    fun getIntent(activity: Activity?) = liveData(Dispatchers.Main)  {
        val bundle: Bundle? = activity?.intent?.extras
        val action = activity?.intent?.action
        if (action != Intent.ACTION_SEND){
            Utils.Log(TAG,"type $bundle")
            val data = if (Build.VERSION.SDK_INT >= 33) {
                activity?.intent?.getParcelableExtra(QRScannerApplication.getInstance().getString(R.string.key_data), CreateModel::class.java)
            } else {
                bundle?.get(QRScannerApplication.getInstance().getString(R.string.key_data)) as CreateModel
            }
            if (data != null) {
                create = data
                Utils.Log(TAG,Gson().toJson(create))
                emit(true)
            }
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
}