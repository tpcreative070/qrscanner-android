package tpcreative.co.qrscanner.common.entities;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface SaveDao {
    @Insert
    void insert(SaveEntity... saves);

    @Update
    void update(SaveEntity... saves);

    @Delete
    void delete(SaveEntity... saves);

    @Query("DELETE FROM save")
    public void deleteAllItems();

    @Query("DELETE FROM save where uuId =:uuId")
    public void deleteSpecific(String uuId);

    @Query("Select * FROM save ORDER BY updatedDateTime DESC")
    List<SaveEntity> loadAll();

    @Query("Select * FROM save where isSynced =:isSynced ORDER BY updatedDateTime DESC")
    List<SaveEntity> loadAll(boolean isSynced);

    @Query("Select * FROM save WHERE contentUnique = :contentUnique")
    SaveEntity loadItem(String contentUnique);
}
