package tpcreative.co.qrscanner.helper
import android.content.*
import tpcreative.co.qrscanner.common.*
import tpcreative.co.qrscanner.common.entities.DesignQREntity
import tpcreative.co.qrscanner.common.entities.InstanceGenerator
import tpcreative.co.qrscanner.common.services.QRScannerApplication
import tpcreative.co.qrscanner.model.*
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
            Utils.setRequestHistoryReload(true)
            Utils.onSetCountRating(Utils.onGetCountRating() + 1)
            ResponseSingleton.getInstance()?.onResponseScannerCompleted()
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
            Utils.setRequestHistoryReload(true)
            return getInstance()?.onDelete(mData)
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
            Utils.setRequestSaverReload(true)
            Utils.onSetCountRating(Utils.onGetCountRating() + 1)
            ResponseSingleton.getInstance()?.onResponseCreateCompleted()
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
            Utils.setRequestSaverReload(true)
            Utils.onSetCountRating(Utils.onGetCountRating() + 1)
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
            Utils.setRequestHistoryReload(true)
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
            Utils.setRequestSaverReload(true)
            return getInstance()?.onDelete(mData)
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return false
    }

    private fun getItemByHistory(contentUnique: String?): HistoryModel? {
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

    fun getItemByUUIdOfHistory(uuId: String?): HistoryModel? {
        try {
            val mResult = getInstance()?.getItemByUUId(uuId)
            if (mResult != null) {
                return HistoryModel(mResult)
            }
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return null
    }

    private fun getItemBySave(contentUnique: String?): SaveModel? {
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

    fun onDelete(entity: DesignQRModel?): Boolean? {
        try {
            val mData = DesignQREntityModel(entity)
            return getInstance()?.onDelete(mData)
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return false
    }

    fun onInsert(data: DesignQRModel?) {
        try {
            if (data == null) {
                return
            }
            val mData = DesignQREntityModel(data)
            getInstance()?.onInsert(mData)
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
    }

    fun getDesignQR(uuIdQR: String?): DesignQRModel? {
        try {
            val mResult = getInstance()?.getItemByUUIdQR(uuIdQR)
            if (mResult != null) {
                return DesignQRModel(mResult)
            }
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return null
    }

    fun loadList(): MutableList<DesignQRModel>? {
        try {
            val  mList = mutableListOf<DesignQRModel>()
            val mResult = getInstance()?.getLoadAll()
            if (mResult != null) {
                mResult.forEach {
                    mList.add(DesignQRModel(it))
                }
                return mList
            }
        } catch (e: Exception) {
            Utils.Log(TAG,"${e.message}")
        }
        return null
    }
}