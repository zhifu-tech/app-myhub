package tech.zhifu.app.myhub.datastore.file.storage

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal actual suspend fun resolveWebPrivateMediaAccessUrl(
    key: String,
): String? = wasmAwaitString { onSuccess, onError ->
    resolveWebPrivateMediaUrlCallback(key, onSuccess, onError)
}

internal actual suspend fun writeWebPrivateMediaBytes(
    key: String,
    base64: String,
    mimeType: String,
): String? = wasmAwaitString { onSuccess, onError ->
    putWebPrivateMediaCallback(
        key = key,
        base64 = base64,
        mimeType = mimeType,
        onSuccess = onSuccess,
        onError = onError,
    )
}

internal actual suspend fun copyWebPrivateMediaToKey(
    sourceKey: String,
    targetKey: String,
): String? = wasmAwaitString { onSuccess, onError ->
    copyWebPrivateMediaCallback(
        sourceKey = sourceKey,
        targetKey = targetKey,
        onSuccess = onSuccess,
        onError = onError,
    )
}

internal actual suspend fun webPrivateMediaExists(
    key: String,
): Boolean = wasmAwaitBoolean { onSuccess, onError ->
    hasWebPrivateMediaCallback(key, onSuccess, onError)
}

internal actual suspend fun deleteWebPrivateMediaByKey(
    key: String,
) {
    wasmAwaitUnit { onSuccess, onError ->
        deleteWebPrivateMediaCallback(key, onSuccess, onError)
    }
}

private suspend fun wasmAwaitString(
    register: (onSuccess: (String?) -> Unit, onError: (String) -> Unit) -> Unit,
): String? = suspendCancellableCoroutine { continuation ->
    register(
        { value -> continuation.resume(value) },
        { message -> continuation.resumeWithException(IllegalStateException(message)) },
    )
}

private suspend fun wasmAwaitBoolean(
    register: (onSuccess: (Boolean) -> Unit, onError: (String) -> Unit) -> Unit,
): Boolean = suspendCancellableCoroutine { continuation ->
    register(
        { value -> continuation.resume(value) },
        { message -> continuation.resumeWithException(IllegalStateException(message)) },
    )
}

private suspend fun wasmAwaitUnit(
    register: (onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit,
) = suspendCancellableCoroutine { continuation ->
    register(
        { continuation.resume(Unit) },
        { message -> continuation.resumeWithException(IllegalStateException(message)) },
    )
}
