package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import tech.zhifu.app.myhub.feature.preview.PreviewPayload
import tech.zhifu.app.myhub.feature.preview.sharedWith

@Composable
fun PreviewContent(
    sharedTransitionScope: SharedTransitionScope,
    payload: PreviewPayload,
    visible: Boolean,
) {
    Surface(
        modifier = sharedTransitionScope.sharedWith(
            key = "content-preview-${payload.contentId}",
            visible = visible,
        ).fillMaxWidth()
            .clip(shape = RoundedCornerShape(40.dp)),
        shape = RoundedCornerShape(40.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(all = 24.dp)
        ) {

            PreviewContentTopMeta(
                dateText = payload.dateText ?: "March 12, 2026",
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 4.dp, top = 4.dp, end = 4.dp, bottom = 8.dp)
            )

            PreviewContentTitle(
                title = payload.title ?: "Untitled",
                modifier = sharedTransitionScope.sharedWith(
                    key = "content-title-${payload.contentId}",
                    visible = visible,
                ).fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 8.dp)
            )

            PreviewContentCover(
                coverUrl = payload.coverUrl,
                isVideo = payload.isVideo,
                modifier = sharedTransitionScope.sharedWith(
                    key = "content-image-${payload.contentId}",
                    visible = visible,
                ).fillMaxWidth()
                    .padding(top = 24.dp)
            )

            PreviewContentMeta(
                locationText = payload.location ?: "上海",
                tags = payload.tags.ifEmpty { listOf("美食", "拉面") },
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 24.dp)
            )

            PreviewContentBody(
                bodyText = payload.body
                    ?: "这家店的拉面汤头非常浓郁，推荐加一份叉烧。在繁华的上海街头寻找这一抹地道的烟火气，是一次难得的味蕾慰藉。",
                modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 24.dp, bottom = 32.dp)
            )
        }
    }
}
