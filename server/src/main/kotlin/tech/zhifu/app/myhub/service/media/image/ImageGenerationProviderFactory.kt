package tech.zhifu.app.myhub.service.media.image

import tech.zhifu.app.myhub.service.media.analysis.AnalysisProviderConfig
import tech.zhifu.app.myhub.service.media.analysis.ProviderType

class ImageGenerationProviderFactory(
    private val config: AnalysisProviderConfig,
) {
    fun create(): ImageGenerationProvider {
        return when (config.provider) {
            ProviderType.Qwen -> QwenImageGenerationProvider(config)
            ProviderType.Ollama -> UnsupportedImageGenerationProvider()
        }
    }
}
