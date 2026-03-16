package tech.zhifu.app.myhub.feature.dashboard.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

@Composable
fun MockContent(
    modifier: Modifier
) {
    val gridState = rememberLazyGridState()
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Adaptive(128.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
//        contentPadding = contentPadding,
        modifier = modifier
            .fillMaxSize()
            .testTag("lazy_grid"),
    ) {
        items(50) { index ->
            ImageItem(
                text = "${index + 1}",
                index = index,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3 / 4f),
            )
        }
    }
}

/**
 * Simple pager item which displays an image
 */
@Composable
internal fun ImageItem(
    text: String,
    index: Int = -1,
    modifier: Modifier = Modifier,
) {
    Surface(modifier) {
        Box {
            AsyncImage(
                model = rememberRandomSampleImageUrl(index),
                contentScale = ContentScale.Crop,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
            )

            Text(
                text = text,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .sizeIn(minWidth = 40.dp, minHeight = 40.dp)
                    .padding(8.dp)
                    .wrapContentSize(Alignment.Center),
            )
        }
    }
}

val precannedImageUrls: List<String> by lazy {
    (0 until 50).map { randomSampleImageUrl() }
}

private val rangeForRandom = (0..100_000)

fun randomSampleImageUrl(
    seed: Int = rangeForRandom.random(),
    width: Int = 800,
    height: Int = width,
): String = "https://picsum.photos/seed/$seed/$width/$height"

@Composable
fun rememberRandomSampleImageUrl(index: Int = -1): String = rememberSaveable(index) {
    precannedImageUrls.getOrNull(index) ?: randomSampleImageUrl()
}

