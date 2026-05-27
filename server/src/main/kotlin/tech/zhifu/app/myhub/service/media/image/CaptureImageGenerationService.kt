package tech.zhifu.app.myhub.service.media.image

import java.util.Base64

class CaptureImageGenerationService(
    private val imageGenerationProvider: ImageGenerationProvider,
) {
    fun generate(
        request: CaptureImageGenerationRequest,
    ): CaptureImageGenerationResponse {
        val image = imageGenerationProvider.generate(request)
        return CaptureImageGenerationResponse(
            imageBase64 = Base64.getEncoder().encodeToString(image.bytes),
            mimeType = image.mimeType,
            revisedPrompt = image.revisedPrompt,
        )
    }
}

data class CaptureImageGenerationRequest(
    val prompt: String,
    val language: String,
    val draftId: String,
    val size: String = "1536x1024",
    val quality: String = "medium",
)

data class CaptureImageGenerationResponse(
    val imageBase64: String,
    val mimeType: String = "image/png",
    val revisedPrompt: String? = null,
)
