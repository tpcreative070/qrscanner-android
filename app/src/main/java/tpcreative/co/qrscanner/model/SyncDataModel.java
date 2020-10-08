package tpcreative.co.qrscanner.model;
import com.google.gson.Gson;

import java.io.Serializable;
import java.util.List;

import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.helper.SQLiteHelper;
import tpcreative.co.qrscanner.helper.TimeHelper;

public class SyncDataModel implements Serializable {
    public List<SaveModel> saveList;
    public List<HistoryModel> historyList;
    public String updatedDateTime;


    public SyncDataModel(){
        this.saveList = SQLiteHelper.getListSave();
        this.historyList = SQLiteHelper.getList();
        this.updatedDateTime = Utils.getCurrentDateTimeSort();
    }

    public String toJson(){
        return new Gson().toJson(this);
    }
}
