package tech.zhifu.app.myhub.datastore.model.domain

/**
 * 复习进度信息
 */
data class ReviewProgress(
    val completed: Int = 0,
    val total: Int = 0
) {
    val progress: Float
        get() = if (total > 0) completed.toFloat() / total.toFloat() else 0f
}
