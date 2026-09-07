package com.bytekoders.mypod.games

import com.bytekoders.mypod.source.TrackMetadata
import com.bytekoders.mypod.ui.games.BrickBreakerState
import com.bytekoders.mypod.ui.games.Direction
import com.bytekoders.mypod.ui.games.MusicQuizGameState
import com.bytekoders.mypod.ui.games.ParachuteGameState
import com.bytekoders.mypod.ui.games.Point
import com.bytekoders.mypod.ui.games.SnakeGameState
import com.bytekoders.mypod.ui.games.SolitaireGameState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GamesLogicTest {

    @Test
    fun `brick breaker paddle moves and ball launches`() {
        val game = BrickBreakerState()
        assertEquals(0.5f, game.paddleX, 0.001f)
        assertFalse(game.ballLaunched)

        game.movePaddle(1)
        assertTrue(game.paddleX > 0.5f)

        game.launchOrRestart()
        assertTrue(game.ballLaunched)

        val initialX = game.ballX
        val initialY = game.ballY
        game.updateTick()

        assertTrue(game.ballX != initialX || game.ballY != initialY)
    }

    @Test
    fun `snake turns and eats food`() {
        val game = SnakeGameState(gridCols = 10, gridRows = 10)
        assertEquals(Direction.RIGHT, game.direction)

        game.handleScroll(1) // Turn CW -> DOWN
        assertEquals(Direction.DOWN, game.direction)

        game.handleScroll(-1) // Turn CCW -> RIGHT
        assertEquals(Direction.RIGHT, game.direction)

        val head = game.snake.first()
        game.food = Point(head.x + 1, head.y)

        val initialScore = game.score
        game.updateTick()

        assertEquals(initialScore + 10, game.score)
        assertEquals(game.food.x != head.x + 1 || game.food.y != head.y, true)
    }

    @Test
    fun `solitaire deck setup and cursor move`() {
        val game = SolitaireGameState()
        assertEquals(0, game.cursorIndex)
        assertNull(game.selectedLocation)

        // Deal checked: 1+2+3+4+5+6+7 = 28 cards in tableau, 24 in stock
        assertEquals(24, game.stock.size)
        assertEquals(7, game.tableau.size)

        game.moveCursor(1)
        assertEquals(1, game.cursorIndex)

        // Click Stock (0)
        game.cursorIndex = 0
        game.handleSelectPress()
        assertEquals(1, game.waste.size)
        assertEquals(23, game.stock.size)
    }

    @Test
    fun `parachute turret rotates and fires shells`() {
        val game = ParachuteGameState()
        assertEquals(0f, game.turretAngleDeg, 0.001f)
        assertEquals(0, game.shells.size)

        game.adjustTurretAngle(2)
        assertTrue(game.turretAngleDeg > 0f)

        game.fireShell()
        assertEquals(3, game.shells.size) // Fires triple spread shot

        val shell = game.shells[1] // Center shell
        assertTrue(shell.vy < 0f) // Shell moves upwards
    }

    @Test
    fun `music quiz question generation and scoring`() {
        val tracks = listOf(
            TrackMetadata("t1", "Song One", "Artist A", "Album 1"),
            TrackMetadata("t2", "Song Two", "Artist B", "Album 2"),
            TrackMetadata("t3", "Song Three", "Artist C", "Album 3"),
            TrackMetadata("t4", "Song Four", "Artist D", "Album 4")
        )

        val game = MusicQuizGameState(tracks)
        val q = game.currentQuestion
        assertNotNull(q)
        assertEquals(4, q?.options?.size)

        game.selectedOptionIndex = q!!.correctOptionIndex
        game.submitOrNext()

        assertTrue(game.answerSubmitted)
        assertTrue(game.isCorrect)
        assertTrue(game.score > 0)
    }
}
