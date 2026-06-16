package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.BillDatabase
import com.example.data.model.BillEntity
import com.example.data.model.BillRoom
import com.example.data.model.PointItem
import com.example.data.model.PredefinedPointType
import com.example.data.repository.BillRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class BillViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BillRepository

    init {
        val database = BillDatabase.getDatabase(application)
        repository = BillRepository(database.billDao())
    }

    // State for Search Query
    var searchQuery by mutableStateOf("")

    // Raw bills list from database
    val rawBills: StateFlow<List<BillEntity>> = repository.allBills
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered bills by search query (Client name or Phone)
    val filteredBills: StateFlow<List<BillEntity>> = combine(rawBills, MutableStateFlow("")) { bills, _ ->
        if (searchQuery.isBlank()) {
            bills
        } else {
            bills.filter {
                it.customerName.contains(searchQuery, ignoreCase = true) ||
                        it.customerPhone.contains(searchQuery, ignoreCase = true) ||
                        it.siteAddress.contains(searchQuery, ignoreCase = true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Draft billing state
    var customerName by mutableStateOf("")
    var customerPhone by mutableStateOf("")
    var siteAddress by mutableStateOf("")
    var advanceAmountStr by mutableStateOf("")
    var isGstEnabled by mutableStateOf(false)
    var roomsList by mutableStateOf<List<BillRoom>>(emptyList())
    var signatureBitmap by mutableStateOf<android.graphics.Bitmap?>(null)

    // Active screen navigation
    // "create_bill", "history", "rates_card", "bill_detail"
    var currentScreen by mutableStateOf("create_bill")
    var selectedBillForView by mutableStateOf<BillEntity?>(null)

    // General list of pre-defined electrical points
    val predefinedPoints = listOf(
        PredefinedPointType("Bulb / Holder Point", 80.0, "Lighting"),
        PredefinedPointType("Tube Light Point", 100.0, "Lighting"),
        PredefinedPointType("Ceiling Fan Point", 120.0, "Fan"),
        PredefinedPointType("Exhaust Fan Point", 150.0, "Fan"),
        PredefinedPointType("Simple Switchboard 5A", 150.0, "Switchboard"),
        PredefinedPointType("Power Socket 15A/25A", 250.0, "Power Outlet"),
        PredefinedPointType("AC Point Installation", 450.0, "Power Outlet"),
        PredefinedPointType("Geyser Point Installation", 350.0, "Power Outlet"),
        PredefinedPointType("Single Pole MCB", 200.0, "Protection"),
        PredefinedPointType("Double Pole MCB / ELCD", 400.0, "Protection"),
        PredefinedPointType("Distribution Box Fitting", 800.0, "Protection"),
        PredefinedPointType("Main Line Wiring (per meter)", 40.0, "Wiring")
    )

    // Add empty room
    fun addRoom(roomName: String) {
        if (roomName.isBlank()) return
        val newRoom = BillRoom(name = roomName, items = emptyList())
        roomsList = roomsList + newRoom
    }

    // Update a room's name
    fun updateRoomName(roomId: String, newName: String) {
        if (newName.isBlank()) return
        roomsList = roomsList.map { room ->
            if (room.id == roomId) {
                room.copy(name = newName)
            } else {
                room
            }
        }
    }

    // Delete a room
    fun deleteRoom(roomId: String) {
        roomsList = roomsList.filter { it.id != roomId }
    }

    // Add item to a room
    fun addItemToRoom(roomId: String, itemType: String, quantity: Int, rate: Double) {
        if (quantity <= 0 || rate < 0) return
        roomsList = roomsList.map { room ->
            if (room.id == roomId) {
                // Check if item of same type already exists
                val existingItemIndex = room.items.indexOfFirst { it.type.equals(itemType, ignoreCase = true) }
                val updatedItems = room.items.toMutableList()
                if (existingItemIndex != -1) {
                    val existingItem = updatedItems[existingItemIndex]
                    updatedItems[existingItemIndex] = existingItem.copy(
                        quantity = existingItem.quantity + quantity,
                        total = (existingItem.quantity + quantity) * rate
                    )
                } else {
                    updatedItems.add(
                        PointItem(
                            type = itemType,
                            quantity = quantity,
                            rate = rate,
                            total = quantity * rate
                        )
                    )
                }
                room.copy(items = updatedItems)
            } else {
                room
            }
        }
    }

    // Update individual item in room
    fun updateItemInRoom(roomId: String, itemId: String, itemType: String, quantity: Int, rate: Double) {
        if (quantity < 0) return
        roomsList = roomsList.map { room ->
            if (room.id == roomId) {
                val updatedItems = room.items.map { item ->
                    if (item.id == itemId) {
                        PointItem(
                            id = itemId,
                            type = itemType,
                            quantity = quantity,
                            rate = rate,
                            total = quantity * rate
                        )
                    } else {
                        item
                    }
                }.filter { it.quantity > 0 } // if qty is 0, it deletes the item
                room.copy(items = updatedItems)
            } else {
                room
            }
        }
    }

    // Delete item from room
    fun deleteItemFromRoom(roomId: String, itemId: String) {
        roomsList = roomsList.map { room ->
            if (room.id == roomId) {
                val updatedItems = room.items.filter { it.id != itemId }
                room.copy(items = updatedItems)
            } else {
                room
            }
        }
    }

    // Save active bill draft to repository
    fun saveBill(context: android.content.Context, onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (customerName.isBlank()) {
            onError("कृपया ग्राहकाचे नाव प्रविष्ट करा (Please enter client name)")
            return
        }

        val totalPointsVal = calculateDraftTotal()
        val advanceVal = advanceAmountStr.toDoubleOrNull() ?: 0.0
        val remainingVal = totalPointsVal - advanceVal

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val currentDate = sdf.format(Date())

        val roomsConverter = com.example.data.local.RoomsConverter()
        val roomsJsonStr = roomsConverter.toRoomsString(roomsList)

        val billEntity = BillEntity(
            customerName = customerName,
            customerPhone = customerPhone,
            siteAddress = siteAddress,
            date = currentDate,
            advanceAmount = advanceVal,
            totalAmount = totalPointsVal,
            remainingBalance = remainingVal,
            roomsJson = roomsJsonStr
        )

        viewModelScope.launch {
            try {
                val insertedId = repository.insertBill(billEntity)
                
                // Save signature if signatureBitmap is not null
                signatureBitmap?.let { bmp ->
                    try {
                        val sigFile = java.io.File(context.filesDir, "signature_${insertedId}.png")
                        val fos = java.io.FileOutputStream(sigFile)
                        bmp.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, fos)
                        fos.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                resetDraft()
                onSuccess()
            } catch (e: Exception) {
                onError("बिल सेव्ह करताना एरर आली: ${e.message}")
            }
        }
    }

    // Delete bill
    fun deleteSavedBill(bill: BillEntity) {
        viewModelScope.launch {
            repository.deleteBill(bill)
        }
    }

    // Full reset of drafted bill input form
    fun resetDraft() {
        customerName = ""
        customerPhone = ""
        siteAddress = ""
        advanceAmountStr = ""
        isGstEnabled = false
        roomsList = emptyList()
        signatureBitmap = null
    }

    // Calculations of current draft
    fun calculateDraftSubtotal(): Double {
        return roomsList.flatMap { it.items }.sumOf { it.total }
    }

    fun calculateDraftGst(): Double {
        return if (isGstEnabled) {
            calculateDraftSubtotal() * 0.18
        } else {
            0.0
        }
    }

    fun calculateDraftTotal(): Double {
        return calculateDraftSubtotal() + calculateDraftGst()
    }

    fun calculateTotalPoints(): Int {
        return roomsList.flatMap { it.items }.sumOf { it.quantity }
    }

    // Helper functions to convert JSON of saved bills to classes for drawing
    fun getRoomsFromBill(bill: BillEntity): List<BillRoom> {
        return com.example.data.local.RoomsConverter().fromRoomsString(bill.roomsJson)
    }
}
