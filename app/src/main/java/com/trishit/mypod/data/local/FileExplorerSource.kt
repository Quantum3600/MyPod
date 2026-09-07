package com.trishit.mypod.data.local

import android.content.Context
import androidx.core.net.toUri
import com.trishit.mypod.playback.AudioEngine
import com.trishit.mypod.source.AlbumInfo
import com.trishit.mypod.source.PlaybackSource
import com.trishit.mypod.source.PlaybackSourceType
import com.trishit.mypod.source.PlaylistInfo
import com.trishit.mypod.source.TrackMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

class FileExplorerSource(
    context: Context,
    private val audioEngine: AudioEngine,
    private val userSettingsRepository: UserSettingsRepository
) : PlaybackSource {

    private val explorer = SafStorageExplorer(context)

    override val sourceType: PlaybackSourceType = PlaybackSourceType.FILES
    override val isAvailable: Boolean = true

    private val _currentTrack = MutableStateFlow<TrackMetadata?>(null)
    override val currentTrackFlow: StateFlow<TrackMetadata?> = _currentTrack.asStateFlow()

    override suspend fun getTracks(): List<TrackMetadata> = withContext(Dispatchers.IO) {
        val folderUri = currentFolderUri() ?: return@withContext emptyList()
        explorer.listDirectory(folderUri.toUri())
            .filterIsInstance<SafItem.AudioFile>()
            .map { it.track }
    }

    override suspend fun getArtists(): List<String> = emptyList()
    override suspend fun getAlbums(): List<AlbumInfo> = emptyList()
    override suspend fun getPlaylists(): List<PlaylistInfo> = emptyList()
    override suspend fun getGenres(): List<String> = emptyList()

    override suspend fun getTracksForArtist(artist: String): List<TrackMetadata> = getTracks()
    override suspend fun getTracksForAlbum(albumId: String): List<TrackMetadata> = getTracks()
    override suspend fun getTracksForPlaylist(playlistId: String): List<TrackMetadata> = getTracks()
    override suspend fun getTracksForGenre(genre: String): List<TrackMetadata> = getTracks()

    override suspend fun playTrack(track: TrackMetadata) {
        _currentTrack.value = track
        audioEngine.playTrack(track)
    }

    override suspend fun playQueue(tracks: List<TrackMetadata>, startIndex: Int) {
        if (tracks.isNotEmpty()) {
            val idx = startIndex.coerceIn(tracks.indices)
            _currentTrack.value = tracks[idx]
        }
        audioEngine.playQueue(tracks, startIndex)
    }

    private suspend fun currentFolderUri(): String? = withContext(Dispatchers.IO) {
        userSettingsRepository.safFolderUriFlow.firstOrNull()
    }
}
