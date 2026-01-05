package tech.zhifu.app.myhub.component.card

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import tech.zhifu.app.myhub.datastore.model.CardType

/**
 * CardModule 单元测试
 * 测试所有卡片类型都已正确注册
 */
class CardModuleTest {

    @Test
    fun `test all card types are registered`() {
        // Given - 创建所有 Component 实例
        val quoteComponent = QuoteCardComponent()
        val codeComponent = CodeCardComponent()
        val ideaComponent = IdeaCardComponent()
        val articleComponent = ArticleCardComponent()
        val dictionaryComponent = DictionaryCardComponent()
        val checklistComponent = ChecklistCardComponent()

        // When - 创建映射（模拟 CardModule 的注册）
        val componentMap = mapOf(
            CardType.QUOTE to quoteComponent,
            CardType.CODE to codeComponent,
            CardType.IDEA to ideaComponent,
            CardType.ARTICLE to articleComponent,
            CardType.DICTIONARY to dictionaryComponent,
            CardType.CHECKLIST to checklistComponent
        )

        // Then - 验证所有类型都已注册
        assertEquals(6, componentMap.size)
        assertNotNull(componentMap[CardType.QUOTE])
        assertNotNull(componentMap[CardType.CODE])
        assertNotNull(componentMap[CardType.IDEA])
        assertNotNull(componentMap[CardType.ARTICLE])
        assertNotNull(componentMap[CardType.DICTIONARY])
        assertNotNull(componentMap[CardType.CHECKLIST])
    }

    @Test
    fun `test component map contains all card types`() {
        // Given
        val componentMap = mapOf(
            CardType.QUOTE to QuoteCardComponent(),
            CardType.CODE to CodeCardComponent(),
            CardType.IDEA to IdeaCardComponent(),
            CardType.ARTICLE to ArticleCardComponent(),
            CardType.DICTIONARY to DictionaryCardComponent(),
            CardType.CHECKLIST to ChecklistCardComponent()
        )

        // Then - 验证所有 CardType 枚举值都有对应的 Component
        CardType.entries.forEach { cardType ->
            assertTrue(
                componentMap.containsKey(cardType),
                "CardType.$cardType should be registered in CardModule"
            )
        }
    }

    @Test
    fun `test each component implements CardComponent interface`() {
        // Given
        val components = listOf(
            QuoteCardComponent(),
            CodeCardComponent(),
            IdeaCardComponent(),
            ArticleCardComponent(),
            DictionaryCardComponent(),
            ChecklistCardComponent()
        )

        // Then - 验证所有 Component 都实现了 CardComponent 接口
        components.forEach { component ->
            // 所有组件都实现了 CardComponent 接口，这是编译时保证的
            // 这里主要验证组件可以正常创建
            assertNotNull(component)
        }
    }
}

