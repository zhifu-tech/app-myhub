package tech.zhifu.app.myhub.service.media.image

class UnsupportedImageGenerationProvider : ImageGenerationProvider {
    override fun generate(
        request: CaptureImageGenerationRequest,
    ): GeneratedImagePayload {
        throw IllegalStateException("current_server_provider_does_not_support_image_generation")
    }
}
