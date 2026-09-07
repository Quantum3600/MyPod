package com.bytekoders.mypod.ui.components

sealed interface WheelEvent {
    data class Scroll(val detents: Int) : WheelEvent
    data class MenuPress(val isHold: Boolean = false) : WheelEvent
    data class SelectPress(val isHold: Boolean = false) : WheelEvent
    data class PrevPress(val isHold: Boolean = false) : WheelEvent
    data class NextPress(val isHold: Boolean = false) : WheelEvent
    data class PlayPausePress(val isHold: Boolean = false) : WheelEvent
}
