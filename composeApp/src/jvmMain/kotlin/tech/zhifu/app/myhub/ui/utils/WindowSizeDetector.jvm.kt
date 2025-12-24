package tech.zhifu.app.myhub.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@Composable
actual fun getWindowSize(): DpSize {
    // 桌面端应该从 WindowState 传入，这里返回默认值
    // 实际大小会在 App() 中通过参数传入
    return DpSize(1920.dp, 1080.dp)
}
