package tech.zhifu.app.myhub.service.media.analysis

import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.service.media.CaptureAnalysisRequest

class QwenAnalysisProvider(
    private val config: AnalysisProviderConfig
) : AnalysisProvider {

    override fun analyze(request: CaptureAnalysisRequest): AnalysisModelOutput {
        val apiKey = config.qwenApiKey
            ?: throw IllegalStateException("QWEN_API_KEY is required when CAPTURE_ANALYSIS_PROVIDER=qwen")
        return ProviderHttpSupport.withRetry(
            maxAttempts = config.retryMax,
            initialBackoffMs = config.retryBackoffMs
        ) {
            val start = System.currentTimeMillis()
            val response = ProviderHttpSupport.postJson(
                url = "${config.qwenBaseUrl.trimEnd('/')}/chat/completions",
                body = buildQwenRequestBody(config.qwenModel, request),
                timeoutMs = config.timeoutMs,
                headers = mapOf("Authorization" to "Bearer $apiKey")
            )
            if (response.statusCode() !in 200..299) {
                throw IllegalStateException("qwen http ${response.statusCode()}: ${response.body()}")
            }
            val content = extractQwenContent(response.body())
            val result = AnalysisPromptBuilder.parseModelOutput(content)
            logger.info {
                "capture-analysis provider=qwen model=${config.qwenModel} latencyMs=${System.currentTimeMillis() - start}"
            }
            result
        }
    }
}
