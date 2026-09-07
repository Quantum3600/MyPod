package com.trishit.mypod.lyrics

import com.trishit.mypod.navigation.MenuNavigationManager
import com.trishit.mypod.navigation.MenuState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class CoverFlowNavigationTest {

    @Test
    fun testCoverFlowMenuInTree() {
        val navManager = MenuNavigationManager()

        // Root menu has Cover Flow item
        val rootItems = navManager.currentMenu.items
        val coverFlowItem = rootItems.find { it.id == "cover_flow" }

        assertNotNull(coverFlowItem)
        assertEquals("Cover Flow", coverFlowItem?.title)
        assertEquals("cover_flow_menu", coverFlowItem?.targetMenuId)
    }

    @Test
    fun testNavigateToCoverFlow() {
        val navManager = MenuNavigationManager()

        navManager.pushMenu(
            MenuState(id = "cover_flow_menu", title = "Cover Flow", items = emptyList())
        )

        assertEquals("cover_flow_menu", navManager.currentMenu.id)
        assertEquals("Cover Flow", navManager.currentMenu.title)
    }
}
