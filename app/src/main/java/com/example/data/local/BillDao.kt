package com.example.data.local

import androidx.room.*
import com.example.data.model.BillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY id DESC")
    fun getAllBills(): Flow<List<BillEntity>>

    @Query("SELECT * FROM bills WHERE id = :id")
    fun getBillById(id: Int): Flow<BillEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillEntity): Long

    @Update
    suspend fun updateBill(bill: BillEntity)

    @Delete
    suspend fun deleteBill(bill: BillEntity)

    @Query("DELETE FROM bills WHERE id = :id")
    suspend fun deleteBillById(id: Int)
}
