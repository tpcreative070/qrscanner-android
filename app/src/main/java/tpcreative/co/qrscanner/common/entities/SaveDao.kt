package tpcreative.co.qrscanner.common.entities
import androidx.room.*

@Dao
interface SaveDao {
    @Insert
    fun insert(vararg saves: SaveEntity?)
    @Update
    fun update(vararg saves: SaveEntity?)
    @Delete
    fun delete(vararg saves: SaveEntity?)
    @Query("DELETE FROM save")
    fun deleteAllItems()
    @Query("DELETE FROM save where uuId =:uuId")
    fun deleteSpecific(uuId: String?)
    @Query("Select * FROM save ORDER BY updatedDateTime DESC")
    fun loadAll(): MutableList<SaveEntity>?
    @Query("Select * FROM save where isSynced =:isSynced ORDER BY updatedDateTime DESC")
    fun loadAll(isSynced: Boolean): MutableList<SaveEntity>?
    @Query("Select * FROM save WHERE contentUnique = :contentUnique")
    fun loadItem(contentUnique: String?): SaveEntity?
    @Query("Select * FROM save WHERE id = :id")
    fun loadItem(id: Int?): SaveEntity?
    @Query("Select * FROM save WHERE favorite = :isFavorite")
    fun loadAllItem(isFavorite : Boolean): MutableList<SaveEntity>?
}