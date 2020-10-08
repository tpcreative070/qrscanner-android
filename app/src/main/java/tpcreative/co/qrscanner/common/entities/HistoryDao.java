package tpcreative.co.qrscanner.common.entities;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface HistoryDao {
    @Insert
    void insert(HistoryEntity... history);

    @Update
    void update(HistoryEntity... history);

    @Delete
    void delete(HistoryEntity... history);

    @Query("DELETE FROM history")
    public void deleteAllItems();

    @Query("Select * FROM history ORDER BY updatedDateTime DESC")
    List<HistoryEntity> loadAll();

    @Query("Select * FROM history ORDER BY id DESC LIMIT 10")
    List<HistoryEntity> loadLatestItems();

    @Query("Select * FROM history WHERE contentUnique = :contentUnique")
    HistoryEntity loadItem(String contentUnique);

}
