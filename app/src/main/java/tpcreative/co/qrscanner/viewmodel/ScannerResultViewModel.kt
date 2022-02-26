package tpcreative.co.qrscanner.viewmodel
import android.app.Activity
import android.os.Bundle
import androidx.lifecycle.liveData
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.R
import tpcreative.co.qrscanner.common.HistorySingleton
import tpcreative.co.qrscanner.common.SaveSingleton
import tpcreative.co.qrscanner.common.ScannerSingleton
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.*
import java.util.HashMap

class ScannerResultViewModel : BaseViewModel<ItemNavigation>() {

    override val dataList: MutableList<ItemNavigation>
        get() = super.dataList
    var result: Create?
    var hashClipboard: HashMap<Any?, String?> = HashMap()
    var hashClipboardResult: HashMap<Any?, String?>? = HashMap()
    var isFavorite : Boolean = false
    var takeNoted : String = ""
    protected var stringBuilderClipboard: StringBuilder? = StringBuilder()
    val mListNavigation : MutableList<ItemNavigation> = mutableListOf()

    fun getIntent(activity: Activity?)  = liveData(Dispatchers.Main){
        val bundle: Bundle? = activity?.intent?.extras
        val data = bundle?.get(QRScannerApplication.getInstance().getString(R.string.key_data)) as Create
        result = data
        isFavorite = data.favorite
        emit(true)
    }

    fun doShowAds() = liveData(Dispatchers.Main) {
        if (QRScannerApplication.getInstance().isLiveAds()) {
            emit(true)
        } else {
           emit(false)
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
                SQLiteHelper.onUpdate(mItem,true)
                isFavorite = mItem?.favorite ?: false
            }
            EnumFragmentType.SAVER -> {
                val mItem = SQLiteHelper.getSaveItemById(result?.id)
                mItem?.favorite = !isFavorite
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
                SQLiteHelper.onUpdate(mItem,true)
                takeNoted = ""
            }
            EnumFragmentType.SAVER -> {
                val mItem = SQLiteHelper.getSaveItemById(result?.id)
                mItem?.noted = takeNoted
                SQLiteHelper.onUpdate(mItem,true)
                takeNoted = ""
            }
            else -> Utils.Log("ScannerResultViewModel", "Nothing")
        }
        emit(true)
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

    init {
        result = Create()
    }
}