package tpcreative.co.qrscanner.viewmodel
import androidx.lifecycle.liveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.common.Utils
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

    fun getLatestList(mData: MutableList<HistoryModel>): MutableList<HistoryModel> {
        val mList: MutableList<HistoryModel> = mutableListOf()
        if (mData.size > 0) {
            mLatestValue = mData[0]
            for (index in mData) {
                if (index.createType == mLatestValue.createType) {
                    index.typeCategories = TypeCategories(0, mLatestValue.createType)
                    mList.add(index)
                }
            }
        }
        mList.sortWith(Comparator { o1, o2 -> if (Utils.getMilliseconds(o1.updatedDateTime) < Utils.getMilliseconds(o2.updatedDateTime)) 0 else 1 })
        return mList
    }

    fun getUniqueList(): MutableMap<String?, HistoryModel?> {
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
        Utils.Log(TAG, "Latest list " + Gson().toJson(mLatestType))
        Utils.Log(TAG, "Latest object " + Gson().toJson(mLatestValue))
        for (index in mListCategories) {
            for (history in list) {
                if (index.getType() == history.createType && index.getType() != mLatestValue.createType) {
                    history.typeCategories = index
                    mList.add(history)
                }
            }
        }

        /*Added latest list to ArrayList*/
        mLatestType.addAll(mList)
        this.mList.clear()
        this.mList.addAll(mLatestType)
        return mLatestType
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

    fun deleteItem() = liveData(Dispatchers.Main) {
        val list = mList
        for (index in list) {
            if (index.isDeleted()) {
                if (index.isChecked()) {
                    SQLiteHelper.onDelete(index)
                }
            }
        }
        getListGroup()
        emit(true)
    }

}