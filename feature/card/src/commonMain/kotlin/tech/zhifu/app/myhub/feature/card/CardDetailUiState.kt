package tech.zhifu.app.myhub.feature.card

import tech.zhifu.app.myhub.datastore.model.Card

/**
 * 卡片详情页 UI 状态
 *
 * 使用 sealed class 表示不同的状态，确保状态互斥和类型安全
 */
sealed class CardDetailUiState {
    /**
     * 加载状态
     * 正在加载卡片数据
     */
    data class Loading(
        val cardId: String
    ) : CardDetailUiState()

    /**
     * 内容状态（有数据）
     * 卡片数据已加载，可以正常显示
     */
    data class Content(
        val card: Card,
        val isSharing: Boolean = false,      // 是否正在分享
        val isEditing: Boolean = false,      // 是否正在编辑
        val isSaving: Boolean = false,       // 是否正在保存
        val error: String? = null             // 错误信息（如果有）
    ) : CardDetailUiState()

    /**
     * 错误状态
     * 加载失败或操作失败
     */
    data class Error(
        val message: String,
        val cardId: String,
        val retryable: Boolean = true        // 是否可重试
    ) : CardDetailUiState()
}
