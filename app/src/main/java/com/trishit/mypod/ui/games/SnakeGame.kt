package com.trishit.mypod.ui.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trishit.mypod.ui.components.WheelEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow

data class Point(val x: Int, val y: Int)

enum class Direction {
    UP, RIGHT, DOWN, LEFT;

    fun turnRight(): Direction = when (this) {
        UP -> RIGHT
        RIGHT -> DOWN
        DOWN -> LEFT
        LEFT -> UP
    }

    fun turnLeft(): Direction = when (this) {
        UP -> LEFT
        LEFT -> DOWN
        DOWN -> RIGHT
        RIGHT -> UP
    }
}

class SnakeGameState(val gridCols: Int = 20, val gridRows: Int = 15) {
    var score by mutableIntStateOf(0)
    var isPaused by mutableStateOf(false)
    var isGameOver by mutableStateOf(false)

    val snake = mutableStateListOf<Point>()
    var direction by mutableStateOf(Direction.RIGHT)
    var food by mutableStateOf(Point(15, 7))

    private var accumulatedDetents = 0

    init {
        resetGame()
    }

    fun resetGame() {
        score = 0
        isPaused = false
        isGameOver = false
        direction = Direction.RIGHT
        accumulatedDetents = 0
        snake.clear()
        snake.addAll(listOf(Point(5, 7), Point(4, 7), Point(3, 7)))
        spawnFood()
    }

    fun spawnFood() {
        var p: Point
        do {
            val rx = (0 until gridCols).random()
            val ry = (0 until gridRows).random()
            p = Point(rx, ry)
        } while (snake.contains(p))
        food = p
    }

    fun handleScroll(detents: Int) {
        if (isGameOver || detents == 0) return
        if (detents > 0) {
            direction = direction.turnRight()
        } else {
            direction = direction.turnLeft()
        }
    }

    fun togglePauseOrRestart() {
        if (isGameOver) {
            resetGame()
        } else {
            isPaused = !isPaused
        }
    }

    fun updateTick() {
        if (isPaused || isGameOver) return

        val head = snake.first()
        val nextHead = when (direction) {
            Direction.UP -> Point(head.x, head.y - 1)
            Direction.RIGHT -> Point(head.x + 1, head.y)
            Direction.DOWN -> Point(head.x, head.y + 1)
            Direction.LEFT -> Point(head.x - 1, head.y)
        }

        // Check Wall Collision
        if (nextHead.x < 0 || nextHead.x >= gridCols || nextHead.y < 0 || nextHead.y >= gridRows) {
            isGameOver = true
            return
        }

        // Check Self Collision
        if (snake.contains(nextHead)) {
            isGameOver = true
            return
        }

        // Move
        snake.add(0, nextHead)

        if (nextHead == food) {
            score += 10
            spawnFood()
        } else {
            snake.removeAt(snake.lastIndex)
        }
    }
}

@Composable
fun SnakeScreen(
    wheelEvents: SharedFlow<WheelEvent>,
    modifier: Modifier = Modifier
) {
    val gameState = remember { SnakeGameState() }

    // Collect Wheel Events
    LaunchedEffect(Unit) {
        wheelEvents.collect { event ->
            when (event) {
                is WheelEvent.Scroll -> gameState.handleScroll(event.detents)
                is WheelEvent.SelectPress -> gameState.togglePauseOrRestart()
                else -> {}
            }
        }
    }

    // Game Loop
    LaunchedEffect(Unit) {
        while (true) {
            delay(120L)
            gameState.updateTick()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF9E9D89)) // Classic GameBoy/iPod LCD tint
    ) {
        // Top HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SNAKE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E2818)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "SCORE: ${gameState.score}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E2818)
            )
        }

        // Snake Field
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 22.dp, bottom = 4.dp, start = 4.dp, end = 4.dp)
        ) {
            val cellW = size.width / gameState.gridCols
            val cellH = size.height / gameState.gridRows

            // Draw Border
            drawRect(
                color = Color(0xFF1E2818),
                topLeft = Offset.Zero,
                size = Size(size.width, size.height),
                style = Stroke(width = 3f)
            )

            // Draw Food
            val fx = gameState.food.x * cellW
            val fy = gameState.food.y * cellH
            drawRect(
                color = Color(0xFF8A1010),
                topLeft = Offset(fx + 1f, fy + 1f),
                size = Size(cellW - 2f, cellH - 2f)
            )

            // Draw Snake
            gameState.snake.forEachIndexed { index, segment ->
                val sx = segment.x * cellW
                val sy = segment.y * cellH
                val color = if (index == 0) Color(0xFF0F1A08) else Color(0xFF1E2818)
                drawRect(
                    color = color,
                    topLeft = Offset(sx + 1f, sy + 1f),
                    size = Size(cellW - 2f, cellH - 2f)
                )
            }
        }

        // Overlays
        if (gameState.isPaused && !gameState.isGameOver) {
            Text(
                text = "PAUSED\nPress CENTER to Resume",
                color = Color(0xFF1E2818),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (gameState.isGameOver) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = "GAME OVER",
                    color = Color(0xFF8A1010),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Press CENTER to Restart",
                    color = Color(0xFF1E2818),
                    fontSize = 11.sp
                )
            }
        }
    }
}
