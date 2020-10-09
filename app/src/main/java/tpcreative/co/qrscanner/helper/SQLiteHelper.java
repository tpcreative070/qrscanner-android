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
            final HistoryEntityModel mData = new HistoryEntityModel(cTalkManager);
            if (Utils.isSkipDuplicates()){
                final HistoryModel mItem = getItemByHistory(mData.contentUnique);
                if (mItem!=null){
                    Utils.Log(TAG,"Already existed on history item...!!!");
                    return;
                }
            }
            Utils.setLastTimeSynced(Utils.getCurrentDateTimeSort());
            InstanceGenerator.getInstance(getContext()).onInsert(mData);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public static final List<HistoryModel> getHistoryList(){
        try{
            final List<HistoryEntityModel> mValue =  getInstance().getHistoryList();
            List<HistoryModel> mList = new ArrayList<>();
            for ( HistoryEntityModel index : mValue){
                mList.add(new HistoryModel(index));
            }
            return mList;
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return new ArrayList<>();
    }

    public static final List<HistoryModel> getHistoryList(boolean isSync){
        try{
            final List<HistoryEntityModel> mValue =  getInstance().getHistoryList(isSync);
            List<HistoryModel> mList = new ArrayList<>();
            for ( HistoryEntityModel index : mValue){
                mList.add(new HistoryModel(index));
            }
            return mList;
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return new ArrayList<>();
    }

    public static final boolean onDelete(HistoryModel entity){
        try{
            final HistoryEntityModel mData = new HistoryEntityModel(entity);
            Utils.setHistoryDeletedMap(mData);
            Utils.setLastTimeSynced(Utils.getCurrentDateTimeSort());
            return getInstance().onDelete(mData);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return false;
    }

    public static final boolean onDeleteHistorySpecific(String uuId){
        try{
            return getInstance().onDeleteHistorySpecific(uuId);
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
            final SaveEntityModel mData = new SaveEntityModel(cTalkManager);
            if (Utils.isSkipDuplicates()){
                final SaveModel mItem = getItemBySave(mData.contentUnique);
                if (mItem!=null){
                    Utils.Log(TAG,"Already existed on save item...!!!");
                    return;
                }
            }
            Utils.setLastTimeSynced(Utils.getCurrentDateTimeSort());
            getInstance().onInsert(mData);
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
            final SaveEntityModel mData = new SaveEntityModel(cTalkManager);
            Utils.setLastTimeSynced(Utils.getCurrentDateTimeSort());
            getInstance().onUpdate(mData);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public static void onUpdate(HistoryModel cTalkManager){
        try {
            if (cTalkManager==null){
                return;
            }
            final HistoryEntityModel mData = new HistoryEntityModel(cTalkManager);
            Utils.setLastTimeSynced(Utils.getCurrentDateTimeSort());
            getInstance().onUpdate(mData);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public static final List<SaveModel> getSaveList(){
        try{
            final List<SaveEntityModel> mValue = getInstance().getSaveList();
            List<SaveModel> mList = new ArrayList<>();
            for ( SaveEntityModel index : mValue){
                mList.add(new SaveModel(index));
            }
            return mList;
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return new ArrayList<>();
    }

    public static final List<SaveModel> getSaveList(boolean isSync){
        try{
            final List<SaveEntityModel> mValue = getInstance().getSaveList(isSync);
            List<SaveModel> mList = new ArrayList<>();
            for ( SaveEntityModel index : mValue){
                mList.add(new SaveModel(index));
            }
            return mList;
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return new ArrayList<>();
    }

    public static final boolean onDelete(SaveModel entity){
        try{
            final SaveEntityModel mData = new SaveEntityModel(entity);
            Utils.setSaveDeletedMap(mData);
            Utils.setLastTimeSynced(Utils.getCurrentDateTimeSort());
            return getInstance().onDelete(mData);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return false;
    }

    public static final boolean onDeleteSaveSpecific(String uuId){
        try{
            return getInstance().onDeleteSaveSpecific(uuId);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return false;
    }

    public static final HistoryModel getItemByHistory(String contentUnique){
        try{
            final HistoryEntityModel mResult =  getInstance().getItemByHistory(contentUnique);
            if (mResult!=null){
                return new HistoryModel(mResult);
            }
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public static final SaveModel getItemBySave(String contentUnique){
        try{
            final SaveEntityModel mResult =  getInstance().getItemBySave(contentUnique);
            if (mResult!=null){
                return new SaveModel(mResult);
            }
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public static final void CleanUpData(){
        getInstance().historyDao().deleteAllItems();
        getInstance().saveDao().deleteAllItems();
    }
}
