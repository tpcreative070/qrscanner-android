package tpcreative.co.qrscanner.model.room;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;
import java.util.List;
import java.util.UUID;
import tpcreative.co.qrscanner.model.History;

@Database(entities = {History.class}, version = 1, exportSchema = false)
public abstract class HistoryGenerator  extends RoomDatabase {

    @Ignore
    private static HistoryGenerator instance;

    @Ignore
    public abstract HistoryDao historyDao();


    @Ignore
    public static final String TAG = HistoryGenerator.class.getSimpleName();

    public static HistoryGenerator getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    HistoryGenerator.class,
                    "db-qr-scanner")
                    .allowMainThreadQueries()
                    .build();
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


}



