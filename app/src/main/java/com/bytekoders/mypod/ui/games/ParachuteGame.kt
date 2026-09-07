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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytekoders.mypod.ui.components.WheelEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

data class Shell(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    var isDestroyed: Boolean = false
)

data class Helicopter(
    var x: Float,
    val y: Float,
    val vx: Float,
    var isDestroyed: Boolean = false
)

data class Parachutist(
    var x: Float,
    var y: Float,
    var hasParachute: Boolean = true,
    var isLanded: Boolean = false,
    var isDestroyed: Boolean = false
)

class ParachuteGameState {
    var score by mutableIntStateOf(0)
    var turretHealth by mutableIntStateOf(100)
    var isGameOver by mutableStateOf(false)

    var turretAngleDeg by mutableFloatStateOf(0f) // -75° (left) to +75° (right)

    val shells = mutableStateListOf<Shell>()
    val helicopters = mutableStateListOf<Helicopter>()
    val parachutists = mutableStateListOf<Parachutist>()

    var landedLeftCount by mutableIntStateOf(0)
    var landedRightCount by mutableIntStateOf(0)

    private var tickCounter = 0

    init {
        resetGame()
    }

    fun resetGame() {
        score = 0
        turretHealth = 100
        isGameOver = false
        turretAngleDeg = 0f
        landedLeftCount = 0
        landedRightCount = 0
        shells.clear()
        helicopters.clear()
        parachutists.clear()
        tickCounter = 0
    }

    fun adjustTurretAngle(detents: Int) {
        if (isGameOver) return
        turretAngleDeg = (turretAngleDeg + detents * 6f).coerceIn(-75f, 75f)
    }

    fun fireShell() {
        if (isGameOver) {
            resetGame()
            return
        }

        val angles = listOf(turretAngleDeg - 12f, turretAngleDeg, turretAngleDeg + 12f)
        val speed = 0.025f

        for (angleDeg in angles) {
            val angleRad = Math.toRadians((angleDeg - 90).toDouble())
            val vx = (cos(angleRad) * speed).toFloat()
            val vy = (sin(angleRad) * speed).toFloat()
            shells.add(Shell(x = 0.5f, y = 0.88f, vx = vx, vy = vy))
        }
    }

    fun updateTick() {
        if (isGameOver) return

        tickCounter++

        // Spawn Helicopter
        if (tickCounter % 130 == 0) {
            val fromLeft = (0..1).random() == 0
            val hX = if (fromLeft) -0.1f else 1.1f
            val hVx = if (fromLeft) 0.003f else -0.003f
            val hY = (0.10f..0.25f).random()
            helicopters.add(Helicopter(x = hX, y = hY, vx = hVx))
        }

        // Update Helicopters & Drop Parachutists
        for (h in helicopters) {
            if (h.isDestroyed) continue
            h.x += h.vx

            // Drop parachutist
            if (abs(h.x - 0.5f) < 0.45f && (1..40).random() == 1) {
                parachutists.add(Parachutist(x = h.x, y = h.y + 0.02f))
            }
        }
        helicopters.removeAll { it.x < -0.2f || it.x > 1.2f || it.isDestroyed }

        // Update Parachutists
        for (p in parachutists) {
            if (p.isDestroyed || p.isLanded) continue

            val fallSpeed = if (p.hasParachute) 0.003f else 0.012f
            p.y += fallSpeed

            // Ground Collision
            if (p.y >= 0.88f) {
                p.y = 0.88f
                if (p.hasParachute) {
                    p.isLanded = true
                    if (p.x < 0.45f) {
                        landedLeftCount++
                    } else if (p.x > 0.55f) {
                        landedRightCount++
                    }
                    if (landedLeftCount >= 4 || landedRightCount >= 4) {
                        turretHealth = 0
                        isGameOver = true
                    }
                } else {
                    // Splat if dropped without parachute
                    p.isDestroyed = true
                    score += 10
                }
            }
        }

        // Update Shells
        for (s in shells) {
            if (s.isDestroyed) continue
            s.x += s.vx
            s.y += s.vy

            // Collisions
            // Shell vs Helicopter
            for (h in helicopters) {
                if (!h.isDestroyed && abs(s.x - h.x) < 0.06f && abs(s.y - h.y) < 0.04f) {
                    h.isDestroyed = true
                    s.isDestroyed = true
                    score += 50
                    break
                }
            }

            // Shell vs Parachutist
            if (!s.isDestroyed) {
                for (p in parachutists) {
                    if (!p.isDestroyed && !p.isLanded && abs(s.x - p.x) < 0.04f && abs(s.y - p.y) < 0.04f) {
                        if (p.hasParachute && s.y < p.y) {
                            // Shot parachute
                            p.hasParachute = false
                            score += 15
                        } else {
                            // Shot parachutist directly
                            p.isDestroyed = true
                            score += 25
                        }
                        s.isDestroyed = true
                        break
                    }
                }
            }
        }
        shells.removeAll { it.x < 0f || it.x > 1f || it.y < 0f || it.isDestroyed }
        parachutists.removeAll { it.isDestroyed }
    }

