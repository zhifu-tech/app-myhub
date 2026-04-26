package tech.zhifu.app.myhub.datastore.file.storage

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir

internal actual fun platformManagedAppDataDir(): PlatformFile =
    FileKit.filesDir / "app-data"
