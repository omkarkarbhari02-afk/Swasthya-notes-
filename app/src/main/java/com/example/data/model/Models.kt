package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    OWNER,
    STUDENT
}

enum class ContentType {
    YEAR,
    SUBJECT,
    FOLDER,
    SUBFOLDER,
    CHAPTER,
    PDF_NOTE,
    QUESTION_BANK,
    PYQ,
    SHORT_NOTE,
    IMAGE
}

enum class RequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
    NEED_SCREENSHOT
}

enum class AccessType {
    LIFETIME,
    TEMPORARY,
    FREE_GRANT
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: UserRole = UserRole.STUDENT,
    val bamsYear: String = "BAMS 1st Year",
    val isSuspended: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "content_items")
data class ContentItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val type: ContentType,
    val parentId: String? = null, // parent folder or subject or year id
    val yearTag: String, // "BAMS 1st Year", "BAMS 2nd Year", "BAMS 3rd Year"
    val subjectTag: String = "",
    val price: Int = 0, // 0 = free, >0 = paid in INR
    val isLockedByDefault: Boolean = false,
    val pageCount: Int = 1,
    val fileSizeBytes: Long = 1024 * 1024,
    val contentSummary: String = "",
    val sampleTextContent: String = "",
    val orderIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "access_permissions")
data class AccessPermissionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val contentItemId: String,
    val accessType: AccessType = AccessType.LIFETIME,
    val grantedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long? = null, // null for lifetime
    val isActive: Boolean = true
)

@Entity(tableName = "access_requests")
data class AccessRequestEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val contentItemId: String,
    val contentTitle: String,
    val contentYear: String,
    val pricePaid: Int,
    val utrNumber: String,
    val screenshotUri: String = "",
    val message: String = "",
    val status: RequestStatus = RequestStatus.PENDING,
    val requestedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null,
    val expiryDaysGranted: Int? = null, // e.g. 365 or null for lifetime
    val ownerNote: String = ""
)

@Entity(tableName = "offline_downloads")
data class OfflineDownloadEntity(
    @PrimaryKey val contentItemId: String,
    val userId: String,
    val title: String,
    val yearTag: String,
    val subjectTag: String,
    val encryptedFilePath: String,
    val downloadedAt: Long = System.currentTimeMillis(),
    val fileSizeBytes: Long = 0L
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val targetAudience: String = "ALL", // "ALL", "BAMS 1st Year", "BAMS 2nd Year", "BAMS 3rd Year", "APPROVED"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "app_settings")
data class AppSettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
