package com.bytekoders.mypod.ui.games

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
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytekoders.mypod.ui.components.WheelEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlin.math.abs

data class Brick(
    val row: Int,
    val col: Int,
    val color: Color,
    var isDestroyed: Boolean = false
)

class BrickBreakerState {
    var score by mutableIntStateOf(0)
    var lives by mutableIntStateOf(3)
    var gameOver by mutableStateOf(false)
    var gameWon by mutableStateOf(false)

    // Relative positions (0.0 to 1.0)
    val paddleWidth = 0.22f
    val paddleHeight = 0.03f
    var paddleX by mutableFloatStateOf(0.5f) // center of paddle

    val ballRadius = 0.02f
    var ballX by mutableFloatStateOf(0.5f)
    var ballY by mutableFloatStateOf(0.85f)
    var ballVx by mutableFloatStateOf(0.012f)
    var ballVy by mutableFloatStateOf(-0.016f)
    var ballLaunched by mutableStateOf(false)

    val rows = 5
    val cols = 8
    val bricks = mutableStateListOf<Brick>()

    init {
        resetBricks()
    }

    fun resetBricks() {
        bricks.clear()
        val rowColors = listOf(
            Color(0xFFE53935), // Red
            Color(0xFFFB8C00), // Orange
            Color(0xFFFDD835), // Yellow
            Color(0xFF43A047), // Green
            Color(0xFF1E88E5)  // Blue
        )
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                bricks.add(Brick(row = r, col = c, color = rowColors[r % rowColors.size]))
            }
        }
    }

    fun resetGame() {
        score = 0
        lives = 3
        gameOver = false
        gameWon = false
        paddleX = 0.5f
        ballLaunched = false
        ballX = 0.5f
        ballY = 0.85f
        resetBricks()
    }

    fun movePaddle(detents: Int) {
        val delta = detents * 0.09f
        val halfP = paddleWidth / 2f
        paddleX = (paddleX + delta).coerceIn(halfP, 1f - halfP)
        if (!ballLaunched) {
            ballX = paddleX
        }
    }

    fun launchOrRestart() {
        if (gameOver || gameWon) {
            resetGame()
        } else if (!ballLaunched) {
            ballLaunched = true
            ballVx = if ((0..1).random() == 0) 0.012f else -0.012f
            ballVy = -0.016f
        }
    }

    fun updateTick() {
        if (!ballLaunched || gameOver || gameWon) return

        var nextX = ballX + ballVx
        var nextY = ballY + ballVy

        // Wall collisions
        if (nextX - ballRadius <= 0f) {
            nextX = ballRadius
            ballVx = abs(ballVx)
        } else if (nextX + ballRadius >= 1f) {
            nextX = 1f - ballRadius
            ballVx = -abs(ballVx)
        }

        if (nextY - ballRadius <= 0f) {
            nextY = ballRadius
            ballVy = abs(ballVy)
        }

        // Paddle collision
        val paddleTop = 0.88f
        val halfP = paddleWidth / 2f
        if (nextY + ballRadius >= paddleTop && ballY + ballRadius <= paddleTop + paddleHeight) {
            if (nextX >= paddleX - halfP && nextX <= paddleX + halfP) {
                ballVy = -abs(ballVy)
                val hitOffset = (nextX - paddleX) / halfP // -1.0 to 1.0
                ballVx = hitOffset * 0.018f
                nextY = paddleTop - ballRadius
            }
        }

        // Bottom wall (Missed ball)
        if (nextY - ballRadius > 1.0f) {
            lives--
            if (lives <= 0) {
                gameOver = true
            } else {
                ballLaunched = false
                ballX = paddleX
                ballY = 0.85f
            }
            return
        }

        // Brick collisions
        val brickTopMargin = 0.08f
        val brickAreaHeight = 0.30f
        val brickWidth = 1f / cols
        val brickHeight = brickAreaHeight / rows

        for (brick in bricks) {
            if (brick.isDestroyed) continue

            val bLeft = brick.col * brickWidth
            val bRight = bLeft + brickWidth
            val bTop = brickTopMargin + brick.row * brickHeight
            val bBottom = bTop + brickHeight

            if (nextX + ballRadius >= bLeft && nextX - ballRadius <= bRight &&
                nextY + ballRadius >= bTop && nextY - ballRadius <= bBottom) {

                brick.isDestroyed = true
                score += 10 + (rows - brick.row) * 5
                ballVy = -ballVy

                if (bricks.all { it.isDestroyed }) {
                    gameWon = true
                }
                break
            }
        }

        ballX = nextX
        ballY = nextY
    }
}

@Composable
fun BrickBreakerScreen(
    wheelEvents: SharedFlow<WheelEvent>,
    modifier: Modifier = Modifier
) {
    val gameState = remember { BrickBreakerState() }

    // Collect Wheel Events
    LaunchedEffect(Unit) {
        wheelEvents.collect { event ->
            when (event) {
                is WheelEvent.Scroll -> gameState.movePaddle(event.detents)
                is WheelEvent.SelectPress -> gameState.launchOrRestart()
                else -> {}
            }
        }
    }

    // Game Loop
    LaunchedEffect(Unit) {
        while (true) {
            delay(16L)
            gameState.updateTick()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF18181C))
    ) {
        // Top HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BRICK BREAKER",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFCC00)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "SCORE: ${gameState.score}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text(
                text = "LIVES: ${"❤️".repeat(gameState.lives.coerceAtLeast(0))}",
                fontSize = 10.sp,
                color = Color.Red
            )
        }

        // Canvas for Bricks, Paddle, Ball
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 22.dp)
        ) {
            val w = size.width
            val h = size.height

            // Bricks
            val brickTopMargin = 0.08f * h
            val brickAreaHeight = 0.30f * h
            val brickW = w / gameState.cols
            val brickH = brickAreaHeight / gameState.rows

            for (brick in gameState.bricks) {
                if (!brick.isDestroyed) {
                    val left = brick.col * brickW
                    val top = brickTopMargin + brick.row * brickH
                    drawRect(
                        color = brick.color,
                        topLeft = Offset(left + 1f, top + 1f),
                        size = Size(brickW - 2f, brickH - 2f)
                    )
                }
            }

            // Paddle
            val paddleW = gameState.paddleWidth * w
            val paddleH = gameState.paddleHeight * h
            val paddleLeft = (gameState.paddleX * w) - (paddleW / 2f)
            val paddleTop = 0.88f * h

            drawRect(
                color = Color(0xFF00E5FF),
                topLeft = Offset(paddleLeft, paddleTop),
                size = Size(paddleW, paddleH)
            )

            // Ball
            val ballR = gameState.ballRadius * w
            val bx = gameState.ballX * w
            val by = gameState.ballY * h

            drawCircle(
                color = Color.White,
                radius = ballR,
                center = Offset(bx, by)
            )
        }

        // Overlays
        if (!gameState.ballLaunched && !gameState.gameOver && !gameState.gameWon) {
            Text(
                text = "Press CENTER to Launch",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )
        } else if (gameState.gameOver) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = "GAME OVER",
                    color = Color.Red,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Press CENTER to Restart",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        } else if (gameState.gameWon) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = "YOU WIN!",
                    color = Color.Green,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Press CENTER to Play Again",
                    color = Color.White,
                    fontSize = 11.sp
                )
            }
        }
    }
}
