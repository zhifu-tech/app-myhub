package tech.zhifu.app.myhub.ui.design.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import tech.zhifu.app.myhub.ui.design.resources.Res
import tech.zhifu.app.myhub.ui.design.resources.noto_sc_regular

@Composable
actual fun getAppFontFamily(): FontFamily? = FontFamily(
    Font(Res.font.noto_sc_regular)
)
