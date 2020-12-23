package tpcreative.co.qrscanner.model

import com.google.gson.Gson
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.helper.SQLiteHelper
import java.io.Serializable
import java.util.*

class SyncDataModel(resultUpload: Boolean) : Serializable {
    var saveList: MutableList<SaveModel?>?
    var historyList: MutableList<HistoryModel?>?
    var updatedDateTime: String?
    fun toJson(): String? {
        return Gson().toJson(this)
    }

    init {
        saveList = ArrayList()
        for (index in SQLiteHelper.getSaveList()) {
            saveList.add(SaveModel(index, true))
        }
        historyList = ArrayList()
        for (index in SQLiteHelper.getHistoryList()) {
            historyList.add(HistoryModel(index, true))
        }
        updatedDateTime = Utils.getCurrentDateTimeSort()
        Utils.setLastTimeSynced(updatedDateTime)
        Utils.setRequestSync(false)
    }
}