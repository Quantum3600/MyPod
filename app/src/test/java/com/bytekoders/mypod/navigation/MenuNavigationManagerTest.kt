package com.bytekoders.mypod.navigation

import com.bytekoders.mypod.data.theme.ThemePreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MenuNavigationManagerTest {

    private lateinit var navManager: MenuNavigationManager

    @Before
    fun setUp() {
        navManager = MenuNavigationManager()
    }

    @Test
    fun `initial menu is root with selectedIndex zero`() {
        val current = navManager.currentMenu
        assertEquals("root", current.id)
        assertEquals("iPod", current.title)
        assertEquals(0, current.selectedIndex)
        assertEquals(1, navManager.navigationStack.value.size)
    }

    @Test
    fun `scrollDown increments selectedIndex and loops`() {
        val itemCount = navManager.currentMenu.items.size
        navManager.scrollDown()
        assertEquals(1, navManager.currentMenu.selectedIndex)

        // Scroll until end
        repeat(itemCount - 1) {
            navManager.scrollDown()
        }
        assertEquals(0, navManager.currentMenu.selectedIndex)
    }

    @Test
    fun `scrollUp decrements selectedIndex and loops back`() {
        val itemCount = navManager.currentMenu.items.size
        navManager.scrollUp()
        assertEquals(itemCount - 1, navManager.currentMenu.selectedIndex)

        navManager.scrollDown()
        assertEquals(0, navManager.currentMenu.selectedIndex)
    }

    @Test
    fun `scrollByDetents updates index correctly`() {
        navManager.scrollByDetents(3)
        assertEquals(3, navManager.currentMenu.selectedIndex)

        navManager.scrollByDetents(-1)
        assertEquals(2, navManager.currentMenu.selectedIndex)
    }

    @Test
    fun `navigating to sub-menu pushes new state and preserves parent index`() {
        // Navigate down to item "Music"
        while (navManager.selectedItem?.id != "music") {
            navManager.scrollDown()
        }
        val musicIndex = navManager.currentMenu.selectedIndex

        navManager.onCenterButtonClicked(
            onThemeSelected = {},
            onIntentTriggered = {}
        )

        // Now stack should have size 2 and current menu is "Music"
        assertEquals(2, navManager.navigationStack.value.size)
        assertEquals("music_menu", navManager.currentMenu.id)
        assertEquals("Music", navManager.currentMenu.title)
        assertEquals(0, navManager.currentMenu.selectedIndex)

        // Press MENU button to navigate back
        val popped = navManager.onMenuButtonClicked()
        assertTrue(popped)
        assertEquals(1, navManager.navigationStack.value.size)
        assertEquals("root", navManager.currentMenu.id)
        // Verify index was preserved!
        assertEquals(musicIndex, navManager.currentMenu.selectedIndex)
    }

    @Test
    fun `theme preset selection triggers callback`() {
        // Root -> Settings -> Presets -> Space Gray
        while (navManager.selectedItem?.id != "settings") {
            navManager.scrollDown()
        }
        assertEquals("settings", navManager.selectedItem?.id)

        navManager.onCenterButtonClicked(onThemeSelected = {}, onIntentTriggered = {})
        assertEquals("settings_menu", navManager.currentMenu.id)

        // Presets is index 0
        navManager.onCenterButtonClicked(onThemeSelected = {}, onIntentTriggered = {})
        assertEquals("presets_menu", navManager.currentMenu.id)

        var selectedPreset: ThemePreset? = null
        navManager.onCenterButtonClicked(
            onThemeSelected = { selectedPreset = it },
            onIntentTriggered = {}
        )

        assertNotNull(selectedPreset)
        assertEquals(ThemePreset.SPACE_GRAY, selectedPreset)
    }

    @Test
    fun `external link selection triggers intent callback`() {
        // Root -> About -> Buy Me a Coffee
        while (navManager.selectedItem?.id != "about") {
            navManager.scrollDown()
        }
        assertEquals("about", navManager.selectedItem?.id)

        navManager.onCenterButtonClicked(onThemeSelected = {}, onIntentTriggered = {})
        assertEquals("about_menu", navManager.currentMenu.id)

        // Scroll to "about_coffee"
        while (navManager.selectedItem?.id != "about_coffee") {
            navManager.scrollDown()
        }

        var triggeredUrl: String? = null
        navManager.onCenterButtonClicked(
            onThemeSelected = {},
            onIntentTriggered = { triggeredUrl = it }
        )

        assertEquals("https://buymeacoffee.com/trishit.me", triggeredUrl)
    }

    @Test
    fun `pressing MENU at root returns false`() {
        val popped = navManager.onMenuButtonClicked()
        assertFalse(popped)
        assertEquals(1, navManager.navigationStack.value.size)
    }

    @Test
    fun `registerMenu updates active menu on stack in place while preserving selectedIndex`() {
        val testMenu = MenuState(
            id = "test_menu",
            title = "Test Menu",
            items = listOf(
                MenuItem(id = "item1", title = "Item 1"),
                MenuItem(id = "item2", title = "Item 2")
            )
        )
        navManager.pushMenu(testMenu)
        navManager.scrollDown() // selectedIndex becomes 1

        assertEquals(2, navManager.navigationStack.value.size)
        assertEquals(1, navManager.currentMenu.selectedIndex)

        val updatedMenu = testMenu.copy(
            items = listOf(
                MenuItem(id = "item1", title = "(○) Item 1"),
                MenuItem(id = "item2", title = "(●) Item 2")
            )
        )
        navManager.registerMenu(updatedMenu)

        assertEquals(2, navManager.navigationStack.value.size) // Stack size unchanged
        assertEquals("test_menu", navManager.currentMenu.id)
        assertEquals(1, navManager.currentMenu.selectedIndex) // Preserved index
        assertEquals("(●) Item 2", navManager.currentMenu.items[1].title)
    }

    @Test
    fun `extras menu contains recorder, camera, and gemini chat submenus`() {
        while (navManager.selectedItem?.id != "extras") {
            navManager.scrollDown()
        }
        assertEquals("extras", navManager.selectedItem?.id)

        navManager.onCenterButtonClicked(onThemeSelected = {}, onIntentTriggered = {})
        assertEquals("extras_menu", navManager.currentMenu.id)

        val itemIds = navManager.currentMenu.items.map { it.id }
        assertTrue("recorder" in itemIds)
        assertTrue("camera" in itemIds)
        assertTrue("gemini_chat" in itemIds)
    }
}
