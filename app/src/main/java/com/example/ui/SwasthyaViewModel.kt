package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.SwasthyaRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SwasthyaViewModel(application: Application) : AndroidViewModel(application) {
    val repository = SwasthyaRepository(application)

    // Current User
    val currentUser = repository.currentUser

    // UI Dark mode preference
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode = _isDarkMode.asStateFlow()

    // Navigation state
    private val _currentScreen = MutableStateFlow<AppScreen>(AppScreen.Home)
    val currentScreen = _currentScreen.asStateFlow()

    // Content hierarchy navigation stack: e.g. [Year1, Padartha, Chapter1]
    private val _navPath = MutableStateFlow<List<ContentItemEntity>>(emptyList())
    val navPath = _navPath.asStateFlow()

    // Active item being viewed in Secure PDF Viewer
    private val _activeReaderItem = MutableStateFlow<ContentItemEntity?>(null)
    val activeReaderItem = _activeReaderItem.asStateFlow()

    // Active item for which access request dialog is open
    private val _accessRequestItem = MutableStateFlow<ContentItemEntity?>(null)
    val accessRequestItem = _accessRequestItem.asStateFlow()

    // Search query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // Selected tab in Owner Dashboard
    private val _ownerDashboardTab = MutableStateFlow(0)
    val ownerDashboardTab = _ownerDashboardTab.asStateFlow()

    // Reactive Content Flows
    val years = repository.getYearsFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Children of current path end
    val currentChildren: StateFlow<List<ContentItemEntity>> = _navPath
        .flatMapLatest { path ->
            val parentId = path.lastOrNull()?.id
            if (parentId == null) {
                repository.getYearsFlow()
            } else {
                repository.getChildrenFlow(parentId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search results
    val searchResults: StateFlow<List<ContentItemEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) flowOf(emptyList())
            else repository.searchContentFlow(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Access Requests (Owner)
    val allRequests = repository.getAllRequestsFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val pendingRequests = repository.getPendingRequestsFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Requests for currently logged in student
    val userRequests: StateFlow<List<AccessRequestEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getRequestsByUserFlow(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Offline Downloads for currently logged in student
    val offlineDownloads: StateFlow<List<OfflineDownloadEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getOfflineDownloadsFlow(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All registered users (for Owner)
    val allUsers = repository.getAllUsersFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Notifications
    val notifications = repository.getAllNotificationsFlow().stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    // Settings
    val appSettings: StateFlow<Map<String, String>> = repository.getAllSettingsFlow()
        .map { list -> list.associate { it.key to it.value } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Analytics counters
    val totalUsersCount = repository.getUserCountFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalPdfsCount = repository.getTotalPdfsCountFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val totalDownloadsCount = repository.getTotalDownloadsCountFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val pendingRequestsCount = repository.getPendingCountFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val approvedRequestsCount = repository.getApprovedCountFlow().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // User Permissions
    val userPermissions: StateFlow<List<AccessPermissionEntity>> = currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getUserPermissionsFlow(user.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.initDatabase()
        }
    }

    // Navigation Actions
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun openYear(yearItem: ContentItemEntity) {
        _navPath.value = listOf(yearItem)
        _currentScreen.value = AppScreen.ContentBrowser
    }

    fun drillDown(item: ContentItemEntity) {
        _navPath.value = _navPath.value + item
    }

    fun navigateUpHierarchy(): Boolean {
        val current = _navPath.value
        return if (current.isNotEmpty()) {
            _navPath.value = current.dropLast(1)
            true
        } else {
            false
        }
    }

    fun jumpToBreadcrumb(index: Int) {
        _navPath.value = _navPath.value.take(index + 1)
    }

    fun openSecureReader(item: ContentItemEntity) {
        _activeReaderItem.value = item
        _currentScreen.value = AppScreen.SecurePdfViewer
    }

    fun closeSecureReader() {
        _activeReaderItem.value = null
        _currentScreen.value = AppScreen.ContentBrowser
    }

    fun openAccessRequestDialog(item: ContentItemEntity) {
        _accessRequestItem.value = item
    }

    fun closeAccessRequestDialog() {
        _accessRequestItem.value = null
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setOwnerDashboardTab(tabIndex: Int) {
        _ownerDashboardTab.value = tabIndex
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // AUTH ACTIONS
    fun switchUserRole(role: UserRole) {
        viewModelScope.launch {
            if (role == UserRole.OWNER) {
                repository.switchActiveUser("owner_admin")
            } else {
                repository.switchActiveUser("student_demo")
            }
        }
    }

    fun switchUserById(userId: String) {
        viewModelScope.launch {
            repository.switchActiveUser(userId)
        }
    }

    fun login(email: String, phone: String, role: UserRole, bamsYear: String, name: String) {
        viewModelScope.launch {
            repository.loginOrRegister(name, email, phone, role, bamsYear)
            _currentScreen.value = AppScreen.Home
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _currentScreen.value = AppScreen.Auth
        }
    }

    fun updateUserProfile(user: UserEntity) {
        viewModelScope.launch {
            repository.updateUser(user)
        }
    }

    // PERMISSIONS & ACCESS
    suspend fun hasAccessToItem(item: ContentItemEntity): Boolean {
        val user = currentUser.value ?: return false
        return repository.hasAccess(user.id, item)
    }

    fun requestAccess(item: ContentItemEntity, utrNumber: String, screenshotUri: String, message: String, onComplete: () -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.submitAccessRequest(
                userId = user.id,
                userName = user.name,
                userEmail = user.email,
                item = item,
                utrNumber = utrNumber,
                screenshotUri = screenshotUri,
                message = message
            )
            closeAccessRequestDialog()
            onComplete()
        }
    }

    // OWNER ACTIONS
    fun approveRequestDirect(
        request: AccessRequestEntity,
        accessType: AccessType = AccessType.LIFETIME,
        expiryDays: Int? = null,
        ownerNote: String = "Approved by Owner"
    ) {
        viewModelScope.launch {
            repository.grantAccessDirect(
                userId = request.userId,
                contentItemId = request.contentItemId,
                accessType = accessType,
                expiryDays = expiryDays,
                requestId = request.id,
                ownerNote = ownerNote
            )
        }
    }

    fun rejectRequest(request: AccessRequestEntity, reason: String, askForAnotherScreenshot: Boolean = false) {
        viewModelScope.launch {
            repository.rejectRequest(request, reason, askForAnotherScreenshot)
        }
    }

    fun grantAccessDirectToUser(userId: String, contentItemId: String, accessType: AccessType, expiryDays: Int?) {
        viewModelScope.launch {
            repository.grantAccessDirect(userId, contentItemId, accessType, expiryDays)
        }
    }

    fun revokeAccess(userId: String, contentItemId: String) {
        viewModelScope.launch {
            repository.revokeAccess(userId, contentItemId)
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            repository.deleteUser(userId)
        }
    }

    fun toggleUserSuspension(user: UserEntity) {
        viewModelScope.launch {
            repository.updateUser(user.copy(isSuspended = !user.isSuspended))
        }
    }

    // CONTENT CRUD (OWNER)
    fun saveContentItem(item: ContentItemEntity) {
        viewModelScope.launch {
            repository.saveContentItem(item)
        }
    }

    fun deleteContentItem(itemId: String) {
        viewModelScope.launch {
            repository.deleteContentItem(itemId)
        }
    }

    // OFFLINE DOWNLOADS
    fun downloadItemOffline(item: ContentItemEntity, onResult: (Boolean) -> Unit) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val success = repository.downloadForOffline(user.id, item)
            onResult(success)
        }
    }

    fun deleteOfflineDownload(itemId: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            repository.deleteOfflineDownload(user.id, itemId)
        }
    }

    suspend fun getDecryptedPdfBytes(item: ContentItemEntity): ByteArray? {
        val user = currentUser.value ?: return null
        return repository.getDecryptedPdfBytes(user.id, item)
    }

    // SETTINGS & BROADCAST
    fun updateAppSetting(key: String, value: String) {
        viewModelScope.launch {
            repository.updateSetting(key, value)
        }
    }

    fun sendBroadcastNotification(title: String, message: String, targetAudience: String) {
        viewModelScope.launch {
            repository.sendBroadcastNotification(title, message, targetAudience)
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            repository.deleteNotification(id)
        }
    }
}

sealed class AppScreen {
    object Home : AppScreen()
    object ContentBrowser : AppScreen()
    object Search : AppScreen()
    object OwnerDashboard : AppScreen()
    object StudentProfile : AppScreen()
    object Notifications : AppScreen()
    object Settings : AppScreen()
    object SecurePdfViewer : AppScreen()
    object Auth : AppScreen()
}
