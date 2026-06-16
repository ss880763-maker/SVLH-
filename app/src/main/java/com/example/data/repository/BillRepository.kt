package com.example.data.repository

import com.example.data.local.BillDao
import com.example.data.model.BillEntity
import kotlinx.coroutines.flow.Flow

class BillRepository(private val billDao: BillDao) {
    val allBills: Flow<List<BillEntity>> = billDao.getAllBills()

    fun getBillById(id: Int): Flow<BillEntity?> = billDao.getBillById(id)

    suspend fun insertBill(bill: BillEntity): Long = billDao.insertBill(bill)

    suspend fun updateBill(bill: BillEntity) = billDao.updateBill(bill)

    suspend fun deleteBill(bill: BillEntity) = billDao.deleteBill(bill)

    suspend fun deleteBillById(id: Int) = billDao.deleteBillById(id)
}
