package tpcreative.co.qrscanner.helper
import android.content.*
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.entities.InstanceGenerator
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.HistoryEntityModel
import tpcreative.co.qrscanner.model.HistoryModel
import tpcreative.co.qrscanner.model.SaveEntityModel
import tpcreative.co.qrscanner.model.SaveModel
import java.util.*

object SQLiteHelper {
    private val TAG = SQLiteHelper::class.java.simpleName

    fun getInstance(): InstanceGenerator? {
        return InstanceGenerator.getInstance(QRScannerApplication.getInstance())
    }

    fun onInsert(cTalkManager: HistoryModel?) {
        try {
            if (cTalkManager == null) {
                return
            }
            val mData = HistoryEntityModel(cTalkManager)
            if (Utils.isSkipDuplicates()) {
                val mItem = getItemByHistory(mData.contentUnique)
                if (mItem != null) {
                    Utils.Log(TAG, "Already existed on history item...!!!")
                    return
                }
            }
            Utils.setLastTimeSynced(Utils.getCurrentDateTimeSort())
            Utils.setRequestSync(true)
            getInstance()?.onInsert(mData)
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
    }

    fun getHistoryList(): MutableList<HistoryModel> {
        try {
            val mValue = getInstance()?.getHistoryList()
            val mList: MutableList<HistoryModel> = ArrayList()
            if (mValue != null) {
                for (index in mValue) {
                    mList.add(HistoryModel(index))
                }
            }
            return mList
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return ArrayList()
    }

    fun getHistoryList(isSync: Boolean): MutableList<HistoryModel> {
        try {
            val mValue = getInstance()?.getHistoryList(isSync)
            val mList: MutableList<HistoryModel> = ArrayList()
            if (mValue != null) {
                for (index in mValue) {
                    mList.add(HistoryModel(index))
                }
            }
            return mList
        } catch (e: Exception) {
            Utils.Log(TAG, "${e.message}")
        }
        return ArrayList()
    }

    fun onDelete(entity: HistoryModel?): Boolean? {
        try {
            val mData = HistoryEntityModel(entity)
            Utils.setHistoryDeletedMap(mData)
            Utils.setLastTimeSynced(Utils.getCurrentDateTimeSort())
            Utils.setRequestSync(true)
            return getInstance()?.onDelete(mData)
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return false
    }

    fun onDeleteHistorySpecific(uuId: String?): Boolean? {
        try {
            return getInstance()?.onDeleteHistorySpecific(uuId)
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return false
    }

    fun onInsert(cTalkManager: SaveModel?) {
        try {
            if (cTalkManager == null) {
                return
            }
            val mData = SaveEntityModel(cTalkManager)
            if (Utils.isSkipDuplicates()) {
                val mItem = getItemBySave(mData.contentUnique)
                if (mItem != null) {
                    Utils.Log(TAG, "Already existed on save item...!!!")
                    return
                }
            }
            Utils.setLastTimeSynced(Utils.getCurrentDateTimeSort())
            Utils.setRequestSync(true)
            getInstance()?.onInsert(mData)
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
    }

    fun onUpdate(cTalkManager: SaveModel?, isRequestChangingLifecycle: Boolean) {
        try {
            if (cTalkManager == null) {
                return
            }
            val mData = SaveEntityModel(cTalkManager)
            if (isRequestChangingLifecycle) {
                Utils.setLastTimeSynced(Utils.getCurrentDateTimeSort())
                Utils.setRequestSync(true)
            }
            getInstance()?.onUpdate(mData)
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
    }

    fun onUpdate(cTalkManager: HistoryModel?, isRequestChangingLifecycle: Boolean) {
        try {
            if (cTalkManager == null) {
                return
            }
            val mData = HistoryEntityModel(cTalkManager)
            if (isRequestChangingLifecycle) {
                Utils.setLastTimeSynced(Utils.getCurrentDateTimeSort())
                Utils.setRequestSync(true)
            }
            getInstance()?.onUpdate(mData)
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
    }

    fun getSaveList(): MutableList<SaveModel> {
        try {
            val mValue = getInstance()?.getSaveList()
            val mList: MutableList<SaveModel> = ArrayList()
            if (mValue != null) {
                for (index in mValue) {
                    mList.add(SaveModel(index))
                }
            }
            return mList
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return ArrayList()
    }

    fun getSaveList(isSync: Boolean): MutableList<SaveModel> {
        try {
            val mValue = getInstance()?.getSaveList(isSync)
            val mList: MutableList<SaveModel> = ArrayList()
            if (mValue != null) {
                for (index in mValue) {
                    mList.add(SaveModel(index))
                }
            }
            return mList
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return ArrayList()
    }

    fun onDelete(entity: SaveModel?): Boolean? {
        try {
            val mData = SaveEntityModel(entity)
            Utils.setSaveDeletedMap(mData)
            Utils.setLastTimeSynced(Utils.getCurrentDateTimeSort())
            Utils.setRequestSync(true)
            return getInstance()?.onDelete(mData)
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return false
    }

    fun onDeleteSaveSpecific(uuId: String?): Boolean? {
        try {
            return getInstance()?.onDeleteSaveSpecific(uuId)
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return false
    }

    fun getItemByHistory(contentUnique: String?): HistoryModel? {
        try {
            val mResult = getInstance()?.getItemByHistory(contentUnique)
            if (mResult != null) {
                return HistoryModel(mResult)
            }
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return null
    }

    fun getItemBySave(contentUnique: String?): SaveModel? {
        try {
            val mResult = getInstance()?.getItemBySave(contentUnique)
            if (mResult != null) {
                return SaveModel(mResult)
            }
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return null
    }

    fun getHistoryItemById(id: Int?): HistoryModel? {
        try {
            val mResult = getInstance()?.getItemByHistory(id)
            if (mResult != null) {
                return HistoryModel(mResult)
            }
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return null
    }

    fun getSaveItemById(id: Int?): SaveModel? {
        try {
            val mResult = getInstance()?.getItemBySave(id)
            if (mResult != null) {
                return SaveModel(mResult)
            }
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return null
    }

    fun hasSaveItem(isFavorite : Boolean) : Boolean {
        try {
            val mResult = getInstance()?.getLoadAllSaveFavoriteItems(isFavorite)
            if (mResult != null && mResult.size>0) {
                return true
            }
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return false
    }

    fun hasHistoryItem(isFavorite : Boolean) : Boolean {
        try {
            val mResult = getInstance()?.getLoadAllHistoryFavoriteItems(isFavorite)
            if (mResult != null && mResult.size>0) {
                return true
            }
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return false
    }

    fun getSaveFavoriteItemList(isFavorite : Boolean) :  MutableList<SaveModel> {
        try {
            val mValue = getInstance()?.getLoadAllSaveFavoriteItems(isFavorite)
            val mList: MutableList<SaveModel> = ArrayList()
            if (mValue != null) {
                for (index in mValue) {
                    mList.add(SaveModel(index))
                }
            }
            return mList
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return ArrayList()
    }

    fun getHistoryFavoriteItemList(isFavorite : Boolean) :  MutableList<HistoryModel> {
        try {
            val mValue = getInstance()?.getLoadAllHistoryFavoriteItems(isFavorite)
            val mList: MutableList<HistoryModel> = ArrayList()
            if (mValue != null) {
                for (index in mValue) {
                    mList.add(HistoryModel(index))
                }
            }
            return mList
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return ArrayList()
    }



    fun CleanUpData() {
        getInstance()?.historyDao()?.deleteAllItems()
        getInstance()?.saveDao()?.deleteAllItems()
    }
}