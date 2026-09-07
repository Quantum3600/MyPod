package com.trishit.mypod.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WheelEventTest {

    @Test
    fun `scroll event carries correct detent count`() {
        val scrollEvent = WheelEvent.Scroll(detents = 3)
        assertEquals(3, scrollEvent.detents)
    }

    @Test
    fun `prev press event distinguishes hold from single tap`() {
        val tapEvent = WheelEvent.PrevPress(isHold = false)
        val holdEvent = WheelEvent.PrevPress(isHold = true)

        assertFalse(tapEvent.isHold)
        assertTrue(holdEvent.isHold)
    }

    @Test
    fun `next press event distinguishes hold from single tap`() {
        val tapEvent = WheelEvent.NextPress(isHold = false)
        val holdEvent = WheelEvent.NextPress(isHold = true)

        assertFalse(tapEvent.isHold)
        assertTrue(holdEvent.isHold)
    }

    @Test
    fun `menu select and play pause events default to non hold`() {
        val menuEvent = WheelEvent.MenuPress()
        val selectEvent = WheelEvent.SelectPress()
        val playPauseEvent = WheelEvent.PlayPausePress()

        assertFalse(menuEvent.isHold)
        assertFalse(selectEvent.isHold)
        assertFalse(playPauseEvent.isHold)
    }
}
