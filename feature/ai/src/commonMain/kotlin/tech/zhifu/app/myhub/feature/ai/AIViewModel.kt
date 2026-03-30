package tech.zhifu.app.myhub.feature.ai

import org.orbitmvi.orbit.viewmodel.container
import tech.zhifu.app.myhub.ui.design.util.ViewModelContainerHost

class AIViewModel : ViewModelContainerHost<AIUiState, AISideEffect>() {

    override val container = container<AIUiState, AISideEffect>(
        initialState = AIUiState.Loading,
    ) {
        refresh()
    }

    fun refresh() = intent {
        reduce {
            AIUiState.Content(
                messages = aiMockMessages,
                tags = listOf("美食", "餐厅", "日本料理"),
                isTagInputMode = false,
                newTag = "",
            )
        }
    }

    fun removeTag(tag: String) = intent {
        reduce {
            when (val current = state) {
                is AIUiState.Content -> {
                    current.copy(tags = current.tags.filterNot { it == tag })
                }

                else -> state
            }
        }
    }

    fun showTagInput() = intent {
        reduce {
            when (val current = state) {
                is AIUiState.Content -> {
                    current.copy(isTagInputMode = true)
                }

                else -> state
            }
        }
    }

    fun updateNewTag(value: String) = intent {
        reduce {
            when (val current = state) {
                is AIUiState.Content -> {
                    current.copy(newTag = value)
                }

                else -> state
            }
        }
    }

    fun addTag() = intent {
        reduce {
            when (val current = state) {
                is AIUiState.Content -> {
                    val normalized = current.newTag.trim()
                    if (normalized.isBlank()) {
                        current.copy(isTagInputMode = false)
                    } else {
                        current.copy(
                            tags = current.tags + normalized,
                            newTag = "",
                            isTagInputMode = false,
                        )
                    }
                }

                else -> state
            }
        }
    }
}
