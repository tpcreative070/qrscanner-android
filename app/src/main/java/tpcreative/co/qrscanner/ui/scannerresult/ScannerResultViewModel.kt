package tpcreative.co.qrscanner.ui.scannerresult
import android.app.Activity
import androidx.lifecycle.liveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.HistorySingleton
import tpcreative.co.qrscanner.common.SaveSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.findImageName
import tpcreative.co.qrscanner.common.extension.serializable
import tpcreative.co.qrscanner.common.extension.toJson
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import tpcreative.co.qrscanner.viewmodel.BaseViewModel
import java.util.HashMap

class ScannerResultViewModel : BaseViewModel<ItemNavigation>() {
    val TAG = ScannerResultViewModel::class.java
    override val dataList: MutableList<ItemNavigation>
        get() = super.dataList
    var result: GeneralModel?
    var hashCopy: HashMap<Any?, String?>? = HashMap()
    var hashCopyResult: HashMap<Any?, String?>? = HashMap()
    var isFavorite : Boolean = false
    var takeNoted : String = ""
    protected var stringBuilderClipboard: StringBuilder? = StringBuilder()
    val mListNavigation : MutableList<ItemNavigation> = mutableListOf()

    fun getIntent(activity: Activity?,myCallback: (done:Boolean) -> Unit) {
        val data = activity?.intent?.serializable(QRScannerApplication.getInstance().getString(R.string.key_data),GeneralModel::class.java)
        result = data
        isFavorite = data?.favorite ?: false
        takeNoted = result?.noted.toString()
        Utils.Log(TAG,Gson().toJson(result))
        Utils.Log(TAG,"Restore data 2")
        myCallback.invoke(true)
    }

    fun doShowAds(callback : (Boolean)->Unit) {
        if (QRScannerApplication.getInstance().isLiveAds()) {
            callback.invoke(true)
        } else {
            callback.invoke(false)
        }
    }

    fun reloadData(){
        when (result?.fragmentType) {
            EnumFragmentType.HISTORY -> {
                HistorySingleton.getInstance()?.reloadData()
            }
            EnumFragmentType.SAVER -> {
              SaveSingleton.getInstance()?.reloadData()
            }
            else -> Utils.Log("ScannerResultViewModel", "Nothing")
        }
    }


    fun doUpdatedFavoriteItem()  = liveData(Dispatchers.IO){
        when (result?.fragmentType) {
            EnumFragmentType.HISTORY -> {
                val mItem = SQLiteHelper.getHistoryItemById(result?.id)
                mItem?.favorite = !isFavorite
                mItem?.hiddenDatetime = Utils.getCurrentDateTimeSort()
                SQLiteHelper.onUpdate(mItem,true)
                isFavorite = mItem?.favorite ?: false
            }
            EnumFragmentType.SAVER -> {
                val mItem = SQLiteHelper.getSaveItemById(result?.id)
                mItem?.favorite = !isFavorite
                mItem?.hiddenDatetime = Utils.getCurrentDateTimeSort()
                SQLiteHelper.onUpdate(mItem,true)
                isFavorite = mItem?.favorite ?: false
            }
            else -> Utils.Log("ScannerResultViewModel", "Nothing")
        }
        emit(true)
    }

    fun doUpdatedTakeNoteItem()  = liveData(Dispatchers.IO){
        when (result?.fragmentType) {
            EnumFragmentType.HISTORY -> {
                val mItem = SQLiteHelper.getHistoryItemById(result?.id)
                mItem?.noted = takeNoted
                mItem?.hiddenDatetime = Utils.getCurrentDateTimeSort()
                SQLiteHelper.onUpdate(mItem,true)
            }
            EnumFragmentType.SAVER -> {
                val mItem = SQLiteHelper.getSaveItemById(result?.id)
                mItem?.noted = takeNoted
                mItem?.hiddenDatetime = Utils.getCurrentDateTimeSort()
                SQLiteHelper.onUpdate(mItem,true)
            }
            else -> Utils.Log("ScannerResultViewModel", "Nothing")
        }
        emit(true)
    }

    fun doDelete() = liveData(Dispatchers.IO){
        when (result?.fragmentType) {
            EnumFragmentType.HISTORY -> {
                val mItem = SQLiteHelper.getHistoryItemById(result?.id)
                SQLiteHelper.onDelete(mItem)
                onDeleteChangeDesign(result)
            }
            EnumFragmentType.SAVER -> {
                val mItem = SQLiteHelper.getSaveItemById(result?.id)
                SQLiteHelper.onDelete(mItem)
                onDeleteChangeDesign(result)
            }
            else -> Utils.Log("ScannerResultViewModel", "Nothing")
        }
        emit(true)
    }

    private fun onDeleteChangeDesign(data : GeneralModel?){
        data?.uuId?.findImageName()?.delete()
        val mData = SQLiteHelper.getDesignQR(data?.uuId)
        Utils.Log(TAG,"Data change design requesting delete ${mData?.toJson()}")
        SQLiteHelper.onDelete(mData)
    }

    fun getResult(value: HashMap<Any?, String?>?): String {
        stringBuilderClipboard = StringBuilder()
        if (value != null && value.size > 0) {
            var i = 1
            for ((_, value1) in value) {
                if (i == value.size) {
                    stringBuilderClipboard?.append(value1)
                } else {
                    stringBuilderClipboard?.append(value1)
                    stringBuilderClipboard?.append("\n")
                }
                i += 1
            }
            return stringBuilderClipboard.toString()
        }
        return ""
    }

    fun isBarCode(type : String?) : Boolean{
        if (type == "QR_CODE"){
            return false
        }
        return !type.isNullOrEmpty()
    }

    fun updateId(uuId : String?){
        if (result?.id == 0){
            val mModel = SQLiteHelper.getItemByUUIdOfHistory(uuId)
            result?.fragmentType = EnumFragmentType.HISTORY
            result?.noted = ""
            takeNoted = ""
            result?.id = mModel?.id ?: 0
            result?.uuId = mModel?.uuId ?:Utils.getUUId()
        }
    }

    init {
        result = GeneralModel()
    }
}