package com.trishit.mypod.data.local

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import com.trishit.mypod.source.AlbumInfo
import com.trishit.mypod.source.PlaybackSourceType
import com.trishit.mypod.source.PlaylistInfo
import com.trishit.mypod.source.TrackMetadata
import androidx.core.net.toUri

class MediaStoreScanner(private val context: Context) {

    fun scanAllTracks(): List<TrackMetadata> {
        val tracks = mutableListOf<TrackMetadata>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TRACK
        )

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val trackCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val title = cursor.getString(titleCol) ?: "Unknown Track"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val album = cursor.getString(albumCol) ?: "Unknown Album"
                    val albumId = cursor.getLong(albumIdCol)
                    val duration = cursor.getLong(durationCol)
                    val trackNum = cursor.getInt(trackCol)

                    val mediaUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val artUri = ContentUris.withAppendedId(
                        "content://media/external/audio/albumart".toUri(),
                        albumId
                    )

                    tracks.add(
                        TrackMetadata(
                            id = id.toString(),
                            title = title,
                            artist = artist,
                            album = album,
                            artUri = artUri.toString(),
                            mediaUri = mediaUri.toString(),
                            durationMs = duration,
                            trackNumber = trackNum,
                            sourceType = PlaybackSourceType.LOCAL
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        return tracks
    }

    fun scanArtists(): List<String> {
        val artists = mutableSetOf<String>()
        val projection = arrayOf(MediaStore.Audio.Media.ARTIST)
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"

        try {
            context.contentResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                "${MediaStore.Audio.Media.ARTIST} ASC"
            )?.use { cursor ->
                val col = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                while (cursor.moveToNext()) {
                    cursor.getString(col)?.let { artists.add(it) }
                }
            }
        } catch (_: Exception) {}
        return artists.toList().sorted()
    }

    fun scanAlbums(): List<AlbumInfo> {
        val albums = mutableListOf<AlbumInfo>()
        val projection = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
            MediaStore.Audio.Albums.NUMBER_OF_SONGS
        )

        try {
            context.contentResolver.query(
                MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Audio.Albums.ALBUM} ASC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums._ID)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)
                val countCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Albums.NUMBER_OF_SONGS)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val album = cursor.getString(albumCol) ?: "Unknown Album"
                    val artist = cursor.getString(artistCol) ?: "Unknown Artist"
                    val count = cursor.getInt(countCol)

                    val artUri = ContentUris.withAppendedId(
                        "content://media/external/audio/albumart".toUri(),
                        id
                    )

                    albums.add(
                        AlbumInfo(
                            id = id.toString(),
                            name = album,
                            artist = artist,
                            artUri = artUri.toString(),
                            trackCount = count
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        return albums
    }

    fun scanPlaylists(): List<PlaylistInfo> {
        val playlists = mutableListOf<PlaylistInfo>()
        val projection = arrayOf(
            MediaStore.Audio.Playlists._ID,
            MediaStore.Audio.Playlists.NAME
        )

        try {
            context.contentResolver.query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Audio.Playlists.NAME} ASC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.NAME)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "Playlist"
                    playlists.add(
                        PlaylistInfo(
                            id = id.toString(),
                            name = name,
                            trackCount = 0
                        )
                    )
                }
            }
        } catch (_: Exception) {}
        return playlists
    }

    fun scanGenres(): List<String> {
        val genres = mutableSetOf<String>()
        val projection = arrayOf(MediaStore.Audio.Genres.NAME)

        try {
            context.contentResolver.query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                "${MediaStore.Audio.Genres.NAME} ASC"
            )?.use { cursor ->
                val col = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)
                while (cursor.moveToNext()) {
                    cursor.getString(col)?.let { genres.add(it) }
                }
            }
        } catch (_: Exception) {}
        return genres.toList().sorted()
    }

    fun scanTracksForPlaylist(playlistId: String): List<TrackMetadata> {
        val pId = playlistId.toLongOrNull() ?: return emptyList()
        val membersUri = MediaStore.Audio.Playlists.Members.getContentUri("external", pId)
        val allTracks = scanAllTracks().associateBy { it.id }
        val playlistTracks = mutableListOf<TrackMetadata>()

        try {
            context.contentResolver.query(
                membersUri,
                arrayOf(MediaStore.Audio.Playlists.Members.AUDIO_ID),
                null,
                null,
                "${MediaStore.Audio.Playlists.Members.PLAY_ORDER} ASC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Playlists.Members.AUDIO_ID)
                while (cursor.moveToNext()) {
                    val audioId = cursor.getLong(idCol).toString()
                    allTracks[audioId]?.let { playlistTracks.add(it) }
                }
            }
        } catch (_: Exception) {}

        return playlistTracks.ifEmpty { scanAllTracks() }
    }
}
