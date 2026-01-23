package tech.zhifu.app.myhub.feature.profile

import tech.zhifu.app.myhub.datastore.model.domain.User

/**
 * Profile UI 状态
 */
data class ProfileUiState(
    // 用户信息
    val user: User? = null,

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


