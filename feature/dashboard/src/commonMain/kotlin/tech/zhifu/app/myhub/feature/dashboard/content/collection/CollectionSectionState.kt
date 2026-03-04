package tech.zhifu.app.myhub.feature.dashboard.content.collection

import tech.zhifu.app.myhub.datastore.model.domain.Collection

data class CollectionSectionState(
    val collections: List<Collection> = emptyList(),
    val hasMore: Boolean = false,
    val isLoading: Boolean = false,
    val pageIndex: Int = 1,
    val pageSize: Int = 10,

    val errorMessage: String? = null,
)
