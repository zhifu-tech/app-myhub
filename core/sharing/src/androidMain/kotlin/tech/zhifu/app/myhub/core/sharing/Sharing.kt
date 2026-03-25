package tech.zhifu.app.myhub.core.sharing

// THIS FILE IS MAINLY COPIED FROM https://github.com/software-mansion/kmp-sharing
/**
 * MIT License
 *
 * Copyright (c) 2025 Software Mansion
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * */
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File

@Composable
actual fun rememberShareSupported(): Boolean = true

/** Implementation of [rememberShare] function on Android */
@Composable
actual fun rememberShare(): Share {
    val context = LocalContext.current
    return remember {
        object : Share {
            override fun invoke(data: List<String>, options: SharingOptions?) {
                try {
                    validateSharingConstraints(data)

                    val contentUris = mutableListOf<Uri>()
                    val textItems = mutableListOf<String>()

                    data.forEach { file ->
                        when (getContentType(file)) {
                            DataType.FILE -> {
                                val fileObj = getLocalFileFromUrl(file)
                                val contentUri =
                                    FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        fileObj,
                                    )
                                contentUris.add(contentUri)
                            }

                            DataType.CONTENT -> {
                                contentUris.add(file.toUri())
                            }

                            DataType.LINK,
                            DataType.TEXT -> {
                                textItems.add(file)
                            }
                        }
                    }

                    val intent =
                        Intent(
                            if (contentUris.size > 1) Intent.ACTION_SEND_MULTIPLE
                            else Intent.ACTION_SEND
                        )

                    if (contentUris.isNotEmpty()) {
                        if (contentUris.size == 1) {
                            intent.putExtra(Intent.EXTRA_STREAM, contentUris[0])
                        } else {
                            intent.putParcelableArrayListExtra(
                                Intent.EXTRA_STREAM,
                                ArrayList(contentUris),
                            )
                        }

                        val mimeType = options?.android?.mimeType ?: "image/*"
                        require(options?.android?.previewData == null) {
                            "Custom preview data is not supported for sharing images."
                        }
                        intent.setTypeAndNormalize(mimeType)
                        intent.data = contentUris[0]
                    } else {
                        intent.setTypeAndNormalize("text/plain")
                        options?.android?.previewData?.let { previewData ->
                            val previewUri =
                                when (getContentType(previewData)) {
                                    DataType.FILE -> {
                                        val fileObj = getLocalFileFromUrl(previewData)
                                        FileProvider.getUriForFile(
                                            context,
                                            "${context.packageName}.fileprovider",
                                            fileObj,
                                        )
                                    }

                                    DataType.CONTENT -> {
                                        previewData.toUri()
                                    }

                                    else ->
                                        throw IllegalArgumentException(
                                            "Unsupported preview data type: $previewData"
                                        )
                                }
                            val clipData = ClipData.newRawUri(null, previewUri)
                            intent.clipData = clipData
                        }
                    }

                    if (textItems.isNotEmpty()) {
                        intent.putExtra(Intent.EXTRA_TEXT, textItems.joinToString("\n"))
                    }

                    if (intent.data != null || intent.clipData != null) {
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    options?.android?.dialogTitle?.let { title ->
                        intent.putExtra(Intent.EXTRA_TITLE, title)
                    }

                    context.startActivity(
                        Intent.createChooser(intent, options?.android?.dialogTitle ?: "Share")
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                } catch (e: Exception) {
                    throw RuntimeException("Failed to share: ${e.message}", e)
                }
            }
        }
    }
}

private fun getLocalFileFromUrl(url: String): File {
    val uri = url.toUri()
    require(uri.scheme == "file") {
        "Only local file URLs are supported (expected scheme to be 'file', got '${uri.scheme}')."
    }

    val path = uri.path
    requireNotNull(path) { "Path component of the URL to share cannot be null." }

    val file = File(path)
    require(file.exists()) { "File does not exist: $path" }

    return file
}
