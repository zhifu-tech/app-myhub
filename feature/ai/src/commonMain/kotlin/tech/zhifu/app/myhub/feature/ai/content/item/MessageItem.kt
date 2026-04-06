package tech.zhifu.app.myhub.feature.ai.content.item

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.ai.model.Message

@Composable
fun MessageBubbleItem(message: Message) {
    val isUser = message.role == Message.Role.USER
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .background(
                    color = when (message.role) {
                        Message.Role.AI -> Color(0xFFEFF6FF)
                        Message.Role.USER -> Color(0xFF1D4ED8)
                        Message.Role.SYSTEM -> Color(0xFFE2E8F0)
                    },
                    shape = RoundedCornerShape(14.dp),
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.text,
                color = if (isUser) Color.White else Color(0xFF0F172A),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
