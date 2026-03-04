package tech.zhifu.app.myhub.feature.dashboard.content.review

import tech.zhifu.app.myhub.datastore.model.domain.ReviewProgress

data class ReviewState(
    val reviewProgress: ReviewProgress = ReviewProgress(),
    val showFocusReview: Boolean = true,
    val reviewCardsCount: Int = 0,
)
