package com.trishit.mypod.ui.components


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.trishit.mypod.data.theme.ThemePreset

/**
 * Modifier that efficiently renders metallic grain texture layer
 * (specular glare, brushed hairlines, micro noise dots) using [drawWithCache]
 * to prevent allocations during recomposition and redraws.
 */
fun Modifier.brushedMetalTexture(
    themePreset: ThemePreset,
): Modifier = this.drawWithCache {
    val width = size.width
    val height = size.height

    // 1. Specular metallic glare band tinted with theme body tone
    val glareBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.12f),
            themePreset.bodyPrimary.copy(alpha = 0.05f),
            themePreset.bodySecondary.copy(alpha = 0.15f),
            Color.White.copy(alpha = 0.08f),
        ),
        start = Offset.Zero,
        end = Offset(width, height),
    )

    // 2. Cached Paths for Fine Horizontal/Diagonal Brushed Hairlines
    val lightHairlinesPath = Path()
    val darkHairlinesPath = Path()
    val lineCount = (height / 8f).toInt().coerceAtLeast(20)
    val step = height / lineCount

    for (i in 0..lineCount) {
        val y = i * step
        val isLight = (i % 3) == 0
        val targetPath = if (isLight) lightHairlinesPath else darkHairlinesPath
        targetPath.moveTo(0f, y)
        targetPath.lineTo(width, y + 12f)
    }

    // 3. Cached Paths for Grain / Micro-Noise Dots
    val brightDotsPath = Path()
    val darkDotsPath = Path()
    val grainCols = (width / 6f).toInt().coerceAtMost(80)
    val grainRows = (height / 6f).toInt().coerceAtMost(120)
    val colStep = width / grainCols
    val rowStep = height / grainRows

    for (r in 0 until grainRows) {
        for (c in 0 until grainCols) {
            val hash = ((r * 733) + (c * 433)) % 100
            if (hash < 35) { // ~35% density of noise dots
                val px = c * colStep + (hash % 5)
                val py = r * rowStep + ((hash * 3) % 5)
                val isBright = (hash % 2) == 0
                val radius = 0.8f + (hash % 3) * 0.3f
                val targetPath = if (isBright) brightDotsPath else darkDotsPath
                targetPath.addOval(
                    Rect(
                        center = Offset(px, py),
                        radius = radius,
                    )
                )
            }
        }
    }

    onDrawBehind {
        // Specular Glare
        drawRect(brush = glareBrush)

        // Brushed Hairlines (2 GPU drawPath calls)
        drawPath(
            path = lightHairlinesPath,
            color = Color.White.copy(alpha = 0.04f),
            style = Stroke(width = 1.2f),
        )
        drawPath(
            path = darkHairlinesPath,
            color = Color.Black.copy(alpha = 0.02f),
            style = Stroke(width = 1.2f),
        )

        // Micro-Noise Dots (2 GPU drawPath calls)
        drawPath(
            path = brightDotsPath,
            color = Color.White.copy(alpha = 0.04f),
        )
        drawPath(
            path = darkDotsPath,
            color = Color.Black.copy(alpha = 0.05f),
        )
    }
}

/**
 * Modifier that draws claymorphism 3D rounded inner bevels
 * (top-left light highlight & bottom-right depth shadow) using [drawWithCache].
 */
fun Modifier.claymorphismBevels(
    cornerRadiusPx: Float,
    highlightAlpha: Float = 0.28f,
    shadowAlpha: Float = 0.32f,
): Modifier = this.drawWithCache {
    val highlightBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = highlightAlpha),
            Color.White.copy(alpha = highlightAlpha * 0.3f),
            Color.Transparent,
        ),
        start = Offset.Zero,
        end = Offset(size.width * 0.5f, size.height * 0.5f),
    )

    val shadowBrush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = shadowAlpha * 0.3f),
            Color.Black.copy(alpha = shadowAlpha),
        ),
        start = Offset(size.width * 0.4f, size.height * 0.4f),
        end = Offset(size.width, size.height),
    )

    val cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
    val highlightStroke = Stroke(width = 2.0f)
    val shadowStroke = Stroke(width = 2.2f)

    onDrawBehind {
        // Top-Left 3D Light Highlight Bevel
        drawRoundRect(
            brush = highlightBrush,
            size = size,
            cornerRadius = cornerRadius,
            style = highlightStroke,
        )

        // Bottom-Right 3D Depth Shadow Bevel
        drawRoundRect(
            brush = shadowBrush,
            size = size,
            cornerRadius = cornerRadius,
            style = shadowStroke,
        )
    }
}

