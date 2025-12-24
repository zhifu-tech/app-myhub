package tech.zhifu.app.myhub.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
actual fun getWindowSize(): DpSize {
    // Web 端可以使用 JavaScript 获取，这里先返回默认值
    // 实际大小应该从平台特定代码传入
    return DpSize(1920.dp, 1080.dp)
}
