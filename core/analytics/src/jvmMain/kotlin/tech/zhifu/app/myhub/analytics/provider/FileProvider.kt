package tech.zhifu.app.myhub.analytics.provider

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import tech.zhifu.app.myhub.analytics.AnalyticsEvent
import tech.zhifu.app.myhub.analytics.AnalyticsValue
import tech.zhifu.app.myhub.analytics.BaseAnalyticsProvider
import tech.zhifu.app.myhub.analytics.Platform
import tech.zhifu.app.myhub.analytics.ProviderConfig
import tech.zhifu.app.myhub.analytics.Region
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.time.Instant

/**
 * 文件输出 Provider
 * 用于 Desktop 平台，将事件写入文件（CSV/JSON）
 * 适用于 QA、自动化测试、内部分析
 */
class FileProvider(
    private val outputDir: String = "./analytics",
    private val format: FileFormat = FileFormat.JSON
) : BaseAnalyticsProvider() {
    override val name = "File"
    override val supportedPlatforms = setOf(Platform.JVM)
    override val supportedRegions = setOf(Region.DOMESTIC, Region.OVERSEAS)

    private var fileWriter: PrintWriter? = null
    private val jsonEncoder = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    enum class FileFormat {
        JSON, CSV
    }

    override suspend fun initialize(config: ProviderConfig) {
        withContext(Dispatchers.IO) {
            // 创建输出目录
            ensureOutputDir()

            // 创建文件写入器
            fileWriter = createFileWriter()

            // 写入文件头（CSV 格式）
            if (format == FileFormat.CSV) {
                fileWriter?.println("timestamp,event_name,parameters,value,currency")
            }
        }
        markAsReady()
    }

    override fun doLogEvent(event: AnalyticsEvent) {
        val timestamp = Instant.now().toEpochMilli()

        when (format) {
            FileFormat.JSON -> writeJsonEvent(timestamp, event)
            FileFormat.CSV -> writeCsvEvent(timestamp, event)
        }
    }

    override fun setUserProperty(key: String, value: AnalyticsValue?) {
        val timestamp = Instant.now().toEpochMilli()
        val event = EventRecord(
            timestamp = timestamp,
            type = "user_property",
            key = key,
            value = value?.let { formatValue(it) }
        )
        writeEvent(event)
    }

    override fun setUserId(userId: String?) {
        val timestamp = Instant.now().toEpochMilli()
        val event = EventRecord(
            timestamp = timestamp,
            type = "user_id",
            value = userId
        )
        writeEvent(event)
    }

    override fun setScreen(screenName: String, screenClass: String?) {
        val timestamp = Instant.now().toEpochMilli()
        val event = EventRecord(
            timestamp = timestamp,
            type = "screen",
            key = screenName,
            value = screenClass
        )
        writeEvent(event)
    }

    override fun reset() {
        val timestamp = Instant.now().toEpochMilli()
        val event = EventRecord(
            timestamp = timestamp,
            type = "reset"
        )
        writeEvent(event)
    }

    override fun cleanup() {
        super.cleanup()
        fileWriter?.flush()
        fileWriter?.close()
        fileWriter = null
    }

    private fun writeJsonEvent(timestamp: Long, event: AnalyticsEvent) {
        val record = EventRecord(
            timestamp = timestamp,
            type = "event",
            eventName = event.name,
            parameters = event.parameters.mapValues { formatValue(it.value) },
            value = event.value?.toString(),
            currency = event.currency
        )
        writeEvent(record)
    }

    private fun writeCsvEvent(timestamp: Long, event: AnalyticsEvent) {
        val paramsStr = event.parameters.entries.joinToString(";") {
            "${it.key}=${formatValue(it.value)}"
        }
        val line = listOf(
            timestamp.toString(),
            event.name,
            "\"$paramsStr\"",
            event.value?.toString() ?: "",
            event.currency ?: ""
        ).joinToString(",")
        fileWriter?.println(line)
        fileWriter?.flush()
    }

    private fun writeEvent(record: EventRecord) {
        when (format) {
            FileFormat.JSON -> {
                val json = jsonEncoder.encodeToString(record)
                fileWriter?.println(json)
                fileWriter?.flush()
            }

            FileFormat.CSV -> {
                // CSV 格式已在 writeCsvEvent 中处理
            }
        }
    }

    @Serializable
    private data class EventRecord(
        val timestamp: Long,
        val type: String,
        val eventName: String? = null,
        val key: String? = null,
        val value: String? = null,
        val parameters: Map<String, String>? = null,
        val currency: String? = null
    )

    private fun formatValue(value: AnalyticsValue): String = when (value) {
        is AnalyticsValue.Str -> value.value
        is AnalyticsValue.Num -> value.value.toString()
        is AnalyticsValue.Int -> value.value.toString()
        is AnalyticsValue.Bool -> value.value.toString()
    }

    private fun createFileWriter(): PrintWriter {
        val fileName = when (format) {
            FileFormat.JSON -> "analytics_${System.currentTimeMillis()}.json"
            FileFormat.CSV -> "analytics_${System.currentTimeMillis()}.csv"
        }
        val file = File(outputDir, fileName)
        return PrintWriter(FileWriter(file, true))
    }

    private fun ensureOutputDir() {
        File(outputDir).mkdirs()
    }
}
