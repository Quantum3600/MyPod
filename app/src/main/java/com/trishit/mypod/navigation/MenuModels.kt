package com.trishit.mypod.navigation

import com.trishit.mypod.data.theme.ThemePreset

sealed class RightPaneContent {
    data object DefaultArtwork : RightPaneContent()
    data class MusicCategory(val categoryName: String) : RightPaneContent()
    data class ThemePreview(val preset: ThemePreset) : RightPaneContent()
    data class GamePreview(val gameTitle: String, val description: String) : RightPaneContent()
    data class ExternalLinkPreview(val title: String, val url: String) : RightPaneContent()
    data class ActionPreview(val title: String, val description: String) : RightPaneContent()
}

data class MenuItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val hasSubMenu: Boolean = true,
    val rightPane: RightPaneContent = RightPaneContent.DefaultArtwork,
    val targetMenuId: String? = null,
    val presetToSelect: ThemePreset? = null,
    val intentUrl: String? = null,
    val isEnabled: Boolean = true,
    val onSelectAction: (() -> Unit)? = null
)

data class MenuState(
    val id: String,
    val title: String,
    val items: List<MenuItem>,
    var selectedIndex: Int = 0
)