/**
 * Direct [DrawScope] helper for drawing grainy metal texture where Modifier cannot be used.
 */
fun DrawScope.drawGrainyMetalTexture(
    bodyPrimary: Color,
    bodySecondary: Color,
) {
    // Specular glare
    drawRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.12f),
                bodyPrimary.copy(alpha = 0.05f),
                bodySecondary.copy(alpha = 0.15f),
                Color.White.copy(alpha = 0.08f),
            ),
            start = Offset.Zero,
            end = Offset(size.width, size.height),
        ),
    )

    // Brushed Hairlines Path
    val lightPath = Path()
    val darkPath = Path()
    val lineCount = (size.height / 8f).toInt().coerceAtLeast(15)
    val step = size.height / lineCount
    for (i in 0..lineCount) {
        val y = i * step
        val isLight = (i % 3) == 0
        val targetPath = if (isLight) lightPath else darkPath
        targetPath.moveTo(0f, y)
        targetPath.lineTo(size.width, y + 12f)
    }
    drawPath(lightPath, Color.White.copy(alpha = 0.04f), style = Stroke(width = 1.2f))
    drawPath(darkPath, Color.Black.copy(alpha = 0.02f), style = Stroke(width = 1.2f))

    // Micro noise dots
    val brightDots = Path()
    val darkDots = Path()
    val grainCols = (size.width / 6f).toInt().coerceAtMost(60)
    val grainRows = (size.height / 6f).toInt().coerceAtMost(80)
    val colStep = size.width / grainCols
    val rowStep = size.height / grainRows

    for (r in 0 until grainRows) {
        for (c in 0 until grainCols) {
            val hash = ((r * 733) + (c * 433)) % 100
            if (hash < 35) {
                val px = c * colStep + (hash % 5)
                val py = r * rowStep + ((hash * 3) % 5)
                val isBright = (hash % 2) == 0
                val targetPath = if (isBright) brightDots else darkDots
                targetPath.addOval(
                    Rect(
                        center = Offset(px, py),
                        radius = 0.9f,
                    )
                )
            }
        }
    }
    drawPath(brightDots, Color.White.copy(alpha = 0.04f))
    drawPath(darkDots, Color.Black.copy(alpha = 0.05f))
}

/**
 * Direct [DrawScope] helper for drawing claymorphism 3D rounded inner bevels.
 */
fun DrawScope.drawClaymorphismBevels(
    cornerRadiusPx: Float,
    highlightAlpha: Float = 0.28f,
    shadowAlpha: Float = 0.32f,
) {
    val cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)

    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = highlightAlpha),
                Color.White.copy(alpha = highlightAlpha * 0.3f),
                Color.Transparent,
            ),
            start = Offset.Zero,
            end = Offset(size.width * 0.5f, size.height * 0.5f),
        ),
        size = size,
        cornerRadius = cornerRadius,
        style = Stroke(width = 2.0f),
    )

    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                Color.Black.copy(alpha = shadowAlpha * 0.3f),
                Color.Black.copy(alpha = shadowAlpha),
            ),
            start = Offset(size.width * 0.4f, size.height * 0.4f),
            end = Offset(size.width, size.height),
        ),
        size = size,
        cornerRadius = cornerRadius,
        style = Stroke(width = 2.2f),
    )
}

@Composable
fun BrushedMetalBackground(
    themePreset: ThemePreset,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    MetallicBackground(
        themePreset = themePreset,
        modifier = modifier,
        content = content
    )
}

