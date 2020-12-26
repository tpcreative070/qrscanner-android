package tpcreative.co.qrscanner.viewmodel
import androidx.lifecycle.liveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.SaveModel
import tpcreative.co.qrscanner.model.TypeCategories
import java.util.ArrayList
import java.util.HashMap

class SaveViewModel  : BaseViewModel<TypeCategories>(){
    val TAG = this::class.java.name
    var mListCategories: MutableList<TypeCategories> = mutableListOf()
    var mList: MutableList<SaveModel> = mutableListOf()
    var mLatestValue: SaveModel?
    private var i = 0

    fun getLatestList(mData: MutableList<SaveModel>): MutableList<SaveModel> {
        val mList: MutableList<SaveModel> = mutableListOf()
        if (mData.size > 0) {
            mLatestValue = mData[0]
            for (index in mData) {
                if (index.createType == mLatestValue?.createType) {
                    index.typeCategories = TypeCategories(0, mLatestValue?.createType)
                    mList.add(index)
                }
            }
        }
        mList.sortWith { o1, o2 -> if (Utils.getMilliseconds(o1?.updatedDateTime) < Utils.getMilliseconds(o2?.updatedDateTime)) 0 else 1 }
        return mList
    }

    fun getUniqueList(): MutableMap<String?, SaveModel?> {
        val saver = SQLiteHelper.getSaveList()
        Utils.Log(TAG, "Save list " + Gson().toJson(saver))
        Utils.Log(TAG, Gson().toJson(saver))
        val hashMap: MutableMap<String?, SaveModel?> = HashMap()
        for (index in saver) {
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

    fun getListGroup(): MutableList<SaveModel> {
        getUniqueList()
        val list = SQLiteHelper.getSaveList()
        val mList: MutableList<SaveModel> = mutableListOf()
        val mLatestType = getLatestList(list)
        Utils.Log(TAG, "Latest list " + Gson().toJson(mLatestType))
        Utils.Log(TAG, "Latest object " + Gson().toJson(mLatestValue))
        for (index in mListCategories) {
            for (save in list) {
                if (index.getType() == save.createType && index.getType() != mLatestValue?.createType) {
                    save.typeCategories = index
                    mList.add(save)
                }
            }
        }
        /*Added latest list to ArrayList*/mLatestType.addAll(mList)
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

    init {
        mLatestValue = SaveModel()
    }

}

