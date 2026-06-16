package com.example.data.local

import android.content.Context
import androidx.room.*
import com.example.data.model.BillEntity

@Database(entities = [BillEntity::class], version = 1, exportSchema = false)
@TypeConverters(RoomsConverter::class)
abstract class BillDatabase : RoomDatabase() {
    abstract fun billDao(): BillDao

    companion object {
        @Volatile
        private var INSTANCE: BillDatabase? = null

        fun getDatabase(context: Context): BillDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BillDatabase::class.java,
                    "electrical_billing_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
