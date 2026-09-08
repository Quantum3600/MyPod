package com.trishit.mypod.navigation

import com.trishit.mypod.data.theme.ThemePreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MenuNavigationManager {

    val allMenus: MutableMap<String, MenuState> = createMenuTree().toMutableMap()

    fun getMenu(id: String): MenuState? = allMenus[id]

    // Backstack of MenuState objects
    private val _navigationStack = MutableStateFlow<List<MenuState>>(
        listOf(allMenus["root"] ?: error("Root menu not found"))
    )
    val navigationStack: StateFlow<List<MenuState>> = _navigationStack.asStateFlow()

    val currentMenu: MenuState
        get() = _navigationStack.value.last()

    val selectedItem: MenuItem?
        get() {
            val menu = currentMenu
            if (menu.items.isEmpty()) return null
            val idx = menu.selectedIndex.coerceIn(0, menu.items.lastIndex)
            return menu.items[idx]
        }

    fun scrollUp() {
        val currentStack = _navigationStack.value.toMutableList()
        val lastIdx = currentStack.lastIndex
        if (lastIdx < 0) return

        val activeMenu = currentStack[lastIdx]
        if (activeMenu.items.isEmpty()) return

        val newIndex = if (activeMenu.selectedIndex > 0) {
            activeMenu.selectedIndex - 1
        } else {
            activeMenu.items.lastIndex // Loop around or clamp
        }

        currentStack[lastIdx] = activeMenu.copy(selectedIndex = newIndex)
        _navigationStack.value = currentStack
    }

    fun scrollDown() {
        val currentStack = _navigationStack.value.toMutableList()
        val lastIdx = currentStack.lastIndex
        if (lastIdx < 0) return

        val activeMenu = currentStack[lastIdx]
        if (activeMenu.items.isEmpty()) return

        val newIndex = if (activeMenu.selectedIndex < activeMenu.items.lastIndex) {
            activeMenu.selectedIndex + 1
        } else {
            0 // Loop around
        }

        currentStack[lastIdx] = activeMenu.copy(selectedIndex = newIndex)
        _navigationStack.value = currentStack
    }

    /**
     * Scroll by arbitrary detents count (negative = up, positive = down)
     */
    fun scrollByDetents(detentsCount: Int) {
        if (detentsCount == 0) return
        val currentStack = _navigationStack.value.toMutableList()
        val lastIdx = currentStack.lastIndex
        if (lastIdx < 0) return

        val activeMenu = currentStack[lastIdx]
        val itemCount = activeMenu.items.size
        if (itemCount == 0) return

        var newIndex = activeMenu.selectedIndex + detentsCount
        while (newIndex < 0) {
            newIndex += itemCount
        }
        newIndex %= itemCount

        currentStack[lastIdx] = activeMenu.copy(selectedIndex = newIndex)
        _navigationStack.value = currentStack
    }

    fun pushMenu(menuState: MenuState) {
        allMenus[menuState.id] = menuState
        val currentStack = _navigationStack.value.toMutableList()
        currentStack.add(menuState.copy(selectedIndex = 0))
        _navigationStack.value = currentStack
    }

    fun replaceTopMenu(menuState: MenuState) {
        allMenus[menuState.id] = menuState
        val currentStack = _navigationStack.value.toMutableList()
        if (currentStack.isNotEmpty()) {
            currentStack[currentStack.lastIndex] = menuState.copy(selectedIndex = 0)
        } else {
            currentStack.add(menuState.copy(selectedIndex = 0))
        }
        _navigationStack.value = currentStack
    }

    fun registerMenu(menuState: MenuState) {
        allMenus[menuState.id] = menuState
        val currentStack = _navigationStack.value.toMutableList()
        val lastIdx = currentStack.lastIndex
        if (lastIdx >= 0 && currentStack[lastIdx].id == menuState.id) {
            val preservedIdx = currentStack[lastIdx].selectedIndex.coerceIn(0, (menuState.items.size - 1).coerceAtLeast(0))
            currentStack[lastIdx] = menuState.copy(selectedIndex = preservedIdx)
            _navigationStack.value = currentStack
        }
    }

    fun navigateToNowPlaying() {
        val nowPlayingMenu = allMenus["now_playing_menu"] ?: createFallbackMenu("now_playing_menu", "Now Playing")
        pushMenu(nowPlayingMenu)
    }

    fun navigateToOnboarding() {
        val onboardingMenu = allMenus["onboarding_menu"] ?: createFallbackMenu("onboarding_menu", "User Guide")
        pushMenu(onboardingMenu)
    }

    fun popToRoot() {
        allMenus["root"]?.let { root ->
            _navigationStack.value = listOf(root)
        }
    }

    fun onCenterButtonClicked(
        onThemeSelected: (ThemePreset) -> Unit,
        onIntentTriggered: (String) -> Unit,
        onComingSoonTriggered: ((String) -> Unit)? = null
    ) {
        val item = selectedItem ?: return
        if (!item.isEnabled) {
            onComingSoonTriggered?.invoke(item.title)
            return
        }

        // 1. Theme Selection
        item.presetToSelect?.let { preset ->
            onThemeSelected(preset)
            return
        }

        // 2. External Link Intent
        item.intentUrl?.let { url ->
            onIntentTriggered(url)
            return
        }

        // 3. Custom Action
        item.onSelectAction?.let { action ->
            action()
            return
        }

        // 4. Submenu Navigation
        item.targetMenuId?.let { targetId ->
            val targetMenu = allMenus[targetId] ?: createFallbackMenu(targetId, item.title)
            pushMenu(targetMenu)
            return
        }
    }

    fun onMenuButtonClicked(): Boolean {
        val currentStack = _navigationStack.value.toMutableList()
        if (currentStack.size > 1) {
            currentStack.removeAt(currentStack.lastIndex)
            _navigationStack.value = currentStack
            return true
        }
        return false // Already at root menu
    }

    private fun createFallbackMenu(id: String, title: String): MenuState {
        return MenuState(
            id = id,
            title = title,
            items = listOf(
                MenuItem(
                    id = "${id}_stub",
                    title = title,
                    subtitle = "No items available",
                    hasSubMenu = false,
                    rightPane = RightPaneContent.ActionPreview(title, "Select options or navigate back with MENU.")
                )
            ),
            selectedIndex = 0
        )
    }

    private fun createMenuTree(): Map<String, MenuState> {
        val map = mutableMapOf<String, MenuState>()

        // Root Menu
        map["root"] = MenuState(
            id = "root",
            title = "iPod",
            items = listOf(
                MenuItem(
                    id = "main_search",
                    title = "Search",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Search", "Voice Search local music library."),
                    targetMenuId = "main_search_trigger"
                ),
                MenuItem(
                    id = "now_playing",
                    title = "Now Playing",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.DefaultArtwork,
                    targetMenuId = "now_playing_menu"
                ),
                MenuItem(
                    id = "cover_flow",
                    title = "Cover Flow",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Cover Flow", "Interactive 3D album browser visualizer."),
                    targetMenuId = "cover_flow_menu"
                ),
                MenuItem(
                    id = "music",
                    title = "Music",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.MusicCategory("Music Library"),
                    targetMenuId = "music_menu"
                ),
                MenuItem(
                    id = "playlists",
                    title = "Playlists",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.MusicCategory("Playlists"),
                    targetMenuId = "playlists_menu"
                ),
                MenuItem(
                    id = "shuffle",
                    title = "Shuffle Songs",
                    hasSubMenu = false,
                    rightPane = RightPaneContent.ActionPreview("Shuffle", "Press center button to shuffle all songs."),
                    targetMenuId = "now_playing_menu"
                ),
                MenuItem(
                    id = "extras",
                    title = "Extras",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Extras", "Games, Clock, Notes and more."),
                    targetMenuId = "extras_menu"
                ),
                MenuItem(
                    id = "settings",
                    title = "Settings",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Settings", "Themes, Sound effects and preferences."),
                    targetMenuId = "settings_menu"
                ),
                MenuItem(
                    id = "about",
                    title = "About",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("About MyPod", "Project links and information."),
                    targetMenuId = "about_menu"
                )
            )
        )

        // Music Menu
        map["music_menu"] = MenuState(
            id = "music_menu",
            title = "Music",
            items = listOf(
                MenuItem(
                    id = "ytdlp_music",
                    title = "yt-dlp",
                    subtitle = "YouTube Online",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("yt-dlp", "YouTube Music & Stream Search"),
                    targetMenuId = "ytdlp_menu"
                ),
                MenuItem(
                    id = "cover_flow_music",
                    title = "Cover Flow",
                    subtitle = "3D Album Visualizer",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Cover Flow", "Interactive 3D album visualizer."),
                    targetMenuId = "cover_flow_menu"
                ),
                MenuItem(
                    id = "signin",
                    title = "Sign In",
                    subtitle = "Cloud Sync",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Account", "Sign in to access your cloud music library."),
                    targetMenuId = "signin_menu"
                ),
                MenuItem(
                    id = "songs",
                    title = "Songs",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.MusicCategory("All Songs"),
                    targetMenuId = "songs_menu"
                ),
                MenuItem(
                    id = "artists",
                    title = "Artists",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.MusicCategory("Artists"),
                    targetMenuId = "artists_menu"
                ),
                MenuItem(
                    id = "albums",
                    title = "Albums",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.MusicCategory("Albums"),
                    targetMenuId = "albums_menu"
                ),
                MenuItem(
                    id = "playlists_sub",
                    title = "Playlists",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.MusicCategory("Playlists"),
                    targetMenuId = "playlists_menu"
                ),
                MenuItem(
                    id = "genres",
                    title = "Genres",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.MusicCategory("Genres"),
                    targetMenuId = "genres_menu"
                ),
                MenuItem(
                    id = "files",
                    title = "Files",
                    subtitle = "Storage Explorer",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Storage", "Browse audio files using Android Storage Access Framework."),
                    targetMenuId = "files_menu"
                )
            )
        )

        // Dedicated yt-dlp Menu
        map["ytdlp_menu"] = MenuState(
            id = "ytdlp_menu",
            title = "yt-dlp",
            items = listOf(
                MenuItem(
                    id = "ytdlp_cover_flow",
                    title = "Cover Flow",
                    subtitle = "3D Album Visualizer",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Cover Flow", "Interactive 3D album visualizer for yt-dlp albums."),
                    targetMenuId = "ytdlp_cover_flow_menu"
                ),
                MenuItem(
                    id = "ytdlp_search",
                    title = "Search",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("yt-dlp Search", "Voice Search YouTube online"),
                    targetMenuId = "ytdlp_search_trigger"
                ),
                MenuItem(
                    id = "ytdlp_top_songs",
                    title = "Top Songs",
                    subtitle = "YouTube Music Charts",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Top Songs", "Fetch YouTube Music top hits"),
                    targetMenuId = "ytdlp_top_songs_trigger"
                ),
                MenuItem(
                    id = "ytdlp_songs",
                    title = "Songs",
                    subtitle = "0 Songs",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.MusicCategory("yt-dlp Songs"),
                    targetMenuId = "ytdlp_songs_menu"
                ),
                MenuItem(
                    id = "ytdlp_playlists",
                    title = "Playlists",
                    subtitle = "0 Playlists",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.MusicCategory("yt-dlp Playlists"),
                    targetMenuId = "ytdlp_playlists_menu"
                ),
                MenuItem(
                    id = "ytdlp_albums",
                    title = "Albums",
                    subtitle = "0 Albums",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.MusicCategory("yt-dlp Albums"),
                    targetMenuId = "ytdlp_albums_menu"
                ),
                MenuItem(
                    id = "ytdlp_artists",
                    title = "Artists",
                    subtitle = "0 Artists",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.MusicCategory("yt-dlp Artists"),
                    targetMenuId = "ytdlp_artists_menu"
                )
            )
        )

        map["ytdlp_cover_flow_menu"] = MenuState(
            id = "ytdlp_cover_flow_menu",
            title = "Cover Flow",
            items = emptyList()
        )

        map["ytdlp_songs_menu"] = MenuState(
            id = "ytdlp_songs_menu",
            title = "yt-dlp Songs",
            items = listOf(
                MenuItem(
                    id = "empty_ytdlp_songs",
                    title = "No Songs Available",
                    subtitle = "Use Voice Search or Top Songs",
                    hasSubMenu = false,
                    rightPane = RightPaneContent.ActionPreview("yt-dlp", "Search or fetch Top Songs to add tracks.")
                )
            )
        )

        map["ytdlp_playlists_menu"] = MenuState(
            id = "ytdlp_playlists_menu",
            title = "yt-dlp Playlists",
            items = listOf(
                MenuItem(
                    id = "empty_ytdlp_pl",
                    title = "No Playlists Available",
                    subtitle = "Use Voice Search or Top Songs",
                    hasSubMenu = false,
                    rightPane = RightPaneContent.ActionPreview("yt-dlp", "Fetch playlists or search online.")
                )
            )
        )

        map["ytdlp_albums_menu"] = MenuState(
            id = "ytdlp_albums_menu",
            title = "yt-dlp Albums",
            items = listOf(
                MenuItem(
                    id = "empty_ytdlp_alb",
                    title = "No Albums Available",
                    subtitle = "Use Voice Search or Top Songs",
                    hasSubMenu = false,
                    rightPane = RightPaneContent.ActionPreview("yt-dlp", "Search YouTube or fetch top albums.")
                )
            )
        )

        map["ytdlp_artists_menu"] = MenuState(
            id = "ytdlp_artists_menu",
            title = "yt-dlp Artists",
            items = listOf(
                MenuItem(
                    id = "empty_ytdlp_art",
                    title = "No Artists Available",
                    subtitle = "Use Voice Search or Top Songs",
                    hasSubMenu = false,
                    rightPane = RightPaneContent.ActionPreview("yt-dlp", "Search YouTube to discover artists.")
                )
            )
        )

        // Extras Menu
        map["extras_menu"] = MenuState(
            id = "extras_menu",
            title = "Extras",
            items = listOf(
                MenuItem(
                    id = "games",
                    title = "Games",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.GamePreview("Retro Arcade", "Classic iPod Games"),
                    targetMenuId = "games_menu"
                ),
                MenuItem(
                    id = "clock",
                    title = "Clock",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Clock", "World clock, Alarm & Timer"),
                    targetMenuId = "clock_menu"
                ),
                MenuItem(
                    id = "calendar",
                    title = "Calendar",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Calendar", "Events and schedules"),
                    targetMenuId = "calendar_menu"
                ),
                MenuItem(
                    id = "recorder",
                    title = "Voice Recorder",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Voice Recorder", "Record voice memos and play saved audio clips."),
                    targetMenuId = "recorder_menu"
                ),
                MenuItem(
                    id = "camera",
                    title = "Camera",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Camera", "Retro iPod LCD Camera & Photo Viewer."),
                    targetMenuId = "camera_menu"
                ),
                MenuItem(
                    id = "gemini_chat",
                    title = "Gemini AI Voice Chat",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Gemini AI Chat", "Ask questions and chat via text & voice."),
                    targetMenuId = "gemini_chat_menu"
                )
            )
        )

        map["clock_menu"] = MenuState(
            id = "clock_menu",
            title = "Clock",
            items = emptyList()
        )

        map["calendar_menu"] = MenuState(
            id = "calendar_menu",
            title = "Calendar",
            items = emptyList()
        )

        map["recorder_menu"] = MenuState(
            id = "recorder_menu",
            title = "Voice Recorder",
            items = emptyList()
        )

        map["camera_menu"] = MenuState(
            id = "camera_menu",
            title = "Camera",
            items = emptyList()
        )

        map["gemini_chat_menu"] = MenuState(
            id = "gemini_chat_menu",
            title = "Gemini AI Chat",
            items = emptyList()
        )

        // Games Menu
        map["games_menu"] = MenuState(
            id = "games_menu",
            title = "Games",
            items = listOf(
                MenuItem(
                    id = "game_brick",
                    title = "Brick Breaker",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.GamePreview("Brick Breaker", "Bounce the ball and destroy bricks with your Click Wheel paddle!"),
                    targetMenuId = "brick_breaker_stub"
                ),
                MenuItem(
                    id = "game_snake",
                    title = "Snake",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.GamePreview("Snake", "Control the snake using the Click Wheel and eat apples!"),
                    targetMenuId = "snake_stub"
                ),
                MenuItem(
                    id = "game_solitaire",
                    title = "Solitaire",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.GamePreview("Solitaire", "Classic Klondike Solitaire card game."),
                    targetMenuId = "solitaire_stub"
                ),
                MenuItem(
                    id = "game_parachute",
                    title = "Parachute",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.GamePreview("Parachute", "Defend your base from paratroopers!"),
                    targetMenuId = "parachute_stub"
                ),
                MenuItem(
                    id = "game_quiz",
                    title = "Music Quiz",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.GamePreview("Music Quiz", "Test your knowledge of your music library!"),
                    targetMenuId = "quiz_stub"
                )
            )
        )

        map["onboarding_menu"] = MenuState(
            id = "onboarding_menu",
            title = "User Guide",
            items = emptyList()
        )

        // Settings Menu
        map["settings_menu"] = MenuState(
            id = "settings_menu",
            title = "Settings",
            items = listOf(
                MenuItem(
                    id = "presets",
                    title = "Theme Presets",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Themes", "Customize your iPod hardware chassis colors."),
                    targetMenuId = "presets_menu"
                ),
                MenuItem(
                    id = "audio_sources",
                    title = "Audio Sources",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("Audio Sources", "Select active source & stream resolver preferences."),
                    targetMenuId = "audio_sources_menu"
                ),
                MenuItem(
                    id = "user_guide",
                    title = "User Guide",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("User Guide", "Interactive guide on using your iPod Classic player."),
                    targetMenuId = "onboarding_menu"
                ),
                MenuItem(
                    id = "about_settings",
                    title = "About",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("About", "App links and developer details."),
                    targetMenuId = "about_menu"
                )
            )
        )

        // Theme Presets Menu
        map["presets_menu"] = MenuState(
            id = "presets_menu",
            title = "Presets",
            items = ThemePreset.entries.map { preset ->
                MenuItem(
                    id = "preset_${preset.id}",
                    title = preset.displayName,
                    hasSubMenu = false,
                    rightPane = RightPaneContent.ThemePreview(preset),
                    presetToSelect = preset
                )
            }
        )

        // About Menu
        map["about_menu"] = MenuState(
            id = "about_menu",
            title = "About",
            items = listOf(
                MenuItem(
                    id = "about_guide",
                    title = "User Guide",
                    hasSubMenu = true,
                    rightPane = RightPaneContent.ActionPreview("User Guide", "Interactive guide on using your iPod Classic player."),
                    targetMenuId = "onboarding_menu"
                ),
                MenuItem(
                    id = "about_app_info",
                    title = "MyPod v1.0.0",
                    subtitle = "Developer: Trishit Majumdar",
                    hasSubMenu = false,
                    rightPane = RightPaneContent.ActionPreview("MyPod v1.0.0", "Pixel-accurate iPod Classic player created by Trishit.")
                ),
                MenuItem(
                    id = "about_coffee",
                    title = "Buy Me a Coffee",
                    subtitle = "Support Project",
                    hasSubMenu = false,
                    rightPane = RightPaneContent.ExternalLinkPreview("Buy Me a Coffee", "https://buymeacoffee.com/trishit.me"),
                    intentUrl = "https://buymeacoffee.com/trishit.me"
                ),
                MenuItem(
                    id = "about_linkedin",
                    title = "LinkedIn",
                    subtitle = "Connect with Developer",
                    hasSubMenu = false,
                    rightPane = RightPaneContent.ExternalLinkPreview("LinkedIn", "https://linkedin.com/trishit-majumdar"),
                    intentUrl = "https://linkedin.com/trishit-majumdar"
                ),
                MenuItem(
                    id = "about_github",
                    title = "GitHub",
                    subtitle = "Source Code",
                    hasSubMenu = false,
                    rightPane = RightPaneContent.ExternalLinkPreview("GitHub", "https://github.com/quantum3600/MyPod"),
                    intentUrl = "https://github.com/quantum3600/MyPod"
                )
            )
        )

        // Playlists Menu
        map["playlists_menu"] = MenuState(
            id = "playlists_menu",
            title = "Playlists",
            items = listOf(
                MenuItem("pl_favorites", "Favorites", subtitle = "24 Songs", hasSubMenu = true, rightPane = RightPaneContent.MusicCategory("Favorites")),
                MenuItem("pl_top_rated", "Top Rated", subtitle = "15 Songs", hasSubMenu = true, rightPane = RightPaneContent.MusicCategory("Top Rated")),
                MenuItem("pl_recently_added", "Recently Added", subtitle = "10 Songs", hasSubMenu = true, rightPane = RightPaneContent.MusicCategory("Recently Added")),
                MenuItem("pl_90s", "90s Hits", subtitle = "32 Songs", hasSubMenu = true, rightPane = RightPaneContent.MusicCategory("90s Hits"))
            )
        )

        // Now Playing Menu
        map["now_playing_menu"] = MenuState(
            id = "now_playing_menu",
            title = "Now Playing",
            items = listOf(
                MenuItem(
                    id = "np_current",
                    title = "No Track Playing",
                    subtitle = "Select music to play",
                    hasSubMenu = false,
                    rightPane = RightPaneContent.DefaultArtwork
                )
            )
        )

        // Cover Flow Menu State
        map["cover_flow_menu"] = MenuState(
            id = "cover_flow_menu",
            title = "Cover Flow",
            items = emptyList()
        )

        return map
    }
}
