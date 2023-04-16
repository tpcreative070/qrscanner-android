package tpcreative.co.qrscanner.common.entities

import androidx.room.*

@Dao
interface DesignQRDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(vararg design: DesignQREntity?)
    @Update
    fun update(vararg design: DesignQREntity?)
    @Delete
    fun delete(vararg design: DesignQREntity?)
    @Query("DELETE FROM design_qr where uuIdQR =:uuIdQR")
    fun delete(uuIdQR: String?)
    @Query("Select * FROM design_qr WHERE uuIdQR = :uuIdQR")
    fun loadItem(uuIdQR: String?): DesignQREntity?
    @Query("Select * FROM design_qr ORDER BY updatedDateTime DESC")
    fun loadAll(): MutableList<DesignQREntity>?
}