package tech.zhifu.app.myhub.feature.sharing

import androidx.compose.runtime.Composable

/** Interface for sharing files */
public interface Share {
    /**
     * Share URL, text, or file using the specified options.
     *
     * @param data URL, text, or file to be shared
     * @param options Configuration options for sharing
     */
    public operator fun invoke(data: String, options: SharingOptions? = null) {
        invoke(listOf(data), options)
    }

    /**
     * Share a list of URL, text, or files using the specified options.
     *
     * Note: [data] parameter restrictions: maximum one URL OR one text item (not both), but
     * multiple files are permitted.
     *
     * @param data List of URL, text, or files to be shared
     * @param options Configuration options for sharing
     */
    public operator fun invoke(data: List<String>, options: SharingOptions? = null)
}

/**
 * Remember a sharing function that uses the native sharing mechanism of the platform.
 */
@Composable public expect fun rememberShare(): Share
