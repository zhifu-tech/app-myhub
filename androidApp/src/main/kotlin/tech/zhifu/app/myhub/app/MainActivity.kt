package tech.zhifu.app.myhub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val configuration = LocalConfiguration.current
            val windowSize = DpSize(
                width = configuration.screenWidthDp.dp,
                height = configuration.screenHeightDp.dp
            )
            App(windowSize = windowSize)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
