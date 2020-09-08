package tpcreative.co.qrscanner.helper;
import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.common.entities.InstanceGenerator;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.model.HistoryEntityModel;
import tpcreative.co.qrscanner.model.HistoryModel;
import tpcreative.co.qrscanner.model.SaveEntityModel;
import tpcreative.co.qrscanner.model.SaveModel;

public class SQLiteHelper {
    private static final String TAG = SQLiteHelper.class.getSimpleName();

    public static Context getContext() {
        return QRScannerApplication.getInstance();
    }

    public static InstanceGenerator getInstance(){
        return InstanceGenerator.getInstance(getContext());
    }

    public static void onInsert(HistoryModel cTalkManager){
        try {
            if (cTalkManager==null){
                return;
            }
            InstanceGenerator.getInstance(getContext()).onInsert(new HistoryEntityModel(cTalkManager));
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public static final List<HistoryModel> getList(){
        try{
            final List<HistoryEntityModel> mValue =  getInstance().getList();
            List<HistoryModel> mList = new ArrayList<>();
            for ( HistoryEntityModel index : mValue){
                mList.add(new HistoryModel(index));
            }
            return mList;
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public static final  List<HistoryModel> getListLatest(){
        try{
            final List<HistoryEntityModel> mValue =  getInstance().getListLatest();
            List<HistoryModel> mList = new ArrayList<>();
            for ( HistoryEntityModel index : mValue){
                mList.add(new HistoryModel(index));
            }
            return mList;
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }


    public static final boolean onDelete(HistoryModel entity){
        try{
            return getInstance().onDelete(new HistoryEntityModel(entity));
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return false;
    }

    public static void onInsert(SaveModel cTalkManager){
        try {
            if (cTalkManager==null){
                return;
            }
            getInstance().onInsert(new SaveEntityModel(cTalkManager));
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public static void onUpdate(SaveModel cTalkManager){
        try {
            if (cTalkManager==null){
                return;
            }
            getInstance().onUpdate(new SaveEntityModel(cTalkManager));
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public static final List<SaveModel> getListSave(){
        try{
            final List<SaveEntityModel> mValue = getInstance().getListSave();
            List<SaveModel> mList = new ArrayList<>();
            for ( SaveEntityModel index : mValue){
                mList.add(new SaveModel(index));
            }
            return mList;
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public static final boolean onDelete(SaveModel entity){
        try{
            return getInstance().onDelete(new SaveEntityModel(entity));
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return false;
    }


}
