package tech.zhifu.app.myhub.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import tech.zhifu.app.myhub.platform.resources.Res
import tech.zhifu.app.myhub.platform.resources.noto_sc_regular

@Composable
actual fun getAppFontFamily(): FontFamily? = FontFamily(
    Font(Res.font.noto_sc_regular)
)
