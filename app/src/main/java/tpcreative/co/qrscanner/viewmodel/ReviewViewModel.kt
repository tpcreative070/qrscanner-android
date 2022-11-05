package tpcreative.co.qrscanner.viewmodel
import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.liveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.BuildConfig
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.CreateModel
import tpcreative.co.qrscanner.model.ItemNavigation

class ReviewViewModel : BaseViewModel<ItemNavigation>() {
    val TAG = this::class.java.name
    var create: CreateModel = CreateModel()
    var mListItemNavigation: MutableList<ItemNavigation> = mutableListOf()
    fun getIntent(activity: Activity?) = liveData(Dispatchers.Main)  {
        try {
            val bundle: Bundle? = activity?.intent?.extras
            val result : CreateModel  = bundle?.get(QRScannerApplication.getInstance().getString(R.string.key_create_intent)) as CreateModel
            create = result
            emit(true)
            if (BuildConfig.DEBUG) {
                Utils.Log(TAG, Gson().toJson(create))
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
}