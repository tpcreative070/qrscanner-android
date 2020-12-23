package tpcreative.co.qrscanner.common.entities

import androidx.room.*

@Dao
interface SaveDao {
    @Insert
    open fun insert(vararg saves: SaveEntity?)
    @Update
    open fun update(vararg saves: SaveEntity?)
    @Delete
    open fun delete(vararg saves: SaveEntity?)
    @Query("DELETE FROM save")
    open fun deleteAllItems()
    @Query("DELETE FROM save where uuId =:uuId")
    open fun deleteSpecific(uuId: String?)
    @Query("Select * FROM save ORDER BY updatedDateTime DESC")
    open fun loadAll(): MutableList<SaveEntity?>?
    @Query("Select * FROM save where isSynced =:isSynced ORDER BY updatedDateTime DESC")
    open fun loadAll(isSynced: Boolean): MutableList<SaveEntity?>?
    @Query("Select * FROM save WHERE contentUnique = :contentUnique")
    open fun loadItem(contentUnique: String?): SaveEntity?
}