    private fun ClosedRange<Float>.random(): Float =
        start + (Math.random().toFloat() * (endInclusive - start))
}

@Composable
fun ParachuteScreen(
    wheelEvents: SharedFlow<WheelEvent>,
    modifier: Modifier = Modifier
) {
    val gameState = remember { ParachuteGameState() }

    // Collect Wheel Events
    LaunchedEffect(Unit) {
        wheelEvents.collect { event ->
            when (event) {
                is WheelEvent.Scroll -> gameState.adjustTurretAngle(event.detents)
                is WheelEvent.SelectPress, is WheelEvent.PlayPausePress -> gameState.fireShell()
                else -> {}
            }
        }
    }

    // Game Loop
    LaunchedEffect(Unit) {
        while (true) {
            delay(25L)
            gameState.updateTick()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF101C24)) // Dark sky theme
    ) {
        // Top HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PARACHUTE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00E5FF)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "SCORE: ${gameState.score}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // Parachute Field Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 22.dp)
        ) {
            val w = size.width
            val h = size.height

            // Draw Ground
            drawRect(
                color = Color(0xFF2E7D32),
                topLeft = Offset(0f, h * 0.88f),
                size = Size(w, h * 0.12f)
            )

            // Draw Turret Base
            val turretX = w * 0.5f
            val turretY = h * 0.88f

            drawCircle(
                color = Color(0xFF9E9E9E),
                radius = w * 0.06f,
                center = Offset(turretX, turretY)
            )

            // Draw Gun Barrel
            val angleRad = Math.toRadians((gameState.turretAngleDeg - 90).toDouble())
            val barrelLength = w * 0.10f
            val endX = turretX + (cos(angleRad) * barrelLength).toFloat()
            val endY = turretY + (sin(angleRad) * barrelLength).toFloat()

            drawLine(
                color = Color.White,
                start = Offset(turretX, turretY),
                end = Offset(endX, endY),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )

            // Draw Helicopters
            for (heli in gameState.helicopters) {
                if (heli.isDestroyed) continue
                val hX = heli.x * w
                val hY = heli.y * h

                drawRect(
                    color = Color(0xFFFF9800),
                    topLeft = Offset(hX - 16f, hY - 8f),
                    size = Size(32f, 16f)
                )
                // Rotor
                drawLine(
                    color = Color.White,
                    start = Offset(hX - 24f, hY - 12f),
                    end = Offset(hX + 24f, hY - 12f),
                    strokeWidth = 3f
                )
            }

            // Draw Parachutists
            for (p in gameState.parachutists) {
                if (p.isDestroyed) continue
                val px = p.x * w
                val py = p.y * h

                // Parachute dome
                if (p.hasParachute && !p.isLanded) {
                    drawArc(
                        color = Color.White,
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(px - 14f, py - 18f),
                        size = Size(28f, 18f)
                    )
                }

                // Parachutist body
                drawCircle(
                    color = Color(0xFFFFD54F),
                    radius = 5f,
                    center = Offset(px, py)
                )
                drawLine(
                    color = Color(0xFFFFD54F),
                    start = Offset(px, py),
                    end = Offset(px, py + 12f),
                    strokeWidth = 3f
                )
            }

            // Draw Shells
            for (s in gameState.shells) {
                if (s.isDestroyed) continue
                drawCircle(
                    color = Color(0xFFFFEB3B),
                    radius = 4f,
                    center = Offset(s.x * w, s.y * h)
                )
            }
        }

        if (gameState.isGameOver) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = "BASE DESTROYED!",
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
        }
    }
}
