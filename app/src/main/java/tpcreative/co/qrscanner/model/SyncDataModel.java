package tpcreative.co.qrscanner.model;
import com.google.gson.Gson;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.helper.SQLiteHelper;

public class SyncDataModel implements Serializable {
    public List<SaveModel> saveList;
    public List<HistoryModel> historyList;
    public String updatedDateTime;

    public SyncDataModel(boolean resultUpload){
        this.saveList = new ArrayList<>();
        for (SaveModel index : SQLiteHelper.getSaveList()){
            this.saveList.add(new SaveModel(index,true));
        }
        this.historyList = new ArrayList<>();
        for (HistoryModel index : SQLiteHelper.getHistoryList()){
            this.historyList.add(new HistoryModel(index,true));
        }
        this.updatedDateTime = Utils.getCurrentDateTimeSort();
        Utils.setLastTimeSynced(this.updatedDateTime);
        Utils.setRequestSync(false);
    }
    public String toJson(){
        return new Gson().toJson(this);
    }
}
