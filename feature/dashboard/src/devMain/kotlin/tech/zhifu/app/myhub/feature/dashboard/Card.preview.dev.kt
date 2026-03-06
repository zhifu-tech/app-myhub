package tech.zhifu.app.myhub.feature.dashboard

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import tech.zhifu.app.myhub.datastore.model.domain.Card
import tech.zhifu.app.myhub.datastore.model.domain.CardMetadata
import tech.zhifu.app.myhub.datastore.model.domain.CardSource
import tech.zhifu.app.myhub.datastore.model.domain.CardType
import kotlin.time.Instant

class CardsParameterProvider : PreviewParameterProvider<List<Card>> {
    override val values: Sequence<List<Card>> = sequenceOf(cardList)
}

val cardList = listOf(
    Card(
        id = "card-001",
        type = CardType.Review,
        source = CardSource.Own,
        carriers = "[\"text\"]",
        userId = "preview-user",
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z"),
        metadata = listOf(
            CardMetadata.Content(
                title = "编程名言",
                content = "\"The more that you read, the more things you will know. The more that you learn, the more places you'll go.\"",
            )
        ),
    ),
    Card(
        id = "card-002",
        type = CardType.Material,
        source = CardSource.Own,
        carriers = "[\"text\",\"code\"]",
        userId = "preview-user",
        createdAt = Instant.parse("2024-01-02T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-02T00:00:00Z"),
        metadata = listOf(
            CardMetadata.Content(
                title = "Quick Sort Algorithm",
                content = "function quickSort(arr) {\n  if (arr.length <= 1) return arr;\n  let pivot = arr[0];\n  let left = [];\n  let right = [];\n  for (let i = 1; i < arr.length; i++) {\n    if (arr[i] < pivot) left.push(arr[i]);\n    else right.push(arr[i]);\n  }\n  return [...quickSort(left), pivot, ...quickSort(right)];\n}",
            )
        ),
    ),
    Card(
        id = "card-003",
        type = CardType.Material,
        source = CardSource.Own,
        carriers = "[\"text\"]",
        userId = "preview-user",
        createdAt = Instant.parse("2024-01-03T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-03T00:00:00Z"),
        metadata = listOf(
            CardMetadata.Content(
                title = "Project Idea",
                content = "Project idea: Build a browser extension that automatically converts highlighted text into a Study Room card.",
            )
        ),
    ),
    Card(
        id = "card-004",
        type = CardType.Material,
        source = CardSource.Own,
        carriers = "[\"text\"]",
        userId = "preview-user",
        createdAt = Instant.parse("2024-01-04T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-04T00:00:00Z"),
        metadata = listOf(
            CardMetadata.Content(
                title = "The Future of AI",
                content = "Artificial intelligence is rapidly evolving, moving from narrow tasks to broader capabilities. The integration of LLMs into daily workflows suggests a paradigm shift in how we process information and interact with technology.",
            )
        ),
    ),
    Card(
        id = "card-005",
        type = CardType.Material,
        source = CardSource.Own,
        carriers = "[\"text\"]",
        userId = "preview-user",
        createdAt = Instant.parse("2024-01-05T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-05T00:00:00Z"),
        metadata = listOf(
            CardMetadata.Content(
                title = "Serendipity",
                content = "The occurrence and development of events by chance in a happy or beneficial way.",
            )
        ),
    ),
    Card(
        id = "card-006",
        type = CardType.Do,
        source = CardSource.Own,
        carriers = "[\"text\"]",
        userId = "preview-user",
        createdAt = Instant.parse("2024-01-06T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-06T00:00:00Z"),
        metadata = listOf(
            CardMetadata.Content(
                title = "Weekly Review",
                content = "Weekly review checklist",
            )
        ),
    ),
    Card(
        id = "card-007",
        type = CardType.Review,
        source = CardSource.Own,
        carriers = "[\"text\"]",
        userId = "preview-user",
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-01T00:00:00Z"),
        metadata = listOf(
            CardMetadata.Content(
                title = "编程名言",
                content = "代码是写给人看的，只是偶尔在机器上运行。",
            )
        ),
    ),
    Card(
        id = "card-008",
        type = CardType.Material,
        source = CardSource.Own,
        carriers = "[\"text\",\"code\"]",
        userId = "preview-user",
        createdAt = Instant.parse("2024-01-02T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-02T00:00:00Z"),
        metadata = listOf(
            CardMetadata.Content(
                title = "Hello World",
                content = "fun main() {\n    println(\"Hello, World!\")\n}",
            )
        ),
    ),
    Card(
        id = "card-009",
        type = CardType.Review,
        source = CardSource.Own,
        carriers = "[\"text\"]",
        userId = "preview-user",
        createdAt = Instant.parse("2024-01-07T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-07T00:00:00Z"),
        metadata = listOf(
            CardMetadata.Content(
                title = "Steve Jobs Quote",
                content = "\"The only way to do great work is to love what you do.\"",
            )
        ),
    ),
    Card(
        id = "card-010",
        type = CardType.Review,
        source = CardSource.Own,
        carriers = "[\"text\"]",
        userId = "preview-user",
        createdAt = Instant.parse("2024-01-08T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-08T00:00:00Z"),
        metadata = listOf(
            CardMetadata.Content(
                title = "C.G. Jung Quote",
                content = "\"The privilege of a lifetime is to become who you truly are.\"",
            )
        ),
    ),
    Card(
        id = "card-011",
        type = CardType.Material,
        source = CardSource.Own,
        carriers = "[\"text\"]",
        userId = "preview-user",
        createdAt = Instant.parse("2024-01-09T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-09T00:00:00Z"),
        metadata = listOf(
            CardMetadata.Content(
                title = "Understanding the Transformer Architecture",
                content = "A deep dive into self-attention mechanisms and how they revolutionized NLP processing. The transformer architecture introduced a new paradigm for sequence-to-sequence tasks, enabling parallel processing and capturing long-range dependencies.",
            )
        ),
    ),
    Card(
        id = "card-012",
        type = CardType.Do,
        source = CardSource.Own,
        carriers = "[\"text\"]",
        userId = "preview-user",
        createdAt = Instant.parse("2024-01-10T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-10T00:00:00Z"),
        metadata = listOf(
            CardMetadata.Content(
                title = "WEEKLY REVIEW",
                content = "• Archive completed assets\n• Tag new captures\n• Set goals for next week",
            )
        ),
    ),
    Card(
        id = "card-video-001",
        type = CardType.Material,
        source = CardSource.Own,
        carriers = "[\"video\",\"text\"]",
        userId = "preview-user",
        createdAt = Instant.parse("2024-01-11T00:00:00Z"),
        updatedAt = Instant.parse("2024-01-11T00:00:00Z"),
        metadata = listOf(
            CardMetadata.Content(
                title = "The Future of Generative AI",
                content = "A comprehensive overview of generative AI technologies and their impact on various industries.",
            )
        ),
    ),
)
