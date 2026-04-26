package tech.zhifu.app.myhub.feature.ai.layer.agent.provider.image

data class ProviderImageGenerationRequest(
    val prompt: String,
    val language: String,
    val draftId: String,
    val size: String = "1024x1024",
    val quality: String = "medium",
)

data class ProviderImageGenerationProgress(
    val stage: Stage,
    val completed: Int? = null,
    val total: Int? = null,
    val status: String? = null,
) {
    enum class Stage {
        PREPARING,
        GENERATING,
        FINALIZING,
    }
}

data class ProviderGeneratedImage(
    val bytes: ByteArray,
    val mimeType: String = "image/png",
    val revisedPrompt: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ProviderGeneratedImage) return false

        return bytes.contentEquals(other.bytes) &&
            mimeType == other.mimeType &&
            revisedPrompt == other.revisedPrompt
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + (revisedPrompt?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "ProviderGeneratedImage(bytes=${bytes.size} bytes, mimeType=$mimeType, revisedPrompt=$revisedPrompt)"
    }
}

sealed interface ProviderImageGenerationResult {
    data class Success(
        val image: ProviderGeneratedImage,
    ) : ProviderImageGenerationResult

    data class Failed(
        val reason: String,
        val category: ProviderImageGenerationError,
    ) : ProviderImageGenerationResult
}

enum class ProviderImageGenerationError {
    CONFIG,
    AUTH,
    TIMEOUT,
    HTTP,
    PARSE,
    UNAVAILABLE,
}
