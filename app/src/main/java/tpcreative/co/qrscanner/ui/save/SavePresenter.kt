package tpcreative.co.qrscanner.ui.save

import androidx.fragment.app.Fragment
import com.google.gson.Gson
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.presenter.Presenter
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.SaveModel
import tpcreative.co.qrscanner.model.TypeCategories
import java.util.*

class SavePresenter : Presenter<SaveView?>() {
    private val TAG = SavePresenter::class.java.simpleName
    protected var mListCategories: MutableList<TypeCategories?>?
    var mList: MutableList<SaveModel?>?
    protected var mLatestValue: SaveModel?
    protected var mFragment: MutableList<Fragment?>?
    private var i = 0
    fun getLatestList(mData: MutableList<SaveModel?>?): MutableList<SaveModel?>? {
        val mList: MutableList<SaveModel?> = ArrayList()
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

    fun getUniqueList(): MutableMap<String?, SaveModel?>? {
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

    fun getListGroup(): MutableList<SaveModel?>? {
        getUniqueList()
        val view = view()
        val list = SQLiteHelper.getSaveList()
        val mList: MutableList<SaveModel?> = ArrayList()
        val mLatestType = getLatestList(list)
        Utils.Log(TAG, "Latest list " + Gson().toJson(mLatestType))
        Utils.Log(TAG, "Latest object " + Gson().toJson(mLatestValue))
        for (index in mListCategories) {
            for (save in list) {
                if (index.type == save.createType && index.type != mLatestValue.createType) {
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

    init {
        mListCategories = ArrayList()
        mList = ArrayList()
        mFragment = ArrayList()
        mLatestValue = SaveModel()
    }
}