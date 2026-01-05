package tech.zhifu.app.myhub.analytics.provider

import kotlinx.coroutines.test.runTest
import tech.zhifu.app.myhub.analytics.AnalyticsEvent
import tech.zhifu.app.myhub.analytics.AnalyticsValue
import tech.zhifu.app.myhub.analytics.ProviderConfig
import tech.zhifu.app.myhub.analytics.ProviderType
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FileProviderTest {

    private val testOutputDir = "./test_analytics_output"

    @BeforeTest
    fun setup() {
        // 清理测试目录
        File(testOutputDir).deleteRecursively()
    }

    @AfterTest
    fun cleanup() {
        // 清理测试目录
        File(testOutputDir).deleteRecursively()
    }

    @Test
    fun `file provider initializes correctly`() = runTest {
        val provider = FileProvider(
            outputDir = testOutputDir,
            format = FileProvider.FileFormat.JSON
        )

        assertEquals("File", provider.name)
        assertTrue(provider.supportedPlatforms.contains(tech.zhifu.app.myhub.analytics.Platform.JVM))

        provider.initialize(ProviderConfig(type = ProviderType.FILE))

        assertTrue(provider.isInitialized)
        
        // 验证输出目录已创建
        assertTrue(File(testOutputDir).exists())
    }

    @Test
    fun `file provider writes JSON events`() = runTest {
        val provider = FileProvider(
            outputDir = testOutputDir,
            format = FileProvider.FileFormat.JSON
        )
        provider.initialize(ProviderConfig(type = ProviderType.FILE))

        val event = AnalyticsEvent(
            name = "test_event",
            parameters = mapOf(
                "key1" to AnalyticsValue.Str("value1"),
                "key2" to AnalyticsValue.Int(123L)
            ),
            value = 456.78,
            currency = "USD"
        )

        provider.logEvent(event)
        provider.cleanup()

        // 验证文件已创建
        val files = File(testOutputDir).listFiles { _, name -> name.endsWith(".json") }
        assertTrue(files != null && files.isNotEmpty())
    }

    @Test
    fun `file provider writes CSV events`() = runTest {
        val provider = FileProvider(
            outputDir = testOutputDir,
            format = FileProvider.FileFormat.CSV
        )
        provider.initialize(ProviderConfig(type = ProviderType.FILE))

        val event = AnalyticsEvent(
            name = "test_event",
            parameters = mapOf(
                "key1" to AnalyticsValue.Str("value1")
            )
        )

        provider.logEvent(event)
        provider.cleanup()

        // 验证文件已创建
        val files = File(testOutputDir).listFiles { _, name -> name.endsWith(".csv") }
        assertTrue(files != null && files.isNotEmpty())
    }

    @Test
    fun `file provider sets user properties`() = runTest {
        val provider = FileProvider(
            outputDir = testOutputDir,
            format = FileProvider.FileFormat.JSON
        )
        provider.initialize(ProviderConfig(type = ProviderType.FILE))

        provider.setUserProperty("key1", AnalyticsValue.Str("value1"))
        provider.setUserProperty("key2", null)
        provider.cleanup()

        assertTrue(provider.isInitialized)
    }

    @Test
    fun `file provider sets user ID`() = runTest {
        val provider = FileProvider(
            outputDir = testOutputDir,
            format = FileProvider.FileFormat.JSON
        )
        provider.initialize(ProviderConfig(type = ProviderType.FILE))

        provider.setUserId("user123")
        provider.setUserId(null)
        provider.cleanup()

        assertTrue(provider.isInitialized)
    }

    @Test
    fun `file provider sets screen`() = runTest {
        val provider = FileProvider(
            outputDir = testOutputDir,
            format = FileProvider.FileFormat.JSON
        )
        provider.initialize(ProviderConfig(type = ProviderType.FILE))

        provider.setScreen("Dashboard", "DashboardScreen")
        provider.cleanup()

        assertTrue(provider.isInitialized)
    }

    @Test
    fun `file provider resets`() = runTest {
        val provider = FileProvider(
            outputDir = testOutputDir,
            format = FileProvider.FileFormat.JSON
        )
        provider.initialize(ProviderConfig(type = ProviderType.FILE))

        provider.reset()
        provider.cleanup()

        assertTrue(provider.isInitialized)
    }
}
