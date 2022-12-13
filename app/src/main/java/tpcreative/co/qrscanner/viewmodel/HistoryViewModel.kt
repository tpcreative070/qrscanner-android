package tpcreative.co.qrscanner.viewmodel
import androidx.lifecycle.liveData
import com.google.api.client.json.Json
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.toJson
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.HistoryModel
import tpcreative.co.qrscanner.model.TypeCategories
import java.util.*

class HistoryViewModel : BaseViewModel<HistoryModel>() {
    val TAG = this::class.java.name
    var mListCategories: MutableList<TypeCategories> = mutableListOf()
    var mList: MutableList<HistoryModel> = mutableListOf()
    var mLatestValue: HistoryModel = HistoryModel()
    private var i = 0
    var mFavoriteCategory = TypeCategories(-1,"Favorite")

    private fun getFavorite() : MutableList<HistoryModel> {
        val mList: MutableList<HistoryModel> = mutableListOf()
        val mFavoriteList = SQLiteHelper.getHistoryFavoriteItemList(true)
        for (index in mFavoriteList) {
            index.typeCategories = mFavoriteCategory
            mList.add(index)
        }
        mList.sortBy { it.getUpdatedTimeToMilliseconds()}
        mList.reverse()
        return mList
    }

    private fun getLatestList(mData: MutableList<HistoryModel>): MutableList<HistoryModel> {
        val mList: MutableList<HistoryModel> = mutableListOf()
        if (mData.size > 0) {
            mLatestValue = mData[0]
            for (index in mData) {
                if (index.createType == mLatestValue.createType && index.favorite == false) {
                    index.typeCategories = TypeCategories(0, mLatestValue.createType)
                    mList.add(index)
                }
            }
        }
        mList.sortBy { it.getUpdatedTimeToMilliseconds()}
        mList.reverse()
        return mList
    }

    private fun getUniqueList(): MutableMap<String?, HistoryModel?> {
        val histories = SQLiteHelper.getHistoryList()
        Utils.Log(TAG, "History list " + Gson().toJson(histories))
        Utils.Log(TAG, Gson().toJson(histories))
        val hashMap: MutableMap<String?, HistoryModel?> = HashMap()
        for (index in histories) {
            hashMap[index.createType] = index
        }
        mListCategories.clear()
        i = 1
        for ((key) in hashMap) {
            mListCategories.add(TypeCategories(i, key))
            i += 1
        }
        return hashMap
    }

    fun getListGroup(): MutableList<HistoryModel> {
        getUniqueList()
        val list = SQLiteHelper.getHistoryList()
        val mList: MutableList<HistoryModel> = mutableListOf()
        val mLatestType = getLatestList(list)
        val mFavoriteList = getFavorite()
        Utils.Log(TAG, "Favorite list " + Gson().toJson(mFavoriteList))
        Utils.Log(TAG, "Latest list " + Gson().toJson(mLatestType))
        Utils.Log(TAG, "Latest object " + Gson().toJson(mLatestValue))
        for (index in mListCategories) {
            for (history in list) {
                if (index.getType() == history.createType && index.getType() != mLatestValue.createType && history.favorite == false) {
                    history.typeCategories = index
                    mList.add(history)
                }
            }
        }

        /*Added latest list to ArrayList*/
        mFavoriteList.addAll(mLatestType)
        mFavoriteList.addAll(mList)
        this.mList.clear()
        this.mList.addAll(mFavoriteList)
        return mFavoriteList
    }

    fun getCheckedCount(): Int {
        var count = 0
        for (index in mList) {
            if (index.isChecked()) {
                count += 1
            }
        }
        return count
    }

    fun deleteItem() = liveData(Dispatchers.IO) {
        mList.let {
            for (index in it) {
                Utils.Log(TAG,"deleteItem 0")
                if (index.isDeleted()) {
                    Utils.Log(TAG,"deleteItem 1")
                    if (index.isChecked()) {
                        Utils.Log(TAG,"deleteItem 2")
                        SQLiteHelper.onDelete(index)
                    }
                }
            }
            getListGroup()
            emit(true)
        }
    }

    fun isBarCode(type : String?) : Boolean{
        return !type.isNullOrEmpty()
    }

}