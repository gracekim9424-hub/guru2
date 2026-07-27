package com.damyeoom.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.damyeoom.app.dao.ChecklistDao
import com.damyeoom.app.dao.FriendDao
import com.damyeoom.app.dao.PlaceDao
import com.damyeoom.app.dao.TravelRecordDao
import com.damyeoom.app.dao.UserDao
import com.damyeoom.app.entity.ChecklistItem
import com.damyeoom.app.entity.FriendEntity
import com.damyeoom.app.entity.PlaceEntity
import com.damyeoom.app.entity.TravelRecord
import com.damyeoom.app.entity.User

@Database(
    entities = [
        TravelRecord::class,
        ChecklistItem::class,
        PlaceEntity::class,
        User::class,
        FriendEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun travelRecordDao(): TravelRecordDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun placeDao(): PlaceDao
    abstract fun userDao(): UserDao
    abstract fun friendDao(): FriendDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `friends` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `userId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `colorArgb` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS `index_friends_userId_name`
                    ON `friends` (`userId`, `name`)
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travel_archive.db"
                )
                    .addMigrations(MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}