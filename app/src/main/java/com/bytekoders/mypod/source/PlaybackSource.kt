package com.bytekoders.mypod.source

import kotlinx.coroutines.flow.StateFlow

interface PlaybackSource {
    val sourceType: PlaybackSourceType
    val isAvailable: Boolean
    val currentTrackFlow: StateFlow<TrackMetadata?>

    suspend fun getTracks(): List<TrackMetadata>
    suspend fun getArtists(): List<String>
    suspend fun getAlbums(): List<AlbumInfo>
    suspend fun getPlaylists(): List<PlaylistInfo>
    suspend fun getGenres(): List<String>

    suspend fun getTracksForArtist(artist: String): List<TrackMetadata>
    suspend fun getTracksForAlbum(albumId: String): List<TrackMetadata>
    suspend fun getTracksForPlaylist(playlistId: String): List<TrackMetadata>
    suspend fun getTracksForGenre(genre: String): List<TrackMetadata>

    suspend fun playTrack(track: TrackMetadata)
    suspend fun playQueue(tracks: List<TrackMetadata>, startIndex: Int = 0)
}
