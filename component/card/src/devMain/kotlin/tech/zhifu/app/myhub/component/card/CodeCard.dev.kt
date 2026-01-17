package tech.zhifu.app.myhub.component.card

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import tech.zhifu.app.myhub.datastore.model.Card
import tech.zhifu.app.myhub.datastore.model.CardMetadata
import tech.zhifu.app.myhub.datastore.model.CardType
import tech.zhifu.app.myhub.theme.AppTheme
import kotlin.time.Clock

/**
 * Preview 函数 - 浅色主题
 */
@Preview
@Composable
private fun CodeCardLightPreview() {
    AppTheme(darkTheme = false) {
        CodeCard(
            card = createSampleCodeCard(
                title = "Kotlin Extension Function",
                code = """fun String.isEmail(): Boolean {
    return this.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
}""",
                language = "kotlin",
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
private fun CodeCardDarkPreview() {
    AppTheme(darkTheme = true) {
        CodeCard(
            card = createSampleCodeCard(
                title = "Kotlin Extension Function",
                code = """fun String.isEmail(): Boolean {
    return this.matches(Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"))
}""",
                language = "kotlin",
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
private fun CodeCardFavoritePreview() {
    AppTheme(darkTheme = false) {
        CodeCard(
            card = createSampleCodeCard(
                title = "Compose State Management",
                code = $$"""@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}""",
                language = "kotlin",
                isFavorite = true
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview 函数 - 长代码
 */
@Preview
@Composable
private fun CodeCardLongCodePreview() {
    AppTheme(darkTheme = false) {
        CodeCard(
            card = createSampleCodeCard(
                title = "Data Class Example",
                code = """data class User(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: Instant,
    val updatedAt: Instant? = null
) {
    fun toDto(): UserDto {
        return UserDto(
            id = id,
            name = name,
            email = email,
            createdAt = createdAt.toString(),
            updatedAt = updatedAt?.toString()
        )
    }
}""",
                language = "kotlin",
                isFavorite = false
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * Preview 函数 - 不同语言
 */
@Preview
@Composable
private fun CodeCardJavaScriptPreview() {
    AppTheme(darkTheme = false) {
        CodeCard(
            card = createSampleCodeCard(
                title = "Async Function",
                code = """async function fetchData(url) {
    const response = await fetch(url);
    const data = await response.json();
    return data;
}""",
                language = "javascript",
                isFavorite = false
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

/**
 * 创建示例代码卡片数据
 */
private fun createSampleCodeCard(
    title: String,
    code: String,
    language: String,
    isFavorite: Boolean = false
): Card {
    val now = Clock.System.now()
    return Card(
        id = "preview-code-1",
        type = CardType.CODE,
        title = title,
        content = code,
        language = language,
        isFavorite = isFavorite,
        createdAt = now,
        updatedAt = now,
        metadata = CardMetadata(
            codeLanguage = language,
            codeSnippet = code
        )
    )
}

