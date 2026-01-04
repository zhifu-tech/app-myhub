package tech.zhifu.app.myhub.profile

import tech.zhifu.app.myhub.datastore.model.Statistics
import tech.zhifu.app.myhub.datastore.model.User

/**
 * Profile UI 状态
 */
data class ProfileUiState(
    // 用户信息
    val user: User? = null,
    
    // 统计数据
    val statistics: Statistics? = null,
    
    // UI 状态
    val isLoading: Boolean = false,
    val error: String? = null,
    
    // 编辑状态
    val isEditingProfile: Boolean = false,
    val editProfileDialog: EditProfileDialogState? = null
)

/**
 * 编辑资料对话框状态
 */
data class EditProfileDialogState(
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String = ""
)


