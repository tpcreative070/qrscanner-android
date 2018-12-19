package tpcreative.co.qrscanner.model.room;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.util.Log;
import java.util.List;
import java.util.UUID;

import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.services.QRScannerApplication;
import tpcreative.co.qrscanner.model.History;
import tpcreative.co.qrscanner.model.Save;

@Database(entities = {History.class, Save.class}, version = 1, exportSchema = false)
public abstract class InstanceGenerator extends RoomDatabase {

    @Ignore
    private static InstanceGenerator instance;

    @Ignore
    public abstract HistoryDao historyDao();
    @Ignore
    public abstract SaveDao saveDao();


    @Ignore
    public static final String TAG = InstanceGenerator.class.getSimpleName();


//    static final Migration MIGRATION_1_2 = new Migration(1,2) {
//        @Override
//        public void migrate(SupportSQLiteDatabase database) {
//            database.execSQL("ALTER TABLE save"
//                    + " ADD COLUMN 'barcodeFormat' TEXT NOT NULL DEFAULT '' ");
//        }
//    };
//
//    static final Migration MIGRATION_2_3 = new Migration(2,3) {
//        @Override
//        public void migrate(SupportSQLiteDatabase database) {
//            database.execSQL("ALTER TABLE history"
//                    + " ADD COLUMN 'barcodeFormat' TEXT NOT NULL DEFAULT '' ");
//        }
//    };

    public static InstanceGenerator getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context,
                     InstanceGenerator.class,
                     context.getString(R.string.database_name))
                     .allowMainThreadQueries()
                     .build();

//            instance = Room.databaseBuilder(context,
//                     InstanceGenerator.class,context.getString(R.string.database_name))
//                    .addMigrations(MIGRATION_1_2,MIGRATION_2_3)
//                    .allowMainThreadQueries()
//                    .build();
        }
        return instance;
    }

    public String getUUId(){
        try {
            return UUID.randomUUID().toString();
        }
        catch (Exception e){
            return ""+System.currentTimeMillis();
        }
    }

    public synchronized void onInsert(History cTalkManager){
        try {
            if (cTalkManager==null){
                return;
            }
            instance.historyDao().insert(cTalkManager);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public final synchronized List<History> getList(){
        try{
            return instance.historyDao().loadAll();
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized List<History> getListLatest(){
        try{
            return instance.historyDao().loadLatestItems();
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized boolean onDelete(History entity){
        try{
            instance.historyDao().delete(entity);
            return true;
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return false;
    }


    public synchronized void onInsert(Save cTalkManager){
        try {
            if (cTalkManager==null){
                return;
            }
            instance.saveDao().insert(cTalkManager);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public synchronized void onUpdate(Save cTalkManager){
        try {
            if (cTalkManager==null){
                return;
            }
            instance.saveDao().update(cTalkManager);
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public final synchronized List<Save> getListSave(){
        try{
            return instance.saveDao().loadAll();
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final synchronized boolean onDelete(Save entity){
        try{
            instance.saveDao().delete(entity);
            return true;
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return false;
    }

}



