package tech.zhifu.app.myhub.feature.ai.content

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import tech.zhifu.app.myhub.feature.ai.model.Message

@Composable
fun Message.text() =
    textRes?.let { res ->
        stringResource(
            resource = res,
            formatArgs = textArgs.toTypedArray(),
        )
    } ?: text
