package tpcreative.co.qrscanner.common.entities
import androidx.room.*

@Dao
interface HistoryDao {
    @Insert
    fun insert(vararg history: HistoryEntity?)
    @Update
    fun update(vararg history: HistoryEntity?)
    @Delete
    fun delete(vararg history: HistoryEntity?)
    @Query("DELETE FROM history")
    fun deleteAllItems()
    @Query("DELETE FROM history where uuId =:uuId")
    fun deleteSpecific(uuId: String?)
    @Query("Select * FROM history ORDER BY updatedDateTime DESC")
    fun loadAll(): MutableList<HistoryEntity>?
    @Query("Select * FROM history where isSynced =:isSynced ORDER BY updatedDateTime DESC")
    fun loadAll(isSynced: Boolean): MutableList<HistoryEntity>?
    @Query("Select * FROM history WHERE contentUnique = :contentUnique")
    fun loadItem(contentUnique: String?): HistoryEntity?
    @Query("Select * FROM history WHERE id = :id")
    fun loadItem(id: Int?): HistoryEntity?
    @Query("Select * FROM history WHERE favorite = :isFavorite")
    fun loadAllItem(isFavorite : Boolean): MutableList<HistoryEntity>?
    @Query("Select * FROM history WHERE uuId = :uuId")
    fun loadUUId(uuId: String?): HistoryEntity?
}