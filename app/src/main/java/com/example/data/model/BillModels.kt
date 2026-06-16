package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerName: String,
    val customerPhone: String,
    val siteAddress: String,
    val date: String,
    val advanceAmount: Double,
    val totalAmount: Double,
    val remainingBalance: Double,
    val roomsJson: String // Serialized List<BillRoom>
)

data class BillRoom(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val items: List<PointItem> = emptyList()
)

data class PointItem(
    val id: String = UUID.randomUUID().toString(),
    val type: String,
    val quantity: Int,
    val rate: Double,
    val total: Double = quantity * rate
)

data class PredefinedPointType(
    val type: String,
    val defaultRate: Double,
    val category: String
)
