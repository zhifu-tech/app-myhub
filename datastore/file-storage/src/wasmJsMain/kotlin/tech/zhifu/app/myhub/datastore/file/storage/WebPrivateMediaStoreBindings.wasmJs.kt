@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@file:JsModule("./webPrivateMediaStore.mjs")

package tech.zhifu.app.myhub.datastore.file.storage

external fun putWebPrivateMediaCallback(
    key: String,
    base64: String,
    mimeType: String,
    onSuccess: (String?) -> Unit,
    onError: (String) -> Unit,
)

external fun copyWebPrivateMediaCallback(
    sourceKey: String,
    targetKey: String,
    onSuccess: (String?) -> Unit,
    onError: (String) -> Unit,
)

external fun resolveWebPrivateMediaUrlCallback(
    key: String,
    onSuccess: (String?) -> Unit,
    onError: (String) -> Unit,
)

external fun hasWebPrivateMediaCallback(
    key: String,
    onSuccess: (Boolean) -> Unit,
    onError: (String) -> Unit,
)

external fun deleteWebPrivateMediaCallback(
    key: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit,
)
