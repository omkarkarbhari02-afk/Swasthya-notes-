package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.data.model.*

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = runCatching { UserRole.valueOf(value) }.getOrDefault(UserRole.STUDENT)

    @TypeConverter
    fun fromContentType(type: ContentType): String = type.name

    @TypeConverter
    fun toContentType(value: String): ContentType = runCatching { ContentType.valueOf(value) }.getOrDefault(ContentType.PDF_NOTE)

    @TypeConverter
    fun fromRequestStatus(status: RequestStatus): String = status.name

    @TypeConverter
    fun toRequestStatus(value: String): RequestStatus = runCatching { RequestStatus.valueOf(value) }.getOrDefault(RequestStatus.PENDING)

    @TypeConverter
    fun fromAccessType(type: AccessType): String = type.name

    @TypeConverter
    fun toAccessType(value: String): AccessType = runCatching { AccessType.valueOf(value) }.getOrDefault(AccessType.LIFETIME)
}

@Database(
    entities = [
        UserEntity::class,
        ContentItemEntity::class,
        AccessPermissionEntity::class,
        AccessRequestEntity::class,
        OfflineDownloadEntity::class,
        NotificationEntity::class,
        AppSettingEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SwasthyaDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun contentDao(): ContentDao
    abstract fun accessDao(): AccessDao
    abstract fun accessRequestDao(): AccessRequestDao
    abstract fun offlineDownloadDao(): OfflineDownloadDao
    abstract fun notificationDao(): NotificationDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: SwasthyaDatabase? = null

        fun getDatabase(context: Context): SwasthyaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SwasthyaDatabase::class.java,
                    "swasthya_notes.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
