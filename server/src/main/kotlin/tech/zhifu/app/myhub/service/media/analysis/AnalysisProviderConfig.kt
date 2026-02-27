package tech.zhifu.app.myhub.service.media.analysis

data class AnalysisProviderConfig(
    val provider: ProviderType = ProviderType.Ollama,
    val timeoutMs: Long = 60_000L,
    val ollamaTextTimeoutMs: Long = 90_000L,
    val ollamaVisionTimeoutMs: Long = 240_000L,
    val retryMax: Int = 2,
    val retryBackoffMs: Long = 500L,
    val ollamaBaseUrl: String = "http://127.0.0.1:11434",
    val ollamaTextModel: String = "qwen2.5:7b",
    val ollamaVisionModel: String = "qwen3-vl:8b",
    val ollamaVisionImageLimit: Int = 6,
    val videoFrameCount: Int = 3,
    val videoFrameIntervalSeconds: Int = 2,
    val qwenBaseUrl: String = "https://dashscope-intl.aliyuncs.com/compatible-mode/v1",
    val qwenApiKey: String? = null,
    val qwenModel: String = "qwen-plus"
) {
    companion object {
        fun fromEnv(): AnalysisProviderConfig {
            return AnalysisProviderConfig(
                provider = when (read("CAPTURE_ANALYSIS_PROVIDER", "ollama").lowercase()) {
                    "qwen" -> ProviderType.Qwen
                    else -> ProviderType.Ollama
                },
                timeoutMs = read("CAPTURE_ANALYSIS_TIMEOUT_MS", "60000").toLongOrNull() ?: 60_000L,
                ollamaTextTimeoutMs = read("OLLAMA_TEXT_TIMEOUT_MS", "90000").toLongOrNull() ?: 90_000L,
                ollamaVisionTimeoutMs = read("OLLAMA_VISION_TIMEOUT_MS", "240000").toLongOrNull() ?: 240_000L,
                retryMax = read("CAPTURE_ANALYSIS_RETRY_MAX", "2").toIntOrNull() ?: 2,
                retryBackoffMs = read("CAPTURE_ANALYSIS_RETRY_BACKOFF_MS", "500").toLongOrNull() ?: 500L,
                ollamaBaseUrl = read("OLLAMA_BASE_URL", "http://127.0.0.1:11434"),
                ollamaTextModel = read("OLLAMA_TEXT_MODEL", "qwen2.5:7b"),
                ollamaVisionModel = read("OLLAMA_VISION_MODEL", "qwen3-vl:8b"),
                ollamaVisionImageLimit = read("OLLAMA_VISION_IMAGE_LIMIT", "6").toIntOrNull() ?: 6,
                videoFrameCount = read("VIDEO_FRAME_COUNT", "3").toIntOrNull() ?: 3,
                videoFrameIntervalSeconds = read("VIDEO_FRAME_INTERVAL_SECONDS", "2").toIntOrNull() ?: 2,
                qwenBaseUrl = read("QWEN_BASE_URL", "https://dashscope-intl.aliyuncs.com/compatible-mode/v1"),
                qwenApiKey = readOrNull("QWEN_API_KEY"),
                qwenModel = read("QWEN_MODEL", "qwen-plus")
            )
        }

        private fun read(key: String, default: String): String {
            val v = System.getProperty(key) ?: System.getenv(key)
            return if (v.isNullOrBlank()) default else v
        }

        private fun readOrNull(key: String): String? {
            val v = System.getProperty(key) ?: System.getenv(key)
            return v?.takeIf { it.isNotBlank() }
        }
    }
}

enum class ProviderType {
    Ollama,
    Qwen
}
