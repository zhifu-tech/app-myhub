package tech.zhifu.app.myhub.service.media.image

interface ImageGenerationProvider {
    fun generate(
        request: CaptureImageGenerationRequest,
    ): GeneratedImagePayload
}

data class GeneratedImagePayload(
    val bytes: ByteArray,
    val mimeType: String = "image/png",
    val revisedPrompt: String? = null,
)
