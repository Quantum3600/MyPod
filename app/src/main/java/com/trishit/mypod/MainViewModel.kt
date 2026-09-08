package com.trishit.mypod

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.trishit.mypod.audio.ClickSoundPlayer
import com.trishit.mypod.data.local.FileExplorerSource
import com.trishit.mypod.data.local.LocalSource
import com.trishit.mypod.data.local.SafItem
import com.trishit.mypod.data.local.SafStorageExplorer
import com.trishit.mypod.data.lyrics.LyricsRepository
import com.trishit.mypod.data.lyrics.LyricsResult
import com.trishit.mypod.data.playlist.PlaylistEntity
import com.trishit.mypod.data.playlist.PlaylistRepository
import com.trishit.mypod.data.theme.ThemePreset
import com.trishit.mypod.data.theme.ThemeRepository
import com.trishit.mypod.navigation.MenuItem
import com.trishit.mypod.navigation.MenuNavigationManager
import com.trishit.mypod.navigation.MenuState
import com.trishit.mypod.navigation.RightPaneContent
import com.trishit.mypod.playback.AudioEngine
import com.trishit.mypod.source.AccountManager
import com.trishit.mypod.source.AlbumInfo
import com.trishit.mypod.source.AuthState
import com.trishit.mypod.source.NowPlayingState
import com.trishit.mypod.source.PlaybackSource
import com.trishit.mypod.source.PlaybackSourceType
import com.trishit.mypod.source.TrackMetadata
import com.trishit.mypod.source.applemusic.AppleMusicSource
import com.trishit.mypod.source.spotify.SpotifySource
import com.trishit.mypod.source.youtube.YouTubeSource
import com.trishit.mypod.source.youtube.YtDlpSource
import com.trishit.mypod.ui.components.WheelEvent
import com.trishit.mypod.ui.sensors.TiltSensorManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val themeRepository = ThemeRepository(application)
    private val clickSoundPlayer = ClickSoundPlayer(application)
    val audioEngine = AudioEngine.getInstance(application)
    val accountManager = AccountManager(application, viewModelScope)
    val navigationManager = MenuNavigationManager()

    val lyricsRepository = LyricsRepository(application)
    val tiltSensorManager = TiltSensorManager(application)

    // Sources
    val localSource = LocalSource(application, audioEngine)
    val fileExplorerSource = FileExplorerSource(application, audioEngine, accountManager.userSettingsRepository)
    val spotifySource = SpotifySource(accountManager.spotifyAuthProvider, audioEngine)
    val youTubeSource = YouTubeSource(accountManager.youtubeAuthProvider, audioEngine)
    val ytDlpSource = YtDlpSource(audioEngine)
    val appleMusicSource = AppleMusicSource()

    private val safExplorer = SafStorageExplorer(application)

    val nowPlayingState: StateFlow<NowPlayingState> = audioEngine.nowPlayingState

    private val _lyricsState = MutableStateFlow<LyricsResult?>(null)
    val lyricsState: StateFlow<LyricsResult?> = _lyricsState.asStateFlow()

    private val _isLoadingLyricsState = MutableStateFlow(false)
    val isLoadingLyricsState: StateFlow<Boolean> = _isLoadingLyricsState.asStateFlow()

    val tiltSensorState: StateFlow<Pair<Float, Float>> = tiltSensorManager.tiltState

    private val _coverFlowAlbumsState = MutableStateFlow<List<AlbumInfo>>(emptyList())
    val coverFlowAlbumsState: StateFlow<List<AlbumInfo>> = _coverFlowAlbumsState.asStateFlow()

    private val _coverFlowIndexState = MutableStateFlow(0)
    val coverFlowIndexState: StateFlow<Int> = _coverFlowIndexState.asStateFlow()

    private val _ytdlpCoverFlowAlbumsState = MutableStateFlow<List<AlbumInfo>>(emptyList())
    val ytdlpCoverFlowAlbumsState: StateFlow<List<AlbumInfo>> = _ytdlpCoverFlowAlbumsState.asStateFlow()

    private val _ytdlpCoverFlowIndexState = MutableStateFlow(0)
    val ytdlpCoverFlowIndexState: StateFlow<Int> = _ytdlpCoverFlowIndexState.asStateFlow()

    val activeSourceState: StateFlow<PlaybackSourceType> = accountManager.activeSourceState
    val ytdlpResolverEnabledState: StateFlow<Boolean> = accountManager.ytdlpResolverEnabledState

    private val _launchSafFolderPickerEvent = MutableSharedFlow<Unit>()
    val launchSafFolderPickerEvent: SharedFlow<Unit> = _launchSafFolderPickerEvent.asSharedFlow()

    val gameWheelEvents = MutableSharedFlow<WheelEvent>(extraBufferCapacity = 64)

    private val _quizTracksState = MutableStateFlow<List<TrackMetadata>>(emptyList())
    val quizTracksState: StateFlow<List<TrackMetadata>> = _quizTracksState.asStateFlow()

    fun playQuizSnippet(track: TrackMetadata) {
        viewModelScope.launch {
            activeSource().playTrack(track)
        }
    }

    val geminiApiKeyState: StateFlow<String> = accountManager.geminiApiKeyState

    fun saveGeminiApiKey(apiKey: String) {
        accountManager.setGeminiApiKey(apiKey)
    }

    val hasCompletedOnboardingState: StateFlow<Boolean> = accountManager.hasCompletedOnboardingState

    fun completeOnboarding() {
        accountManager.setHasCompletedOnboarding(true)
        if (currentMenuState.value.id == "onboarding_menu") {
            navigationManager.onMenuButtonClicked()
        }
    }

    private fun isGameMenu(id: String): Boolean {
        return id in setOf(
            "brick_breaker_stub", "snake_stub", "solitaire_stub", "parachute_stub", "quiz_stub",
            "clock_menu", "calendar_menu", "recorder_menu", "camera_menu", "gemini_chat_menu",
            "onboarding_menu", "main_search_trigger", "ytdlp_search_trigger"
        )
    }

    val selectedThemeState: StateFlow<ThemePreset> = themeRepository.selectedThemeFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemePreset.SPACE_GRAY
    )

    val clickSoundEnabledState: StateFlow<Boolean> = themeRepository.clickSoundEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val playlistRepository = PlaylistRepository(application)
    private val _isFavoriteState = MutableStateFlow(false)
    val isFavoriteState: StateFlow<Boolean> = _isFavoriteState.asStateFlow()

    val currentMenuState: StateFlow<MenuState> = navigationManager.navigationStack.mapStateFlow { stack ->
        stack.last()
    }

    private var lyricsJob: Job? = null
    private var favoriteJob: Job? = null

    init {
        tiltSensorManager.start()

        ytDlpSource.onTrackPlayedListener = { track ->
            viewModelScope.launch {
                playlistRepository.addListenedYtDlpTrack(track)
            }
        }

        viewModelScope.launch {
            playlistRepository.listenedYtDlpTracksFlow.collect { listenedTracks ->
                ytDlpSource.setListenedTracks(listenedTracks)
                refreshYtDlpSubmenus()
            }
        }

        viewModelScope.launch {
            val completed = hasCompletedOnboardingState.first()
            if (!completed && currentMenuState.value.id == "root") {
                navigationManager.navigateToOnboarding()
            }
        }

        viewModelScope.launch {
            playlistRepository.initDefaultPlaylists()
            playlistRepository.playlistsFlow.collect { playlists ->
                refreshPlaylistsMenu(playlists)
            }
        }

        viewModelScope.launch {
            selectedThemeState.collect {
                refreshPresetsMenu()
                refreshSettingsMenu()
            }
        }
        viewModelScope.launch {
            clickSoundEnabledState.collect {
                refreshSettingsMenu()
            }
        }
        viewModelScope.launch {
            activeSourceState.collect { source ->
                audioEngine.setSourceType(source)
                refreshMusicSubmenus()
                refreshAudioSourcesMenu()
                refreshSettingsMenu()
            }
        }
        viewModelScope.launch {
            accountManager.userSettingsRepository.safFolderUriFlow.collect {
                refreshFilesMenu()
            }
        }
        viewModelScope.launch {
            ytdlpResolverEnabledState.collect {
                refreshAudioSourcesMenu()
                refreshSettingsMenu()
            }
        }
        viewModelScope.launch {
            nowPlayingState
                .map { it.currentTrack }
                .distinctUntilChanged { old, new ->
                    old?.id == new?.id && old?.title == new?.title && old?.artist == new?.artist
                }
                .collect { track ->
                    lyricsJob?.cancel()
                    favoriteJob?.cancel()

                    if (track != null) {
                        _lyricsState.value = null
                        _isLoadingLyricsState.value = true
                        refreshNowPlayingMenu(track)

                        // Automatically record listened yt-dlp tracks into history library
                        if (track.sourceType == PlaybackSourceType.YTDLP || track.id.startsWith("ytdlp_")) {
                            viewModelScope.launch {
                                playlistRepository.addListenedYtDlpTrack(track)
                            }
                        }

                        lyricsJob = viewModelScope.launch(Dispatchers.IO) {
                            val result = lyricsRepository.fetchLyrics(
                                title = track.title,
                                artist = track.artist,
                                album = track.album,
                                durationMs = track.durationMs
                            )
                            _lyricsState.value = result
                            _isLoadingLyricsState.value = false
                        }

                        favoriteJob = viewModelScope.launch {
                            playlistRepository.isFavoriteFlow(track.id).collect { fav ->
                                _isFavoriteState.value = fav
                                refreshNowPlayingMenu(track)
                            }
                        }
                    } else {
                        _lyricsState.value = null
                        _isLoadingLyricsState.value = false
                        _isFavoriteState.value = false
                        refreshNowPlayingMenu(null)
                    }
                }
        }

        refreshRootMenu()
        refreshMusicSubmenus()
        refreshYtDlpSubmenus()
        refreshSignInMenu()
        refreshAudioSourcesMenu()
        refreshPresetsMenu()
        refreshSettingsMenu()
    }

    private fun refreshRootMenu() {
        val rootState = navigationManager.getMenu("root") ?: return
        val updatedItems = rootState.items.map { item ->
            if (item.id == "shuffle") {
                item.copy(onSelectAction = { shuffleAllSongs() })
            } else item
        }
        navigationManager.registerMenu(rootState.copy(items = updatedItems))
    }

    fun shuffleAllSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            val source = activeSource()
            val allTracks = source.getTracks()
            val validTracks = allTracks.filter { trk ->
                trk.title.isNotBlank() &&
                !trk.artist.contains("Unknown", ignoreCase = true) &&
                !trk.album.contains("Unknown", ignoreCase = true)
            }.ifEmpty {
                allTracks.ifEmpty { quizTracksState.value }
            }

            if (validTracks.isNotEmpty()) {
                val shuffled = validTracks.shuffled()
                source.playQueue(shuffled, 0)
                withContext(Dispatchers.Main) {
                    navigationManager.navigateToNowPlaying()
                }
            }
        }
    }

    fun openQueueMenu() {
        val state = nowPlayingState.value
        val queue = state.queue
        val currentIdx = state.queueIndex

        val items = if (queue.isEmpty()) {
            listOf(
                MenuItem(
                    id = "queue_empty",
                    title = "Queue is Empty",
                    subtitle = "Select music to play",
                    hasSubMenu = false
                )
            )
        } else {
            queue.mapIndexed { idx, trk ->
                val isCurrent = idx == currentIdx
                MenuItem(
                    id = "queue_item_${trk.id}_$idx",
                    title = trk.title,
                    subtitle = "${trk.artist} • ${if (isCurrent) "▶ Playing (#${idx + 1})" else "#${idx + 1}"}",
                    hasSubMenu = false,
                    onSelectAction = {
                        viewModelScope.launch {
                            audioEngine.playQueue(queue, idx)
                            navigationManager.navigateToNowPlaying()
                        }
                    }
                )
            }
        }

        navigationManager.pushMenu(
            MenuState(
                id = "queue_menu",
                title = "Up Next (${queue.size})",
                items = items,
                selectedIndex = currentIdx.coerceIn(0, (items.size - 1).coerceAtLeast(0))
            )
        )
    }

    fun activeSource(): PlaybackSource {
        return when (activeSourceState.value) {
            PlaybackSourceType.LOCAL -> localSource
            PlaybackSourceType.FILES -> fileExplorerSource
            PlaybackSourceType.SPOTIFY -> spotifySource
            PlaybackSourceType.YOUTUBE -> youTubeSource
            PlaybackSourceType.YTDLP -> ytDlpSource
            PlaybackSourceType.APPLE_MUSIC -> appleMusicSource
        }
    }

    fun onSafFolderPicked(uriString: String) {
        viewModelScope.launch(Dispatchers.IO) {
            accountManager.userSettingsRepository.setSafFolderUri(uriString)
            refreshFilesMenu()
        }
    }

    fun playAlbumByInfo(album: AlbumInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            val source = if (album.id.startsWith("ytdlp_")) ytDlpSource else activeSource()
            val albumTracks = source.getTracksForAlbum(album.id)
            if (albumTracks.isNotEmpty()) {
                source.playQueue(albumTracks, 0)
                withContext(Dispatchers.Main) {
                    navigationManager.navigateToNowPlaying()
                }
            }
        }
    }

    private fun refreshMusicSubmenus() {
        viewModelScope.launch(Dispatchers.IO) {
            val source = activeSource()

            // 1. All Songs
            val tracks = source.getTracks()
            _quizTracksState.value = tracks
            val songItems = if (tracks.isEmpty()) {
                listOf(
                    MenuItem(
                        id = "no_songs",
                        title = "No Songs Found",
                        subtitle = "Source: ${source.sourceType.displayName}",
                        hasSubMenu = false,
                        rightPane = RightPaneContent.ActionPreview("Music", "No audio tracks found in ${source.sourceType.displayName}.")
                    )
                )
            } else {
                tracks.mapIndexed { index, track ->
                    MenuItem(
                        id = "song_${track.id}",
                        title = track.title,
                        subtitle = "${track.artist} • ${track.album}",
                        hasSubMenu = false,
                        rightPane = RightPaneContent.MusicCategory(track.album),
                        onSelectAction = {
                            viewModelScope.launch {
                                source.playQueue(tracks, index)
                                navigationManager.navigateToNowPlaying()
                            }
                        }
                    )
                }
            }
            navigationManager.registerMenu(
                MenuState(id = "songs_menu", title = "Songs", items = songItems)
            )

            // 2. Artists
            val artists = source.getArtists()
            val artistItems = if (artists.isEmpty()) {
                listOf(
                    MenuItem("no_artists", "No Artists", subtitle = "Source: ${source.sourceType.displayName}", hasSubMenu = false)
                )
            } else {
                artists.map { artist ->
                    MenuItem(
                        id = "artist_$artist",
                        title = artist,
                        hasSubMenu = true,
                        rightPane = RightPaneContent.MusicCategory(artist),
                        onSelectAction = {
                            viewModelScope.launch {
                                val artistTracks = source.getTracksForArtist(artist)
                                val subItems = artistTracks.mapIndexed { idx, trk ->
                                    MenuItem(
                                        id = "art_trk_${trk.id}",
                                        title = trk.title,
                                        subtitle = trk.album,
                                        hasSubMenu = false,
                                        onSelectAction = {
                                            viewModelScope.launch {
                                                source.playQueue(artistTracks, idx)
                                                navigationManager.navigateToNowPlaying()
                                            }
                                        }
                                    )
                                }
                                navigationManager.pushMenu(
                                    MenuState(id = "artist_tracks_$artist", title = artist, items = subItems)
                                )
                            }
                        }
                    )
                }
            }
            navigationManager.registerMenu(
                MenuState(id = "artists_menu", title = "Artists", items = artistItems)
            )

            // 3. Albums (pure local/active library, no ytdlp blending in normal music submenus)
            val sourceAlbums = source.getAlbums()
            _coverFlowAlbumsState.value = sourceAlbums
            val albumItems = if (sourceAlbums.isEmpty()) {
                listOf(
                    MenuItem("no_albums", "No Albums", subtitle = "Source: ${source.sourceType.displayName}", hasSubMenu = false)
                )
            } else {
                sourceAlbums.map { album ->
                    MenuItem(
                        id = "album_${album.id}",
                        title = album.name,
                        subtitle = "${album.artist} • ${album.trackCount} Songs",
                        hasSubMenu = true,
                        rightPane = RightPaneContent.MusicCategory(album.name),
                        onSelectAction = {
                            viewModelScope.launch {
                                val albumTracks = source.getTracksForAlbum(album.id)
                                val subItems = albumTracks.mapIndexed { idx, trk ->
                                    MenuItem(
                                        id = "alb_trk_${trk.id}",
                                        title = trk.title,
                                        subtitle = trk.artist,
                                        hasSubMenu = false,
                                        onSelectAction = {
                                            viewModelScope.launch {
                                                source.playQueue(albumTracks, idx)
                                                navigationManager.navigateToNowPlaying()
                                            }
                                        }
                                    )
                                }
                                navigationManager.pushMenu(
                                    MenuState(id = "album_tracks_${album.id}", title = album.name, items = subItems)
                                )
                            }
                        }
                    )
                }
            }
            navigationManager.registerMenu(
                MenuState(id = "albums_menu", title = "Albums", items = albumItems)
            )

            // 4. Genres
            val genres = source.getGenres()
            val genreItems = if (genres.isEmpty()) {
                listOf(
                    MenuItem("no_genres", "No Genres", subtitle = "Source: ${source.sourceType.displayName}", hasSubMenu = false)
                )
            } else {
                genres.map { genre ->
                    MenuItem(
                        id = "genre_$genre",
                        title = genre,
                        hasSubMenu = true,
                        rightPane = RightPaneContent.MusicCategory(genre)
                    )
                }
            }
            navigationManager.registerMenu(
                MenuState(id = "genres_menu", title = "Genres", items = genreItems)
            )
        }
    }

    fun refreshYtDlpSubmenus() {
        viewModelScope.launch(Dispatchers.IO) {
            val tracks = ytDlpSource.getTracks()
            val albums = ytDlpSource.getAlbums()
            val artists = ytDlpSource.getArtists()
            val playlists = ytDlpSource.getPlaylists()

            _ytdlpCoverFlowAlbumsState.value = albums

            // 1. Songs
            val songItems = if (tracks.isEmpty()) {
                listOf(
                    MenuItem(
                        id = "empty_ytdlp_songs",
                        title = "No Songs Available",
                        subtitle = "Use Voice Search or Top Songs",
                        hasSubMenu = false,
                        rightPane = RightPaneContent.ActionPreview("yt-dlp", "Search or fetch Top Songs to add tracks.")
                    )
                )
            } else {
                tracks.mapIndexed { idx, trk ->
                    MenuItem(
                        id = "ytdlp_trk_${trk.id}",
                        title = trk.title,
                        subtitle = "${trk.artist} • ${trk.album}",
                        hasSubMenu = false,
                        onSelectAction = {
                            viewModelScope.launch {
                                ytDlpSource.playQueue(tracks, idx)
                                navigationManager.navigateToNowPlaying()
                            }
                        }
                    )
                }
            }
            navigationManager.registerMenu(
                MenuState(id = "ytdlp_songs_menu", title = "yt-dlp Songs", items = songItems)
            )

            // 2. Playlists
            val playlistItems = if (playlists.isEmpty()) {
                listOf(
                    MenuItem(
                        id = "empty_ytdlp_pl",
                        title = "No Playlists Available",
                        subtitle = "Use Voice Search or Top Songs",
                        hasSubMenu = false
                    )
                )
            } else {
                playlists.map { pl ->
                    MenuItem(
                        id = "ytdlp_pl_${pl.id}",
                        title = pl.name,
                        subtitle = "${pl.trackCount} Songs",
                        hasSubMenu = true,
                        onSelectAction = {
                            viewModelScope.launch {
                                val plTracks = ytDlpSource.getTracksForPlaylist(pl.id)
                                val subItems = plTracks.mapIndexed { idx, trk ->
                                    MenuItem(
                                        id = "ytdlp_pl_trk_${trk.id}",
                                        title = trk.title,
                                        subtitle = trk.artist,
                                        hasSubMenu = false,
                                        onSelectAction = {
                                            viewModelScope.launch {
                                                ytDlpSource.playQueue(plTracks, idx)
                                                navigationManager.navigateToNowPlaying()
                                            }
                                        }
                                    )
                                }
                                navigationManager.pushMenu(
                                    MenuState(id = "ytdlp_pl_items_${pl.id}", title = pl.name, items = subItems)
                                )
                            }
                        }
                    )
                }
            }
            navigationManager.registerMenu(
                MenuState(id = "ytdlp_playlists_menu", title = "yt-dlp Playlists", items = playlistItems)
            )

            // 3. Albums
            val albumItems = if (albums.isEmpty()) {
                listOf(
                    MenuItem(
                        id = "empty_ytdlp_alb",
                        title = "No Albums Available",
                        subtitle = "Use Voice Search or Top Songs",
                        hasSubMenu = false
                    )
                )
            } else {
                albums.map { alb ->
                    MenuItem(
                        id = "ytdlp_alb_${alb.id}",
                        title = alb.name,
                        subtitle = "${alb.artist} • ${alb.trackCount} Tracks",
                        hasSubMenu = true,
                        onSelectAction = {
                            viewModelScope.launch {
                                val albTracks = ytDlpSource.getTracksForAlbum(alb.id)
                                val subItems = albTracks.mapIndexed { idx, trk ->
                                    MenuItem(
                                        id = "ytdlp_alb_trk_${trk.id}",
                                        title = trk.title,
                                        subtitle = trk.artist,
                                        hasSubMenu = false,
                                        onSelectAction = {
                                            viewModelScope.launch {
                                                ytDlpSource.playQueue(albTracks, idx)
                                                navigationManager.navigateToNowPlaying()
                                            }
                                        }
                                    )
                                }
                                navigationManager.pushMenu(
                                    MenuState(id = "ytdlp_alb_items_${alb.id}", title = alb.name, items = subItems)
                                )
                            }
                        }
                    )
                }
            }
            navigationManager.registerMenu(
                MenuState(id = "ytdlp_albums_menu", title = "yt-dlp Albums", items = albumItems)
            )

            // 4. Artists
            val artistItems = if (artists.isEmpty()) {
                listOf(
                    MenuItem(
                        id = "empty_ytdlp_art",
                        title = "No Artists Available",
                        subtitle = "Use Voice Search or Top Songs",
                        hasSubMenu = false
                    )
                )
            } else {
                artists.map { art ->
                    MenuItem(
                        id = "ytdlp_art_$art",
                        title = art,
                        hasSubMenu = true,
                        onSelectAction = {
                            viewModelScope.launch {
                                val artTracks = ytDlpSource.getTracksForArtist(art)
                                val subItems = artTracks.mapIndexed { idx, trk ->
                                    MenuItem(
                                        id = "ytdlp_art_trk_${trk.id}",
                                        title = trk.title,
                                        subtitle = trk.album,
                                        hasSubMenu = false,
                                        onSelectAction = {
                                            viewModelScope.launch {
                                                ytDlpSource.playQueue(artTracks, idx)
                                                navigationManager.navigateToNowPlaying()
                                            }
                                        }
                                    )
                                }
                                navigationManager.pushMenu(
                                    MenuState(id = "ytdlp_art_items_$art", title = art, items = subItems)
                                )
                            }
                        }
                    )
                }
            }
            navigationManager.registerMenu(
                MenuState(id = "ytdlp_artists_menu", title = "yt-dlp Artists", items = artistItems)
            )

            // Update main yt-dlp menu subtitles and trigger action
            val ytdlpMenu = navigationManager.getMenu("ytdlp_menu")
            if (ytdlpMenu != null) {
                val updatedYtdlpItems = ytdlpMenu.items.map { item ->
                    when (item.id) {
                        "ytdlp_songs" -> item.copy(subtitle = "${tracks.size} Songs")
                        "ytdlp_playlists" -> item.copy(subtitle = "${playlists.size} Playlists")
                        "ytdlp_albums" -> item.copy(subtitle = "${albums.size} Albums")
                        "ytdlp_artists" -> item.copy(subtitle = "${artists.size} Artists")
                        "ytdlp_top_songs" -> item.copy(onSelectAction = { fetchYtDlpTopSongs() })
                        else -> item
                    }
                }
                navigationManager.registerMenu(ytdlpMenu.copy(items = updatedYtdlpItems))
            }
        }
    }

    fun onVoiceSearchMain(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val tracks = localSource.getTracks()
            val matchingTracks = tracks.filter { trk ->
                trk.title.contains(query, ignoreCase = true) ||
                trk.artist.contains(query, ignoreCase = true) ||
                trk.album.contains(query, ignoreCase = true)
            }

            val items = if (matchingTracks.isEmpty()) {
                listOf(
                    MenuItem(
                        id = "no_search_results",
                        title = "No Local Songs Found",
                        subtitle = "Query: '$query'",
                        hasSubMenu = false
                    )
                )
            } else {
                matchingTracks.mapIndexed { idx, trk ->
                    MenuItem(
                        id = "srch_trk_${trk.id}",
                        title = trk.title,
                        subtitle = "${trk.artist} • ${trk.album}",
                        hasSubMenu = false,
                        onSelectAction = {
                            viewModelScope.launch {
                                localSource.playQueue(matchingTracks, idx)
                                navigationManager.navigateToNowPlaying()
                            }
                        }
                    )
                }
            }

            withContext(Dispatchers.Main) {
                navigationManager.pushMenu(
                    MenuState(
                        id = "main_search_results",
                        title = "Search: '$query'",
                        items = items
                    )
                )
            }
        }
    }

    fun onVoiceSearchYtDlp(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val results = ytDlpSource.search(query)
            refreshYtDlpSubmenus()

            val items = if (results.isEmpty()) {
                listOf(
                    MenuItem(
                        id = "no_ytdlp_results",
                        title = "No YouTube Results",
                        subtitle = "Query: '$query'",
                        hasSubMenu = false
                    )
                )
            } else {
                results.mapIndexed { idx, trk ->
                    MenuItem(
                        id = "ytdlp_srch_trk_${trk.id}",
                        title = trk.title,
                        subtitle = "${trk.artist} • ${trk.album}",
                        hasSubMenu = false,
                        onSelectAction = {
                            viewModelScope.launch {
                                ytDlpSource.playQueue(results, idx)
                                navigationManager.navigateToNowPlaying()
                            }
                        }
                    )
                }
            }

            withContext(Dispatchers.Main) {
                navigationManager.pushMenu(
                    MenuState(
                        id = "ytdlp_search_results",
                        title = "yt-dlp: '$query'",
                        items = items
                    )
                )
            }
        }
    }

    fun fetchYtDlpTopSongs() {
        viewModelScope.launch(Dispatchers.IO) {
            val topSongs = ytDlpSource.fetchTopSongs()
            refreshYtDlpSubmenus()

            val items = topSongs.mapIndexed { idx, trk ->
                MenuItem(
                    id = "ytdlp_top_trk_${trk.id}",
                    title = trk.title,
                    subtitle = "${trk.artist} • ${trk.album}",
                    hasSubMenu = false,
                    onSelectAction = {
                        viewModelScope.launch {
                            ytDlpSource.playQueue(topSongs, idx)
                            navigationManager.navigateToNowPlaying()
                        }
                    }
                )
            }

            withContext(Dispatchers.Main) {
                navigationManager.pushMenu(
                    MenuState(
                        id = "ytdlp_top_songs_list",
                        title = "YouTube Top Songs",
                        items = items
                    )
                )
            }
        }
    }

    private fun refreshFilesMenu() {
        viewModelScope.launch(Dispatchers.IO) {
            val uriStr = accountManager.userSettingsRepository.safFolderUriFlow.firstOrNull()
            val fileItems = mutableListOf<MenuItem>()

            // Folder Picker button at top
            fileItems.add(
                MenuItem(
                    id = "pick_saf_folder",
                    title = "📁 Pick Storage Folder...",
                    subtitle = if (uriStr != null) "Folder Selected" else "Tap to grant access",
                    hasSubMenu = false,
                    rightPane = RightPaneContent.ActionPreview("Storage", "Select a folder via Storage Access Framework."),
                    onSelectAction = {
                        viewModelScope.launch {
                            _launchSafFolderPickerEvent.emit(Unit)
                        }
                    }
                )
            )

            if (uriStr != null) {
                val directoryItems = safExplorer.listDirectory(uriStr.toUri())
                val audioTracksInDir = directoryItems.filterIsInstance<SafItem.AudioFile>().map { it.track }

                for (safItem in directoryItems) {
                    when (safItem) {
                        is SafItem.Folder -> {
                            fileItems.add(
                                MenuItem(
                                    id = "saf_dir_${safItem.name}",
                                    title = "📁 ${safItem.name}",
                                    subtitle = "${safItem.itemCount} items",
                                    hasSubMenu = true,
                                    rightPane = RightPaneContent.ActionPreview("Directory", safItem.name),
                                    onSelectAction = {
                                        loadSafSubdirectory(safItem.uri)
                                    }
                                )
                            )
                        }
                        is SafItem.AudioFile -> {
                            fileItems.add(
                                MenuItem(
                                    id = "saf_file_${safItem.name}",
                                    title = "🎵 ${safItem.track.title}",
                                    subtitle = safItem.name,
                                    hasSubMenu = false,
                                    rightPane = RightPaneContent.ActionPreview("Audio File", safItem.name),
                                    onSelectAction = {
                                        viewModelScope.launch {
                                            fileExplorerSource.playQueue(audioTracksInDir, audioTracksInDir.indexOf(safItem.track).coerceAtLeast(0))
                                            navigationManager.navigateToNowPlaying()
                                        }
                                    }
                                )
                            )
                        }
                    }
                }
            }

            navigationManager.registerMenu(
                MenuState(id = "files_menu", title = "Files", items = fileItems)
            )
        }
    }

    private fun loadSafSubdirectory(dirUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val directoryItems = safExplorer.listDirectory(dirUri)
            val audioTracksInDir = directoryItems.filterIsInstance<SafItem.AudioFile>().map { it.track }
            val folderName = dirUri.lastPathSegment ?: "Folder"

            val subItems = directoryItems.map { item ->
                when (item) {
                    is SafItem.Folder -> {
                        MenuItem(
                            id = "saf_sub_${item.name}",
                            title = "📁 ${item.name}",
                            subtitle = "${item.itemCount} items",
                            hasSubMenu = true,
                            onSelectAction = {
                                loadSafSubdirectory(item.uri)
                            }
                        )
                    }
                    is SafItem.AudioFile -> {
                        MenuItem(
                            id = "saf_file_${item.name}",
                            title = "🎵 ${item.track.title}",
                            subtitle = item.name,
                            hasSubMenu = false,
                            onSelectAction = {
                                viewModelScope.launch {
                                    fileExplorerSource.playQueue(audioTracksInDir, audioTracksInDir.indexOf(item.track).coerceAtLeast(0))
                                    navigationManager.navigateToNowPlaying()
                                }
                            }
                        )
                    }
                }
            }

            withContext(Dispatchers.Main) {
                navigationManager.pushMenu(
                    MenuState(id = "saf_dir_${dirUri.hashCode()}", title = folderName, items = subItems)
                )
            }
        }
    }

    private fun refreshSignInMenu() {
        val spotifyAuth = accountManager.spotifyAuthProvider
        val youtubeAuth = accountManager.youtubeAuthProvider
        val appleAuth = accountManager.appleMusicAuthProvider

        val items = listOf(
            MenuItem(
                id = "acc_spotify",
                title = "Spotify",
                subtitle = when (val state = spotifyAuth.authState.value) {
                    is AuthState.LoggedIn -> "Signed In as ${state.displayName}"
                    else -> "Tap to Sign In"
                },
                hasSubMenu = false,
                rightPane = RightPaneContent.ActionPreview("Spotify", "Stream Spotify music library"),
                onSelectAction = {
                    viewModelScope.launch {
                        if (spotifyAuth.authState.value is AuthState.LoggedIn) {
                            spotifyAuth.signOut()
                        } else {
                            spotifyAuth.signIn()
                        }
                        refreshSignInMenu()
                    }
                }
            ),
            MenuItem(
                id = "acc_youtube",
                title = "YouTube Music",
                subtitle = when (val state = youtubeAuth.authState.value) {
                    is AuthState.LoggedIn -> "Signed In as ${state.displayName}"
                    else -> "Tap to Sign In"
                },
                hasSubMenu = false,
                rightPane = RightPaneContent.ActionPreview("YouTube Music", "Stream YouTube Music"),
                onSelectAction = {
                    viewModelScope.launch {
                        if (youtubeAuth.authState.value is AuthState.LoggedIn) {
                            youtubeAuth.signOut()
                        } else {
                            youtubeAuth.signIn()
                        }
                        refreshSignInMenu()
                    }
                }
            ),
            MenuItem(
                id = "acc_apple",
                title = "Apple Music",
                subtitle = "Coming Soon (Credentials Required)",
                hasSubMenu = false,
                rightPane = RightPaneContent.ActionPreview("Apple Music", "Apple Music integration - Coming Soon")
            )
        )

        navigationManager.registerMenu(
            MenuState(id = "signin_menu", title = "Accounts", items = items)
        )
    }

    private fun refreshAudioSourcesMenu() {
        val currentSource = activeSourceState.value
        val isYtdlp = ytdlpResolverEnabledState.value
        val sources = PlaybackSourceType.entries

        val items = mutableListOf<MenuItem>()

        sources.forEach { src ->
            val isSupported = src in setOf(
                PlaybackSourceType.LOCAL,
                PlaybackSourceType.FILES,
                PlaybackSourceType.YTDLP
            )
            val isSelected = src == currentSource
            val radioSymbol = if (isSelected) "(●) " else "(○) "

            val sub = when {
                isSelected -> "Active Playback Source"
                !isSupported && src == PlaybackSourceType.SPOTIFY -> "Disabled (Spotify App Remote Required)"
                !isSupported && src == PlaybackSourceType.YOUTUBE -> "Disabled (YouTube Data API Key Required)"
                !isSupported && src == PlaybackSourceType.APPLE_MUSIC -> "Disabled (MusicKit Token Required)"
                else -> "Tap to select"
            }

            items.add(
                MenuItem(
                    id = "src_${src.name}",
                    title = "$radioSymbol${src.displayName}",
                    subtitle = sub,
                    hasSubMenu = false,
                    isEnabled = isSupported,
                    rightPane = RightPaneContent.ActionPreview("Audio Source", src.displayName),
                    onSelectAction = {
                        accountManager.setActiveSource(src)
                        refreshAudioSourcesMenu()
                        refreshSettingsMenu()
                    }
                )
            )
        }

        val ytdlpCheck = if (isYtdlp) "[x] " else "[ ] "
        items.add(
            MenuItem(
                id = "ytdlp_toggle",
                title = "${ytdlpCheck}yt-dlp Stream Resolver",
                subtitle = if (isYtdlp) "Status: Enabled (User Library Streams)" else "Status: Disabled",
                hasSubMenu = false,
                rightPane = RightPaneContent.ActionPreview("yt-dlp", "Direct YouTube audio stream extraction"),
                onSelectAction = {
                    accountManager.setYtdlpResolverEnabled(!isYtdlp)
                    refreshAudioSourcesMenu()
                    refreshSettingsMenu()
                }
            )
        )

        navigationManager.registerMenu(
            MenuState(id = "audio_sources_menu", title = "Audio Sources", items = items)
        )
    }

    private fun refreshPresetsMenu() {
        val currentTheme = selectedThemeState.value
        val items = ThemePreset.entries.map { preset ->
            val isSelected = preset == currentTheme
            val radioSymbol = if (isSelected) "(●) " else "(○) "
            MenuItem(
                id = "preset_${preset.id}",
                title = "$radioSymbol${preset.displayName}",
                subtitle = if (isSelected) "Active Theme Preset" else "Tap to apply theme",
                hasSubMenu = false,
                rightPane = RightPaneContent.ThemePreview(preset),
                presetToSelect = preset
            )
        }
        navigationManager.registerMenu(
            MenuState(id = "presets_menu", title = "Theme Presets", items = items)
        )
    }

    private fun refreshSettingsMenu() {
        val currentSource = activeSourceState.value
        val clickSound = clickSoundEnabledState.value
        val currentTheme = selectedThemeState.value

        val items = listOf(
            MenuItem(
                id = "set_themes",
                title = "Theme Presets",
                subtitle = currentTheme.displayName,
                hasSubMenu = true,
                rightPane = RightPaneContent.ActionPreview("Themes", "Customize hardware chassis color"),
                targetMenuId = "presets_menu"
            ),
            MenuItem(
                id = "set_audio_sources",
                title = "Audio Sources & Resolvers",
                subtitle = "Active: ${currentSource.displayName}",
                hasSubMenu = true,
                rightPane = RightPaneContent.ActionPreview("Audio Sources", "Select playback source"),
                targetMenuId = "audio_sources_menu"
            ),
            MenuItem(
                id = "set_click_sound",
                title = "Click Wheel Sound Effects",
                subtitle = if (clickSound) "Status: On (mp3)" else "Status: Muted",
                hasSubMenu = false,
                rightPane = RightPaneContent.ActionPreview("Audio Click", "Tactile click sound feedback"),
                onSelectAction = {
                    viewModelScope.launch {
                        themeRepository.setClickSoundEnabled(!clickSound)
                    }
                }
            ),
            MenuItem(
                id = "set_about",
                title = "About MyPod",
                subtitle = "v1.0.0 • Developer: Trishit Majumdar",
                hasSubMenu = true,
                rightPane = RightPaneContent.ExternalLinkPreview("Developer Website",  url = "https://trishit.me"),
                intentUrl = "https://trishit.me"
            )
        )

        navigationManager.registerMenu(
            MenuState(id = "settings_menu", title = "Settings", items = items)
        )
    }

    fun toggleFavoriteCurrentTrack() {
        val track = nowPlayingState.value.currentTrack ?: return
        viewModelScope.launch {
            val newFav = playlistRepository.toggleFavorite(track)
            _isFavoriteState.value = newFav
            refreshNowPlayingMenu(track)
        }
    }

    private fun refreshNowPlayingMenu(track: TrackMetadata?) {
        val items = mutableListOf<MenuItem>()

        if (track != null) {
            val isFav = _isFavoriteState.value
            items.add(
                MenuItem(
                    id = "np_view_queue",
                    title = "📜 View Queue / Up Next",
                    subtitle = "${nowPlayingState.value.queue.size} Songs in Queue",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Queue", "View upcoming tracks in queue"),
                    onSelectAction = {
                        openQueueMenu()
                    }
                )
            )

            items.add(
                MenuItem(
                    id = "np_favorite",
                    title = if (isFav) "❤️ Liked (Favorited)" else "🤍 Like Track",
                    subtitle = if (isFav) "Tap to remove from Favorites" else "Tap to add to Favorites",
                    hasSubMenu = false,
                    rightPane = RightPaneContent.ActionPreview("Favorites", track.title),
                    onSelectAction = {
                        toggleFavoriteCurrentTrack()
                    }
                )
            )

            items.add(
                MenuItem(
                    id = "np_add_to_playlist",
                    title = "➕ Add to Playlist...",
                    subtitle = "Save track to a custom playlist",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Playlists", track.title),
                    onSelectAction = {
                        openAddToPlaylistMenu(track)
                    }
                )
            )

            items.add(
                MenuItem(
                    id = "np_current_track",
                    title = track.title,
                    subtitle = "${track.artist} • ${track.album}",
                    hasSubMenu = false,
                    rightPane = RightPaneContent.DefaultArtwork,
                    onSelectAction = {
                        audioEngine.togglePlayPause()
                    }
                )
            )
        } else {
            items.add(
                MenuItem(
                    id = "np_none",
                    title = "No Track Playing",
                    subtitle = "Select music to play",
                    hasSubMenu = false,
                    rightPane = RightPaneContent.DefaultArtwork
                )
            )
        }

        navigationManager.registerMenu(
            MenuState(id = "now_playing_menu", title = "Now Playing", items = items)
        )
    }

    private fun openAddToPlaylistMenu(track: TrackMetadata) {
        viewModelScope.launch {
            val playlists = withContext(Dispatchers.IO) { playlistRepository.playlistsFlow.firstOrNull() } ?: emptyList()
            val subItems = mutableListOf<MenuItem>()

            subItems.add(
                MenuItem(
                    id = "add_pl_create_new",
                    title = "➕ Create New Playlist",
                    hasSubMenu = false,
                    onSelectAction = {
                        viewModelScope.launch {
                            val count = playlists.size
                            val newId = playlistRepository.createPlaylist("Playlist ${count + 1}")
                            playlistRepository.addTrackToPlaylist(newId, track)
                            navigationManager.navigateToNowPlaying()
                        }
                    }
                )
            )

            for (pl in playlists) {
                subItems.add(
                    MenuItem(
                        id = "add_to_pl_${pl.id}",
                        title = pl.name,
                        hasSubMenu = false,
                        onSelectAction = {
                            viewModelScope.launch {
                                playlistRepository.addTrackToPlaylist(pl.id, track)
                                navigationManager.navigateToNowPlaying()
                            }
                        }
                    )
                )
            }

            navigationManager.pushMenu(
                MenuState(id = "select_playlist_add", title = "Add to Playlist", items = subItems)
            )
        }
    }

    private fun playPlaylistTrack(tracks: List<TrackMetadata>, index: Int) {
        if (tracks.isEmpty()) return
        val startTrack = tracks[index.coerceIn(tracks.indices)]
        viewModelScope.launch {
            if (startTrack.sourceType == PlaybackSourceType.YTDLP || startTrack.id.startsWith("ytdlp_")) {
                ytDlpSource.playQueue(tracks, index)
            } else if (startTrack.sourceType == PlaybackSourceType.FILES) {
                fileExplorerSource.playQueue(tracks, index)
            } else {
                localSource.playQueue(tracks, index)
            }
            withContext(Dispatchers.Main) {
                navigationManager.navigateToNowPlaying()
            }
        }
    }

    private fun refreshPlaylistsMenu(playlists: List<PlaylistEntity>) {
        viewModelScope.launch {
            val items = mutableListOf<MenuItem>()

            items.add(
                MenuItem(
                    id = "create_new_playlist",
                    title = "➕ New Playlist",
                    subtitle = "Create custom playlist",
                    hasSubMenu = false,
                    rightPane = RightPaneContent.ActionPreview("Playlists", "Create new custom playlist"),
                    onSelectAction = {
                        viewModelScope.launch {
                            val count = playlists.size
                            playlistRepository.createPlaylist("Playlist ${count + 1}")
                        }
                    }
                )
            )

            for (pl in playlists) {
                val plTracks = withContext(Dispatchers.IO) { playlistRepository.getTracksForPlaylist(pl.id) }
                items.add(
                    MenuItem(
                        id = "pl_${pl.id}",
                        title = pl.name,
                        subtitle = "${plTracks.size} Songs",
                        hasSubMenu = true,
                        rightPane = RightPaneContent.MusicCategory(pl.name),
                        onSelectAction = {
                            viewModelScope.launch {
                                val currentPlTracks = withContext(Dispatchers.IO) { playlistRepository.getTracksForPlaylist(pl.id) }
                                val trackItems = if (currentPlTracks.isEmpty()) {
                                    listOf(
                                        MenuItem(
                                            id = "empty_pl_${pl.id}",
                                            title = "No Tracks in Playlist",
                                            subtitle = "Add songs from Now Playing",
                                            hasSubMenu = false
                                        )
                                    )
                                } else {
                                    currentPlTracks.mapIndexed { idx, trk ->
                                        MenuItem(
                                            id = "pl_trk_${trk.id}_$idx",
                                            title = trk.title,
                                            subtitle = "${trk.artist} • ${trk.album}",
                                            hasSubMenu = false,
                                            onSelectAction = {
                                                playPlaylistTrack(currentPlTracks, idx)
                                            }
                                        )
                                    }
                                }

                                navigationManager.pushMenu(
                                    MenuState(id = "playlist_content_${pl.id}", title = pl.name, items = trackItems)
                                )
                            }
                        }
                    )
                )
            }

            navigationManager.registerMenu(
                MenuState(id = "playlists_menu", title = "Playlists", items = items)
            )
        }
    }

    fun onWheelEvent(event: WheelEvent) {
        gameWheelEvents.tryEmit(event)
        when (event) {
            is WheelEvent.Scroll -> onScrollDetents(event.detents)
            is WheelEvent.PrevPress -> if (event.isHold) onPrevHold() else onPrevClick(null)
            is WheelEvent.NextPress -> if (event.isHold) onNextHold() else onNextClick(null)
            is WheelEvent.PlayPausePress -> onPlayPauseClick(null)
            is WheelEvent.MenuPress -> {
                val currentId = currentMenuState.value.id
                if (currentId !in setOf("solitaire_stub", "clock_menu", "calendar_menu", "recorder_menu", "camera_menu", "gemini_chat_menu", "onboarding_menu", "main_search_trigger", "ytdlp_search_trigger")) {
                    if (event.isHold) {
                        triggerDetentTick(null)
                        navigationManager.popToRoot()
                    } else {
                        onMenuClick(null)
                    }
                }
            }
            is WheelEvent.SelectPress -> onCenterClick(null)
        }
    }

    fun onScrollDetents(detents: Int) {
        val currentId = currentMenuState.value.id
        if (isGameMenu(currentId)) {
            return
        }
        when (currentId) {
            "now_playing_menu" -> {
                audioEngine.seekRelative(detents * 2500L)
            }
            "cover_flow_menu" -> {
                val albums = _coverFlowAlbumsState.value
                val count = if (albums.isEmpty()) 4 else albums.size
                var newIndex = _coverFlowIndexState.value + detents
                while (newIndex < 0) newIndex += count
                _coverFlowIndexState.value = newIndex % count
            }
            "ytdlp_cover_flow_menu" -> {
                val albums = _ytdlpCoverFlowAlbumsState.value
                val count = if (albums.isEmpty()) 4 else albums.size
                var newIndex = _ytdlpCoverFlowIndexState.value + detents
                while (newIndex < 0) newIndex += count
                _ytdlpCoverFlowIndexState.value = newIndex % count
            }
            else -> {
                navigationManager.scrollByDetents(detents)
            }
        }
    }

    fun triggerDetentTick(view: View?) {
        clickSoundPlayer.playClick(view, soundEnabled = clickSoundEnabledState.value)
    }

    fun onCenterClick(view: View?) {
        triggerDetentTick(view)

        val currentId = currentMenuState.value.id
        if (isGameMenu(currentId)) {
            return
        }

        when (currentId) {
            "now_playing_menu" -> {
                audioEngine.togglePlayPause()
                return
            }
            "cover_flow_menu" -> {
                val albums = _coverFlowAlbumsState.value
                if (albums.isNotEmpty()) {
                    val selectedAlbum = albums[_coverFlowIndexState.value.coerceIn(0, albums.lastIndex)]
                    playAlbumByInfo(selectedAlbum)
                } else {
                    navigationManager.navigateToNowPlaying()
                }
                return
            }
            "ytdlp_cover_flow_menu" -> {
                val albums = _ytdlpCoverFlowAlbumsState.value
                if (albums.isNotEmpty()) {
                    val selectedAlbum = albums[_ytdlpCoverFlowIndexState.value.coerceIn(0, albums.lastIndex)]
                    playAlbumByInfo(selectedAlbum)
                } else {
                    navigationManager.navigateToNowPlaying()
                }
                return
            }
        }

        navigationManager.onCenterButtonClicked(
            onThemeSelected = { preset ->
                viewModelScope.launch {
                    themeRepository.setThemePreset(preset)
                }
            },
            onIntentTriggered = { url ->
                try {
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    getApplication<Application>().startActivity(intent)
                } catch (_: Exception) {}
            },
            onComingSoonTriggered = { title ->
                Toast.makeText(
                    getApplication(),
                    "Coming Soon: $title integration requires external credentials.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    fun onMenuClick(view: View?) {
        triggerDetentTick(view)
        navigationManager.onMenuButtonClicked()
    }

    fun onPrevClick(view: View?) {
        triggerDetentTick(view)
        audioEngine.skipToPrevious()
    }

    fun onPrevHold() {
        audioEngine.rewind(5000L)
    }

    fun onNextClick(view: View?) {
        triggerDetentTick(view)
        audioEngine.skipToNext()
    }

    fun onNextHold() {
        audioEngine.fastForward(5000L)
    }

    fun onPlayPauseClick(view: View?) {
        triggerDetentTick(view)
        audioEngine.togglePlayPause()
    }

    override fun onCleared() {
        tiltSensorManager.stop()
        audioEngine.release()
        clickSoundPlayer.release()
    }

    private suspend fun <T> Flow<T>.firstOrNull(): T? {
        var result: T? = null
        try {
            collect { value ->
                result = value
                throw CancellationException()
            }
        } catch (_: Exception) {}
        return result
    }

    private fun <T, R> StateFlow<T>.mapStateFlow(transform: (T) -> R): StateFlow<R> {
        val initial = transform(this.value)
        val mutable = MutableStateFlow(initial)
        viewModelScope.launch {
            this@mapStateFlow.collect { value ->
                mutable.value = transform(value)
            }
        }
        return mutable
    }
}
