package tpcreative.co.qrscanner.model
import com.google.gson.Gson
import tpcreative.co.qrscanner.common.Utils
import tpcreative.co.qrscanner.helper.SQLiteHelper
import java.io.Serializable

class SyncDataModel(resultUpload: Boolean) : Serializable {
    var saveList: MutableList<SaveModel> = mutableListOf()
    var historyList: MutableList<HistoryModel> = mutableListOf()
    var updatedDateTime: String?
    fun toJson(): String? {
        return Gson().toJson(this)
    }

    init {
        for (index in SQLiteHelper.getSaveList()) {
            saveList.add(SaveModel(index, true))
        }
        for (index in SQLiteHelper.getHistoryList()) {
            historyList.add(HistoryModel(index, true))
        }
        updatedDateTime = Utils.getCurrentDateTimeSort()
        Utils.setLastTimeSynced(updatedDateTime)
        Utils.setRequestSync(false)
    }
}