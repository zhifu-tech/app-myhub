package tech.zhifu.app.myhub.component.card

import androidx.compose.ui.graphics.Color
import kotlin.test.Test
import kotlin.test.assertEquals
import tech.zhifu.app.myhub.component.card.test.MockCardHelpers
import tech.zhifu.app.myhub.datastore.model.CardType

/**
 * CardComponent 集成测试
 * 测试 Component 接口实现的一致性
 */
class CardComponentIntegrationTest {

    @Test
    fun `test all components return correct icon colors`() {
        // Given
        val components = mapOf(
            CardType.QUOTE to QuoteCardComponent(),
            CardType.CODE to CodeCardComponent(),
            CardType.IDEA to IdeaCardComponent(),
            CardType.ARTICLE to ArticleCardComponent(),
            CardType.DICTIONARY to DictionaryCardComponent(),
            CardType.CHECKLIST to ChecklistCardComponent()
        )

        // Then - 验证每个 Component 返回正确的图标颜色
        assertEquals(Color(0xFF8B5CF6), components[CardType.QUOTE]!!.getTypeIconColor())
        assertEquals(Color(0xFF10B981), components[CardType.CODE]!!.getTypeIconColor())
        assertEquals(Color(0xFFF59E0B), components[CardType.IDEA]!!.getTypeIconColor())
        assertEquals(Color(0xFF3B82F6), components[CardType.ARTICLE]!!.getTypeIconColor())
        assertEquals(Color(0xFF06B6D4), components[CardType.DICTIONARY]!!.getTypeIconColor())
        assertEquals(Color(0xFFEF4444), components[CardType.CHECKLIST]!!.getTypeIconColor())
    }

    @Test
    fun `test all components return correct icon texts`() {
        // Given
        val components = mapOf(
            CardType.QUOTE to QuoteCardComponent(),
            CardType.CODE to CodeCardComponent(),
            CardType.IDEA to IdeaCardComponent(),
            CardType.ARTICLE to ArticleCardComponent(),
            CardType.DICTIONARY to DictionaryCardComponent(),
            CardType.CHECKLIST to ChecklistCardComponent()
        )

        // Then - 验证每个 Component 返回正确的图标文本
        assertEquals("Q", components[CardType.QUOTE]!!.getTypeIconText())
        assertEquals("C", components[CardType.CODE]!!.getTypeIconText())
        assertEquals("I", components[CardType.IDEA]!!.getTypeIconText())
        assertEquals("A", components[CardType.ARTICLE]!!.getTypeIconText())
        assertEquals("D", components[CardType.DICTIONARY]!!.getTypeIconText())
        assertEquals("✓", components[CardType.CHECKLIST]!!.getTypeIconText())
    }

    @Test
    fun `test component consistency across card types`() {
        // Given
        val cards = listOf(
            MockCardHelpers.createTestQuoteCard(),
            MockCardHelpers.createTestCodeCard(),
            MockCardHelpers.createTestIdeaCard(),
            MockCardHelpers.createTestArticleCard(),
            MockCardHelpers.createTestDictionaryCard(),
            MockCardHelpers.createTestChecklistCard()
        )

        // Then - 验证每个卡片都有对应的 Component
        cards.forEach { card ->
            val component = when (card.type) {
                CardType.QUOTE -> QuoteCardComponent()
                CardType.CODE -> CodeCardComponent()
                CardType.IDEA -> IdeaCardComponent()
                CardType.ARTICLE -> ArticleCardComponent()
                CardType.DICTIONARY -> DictionaryCardComponent()
                CardType.CHECKLIST -> ChecklistCardComponent()
            }
            assertEquals(card.type, when (component.getTypeIconText()) {
                "Q" -> CardType.QUOTE
                "C" -> CardType.CODE
                "I" -> CardType.IDEA
                "A" -> CardType.ARTICLE
                "D" -> CardType.DICTIONARY
                "✓" -> CardType.CHECKLIST
                else -> throw IllegalArgumentException("Unknown icon text")
            })
        }
    }
}

