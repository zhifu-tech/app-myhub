package tech.zhifu.app.myhub.feature.preview.content

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.get
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreGraphics.CGImageCreateWithImageInRect
import platform.CoreGraphics.CGImageGetBytesPerRow
import platform.CoreGraphics.CGImageGetDataProvider
import platform.CoreGraphics.CGImageGetHeight
import platform.CoreGraphics.CGImageGetWidth
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSDate
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import platform.Foundation.timeIntervalSince1970
import platform.Foundation.writeToFile
import platform.UIKit.UIApplication
import platform.UIKit.UIGraphicsBeginImageContextWithOptions
import platform.UIKit.UIGraphicsEndImageContext
import platform.UIKit.UIGraphicsGetCurrentContext
import platform.UIKit.UIGraphicsGetImageFromCurrentImageContext
import platform.UIKit.UIImage
import platform.UIKit.UIImagePNGRepresentation
import platform.UIKit.UIScreen
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowLevelNormal
import tech.zhifu.app.myhub.feature.preview.PreviewPayload
import tech.zhifu.app.myhub.logger.debug
import tech.zhifu.app.myhub.logger.error
import tech.zhifu.app.myhub.logger.logger
import tech.zhifu.app.myhub.theme.AppTheme
import kotlin.math.abs

@Composable
internal actual fun rememberPreviewShareImage(
    payload: PreviewPayload,
    width: Dp,
): suspend () -> String? {
    return remember(payload, width) {
        suspend {
            // widthPt 本质上就是 widthDp 数值（在 iOS 里对应 point 语义）
            val widthPt = width.value.toDouble().coerceAtLeast(1.0)

            val controller = ComposeUIViewController {
                AppTheme {
                    PreviewContent(
                        payload = payload,
                        modifier = Modifier.width(width),
                    )
                }
            }

            val screenBounds = UIScreen.mainScreen.bounds
            val screenHeightPt = screenBounds.useContents { size.height }
            val exportScale = UIScreen.mainScreen.scale
            val renderHeightPt = screenHeightPt * 3.0

            val tempWindow =
                UIWindow(frame = screenBounds).apply {
                    windowLevel = UIWindowLevelNormal - 1.0
                    rootViewController = controller
                    hidden = false
                }
            tempWindow.makeKeyAndVisible()

            val drawRect = CGRectMake(0.0, 0.0, widthPt, renderHeightPt)
            val view = controller.view.apply {
                setFrame(drawRect)
                setNeedsLayout()
                layoutIfNeeded()
            }

            UIGraphicsBeginImageContextWithOptions(
                size = CGSizeMake(width = widthPt, height = renderHeightPt),
                opaque = false,
                scale = exportScale
            )
            val context = UIGraphicsGetCurrentContext()
            val data =
                if (context == null) {
                    UIGraphicsEndImageContext()
                    tempWindow.hidden = true
                    UIApplication.sharedApplication.keyWindow?.makeKeyAndVisible()
                    logger.error { "Failed to export preview: no graphics context" }
                    null
                } else {
                    val drew = view.drawViewHierarchyInRect(rect = drawRect, afterScreenUpdates = true)
                    if (!drew) {
                        logger.debug { "drawViewHierarchyInRect returned false, fallback to layer.renderInContext" }
                        view.layer.renderInContext(context)
                    }

                    val image = UIGraphicsGetImageFromCurrentImageContext()
                    UIGraphicsEndImageContext()
                    if (image == null) {
                        tempWindow.hidden = true
                        UIApplication.sharedApplication.keyWindow?.makeKeyAndVisible()
                        logger.error { "Failed to export preview: image is null" }
                        null
                    } else {
                        val trimmedImage = trimBottomBackgroundArea(image)
                        logger.debug {
                            "Preview share widthDp=$width, widthPt=$widthPt, scale=$exportScale, renderHeight=$renderHeightPt, trimmedHeight=${trimmedImage.size.useContents { height }}"
                        }
                        val encoded = UIImagePNGRepresentation(trimmedImage)
                        tempWindow.hidden = true
                        UIApplication.sharedApplication.keyWindow?.makeKeyAndVisible()
                        if (encoded == null) {
                            logger.error { "Failed to encode preview image" }
                        }
                        encoded
                    }
                }

            if (data == null) {
                null
            } else {
                withContext(Dispatchers.Default) {
                    val filename = "preview_share_${NSDate().timeIntervalSince1970}_${NSUUID().UUIDString}.png"
                    val tempDir = NSTemporaryDirectory()
                    val path = if (tempDir.endsWith("/")) "$tempDir$filename" else "$tempDir/$filename"
                    if (data.writeToFile(path, true)) {
                        "file://$path"
                    } else {
                        logger.error { "Failed to write preview share image to $path" }
                        null
                    }
                }
            }
        }
    }
}

private fun trimBottomBackgroundArea(image: UIImage): UIImage {
    val cgImage = image.CGImage ?: return image
    val width = CGImageGetWidth(cgImage).toInt()
    val height = CGImageGetHeight(cgImage).toInt()
    val bytesPerRow = CGImageGetBytesPerRow(cgImage).toInt()
    if (width <= 0 || height <= 0 || bytesPerRow <= 0) return image

    val provider = CGImageGetDataProvider(cgImage) ?: return image
    val data = platform.CoreGraphics.CGDataProviderCopyData(provider) ?: return image
    val bytes: CPointer<UByteVar> = CFDataGetBytePtr(data) ?: return image

    val bytesPerPixel = 4
    val rowScanBytes = (width * bytesPerPixel).coerceAtMost(bytesPerRow)
    if (rowScanBytes < bytesPerPixel) return image

    val bgStart = (height - 1) * bytesPerRow
    val bgB = bytes[bgStart].toInt() and 0xFF
    val bgG = bytes[bgStart + 1].toInt() and 0xFF
    val bgR = bytes[bgStart + 2].toInt() and 0xFF
    val bgA = bytes[bgStart + 3].toInt() and 0xFF

    val tolerance = 8
    var lastContentRow = -1

    for (y in height - 1 downTo 0) {
        val rowStart = y * bytesPerRow
        var rowHasContent = false
        var offset = 0
        while (offset + 3 < rowScanBytes) {
            val b = bytes[rowStart + offset].toInt() and 0xFF
            val g = bytes[rowStart + offset + 1].toInt() and 0xFF
            val r = bytes[rowStart + offset + 2].toInt() and 0xFF
            val a = bytes[rowStart + offset + 3].toInt() and 0xFF
            val isDifferent =
                abs(b - bgB) > tolerance ||
                    abs(g - bgG) > tolerance ||
                    abs(r - bgR) > tolerance ||
                    abs(a - bgA) > tolerance
            if (isDifferent) {
                rowHasContent = true
                break
            }
            offset += bytesPerPixel
        }
        if (rowHasContent) {
            lastContentRow = y
            break
        }
    }

    if (lastContentRow < 0 || lastContentRow >= height - 1) return image

    val croppedHeight = (lastContentRow + 1).coerceAtLeast(1)
    val croppedCg =
        CGImageCreateWithImageInRect(
            image = cgImage,
            rect = CGRectMake(
                x = 0.0,
                y = 0.0,
                width = width.toDouble(),
                height = croppedHeight.toDouble()
            ),
        ) ?: return image

    return UIImage.imageWithCGImage(
        cgImage = croppedCg,
        scale = image.scale,
        orientation = image.imageOrientation,
    )
}
