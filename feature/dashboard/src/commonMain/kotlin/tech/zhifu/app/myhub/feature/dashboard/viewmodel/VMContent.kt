package tech.zhifu.app.myhub.feature.dashboard.viewmodel

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onEach
import tech.zhifu.app.myhub.datastore.model.domain.User
import tech.zhifu.app.myhub.datastore.model.domain.UserPreferences
import tech.zhifu.app.myhub.feature.dashboard.DashboardUiState
import tech.zhifu.app.myhub.feature.dashboard.DashboardViewModel
import tech.zhifu.app.myhub.feature.dashboard.content.item.ContentItem
import tech.zhifu.app.myhub.feature.dashboard.content.item.mockContentItems
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error

fun DashboardViewModel.streamContentItems(
    userId: String,
    pageIndex: Int = 1,
    pageSize: Int = 20,
    sortAsDate: Boolean = true,
    sortAsName: Boolean = false,
    user: User,
    userPreferences: UserPreferences,
): Flow<List<ContentItem>> = flowOf(
    mockContentItems().let { items ->
        when {
            sortAsDate -> items
            sortAsName -> items.sortedBy { it.title.lowercase() }
            else -> items
        }
    }
)
    .catch { e ->
        logger.error(e) {
            "获取内容数据失败，显示错误，提示用户重试"
        }
        intent {
            reduce {
                DashboardUiState.Error(
                    message = e.message ?: "加载失败，请重试"
                )
            }
        }
    }
    .onEach { items ->
        if (pageIndex == 1) {
            intent {
                val state = (state as? DashboardUiState.Content)
                    ?.copy(
                        pageIndx = 1,
                        pageSize = pageSize,
                        hasMore = items.size >= pageSize,
                        contentItems = items,
                    )
                    ?: DashboardUiState.Content(
                        user = user,
                        userPreferences = userPreferences,
                        pageIndx = 1,
                        pageSize = pageSize,
                        hasMore = items.size >= pageSize,
                        contentItems = items
                    )
                reduce { state }
            }
        } else {
            intent {
                val state = state as? DashboardUiState.Content ?: run {
                    logger.debug {
                        "当前 index=$pageIndex 非 Content 态，忽略「获取内容数据」通知"
                    }
                    return@intent
                }
                reduce {
                    state.copy(
                        pageIndx = pageIndex,
                        pageSize = pageSize,
                        hasMore = items.size >= pageSize,
                        contentItems = items
                    )
                }
            }
        }
    }


@Composable
fun DashboardViewModel.collectContentState() = collectFieldAsState {
    it as? DashboardUiState.Content
}

@Composable
fun DashboardViewModel.collectContentEmptyState() = collectFieldAsState {
    (it as? DashboardUiState.Content)
        ?.contentItems?.isEmpty() ?: true
}

@Composable
fun DashboardViewModel.collectContentCountThreshold(
    threshold: Int
) = collectFieldAsState { state ->
    val size = (state as? DashboardUiState.Content)
        ?.contentItems?.size ?: 0
    size >= threshold
}
