package tech.zhifu.app.myhub.service.media

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class MediaUploadService {

    private val sessions = ConcurrentHashMap<String, UploadSession>()
    private val mediaAssets = ConcurrentHashMap<String, MediaAsset>()
    private val uploadedBinaries = ConcurrentHashMap<String, UploadedBinary>()
    private val uploadRootDir: Path = Paths.get("server", "uploads")

    init {
        Files.createDirectories(uploadRootDir)
    }

    fun createUploadSession(
        fileName: String,
        mimeType: String,
        fileSize: Long,
        source: String
    ): UploadSession {
        require(fileName.isNotBlank()) { "fileName is required" }
        require(mimeType.isNotBlank()) { "mimeType is required" }
        require(source.isNotBlank()) { "source is required" }
        val sessionId = "us_${UUID.randomUUID().toString().replace("-", "")}"
        val mediaId = "m_${UUID.randomUUID().toString().replace("-", "")}"
        val expiresAt = Instant.now().plusSeconds(15 * 60)
        val session = UploadSession(
            sessionId = sessionId,
            mediaId = mediaId,
            fileName = fileName,
            mimeType = mimeType,
            fileSize = fileSize,
            source = source,
            uploadMethod = "single_part",
            uploadUrl = "/api/media/upload-sessions/$sessionId/binary",
            expiresAt = expiresAt,
            status = MediaUploadStatus.Pending
        )
        sessions[sessionId] = session
        mediaAssets[mediaId] = MediaAsset(
            mediaId = mediaId,
            fileName = fileName,
            mimeType = mimeType,
            fileSize = fileSize,
            remoteUri = "",
            status = MediaUploadStatus.Pending,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
        return session
    }

    fun uploadBinary(sessionId: String, bytes: ByteArray, contentType: String?): UploadedBinary {
        require(bytes.isNotEmpty()) { "upload body is empty" }
        val session = sessions[sessionId] ?: throw IllegalArgumentException("upload session not found")
        if (session.expiresAt.isBefore(Instant.now())) {
            throw IllegalStateException("upload session expired")
        }
        val mediaDir = uploadRootDir.resolve(session.mediaId)
        Files.createDirectories(mediaDir)
        val safeName = session.fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val filePath = mediaDir.resolve(safeName)
        Files.write(filePath, bytes)
        val etag = md5Hex(bytes)
        val checksumSha256 = sha256Hex(bytes)
        val uploaded = UploadedBinary(
            sessionId = sessionId,
            filePath = filePath.toAbsolutePath().normalize(),
            etag = etag,
            checksumSha256 = checksumSha256,
            size = bytes.size.toLong(),
            mimeType = contentType?.ifBlank { null } ?: session.mimeType,
            uploadedAt = Instant.now()
        )
        uploadedBinaries[sessionId] = uploaded
        sessions[sessionId] = session.copy(status = MediaUploadStatus.Uploading)
        return uploaded
    }

    fun completeUpload(sessionId: String, etag: String, checksumSha256: String?): MediaAsset {
        require(etag.isNotBlank()) { "etag is required" }
        val session = sessions[sessionId] ?: throw IllegalArgumentException("upload session not found")
        if (session.expiresAt.isBefore(Instant.now())) {
            throw IllegalStateException("upload session expired")
        }
        val uploaded = uploadedBinaries[sessionId]
            ?: throw IllegalStateException("binary upload is not completed")
        if (uploaded.etag != etag) {
            throw IllegalStateException("etag mismatch")
        }
        if (!checksumSha256.isNullOrBlank() && !checksumSha256.equals(uploaded.checksumSha256, ignoreCase = true)) {
            throw IllegalStateException("checksum mismatch")
        }
        val existing = mediaAssets[session.mediaId]
            ?: throw IllegalStateException("media asset missing")
        if (existing.status == MediaUploadStatus.Uploaded) {
            return existing
        }
        val remoteUri = uploaded.filePath.toUri().toString()
        val updated = existing.copy(
            remoteUri = remoteUri,
            status = MediaUploadStatus.Uploaded,
            etag = uploaded.etag,
            checksumSha256 = uploaded.checksumSha256,
            fileSize = uploaded.size,
            updatedAt = Instant.now()
        )
        mediaAssets[session.mediaId] = updated
        sessions[sessionId] = session.copy(status = MediaUploadStatus.Uploaded)
        return updated
    }

    fun getMedia(mediaId: String): MediaAsset? = mediaAssets[mediaId]

    fun isUploaded(mediaId: String, remoteUri: String): Boolean {
        val media = mediaAssets[mediaId] ?: return false
        return media.status == MediaUploadStatus.Uploaded && media.remoteUri == remoteUri
    }

    private fun md5Hex(bytes: ByteArray): String {
        return MessageDigest.getInstance("MD5")
            .digest(bytes)
            .joinToString(separator = "") { "%02x".format(it) }
    }

    private fun sha256Hex(bytes: ByteArray): String {
        return MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString(separator = "") { "%02x".format(it) }
    }
}

enum class MediaUploadStatus {
    Pending,
    Uploading,
    Uploaded,
    Failed
}

data class UploadSession(
    val sessionId: String,
    val mediaId: String,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long,
    val source: String,
    val uploadMethod: String,
    val uploadUrl: String,
    val expiresAt: Instant,
    val status: MediaUploadStatus
)

data class MediaAsset(
    val mediaId: String,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long,
    val remoteUri: String,
    val status: MediaUploadStatus,
    val etag: String? = null,
    val checksumSha256: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class UploadedBinary(
    val sessionId: String,
    val filePath: Path,
    val etag: String,
    val checksumSha256: String,
    val size: Long,
    val mimeType: String,
    val uploadedAt: Instant
)
