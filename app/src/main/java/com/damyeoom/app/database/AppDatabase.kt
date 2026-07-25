package com.damyeoom.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.damyeoom.app.dao.ChecklistDao
import com.damyeoom.app.dao.PlaceDao
import com.damyeoom.app.dao.TravelRecordDao
import com.damyeoom.app.entity.ChecklistItem
import com.damyeoom.app.entity.PlaceEntity
import com.damyeoom.app.entity.TravelRecord
//import com.example.app.data.dao.ChecklistDao
//import com.example.app.data.dao.PlaceD ao
//import com.example.app.data.dao.TravelRecordDao
//import com.example.app.entity.ChecklistItem
//import com.example.app.entity.PlaceEntity
//import com.example.app.entity.TravelRecord

@Database(
    entities = [
        TravelRecord::class,
        ChecklistItem::class,
        PlaceEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun travelRecordDao(): TravelRecordDao

    abstract fun checklistDao(): ChecklistDao

    abstract fun placeDao(): PlaceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travel_archive.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}