package tech.zhifu.app.myhub.feature.dashboard.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error


fun DashboardViewModel.streamUser(
): Flow<User?> = userRepository.streamUser()
    .onEach { user ->
        // 1.1. 用户不存在，静默登陆到方式
        if (user == null) {
            // 执行app初始化，生成默认用户，插入user到db之后，重新执行监听。
            bootstrap.initialize("default")
            intent {
                val state = state as? DashboardUiState.Loading ?: run {
                    logger.debug { "当前非 Loading 态，忽略「初始化」通知" }
                    return@intent
                }
                reduce {
                    state.copy(message = "资源正在进行初始化...")
                }
            }
        }
        // 1.2. 获取到用户信息，尝试通知外部用户信息变化
        else {
            intent {
                val state = state as? DashboardUiState.Content ?: run {
                    logger.debug { "当前非 Content 态，忽略「获取用户」通知" }
                    return@intent
                }
                reduce { state.copy(user = user) }
            }
        }
    }

fun DashboardViewModel.streamUserPreferences(
    userId: String
): Flow<UserPreferences> = userRepository
    .streamUserPreferences(userId)
    .map { preferences ->
        preferences ?: run {
            logger.debug { "用户偏好不存在，采用默认值" }
            UserPreferences(userId)
        }
    }
    .catch { e ->
        logger.error(e) {
            "获取用户偏好失败，采用默认值"
        }
        emit(UserPreferences(userId))
    }
    .onEach { userPreferences ->
        intent {
            val state = state as? DashboardUiState.Content ?: run {
                logger.debug { "当前非 Content 态，忽略「获取用户偏好」通知" }
                return@intent
            }
            reduce { state.copy(userPreferences = userPreferences) }
        }
    }

@Composable
fun <R> DashboardViewModel.collectUserPreferencesFieldState(
    selector: (UserPreferences) -> R
) = collectFieldAsState { uiState ->
    (uiState as? DashboardUiState.Content)
        ?.userPreferences?.let { selector(it) }
}

fun DashboardViewModel.updateUsePreferencesLayoutAsList(
    layoutAsList: Boolean
) = viewModelScope.launch {
    val userPreferences = (uiState as? DashboardUiState.Content)
        ?.userPreferences
        ?: return@launch
    if (userPreferences.layoutAsList == layoutAsList) {
        return@launch
    }
    userRepository.insertUserPreferences(
        preferences = userPreferences.copy(
            layoutAsList = layoutAsList
        )
    )
}

fun DashboardViewModel.updateUsePreferencesSortAsDate(
    sortAsDate: Boolean
) = viewModelScope.launch {
    val userPreferences = (uiState as? DashboardUiState.Content)
        ?.userPreferences
        ?: return@launch
    if (userPreferences.sortAsDate == sortAsDate) {
        return@launch
    }
    userRepository.insertUserPreferences(
        preferences = userPreferences.copy(
            sortAsDate = sortAsDate
        )
    )
}
