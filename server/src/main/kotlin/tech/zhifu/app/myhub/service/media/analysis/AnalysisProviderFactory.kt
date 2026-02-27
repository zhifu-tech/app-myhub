package tech.zhifu.app.myhub.service.media.analysis

class AnalysisProviderFactory(
    private val config: AnalysisProviderConfig
) {
    fun create(): AnalysisProvider {
        return when (config.provider) {
            ProviderType.Ollama -> OllamaAnalysisProvider(config)
            ProviderType.Qwen -> QwenAnalysisProvider(config)
        }
    }
}
