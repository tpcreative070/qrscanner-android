package tpcreative.co.qrscanner.common.entities

import androidx.room.*

@Dao
interface HistoryDao {
    @Insert
    open fun insert(vararg history: HistoryEntity?)
    @Update
    open fun update(vararg history: HistoryEntity?)
    @Delete
    open fun delete(vararg history: HistoryEntity?)
    @Query("DELETE FROM history")
    open fun deleteAllItems()
    @Query("DELETE FROM history where uuId =:uuId")
    open fun deleteSpecific(uuId: String?)
    @Query("Select * FROM history ORDER BY updatedDateTime DESC")
    open fun loadAll(): MutableList<HistoryEntity?>?
    @Query("Select * FROM history where isSynced =:isSynced ORDER BY updatedDateTime DESC")
    open fun loadAll(isSynced: Boolean): MutableList<HistoryEntity?>?
    @Query("Select * FROM history WHERE contentUnique = :contentUnique")
    open fun loadItem(contentUnique: String?): HistoryEntity?
}