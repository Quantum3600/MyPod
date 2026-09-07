package com.bytekoders.mypod.data.local

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.bytekoders.mypod.source.PlaybackSourceType
import com.bytekoders.mypod.source.TrackMetadata

sealed interface SafItem {
    val name: String
    val uri: Uri

    data class Folder(
        override val name: String,
        override val uri: Uri,
        val itemCount: Int = 0
    ) : SafItem

    data class AudioFile(
        override val name: String,
        override val uri: Uri,
        val track: TrackMetadata
    ) : SafItem
}

class SafStorageExplorer(private val context: Context) {

    private val supportedExtensions = setOf("mp3", "m4a", "flac", "wav", "ogg", "opus")

    fun listDirectory(treeUri: Uri): List<SafItem> {
        val rootDoc = DocumentFile.fromTreeUri(context, treeUri) ?: return emptyList()
        if (!rootDoc.canRead()) return emptyList()

        val items = mutableListOf<SafItem>()
        val files = rootDoc.listFiles()

        for (file in files) {
            val name = file.name ?: continue
            val uri = file.uri

            if (file.isDirectory) {
                items.add(
                    SafItem.Folder(
                        name = name,
                        uri = uri,
                        itemCount = file.listFiles().size
                    )
                )
            } else if (file.isFile && isAudioFile(file)) {
                val extension = name.substringAfterLast('.', "").lowercase()
                val title = name.substringBeforeLast('.')
                val track = TrackMetadata(
                    id = uri.toString(),
                    title = title,
                    artist = "SAF File ($extension)",
                    album = rootDoc.name ?: "Storage",
                    mediaUri = uri.toString(),
                    sourceType = PlaybackSourceType.FILES
                )
                items.add(SafItem.AudioFile(name = name, uri = uri, track = track))
            }
        }

        // Sort folders first, then files alphabetically
        return items.sortedWith(
            compareBy<SafItem> { if (it is SafItem.Folder) 0 else 1 }
                .thenBy { it.name.lowercase() }
        )
    }

    private fun isAudioFile(file: DocumentFile): Boolean {
        val mime = file.type
        if (mime != null && mime.startsWith("audio/")) return true

        val name = file.name ?: return false
        val ext = name.substringAfterLast('.', "").lowercase()
        return ext in supportedExtensions
    }
}
