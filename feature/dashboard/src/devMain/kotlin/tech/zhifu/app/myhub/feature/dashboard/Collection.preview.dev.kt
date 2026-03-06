package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import tech.zhifu.app.myhub.datastore.model.domain.Collection
import kotlin.time.Instant

class CollectionsParameterProvider : PreviewParameterProvider<List<Collection>> {
    override val values: Sequence<List<Collection>> = sequenceOf(collectionList)
}

val collectionList = listOf(
    Collection(
        id = "collection-001",
        name = "Literature Classics",
        userId = "user-001",
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z"),
    ),
    Collection(
        id = "collection-002",
        name = "Algorithm Mastery",
        userId = "user-001",
        createdAt = Instant.parse("2024-01-02T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-02T00:00:00Z"),
    ),
    Collection(
        id = "collection-003",
        name = "AI Trends",
        userId = "user-001",
        createdAt = Instant.parse("2024-01-03T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-03T00:00:00Z"),
    ),
    Collection(
        id = "collection-004",
        name = "Learning Lab",
        userId = "user-001",
        createdAt = Instant.parse("2024-01-04T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-04T00:00:00Z"),
    ),
)
