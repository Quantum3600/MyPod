package com.bytekoders.mypod.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.bytekoders.mypod.data.theme.ThemePreset
import com.bytekoders.mypod.ui.theme.MyPodTheme
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt

private enum class TouchMode {
    NONE, CENTER_BUTTON, WHEEL_RING
}

@Composable
fun ClickWheel(
    themePreset: ThemePreset,
    onScrollDetents: (detents: Int) -> Unit,
    onDetentTick: () -> Unit,
    onCenterClick: () -> Unit,
    onMenuClick: () -> Unit,
    onPrevClick: () -> Unit,
    onNextClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 280.dp,
    onPrevHold: (() -> Unit)? = null,
    onNextHold: (() -> Unit)? = null,
    onWheelEvent: ((WheelEvent) -> Unit)? = null,
) {
    var isCenterPressed by remember { mutableStateOf(value = false) }

    val centerScale by animateFloatAsState(
        targetValue = if (isCenterPressed) 0.92f else 1.0f,
        animationSpec = tween(durationMillis = 80),
        label = "centerScale",
    )

    // Cached drawing modifier for outer rubberized wheel ring
    val wheelRingDrawModifier = Modifier.drawWithCache {
        val radius = size.minDimension / 2f
        val innerRadius = radius * 0.36f

        // Rubber Texture Micro-Stippling / Matte Finish
        val brightStipplePath = Path()
        val darkStipplePath = Path()
        val cols = 36
        val rows = 12

        for (r in 0 until rows) {
            val currentR = innerRadius + (radius - innerRadius) * (r.toFloat() / rows)
            val dotCount = (cols * (currentR / radius)).toInt()
            for (c in 0 until dotCount) {
                val angleRad = (c.toFloat() / dotCount) * 2f * PI.toFloat()
                val hash = (r * 101 + c * 37) % 100
                val dotX = size.width / 2f + currentR * cos(angleRad)
                val dotY = size.height / 2f + currentR * sin(angleRad)
                val isLight = (hash % 2) == 0
                val targetPath = if (isLight) brightStipplePath else darkStipplePath
                targetPath.addOval(
                    Rect(
                        center = Offset(dotX, dotY),
                        radius = 0.9f,
                    )
                )
            }
        }

        // Outer Bevel Border
        val outerBevelShadowColor = Color.Black.copy(alpha = 0.25f)
        val outerBevelStroke = Stroke(width = 2.5f)

        val outerSpecularBrush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.35f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.20f),
            )
        )
        val outerSpecularStroke = Stroke(width = 1.5f)

        // Inner Bevel Border (boundary with Center Button)
        val innerBevelShadowColor = Color.Black.copy(alpha = 0.35f)
        val innerBevelShadowStroke = Stroke(width = 2f)

        val innerBevelHighlightColor = Color.White.copy(alpha = 0.20f)
        val innerBevelHighlightStroke = Stroke(width = 1f)

        // Thin Highlight along Bottom Wheel Rim
        val bottomRimHighlightBrush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.65f),
            ),
            startY = size.height * 0.65f,
            endY = size.height,
        )
        val bottomRimHighlightStroke = Stroke(width = 1.2f)

        onDrawBehind {
            // Rubber Micro-Stippling
            drawPath(brightStipplePath, color = Color.White.copy(alpha = 0.04f))
            drawPath(darkStipplePath, color = Color.Black.copy(alpha = 0.03f))

            // Outer Bevel Rim
            drawCircle(
                color = outerBevelShadowColor,
                radius = radius - 1f,
                style = outerBevelStroke,
            )
            drawCircle(
                brush = outerSpecularBrush,
                radius = radius - 2.5f,
                style = outerSpecularStroke,
            )

            // Thin specular highlight along bottom of outer wheel rim
            drawCircle(
                brush = bottomRimHighlightBrush,
                radius = radius - 1f,
                style = bottomRimHighlightStroke,
            )

            // Inner Bevel Rim
            drawCircle(
                color = innerBevelShadowColor,
                radius = innerRadius + 1f,
                style = innerBevelShadowStroke,
            )
            drawCircle(
                color = innerBevelHighlightColor,
                radius = innerRadius + 2.5f,
                style = innerBevelHighlightStroke,
            )
        }
    }

    // Cached drawing modifier for concave center button
    val concaveCenterButtonDrawModifier = Modifier.drawWithCache {
        val btnRadius = size.minDimension / 2f
        // Radial Metallic Glare Texture
        val metalGlareBrush = Brush.radialGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.12f),
                themePreset.bodyPrimary.copy(alpha = 0.05f),
                Color.Transparent,
            ),
            center = Offset(size.width * 0.35f, size.height * 0.35f),
            radius = btnRadius * 1.2f,
        )

        val btnHairlinesLightPath = Path()
        val btnHairlinesDarkPath = Path()
        val lineCount = (size.height / 6f).toInt().coerceAtLeast(10)
        val step = size.height / lineCount
        for (i in 0..lineCount) {
            val y = i * step
            val isLight = (i % 3) == 0
            val targetPath = if (isLight) btnHairlinesLightPath else btnHairlinesDarkPath
            targetPath.moveTo(0f, y)
            targetPath.lineTo(size.width, y + 8f)
        }

        val btnBrightDotsPath = Path()
        val btnDarkDotsPath = Path()
        val cols = (size.width / 5f).toInt().coerceAtMost(40)
        val rows = (size.height / 5f).toInt().coerceAtMost(40)
        val colStep = size.width / cols
        val rowStep = size.height / rows
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                val hash = ((r * 733) + (c * 433)) % 100
                if (hash < 30) {
                    val px = c * colStep + (hash % 4)
                    val py = r * rowStep + ((hash * 3) % 4)
                    val isBright = (hash % 2) == 0
                    val targetPath = if (isBright) btnBrightDotsPath else btnDarkDotsPath
                    targetPath.addOval(
                        Rect(
                            center = Offset(px, py),
                            radius = 0.8f,
                        )
                    )
                }
            }
        }

        val isLightWheel = themePreset == ThemePreset.SILVER ||
                (themePreset.wheelColor.red > 0.5f && themePreset.wheelColor.green > 0.5f && themePreset.wheelColor.blue > 0.5f)

        // Radial Concave Dish Shading
        val centerDipShadowBrush = Brush.radialGradient(
            colors = listOf(
                Color.Black.copy(alpha = if (isCenterPressed) 0.22f else if (isLightWheel) 0.03f else 0.0f),
                Color.Black.copy(alpha = if (isLightWheel) 0.1f else 0.22f),
                Color.Transparent,
            ),
            center = Offset(size.width / 2f, size.height / 2f),
            radius = btnRadius,
        )

        val outerRimHighlightBrush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                Color.White.copy(alpha = if (isLightWheel) 0.08f else 0.04f),
                Color.White.copy(alpha = if (isCenterPressed) 0.25f else if (isLightWheel) 0.18f else 0.06f),
            ),
            center = Offset(size.width * 0.40f, size.height * 0.40f),
            radius = btnRadius * 1.1f,
        )

        val topInnerShadowBrush = Brush.radialGradient(
            colors = listOf(
                Color.Black.copy(alpha = if (isLightWheel) 0.08f else 0.14f),
                Color.Transparent,
            ),
            center = Offset(size.width * 0.30f, size.height * 0.30f),
            radius = btnRadius * 0.90f,
        )

        val bottomSeamShadowBrush = Brush.verticalGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = if (isLightWheel) 0.12f else 0.22f),
            ),
            startY = size.height * 0.70f,
            endY = size.height,
        )

        val seamColor = Color.Black.copy(alpha = if (isLightWheel) 0.35f else 0.50f)
        val seamStroke = Stroke(width = 1.5f)

        onDrawBehind {
            // 1. Metallic Texture matching chassis
            drawRect(brush = metalGlareBrush)
            drawPath(btnHairlinesLightPath, Color.White.copy(alpha = 0.04f), style = Stroke(width = 1f))
            drawPath(btnHairlinesDarkPath, Color.Black.copy(alpha = 0.02f), style = Stroke(width = 1f))
            drawPath(btnBrightDotsPath, Color.White.copy(alpha = 0.04f))
            drawPath(btnDarkDotsPath, Color.Black.copy(alpha = 0.05f))

            // 2. Concave Dish Shading (Radial)
            drawCircle(brush = centerDipShadowBrush, radius = btnRadius)
            drawCircle(brush = topInnerShadowBrush, radius = btnRadius)
            drawCircle(brush = outerRimHighlightBrush, radius = btnRadius)

            // 3. Bottom subtle dark seam shading near lower edge
            drawCircle(brush = bottomSeamShadowBrush, radius = btnRadius - 1f, style = seamStroke)

            // 4. Recessed Border Seam Line
            drawCircle(color = seamColor, radius = btnRadius - 1f, style = seamStroke)
        }
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        themePreset.wheelColor,
                        themePreset.wheelColor.copy(alpha = 0.96f),
                        themePreset.wheelColor.copy(alpha = 0.88f),
                    )
                )
            )
            .then(wheelRingDrawModifier)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    val width = size.width.toFloat()
                    val height = size.height.toFloat()
                    val centerX = width / 2f
                    val centerY = height / 2f
                    val outerRadius = minOf(centerX, centerY)
                    val innerRadius = outerRadius * 0.36f

                    val downPos = down.position
                    val dx = downPos.x - centerX
                    val dy = downPos.y - centerY
                    val distFromCenter = sqrt((dx * dx) + (dy * dy))

                    val touchMode = when {
                        distFromCenter <= innerRadius -> TouchMode.CENTER_BUTTON
                        distFromCenter <= outerRadius -> TouchMode.WHEEL_RING
                        else -> TouchMode.NONE
                    }

                    if (touchMode == TouchMode.CENTER_BUTTON) {
                        isCenterPressed = true
                    }

                    var lastAngle = atan2(dy, dx) * (180f / PI.toFloat())
                    var accumulatedAngle = 0f
                    var lastTimeMs = System.currentTimeMillis()
                    val pressStartTime = System.currentTimeMillis()
                    var lastHoldTickTime = 0L
                    var isHoldTriggered = false
                    var hasDragged = false

                    val detentBaseAngle = 20.0f // 18 detents per 360 degrees

                    do {
                        val event = awaitPointerEvent()
                        val change: PointerInputChange? = event.changes.firstOrNull { it.pressed }

                        if (change != null) {
                            val curX = change.position.x - centerX
                            val curY = change.position.y - centerY

                            if (touchMode == TouchMode.WHEEL_RING) {
                                val curAngle = atan2(curY, curX) * (180f / PI.toFloat())
                                var deltaAngle = curAngle - lastAngle

                                // Handle 180 / -180 boundary wrapping
                                if (deltaAngle > 180f) deltaAngle -= 360f
                                if (deltaAngle < -180f) deltaAngle += 360f

                                val nowMs = System.currentTimeMillis()
                                val dt = max(nowMs - lastTimeMs, 1L)
                                val angularSpeed = abs(deltaAngle) / dt.toFloat() // deg/ms

                                // Acceleration factor for fast spins
                                val accelMultiplier = when {
                                    angularSpeed >= 0.8f -> 2.5f
                                    angularSpeed >= 0.35f -> 1.5f
                                    else -> 1.0f
                                }

                                if (abs(deltaAngle) >= 1.0f) {
                                    hasDragged = true
                                    accumulatedAngle += deltaAngle * accelMultiplier

                                    while (accumulatedAngle >= detentBaseAngle) {
                                        if (onWheelEvent != null) {
                                            onWheelEvent.invoke(WheelEvent.Scroll(1))
                                        } else {
                                            onScrollDetents(1)
                                        }
                                        onDetentTick()
                                        accumulatedAngle -= detentBaseAngle
                                    }
                                    while (accumulatedAngle <= -detentBaseAngle) {
                                        if (onWheelEvent != null) {
                                            onWheelEvent.invoke(WheelEvent.Scroll(-1))
                                        } else {
                                            onScrollDetents(-1)
                                        }
                                        onDetentTick()
                                        accumulatedAngle += detentBaseAngle
                                    }
                                }

                                lastAngle = curAngle
                                lastTimeMs = nowMs
                            }

                            // Check for Hold/Long press if user hasn't dragged
                            if (!hasDragged) {
                                val nowMs = System.currentTimeMillis()
                                val pressDuration = nowMs - pressStartTime
                                if ((pressDuration >= 350L) && ((nowMs - lastHoldTickTime) >= 200L)) {
                                    isHoldTriggered = true
                                    lastHoldTickTime = nowMs
                                    val tapAngle = atan2(dy, dx) * (180f / PI.toFloat())
                                    if (touchMode == TouchMode.WHEEL_RING || distFromCenter <= outerRadius) {
                                        if (onWheelEvent != null) {
                                            when (tapAngle) {
                                                in -135f..-45f -> onWheelEvent.invoke(WheelEvent.MenuPress(isHold = true))
                                                in -45f..45f -> onWheelEvent.invoke(WheelEvent.NextPress(isHold = true))
                                                in 45f..135f -> onWheelEvent.invoke(WheelEvent.PlayPausePress(isHold = true))
                                                else -> onWheelEvent.invoke(WheelEvent.PrevPress(isHold = true))
                                            }
                                        } else {
                                            when (tapAngle) {
                                                in -135f..-45f -> onMenuClick()
                                                in -45f..45f -> onNextHold?.invoke()
                                                in 45f..135f -> onPlayPauseClick()
                                                else -> onPrevHold?.invoke()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    // Handle Touch Release (Tap vs Drag vs Hold)
                    if (!hasDragged && !isHoldTriggered) {
                        val tapAngle = atan2(dy, dx) * (180f / PI.toFloat())
                        if (onWheelEvent != null) {
                            when {
                                touchMode == TouchMode.CENTER_BUTTON || distFromCenter <= innerRadius -> {
                                    onWheelEvent.invoke(WheelEvent.SelectPress(isHold = false))
                                }
                                touchMode == TouchMode.WHEEL_RING || distFromCenter <= outerRadius -> {
                                    when (tapAngle) {
                                        in -135f..-45f -> onWheelEvent.invoke(WheelEvent.MenuPress(isHold = false))
                                        in -45f..45f -> onWheelEvent.invoke(WheelEvent.NextPress(isHold = false))
                                        in 45f..135f -> onWheelEvent.invoke(WheelEvent.PlayPausePress(isHold = false))
                                        else -> onWheelEvent.invoke(WheelEvent.PrevPress(isHold = false))
                                    }
                                }
                            }
                        } else {
                            when {
                                touchMode == TouchMode.CENTER_BUTTON || distFromCenter <= innerRadius -> {
                                    onCenterClick()
                                }
                                touchMode == TouchMode.WHEEL_RING || distFromCenter <= outerRadius -> {
                                    when (tapAngle) {
                                        in -135f..-45f -> onMenuClick()
                                        in -45f..45f -> onNextClick()
                                        in 45f..135f -> onPlayPauseClick()
                                        else -> onPrevClick()
                                    }
                                }
                            }
                        }
                    }

                    isCenterPressed = false
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        // Directional Labels
        // MENU (Top)
        Text(
            text = "MENU",
            color = themePreset.wheelButtonTextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Default,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (sizeDp * 0.08f)),
        )

        // PREV / Skip Back (Left)
        Icon(
            imageVector = Icons.Rounded.SkipPrevious,
            contentDescription = "Previous",
            tint = themePreset.wheelButtonTextColor,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(x = (sizeDp * 0.06f))
                .size(32.dp),
        )

        // NEXT / Skip Forward (Right)
        Icon(
            imageVector = Icons.Rounded.SkipNext,
            contentDescription = "Next",
            tint = themePreset.wheelButtonTextColor,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = -(sizeDp * 0.06f))
                .size(32.dp),
        )

        // PLAY / PAUSE (Bottom) - show Play and Pause icons closely spaced
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = -(sizeDp * 0.06f)),
            horizontalArrangement = Arrangement.spacedBy((-10).dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = "Play",
                tint = themePreset.wheelButtonTextColor,
                modifier = Modifier.size(28.dp),
            )
            Icon(
                imageVector = Icons.Rounded.Pause,
                contentDescription = "Pause",
                tint = themePreset.wheelButtonTextColor,
                modifier = Modifier.size(28.dp),
            )
        }

        // Concave Middle Center Button
        Box(
            modifier = Modifier
                .size(sizeDp * 0.36f)
                .scale(centerScale)
                .shadow(elevation = 1.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            themePreset.bodySecondary,
                            themePreset.bodyPrimary,
                            themePreset.bodyPrimary,
                        )
                    )
                )
                .then(concaveCenterButtonDrawModifier),
            contentAlignment = Alignment.Center,
        ) {}
    }
}

@Preview(showBackground = true)
@Composable
fun ClickWheelPreview() {
    MyPodTheme {
        ClickWheel(
            themePreset = ThemePreset.SPACE_GRAY,
            onScrollDetents = {},
            onDetentTick = {},
            onCenterClick = {},
            onMenuClick = {},
            onPrevClick = {},
            onNextClick = {},
            onPlayPauseClick = {}
        )
    }
}


