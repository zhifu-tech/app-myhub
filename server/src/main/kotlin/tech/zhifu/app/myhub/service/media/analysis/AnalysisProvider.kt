package tech.zhifu.app.myhub.service.media.analysis

import tech.zhifu.app.myhub.service.media.CaptureAnalysisMediaRef
import tech.zhifu.app.myhub.service.media.CaptureAnalysisRequest

interface AnalysisProvider {
    fun analyze(request: CaptureAnalysisRequest): AnalysisModelOutput
}

data class AnalysisModelOutput(
    val title: String? = null,
    val text: String? = null,
    val sourceForm: String? = null,
    val tags: List<String> = emptyList(),
    val code: String? = null,
    val codeLanguage: String? = null,
    val imageOcrSummary: String? = null,
    val imageOcrInfo: String? = null,
    val videoMetadataSummary: String? = null,
    val videoMetadataInfo: String? = null,
    val primaryContentType: String? = null
)

data class AnalysisPromptPayload(
    val inputText: String,
    val intent: String,
    val sourceForm: String,
    val media: List<CaptureAnalysisMediaRef>
)
