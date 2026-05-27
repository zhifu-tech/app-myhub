package tech.zhifu.app.myhub.datastore.file.storage

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.div
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

internal actual fun platformManagedAppDataDir(): PlatformFile {
    val appSupportUrl = NSFileManager.defaultManager
        .URLsForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomains = NSUserDomainMask
        )
        .firstOrNull() as NSURL?
        ?: error("Could not find Application Support directory")
    return PlatformFile(appSupportUrl) / "app-data"
}
