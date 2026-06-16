package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.BillRoom
import com.example.data.model.PointItem
import org.json.JSONArray
import org.json.JSONObject

class RoomsConverter {
    @TypeConverter
    fun fromRoomsString(value: String?): List<BillRoom> {
        if (value.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<BillRoom>()
        try {
            val jsonArray = JSONArray(value)
            for (i in 0 until jsonArray.length()) {
                val roomObj = jsonArray.getJSONObject(i)
                val roomId = roomObj.optString("id", "")
                val roomName = roomObj.optString("name", "")
                
                val itemsList = mutableListOf<PointItem>()
                val itemsArray = roomObj.optJSONArray("items")
                if (itemsArray != null) {
                    for (j in 0 until itemsArray.length()) {
                        val itemObj = itemsArray.getJSONObject(j)
                        itemsList.add(
                            PointItem(
                                id = itemObj.optString("id", ""),
                                type = itemObj.optString("type", ""),
                                quantity = itemObj.optInt("quantity", 0),
                                rate = itemObj.optDouble("rate", 0.0)
                            )
                        )
                    }
                }
                list.add(BillRoom(id = roomId, name = roomName, items = itemsList))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    @TypeConverter
    fun toRoomsString(rooms: List<BillRoom>?): String {
        if (rooms == null) return "[]"
        val jsonArray = JSONArray()
        try {
            for (room in rooms) {
                val roomObj = JSONObject()
                roomObj.put("id", room.id)
                roomObj.put("name", room.name)
                
                val itemsArray = JSONArray()
                for (item in room.items) {
                    val itemObj = JSONObject()
                    itemObj.put("id", item.id)
                    itemObj.put("type", item.type)
                    itemObj.put("quantity", item.quantity)
                    itemObj.put("rate", item.rate)
                    itemsArray.put(itemObj)
                }
                roomObj.put("items", itemsArray)
                jsonArray.put(roomObj)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return jsonArray.toString()
    }
}
