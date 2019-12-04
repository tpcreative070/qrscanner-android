package tpcreative.co.qrscanner.model.room;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;
import tpcreative.co.qrscanner.model.Save;


@Dao
public interface SaveDao {
    @Insert
    void insert(Save... saves);

    @Update
    void update(Save... saves);

    @Delete
    void delete(Save... saves);

    @Query("Select * FROM save ORDER BY updatedDateTime DESC")
    List<Save> loadAll();
}
