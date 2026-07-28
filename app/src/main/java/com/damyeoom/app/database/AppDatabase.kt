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

// 앱에서 사용하는 Room 데이터베이스 설정
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

    // 각 데이터에 접근할 DAO 연결
    abstract fun travelRecordDao(): TravelRecordDao
    abstract fun checklistDao(): ChecklistDao
    abstract fun placeDao(): PlaceDao
    abstract fun userDao(): UserDao
    abstract fun friendDao(): FriendDao

    companion object {

        // 데이터베이스 인스턴스를 하나만 유지
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // DB 버전 5에서 6으로 변경 시 friends 테이블 생성
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {

                // 친구 정보를 저장할 테이블 생성
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

                // 사용자별 친구 이름의 중복을 방지하는 인덱스 생성
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS `index_friends_userId_name`
                    ON `friends` (`userId`, `name`)
                    """.trimIndent()
                )
            }
        }

        // 앱의 Room 데이터베이스 생성 및 반환
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travel_archive.db"
                )
                    // DB 버전 변경 규칙 적용
                    .addMigrations(MIGRATION_5_6)

                    // 마이그레이션 실패 시 기존 DB를 삭제하고 재생성
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}