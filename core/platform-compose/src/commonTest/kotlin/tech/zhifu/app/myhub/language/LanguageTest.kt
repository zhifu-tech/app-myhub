package tech.zhifu.app.myhub.language

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Language 单元测试
 */
class LanguageTest {

    @Test
    fun `test toLanguage with exact match`() {
        // When
        val result = "en".toLanguage()

        // Then
        assertEquals(Language.English, result)
    }

    @Test
    fun `test toLanguage with zh-CN`() {
        // When
        val result = "zh-CN".toLanguage()

        // Then
        assertEquals(Language.SimplifiedChinese, result)
    }

    @Test
    fun `test toLanguage with zh-TW`() {
        // When
        val result = "zh-TW".toLanguage()

        // Then
        assertEquals(Language.TraditionalChinese, result)
    }

    @Test
    fun `test toLanguage with ja`() {
        // When
        val result = "ja".toLanguage()

        // Then
        assertEquals(Language.Japanese, result)
    }

    @Test
    fun `test toLanguage with partial match zh`() {
        // When
        val result = "zh".toLanguage()

        // Then
        // 应该匹配到第一个以 "zh" 开头的语言（SimplifiedChinese）
        assertEquals(Language.SimplifiedChinese, result)
    }

    @Test
    fun `test toLanguage with unknown code returns English`() {
        // When
        val result = "fr".toLanguage()

        // Then
        assertEquals(Language.English, result)
    }

    @Test
    fun `test normalizeLanguageTag with zh`() {
        assertEquals(AppLocale.ZH_CN, normalizeLanguageTag("zh"))
    }

    @Test
    fun `test normalizeLanguageTag with zh-rCN legacy tag`() {
        assertEquals(AppLocale.ZH_CN, normalizeLanguageTag("zh-rCN"))
    }

    @Test
    fun `test normalizeLanguageTag with underscore`() {
        assertEquals(AppLocale.ZH_CN, normalizeLanguageTag("zh_CN"))
    }

    @Test
    fun `test normalizeLanguageTag with null uses default`() {
        assertEquals(AppLocale.DEFAULT, normalizeLanguageTag(null))
    }

    @Test
    fun `test toLanguage with empty string returns app default`() {
        // When
        val result = "".toLanguage()

        // Then
        assertEquals(Language.SimplifiedChinese, result)
    }

    @Test
    fun `test toCode for all languages`() {
        // Then
        assertEquals("en", Language.English.toCode())
        assertEquals("zh-CN", Language.SimplifiedChinese.toCode())
        assertEquals("zh-TW", Language.TraditionalChinese.toCode())
        assertEquals("ja", Language.Japanese.toCode())
    }

    @Test
    fun `test Language enum values`() {
        // Then
        assertEquals(4, Language.entries.size)
        assertEquals("en", Language.English.code)
        assertEquals(null, Language.English.region)
        assertEquals("zh-CN", Language.SimplifiedChinese.code)
        assertEquals("CN", Language.SimplifiedChinese.region)
        assertEquals("zh-TW", Language.TraditionalChinese.code)
        assertEquals("TW", Language.TraditionalChinese.region)
        assertEquals("ja", Language.Japanese.code)
        assertEquals(null, Language.Japanese.region)
    }
}
