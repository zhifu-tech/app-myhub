package tech.zhifu.app.myhub.service.media.analysis

import tech.zhifu.app.myhub.logger.info
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.service.media.CaptureAnalysisRequest

class OllamaAnalysisProvider(
    private val config: AnalysisProviderConfig
) : AnalysisProvider {

    override fun analyze(request: CaptureAnalysisRequest): AnalysisModelOutput {
        val imagesBase64 = AnalysisMediaInputBuilder.buildOllamaImages(request, config)
        val useVision = imagesBase64.isNotEmpty()
        val model = if (useVision) config.ollamaVisionModel else config.ollamaTextModel
        val timeoutMs = if (useVision) config.ollamaVisionTimeoutMs else config.ollamaTextTimeoutMs
        return ProviderHttpSupport.withRetry(
            maxAttempts = config.retryMax,
            initialBackoffMs = config.retryBackoffMs
        ) {
            val start = System.currentTimeMillis()
            val response = ProviderHttpSupport.postJson(
                url = "${config.ollamaBaseUrl.trimEnd('/')}/api/chat",
                body = buildOllamaRequestBody(model, request, imagesBase64),
                timeoutMs = timeoutMs
            )
            if (response.statusCode() !in 200..299) {
                throw IllegalStateException("ollama http ${response.statusCode()}: ${response.body()}")
            }
            val content = extractOllamaContent(response.body())
            val result = AnalysisPromptBuilder.parseModelOutput(content)
            logger.info {
                "capture-analysis provider=ollama model=$model " +
                    "latencyMs=${System.currentTimeMillis() - start} timeoutMs=$timeoutMs imageCount=${imagesBase64.size}"
            }
            result
        }
    }
}
