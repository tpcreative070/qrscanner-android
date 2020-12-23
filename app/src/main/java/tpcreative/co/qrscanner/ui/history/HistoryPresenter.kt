package tpcreative.co.qrscanner.ui.history

import com.google.gson.Gson
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.presenter.Presenter
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.HistoryModel
import tpcreative.co.qrscanner.model.TypeCategories
import java.util.*

class HistoryPresenter : Presenter<HistoryView?>() {
    protected var mListCategories: MutableList<TypeCategories?>?
    var mList: MutableList<HistoryModel?>?
    protected var mLatestValue: HistoryModel?
    private var i = 0
    fun getLatestList(mData: MutableList<HistoryModel?>?): MutableList<HistoryModel?>? {
        val mList: MutableList<HistoryModel?> = ArrayList()
        if (mData.size > 0) {
            mLatestValue = mData.get(0)
            for (index in mData) {
                if (index.createType == mLatestValue.createType) {
                    index.typeCategories = TypeCategories(0, mLatestValue.createType)
                    mList.add(index)
                }
            }
        }
        Collections.sort(mList) { o1, o2 -> if (Utils.getMilliseconds(o1.updatedDateTime) < Utils.getMilliseconds(o2.updatedDateTime)) 0 else 1 }
        return mList
    }

    fun getUniqueList(): MutableMap<String?, HistoryModel?>? {
        val view = view()
        val histories = SQLiteHelper.getHistoryList()
        Utils.Log(TAG, "History list " + Gson().toJson(histories))
        if (histories == null) {
            return HashMap()
        }
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

    fun getListGroup(): MutableList<HistoryModel?>? {
        getUniqueList()
        val view = view()
        val list = SQLiteHelper.getHistoryList()
        val mList: MutableList<HistoryModel?> = ArrayList()
        val mLatestType = getLatestList(list)
        Utils.Log(TAG, "Latest list " + Gson().toJson(mLatestType))
        Utils.Log(TAG, "Latest object " + Gson().toJson(mLatestValue))
        for (index in mListCategories) {
            for (history in list) {
                if (index.type == history.createType && index.type != mLatestValue.createType) {
                    history.typeCategories = index
                    mList.add(history)
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

    fun deleteItem() {
        val view = view()
        val list = mList
        for (index in list) {
            if (index.isDeleted()) {
                if (index.isChecked()) {
                    SQLiteHelper.onDelete(index)
                }
            }
        }
        getListGroup()
        view.updateView()
    }

    companion object {
        private val TAG = HistoryPresenter::class.java.simpleName
    }

    init {
        mListCategories = ArrayList()
        mList = ArrayList()
        mLatestValue = HistoryModel()
    }
}