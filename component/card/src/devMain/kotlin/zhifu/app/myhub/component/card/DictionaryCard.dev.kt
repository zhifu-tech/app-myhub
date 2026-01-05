package zhifu.app.myhub.component.card

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import tech.zhifu.app.myhub.component.card.DictionaryCard
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardMetadata
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.theme.AppTheme

/**
 * Preview 函数 - 浅色主题
 */
@Preview
@Composable
private fun DictionaryCardLightPreview() {
    AppTheme(darkTheme = false) {
        DictionaryCard(
            card = createSampleDictionaryCard(
                word = "Serendipity",
                pronunciation = "/ˌserənˈdipitē/",
                definition = "The occurrence and development of events by chance in a happy or beneficial way.",
                example = "A fortunate stroke of serendipity brought the two old friends together.",
                isFavorite = false
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview 函数 - 深色主题
 */
@Preview
@Composable
private fun DictionaryCardDarkPreview() {
    AppTheme(darkTheme = true) {
        DictionaryCard(
            card = createSampleDictionaryCard(
                word = "Serendipity",
                pronunciation = "/ˌserənˈdipitē/",
                definition = "The occurrence and development of events by chance in a happy or beneficial way.",
                example = "A fortunate stroke of serendipity brought the two old friends together.",
                isFavorite = false
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview 函数 - 收藏状态
 */
@Preview
@Composable
private fun DictionaryCardFavoritePreview() {
    AppTheme(darkTheme = false) {
        DictionaryCard(
            card = createSampleDictionaryCard(
                word = "Ephemeral",
                pronunciation = "/əˈfem(ə)rəl/",
                definition = "Lasting for a very short time.",
                example = "The ephemeral beauty of cherry blossoms attracts visitors from around the world.",
                isFavorite = true
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview 函数 - 无示例
 */
@Preview
@Composable
private fun DictionaryCardNoExamplePreview() {
    AppTheme(darkTheme = false) {
        DictionaryCard(
            card = createSampleDictionaryCard(
                word = "Ubiquitous",
                pronunciation = "/yo͞oˈbikwədəs/",
                definition = "Present, appearing, or found everywhere.",
                example = null,
                isFavorite = false
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview 函数 - 长定义
 */
@Preview
@Composable
private fun DictionaryCardLongDefinitionPreview() {
    AppTheme(darkTheme = false) {
        DictionaryCard(
            card = createSampleDictionaryCard(
                word = "Metamorphosis",
                pronunciation = "/ˌmedəˈmôrfəsəs/",
                definition = "A change of the form or nature of a thing or person into a completely different one, " +
                    "by natural or supernatural means. In biology, it refers to the process of transformation " +
                    "from an immature form to an adult form in two or more distinct stages.",
                example = "The metamorphosis of a caterpillar into a butterfly is one of nature's most remarkable transformations.",
                isFavorite = false
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * 创建示例字典卡片数据
 */
private fun createSampleDictionaryCard(
    word: String,
    pronunciation: String,
    definition: String,
    example: String?,
    isFavorite: Boolean = false
): Card {
    val now = kotlin.time.Clock.System.now()
    return Card(
        id = "preview-dictionary-1",
        type = CardType.DICTIONARY,
        title = word,
        content = definition,
        isFavorite = isFavorite,
        createdAt = now,
        updatedAt = now,
        metadata = CardMetadata(
            wordPronunciation = pronunciation,
            wordDefinition = definition,
            wordExample = example
        )
    )
}

