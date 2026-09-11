package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)

    @Query("SELECT COUNT(*) FROM users")
    fun getUserCountFlow(): Flow<Int>
}

@Dao
interface ContentDao {
    @Query("SELECT * FROM content_items ORDER BY orderIndex ASC, title ASC")
    fun getAllContentFlow(): Flow<List<ContentItemEntity>>

    @Query("SELECT * FROM content_items WHERE parentId IS NULL AND type = 'YEAR' ORDER BY orderIndex ASC")
    fun getYearsFlow(): Flow<List<ContentItemEntity>>

    @Query("SELECT * FROM content_items WHERE parentId = :parentId ORDER BY orderIndex ASC, title ASC")
    fun getChildrenFlow(parentId: String): Flow<List<ContentItemEntity>>

    @Query("SELECT * FROM content_items WHERE yearTag = :yearTag ORDER BY orderIndex ASC, title ASC")
    fun getContentByYearFlow(yearTag: String): Flow<List<ContentItemEntity>>

    @Query("SELECT * FROM content_items WHERE id = :id LIMIT 1")
    suspend fun getContentById(id: String): ContentItemEntity?

    @Query("SELECT * FROM content_items WHERE title LIKE '%' || :query || '%' OR subjectTag LIKE '%' || :query || '%' OR yearTag LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchContentFlow(query: String): Flow<List<ContentItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ContentItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ContentItemEntity>)

    @Update
    suspend fun updateItem(item: ContentItemEntity)

    @Delete
    suspend fun deleteItem(item: ContentItemEntity)

    @Query("DELETE FROM content_items WHERE id = :id OR parentId = :id")
    suspend fun deleteItemAndChildren(id: String)

    @Query("SELECT COUNT(*) FROM content_items WHERE type IN ('PDF_NOTE', 'QUESTION_BANK', 'PYQ', 'SHORT_NOTE')")
    fun getTotalPdfsCountFlow(): Flow<Int>
}

@Dao
interface AccessDao {
    @Query("SELECT * FROM access_permissions WHERE userId = :userId AND isActive = 1")
    fun getUserPermissionsFlow(userId: String): Flow<List<AccessPermissionEntity>>

    @Query("SELECT * FROM access_permissions WHERE userId = :userId AND contentItemId = :contentItemId AND isActive = 1 LIMIT 1")
    suspend fun getPermission(userId: String, contentItemId: String): AccessPermissionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPermission(permission: AccessPermissionEntity)

    @Query("UPDATE access_permissions SET isActive = 0 WHERE userId = :userId AND contentItemId = :contentItemId")
    suspend fun revokePermission(userId: String, contentItemId: String)

    @Query("DELETE FROM access_permissions WHERE userId = :userId AND contentItemId = :contentItemId")
    suspend fun deletePermission(userId: String, contentItemId: String)

    @Query("SELECT * FROM access_permissions ORDER BY grantedAt DESC")
    fun getAllPermissionsFlow(): Flow<List<AccessPermissionEntity>>
}

@Dao
interface AccessRequestDao {
    @Query("SELECT * FROM access_requests ORDER BY requestedAt DESC")
    fun getAllRequestsFlow(): Flow<List<AccessRequestEntity>>

    @Query("SELECT * FROM access_requests WHERE userId = :userId ORDER BY requestedAt DESC")
    fun getRequestsByUserFlow(userId: String): Flow<List<AccessRequestEntity>>

    @Query("SELECT * FROM access_requests WHERE status = 'PENDING' ORDER BY requestedAt DESC")
    fun getPendingRequestsFlow(): Flow<List<AccessRequestEntity>>

    @Query("SELECT * FROM access_requests WHERE userId = :userId AND contentItemId = :contentItemId LIMIT 1")
    suspend fun getRequestForUserAndItem(userId: String, contentItemId: String): AccessRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRequest(request: AccessRequestEntity)

    @Update
    suspend fun updateRequest(request: AccessRequestEntity)

    @Query("SELECT COUNT(*) FROM access_requests WHERE status = 'PENDING'")
    fun getPendingCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM access_requests WHERE status = 'APPROVED'")
    fun getApprovedCountFlow(): Flow<Int>
}

@Dao
interface OfflineDownloadDao {
    @Query("SELECT * FROM offline_downloads WHERE userId = :userId ORDER BY downloadedAt DESC")
    fun getDownloadsByUserFlow(userId: String): Flow<List<OfflineDownloadEntity>>

    @Query("SELECT * FROM offline_downloads WHERE userId = :userId AND contentItemId = :contentItemId LIMIT 1")
    suspend fun getDownload(userId: String, contentItemId: String): OfflineDownloadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(download: OfflineDownloadEntity)

    @Query("DELETE FROM offline_downloads WHERE userId = :userId AND contentItemId = :contentItemId")
    suspend fun deleteDownload(userId: String, contentItemId: String)

    @Query("DELETE FROM offline_downloads WHERE userId = :userId")
    suspend fun deleteAllUserDownloads(userId: String)

    @Query("SELECT COUNT(*) FROM offline_downloads")
    fun getTotalDownloadsCountFlow(): Flow<Int>
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotificationsFlow(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteNotification(id: String)
}

@Dao
interface SettingsDao {
    @Query("SELECT value FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Query("SELECT * FROM app_settings")
    fun getAllSettingsFlow(): Flow<List<AppSettingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: AppSettingEntity)
}
