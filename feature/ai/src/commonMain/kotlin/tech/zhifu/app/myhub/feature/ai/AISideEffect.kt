package tech.zhifu.app.myhub.feature.ai

sealed class AISideEffect {
    data class ShowSnack(val message: String) : AISideEffect()
}
