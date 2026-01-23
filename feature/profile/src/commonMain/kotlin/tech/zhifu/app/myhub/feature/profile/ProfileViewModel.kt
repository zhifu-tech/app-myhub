package tech.zhifu.app.myhub.feature.profile

//import tech.zhifu.app.myhub.datastore.repository.ReactiveStatisticsRepository
//import tech.zhifu.app.myhub.datastore.repository.ReactiveUserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger

/**
 * Profile ViewModel
 *
 * 管理 Profile 页面的状态和业务逻辑
 */
class ProfileViewModel(
    private val coroutineScope: CoroutineScope,
//    private val userRepository: ReactiveUserRepository,
//    private val statisticsRepository: ReactiveStatisticsRepository
) {
    private val logger = logger("Profile")

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))

    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfileData()
    }

    /**
     * 加载 Profile 数据
     */
    private fun loadProfileData() {
        logger.info { "Loading profile data" }
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

//        // 监听用户信息
//        userRepository.observeCurrentUser()
//            .catch { e ->
//                logger.error(e) { "Failed to observe user: ${e.message}" }
//                _uiState.value = _uiState.value.copy(
//                    error = e.message ?: "Failed to load user",
//                    isLoading = false
//                )
//            }
//            .onEach { user ->
//                _uiState.value = _uiState.value.copy(
//                    user = user,
//                    isLoading = false
//                )
//            }
//            .launchIn(coroutineScope)

//        // 监听统计数据
//        statisticsRepository.observeStatistics()
//            .catch { e ->
//                logger.error(e) { "Failed to observe statistics: ${e.message}" }
//            }
//            .onEach { statistics ->
//                _uiState.value = _uiState.value.copy(
//                    statistics = statistics
//                )
//            }
//            .launchIn(coroutineScope)

//        // 触发初始加载
//        coroutineScope.launch {
//            try {
//                userRepository.getCurrentUser()
//                statisticsRepository.refreshStatistics()
//            } catch (e: Exception) {
//                logger.error(e) { "Failed to load profile data: ${e.message}" }
//            }
//        }
    }

//    /**
//     * 刷新 Profile 数据
//     */
//    fun refreshProfile() {
//        coroutineScope.launch {
//            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
//            try {
//                userRepository.getCurrentUser()
//                statisticsRepository.refreshStatistics()
//            } catch (e: Exception) {
//                logger.error(e) { "Failed to refresh profile: ${e.message}" }
//                _uiState.value = _uiState.value.copy(
//                    error = e.message ?: "Failed to refresh",
//                    isLoading = false
//                )
//            }
//        }
//    }

    /**
     * 开始编辑资料
     */
    fun startEditProfile() {
//        val user = _uiState.value.user
//        _uiState.value = _uiState.value.copy(
//            isEditingProfile = true,
//            editProfileDialog = EditProfileDialogState(
//                displayName = user?.displayName ?: "",
//                email = user?.email ?: "",
//                avatarUrl = user?.avatarUrl ?: ""
//            )
//        )
    }

    /**
     * 取消编辑资料
     */
    fun cancelEditProfile() {
        _uiState.value = _uiState.value.copy(
            isEditingProfile = false,
            editProfileDialog = null
        )
    }

    /**
     * 保存资料
     */
    fun saveProfile(displayName: String, email: String, avatarUrl: String) {
//        val currentUser = _uiState.value.user ?: return
//
//        coroutineScope.launch {
//            try {
//                val updatedUser = currentUser.copy(
//                    displayName = displayName.takeIf { it.isNotBlank() },
//                    email = email.takeIf { it.isNotBlank() },
//                    avatarUrl = avatarUrl.takeIf { it.isNotBlank() }
//                )
//
//                userRepository.updateUser(updatedUser)
//                logger.info { "Profile updated successfully" }
//
//                _uiState.value = _uiState.value.copy(
//                    isEditingProfile = false,
//                    editProfileDialog = null
//                )
//            } catch (e: Exception) {
//                logger.error(e) { "Failed to save profile: ${e.message}" }
//                _uiState.value = _uiState.value.copy(
//                    error = e.message ?: "Failed to save profile"
//                )
//            }
//        }
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

