package tpcreative.co.qrscanner.ui.save
import androidx.lifecycle.liveData
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.common.extension.findImageName
import tpcreative.co.qrscanner.common.extension.toJson
import tpcreative.co.qrscanner.helper.SQLiteHelper
import tpcreative.co.qrscanner.model.EnumImage
import tpcreative.co.qrscanner.model.SaveModel
import tpcreative.co.qrscanner.model.TypeCategories
import tpcreative.co.qrscanner.viewmodel.BaseViewModel
import java.util.HashMap

class SaveViewModel  : BaseViewModel<TypeCategories>(){
    val TAG = this::class.java.name
    var mListCategories: MutableList<TypeCategories> = mutableListOf()
    var mList: MutableList<SaveModel> = mutableListOf()
    var mLatestValue: SaveModel?
    var mFavoriteCategory = TypeCategories(-1,"Favorite")
    private var i = 0

    private fun getFavorite() : MutableList<SaveModel> {
        val mList: MutableList<SaveModel> = mutableListOf()
        val mFavoriteList = SQLiteHelper.getSaveFavoriteItemList(true)
        for (index in mFavoriteList) {
            index.typeCategories = mFavoriteCategory
            mList.add(index)
        }
        mList.sortBy { it.getUpdatedTimeToMilliseconds()}
        mList.reverse()
        return  mList
    }
    private fun getLatestList(mData: MutableList<SaveModel>): MutableList<SaveModel> {
        val mList: MutableList<SaveModel> = mutableListOf()
        if (mData.size > 0) {
            mLatestValue = mData[0]
            for (index in mData) {
                if (index.createType == mLatestValue?.createType && index.favorite == false) {
                    index.typeCategories = TypeCategories(0, mLatestValue?.createType)
                    mList.add(index)
                }
            }
        }
        mList.sortBy { it.getUpdatedTimeToMilliseconds()}
        mList.reverse()
        return  mList
    }

    private fun getUniqueList(){
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
    }

    fun getListGroup(): MutableList<SaveModel> {
        getUniqueList()
        val list = SQLiteHelper.getSaveList()
        val mLocalList: MutableList<SaveModel> = mutableListOf()
        val mLatestType = getLatestList(list)
        val mFavoriteList = getFavorite()
        Utils.Log(TAG, "Favorite list " + Gson().toJson(mFavoriteList))
        Utils.Log(TAG, "Latest list " + Gson().toJson(mLatestType))
        Utils.Log(TAG, "Latest object " + Gson().toJson(mLatestValue))
        for (index in mListCategories) {
            for (save in list) {
                if (index.getType() == save.createType && index.getType() != mLatestValue?.createType && save.favorite == false) {
                    save.typeCategories = index
                    mLocalList.add(save)
                }
            }
        }
        /*Added latest list to ArrayList*/
        mFavoriteList.addAll(mLatestType)
        mFavoriteList.addAll(mLocalList)
        this.mList.clear()
        this.mList.addAll(mFavoriteList)
        Utils.Log(TAG, "Latest object final ${Gson().toJson(this.mList)}")
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
        val list = mList
        list.let {
            for (index in it) {
                if (index.isDeleted()) {
                    if (index.isChecked()) {
                        SQLiteHelper.onDelete(index)
                        onDeleteChangeDesign(index)
                    }
                }
            }
            getListGroup()
            emit(true)
        }
    }

    fun deleteEntireItem() = liveData(Dispatchers.IO) {
        val list = SQLiteHelper.getSaveList()
        if (list.size>0){
            for (index in list) {
                Utils.Log(TAG,"deleteItem 0")
                SQLiteHelper.onDelete(index)
                onDeleteChangeDesign(index)
            }
        }
        getListGroup()
        emit(true)
    }

    private fun onDeleteChangeDesign(data : SaveModel?){
        data?.uuId?.findImageName(EnumImage.QR_CODE)?.delete()
        data?.uuId?.findImageName(EnumImage.LOGO)?.delete()
        val mData = SQLiteHelper.getDesignQR(data?.uuId)
        Utils.Log(TAG,"Data change design requesting delete ${mData?.toJson()}")
        SQLiteHelper.onDelete(mData)
    }

    fun count() : Int{
        val list = SQLiteHelper.getSaveList()
        return list.size
    }

    init {
        mLatestValue = SaveModel()
    }

}

