package tpcreative.co.qrscanner.common.entities;
import android.content.Context;
import android.util.Log;
import androidx.room.Database;
import androidx.room.Ignore;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tpcreative.co.qrscanner.R;
import tpcreative.co.qrscanner.common.Utils;
import tpcreative.co.qrscanner.helper.SQLiteHelper;
import tpcreative.co.qrscanner.model.HistoryEntityModel;
import tpcreative.co.qrscanner.model.HistoryModel;
import tpcreative.co.qrscanner.model.SaveEntityModel;

@Database(entities = {HistoryEntity.class, SaveEntity.class}, version = 4, exportSchema = false)
public abstract class InstanceGenerator extends RoomDatabase {

    @Ignore
    private static InstanceGenerator instance;

    @Ignore
    public abstract HistoryDao historyDao();
    @Ignore
    public abstract SaveDao saveDao();

    @Ignore
    public static final String TAG = InstanceGenerator.class.getSimpleName();

    static final Migration MIGRATION_1_2 = new Migration(1,2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE 'save' ADD COLUMN  'barcodeFormat' TEXT");
            database.execSQL("ALTER TABLE 'save' ADD COLUMN  'favorite' INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE 'save' ADD COLUMN  'updatedDateTime' TEXT");

            database.execSQL("ALTER TABLE 'history' ADD COLUMN  'barcodeFormat' TEXT");
            database.execSQL("ALTER TABLE 'history' ADD COLUMN  'favorite' INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE 'history' ADD COLUMN  'updatedDateTime' TEXT");
        }
    };

    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE 'save' ADD COLUMN  'contentUnique' TEXT");
            database.execSQL("ALTER TABLE 'history' ADD COLUMN  'contentUnique' TEXT");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE 'save' ADD COLUMN  'isSynced' INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE 'save' ADD COLUMN  'uuId' TEXT");

            database.execSQL("ALTER TABLE 'history' ADD COLUMN  'isSynced' INTEGER NOT NULL DEFAULT 0");
            database.execSQL("ALTER TABLE 'history' ADD COLUMN  'uuId' TEXT");
        }
    };

    public static InstanceGenerator getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context,
                     InstanceGenerator.class,context.getString(R.string.database_name))
                    .addMigrations(MIGRATION_1_2,MIGRATION_2_3,MIGRATION_3_4)
                    .allowMainThreadQueries()
                    .build();
        }
        return instance;
    }

    public void onInsert(HistoryEntityModel cTalkManager){
        try {
            if (cTalkManager==null){
                return;
            }
            instance.historyDao().insert(new HistoryEntity(cTalkManager));
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public final List<HistoryEntityModel> getList(){
        try{
            final List<HistoryEntity> mValue =  instance.historyDao().loadAll();
            List<HistoryEntityModel> mList = new ArrayList<>();
            for ( HistoryEntity index : mValue){
                final HistoryEntityModel item = new HistoryEntityModel(index);
                if (item.uuId==null){
                    item.uuId = Utils.getUUId();
                    SQLiteHelper.getInstance().onUpdate(item);
                    Utils.Log(TAG,"Request update........");
                }else {
                    Utils.Log(TAG,"UUID........"+item.uuId);
                }
                mList.add(item);
            }
            return mList;
        }
        catch (Exception e){
            Utils.Log(TAG,e.getMessage());
        }
        return null;
    }

    public final List<HistoryEntityModel> getListLatest(){
        try{
            final List<HistoryEntity> mValue =  instance.historyDao().loadLatestItems();
            List<HistoryEntityModel> mList = new ArrayList<>();
            for ( HistoryEntity index : mValue){
                mList.add(new HistoryEntityModel(index));
            }
            return mList;
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final boolean onDelete(HistoryEntityModel entity){
        try{
            instance.historyDao().delete(new HistoryEntity(entity));
            return true;
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return false;
    }


    public void onInsert(SaveEntityModel cTalkManager){
        try {
            if (cTalkManager==null){
                return;
            }
            instance.saveDao().insert(new SaveEntity(cTalkManager));
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public void onUpdate(SaveEntityModel cTalkManager){
        try {
            if (cTalkManager==null){
                return;
            }
            instance.saveDao().update(new SaveEntity(cTalkManager));
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public void onUpdate(HistoryEntityModel cTalkManager){
        try {
            if (cTalkManager==null){
                return;
            }
            instance.historyDao().update(new HistoryEntity(cTalkManager));
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
    }

    public final List<SaveEntityModel> getListSave(){
        try{
            final List<SaveEntity> mValue =  instance.saveDao().loadAll();
            List<SaveEntityModel> mList = new ArrayList<>();
            for ( SaveEntity index : mValue){
                final SaveEntityModel item = new SaveEntityModel(index);
                if (item.uuId==null){
                    item.uuId = Utils.getUUId();
                    SQLiteHelper.getInstance().onUpdate(item);
                }
                mList.add(item);
            }
            return mList;
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final HistoryEntityModel getItemByHistory(String contentUnique){
        try{
            final HistoryEntity mResult =  instance.historyDao().loadItem(contentUnique);
            if (mResult!=null){
                return new HistoryEntityModel(mResult);
            }
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final SaveEntityModel getItemBySave(String contentUnique){
        try{
            final SaveEntity mResult =  instance.saveDao().loadItem(contentUnique);
            if (mResult!=null){
                return new SaveEntityModel(mResult);
            }
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return null;
    }

    public final boolean onDelete(SaveEntityModel entity){
        try{
            instance.saveDao().delete(new SaveEntity(entity));
            return true;
        }
        catch (Exception e){
            Log.d(TAG,e.getMessage());
        }
        return false;
    }

}



