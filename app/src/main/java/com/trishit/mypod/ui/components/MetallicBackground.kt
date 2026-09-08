package com.trishit.mypod.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.trishit.mypod.data.theme.ChassisStyle
import com.trishit.mypod.data.theme.ThemePreset
import org.intellij.lang.annotations.Language

// --- METALLIC COLOR PALETTES ---
val MetallicDarkGradient = listOf(
    Color(0xFF6E7072), // Upper-left clay specular
    Color(0xFF5E6164), // Upper mid-tone highlight
    Color(0xFF4A4C4E), // Center baseline gray body
    Color(0xFF323538), // Bottom lower mid-tone gray
    Color(0xFF2E2F30)  // Lower-right dark falloff shadow
)

val MetallicLightGradient = listOf(
    Color(0xFFFFFFFF), // Upper-left pure white clay specular
    Color(0xFFF7F8FA), // Upper mid-tone highlight
    Color(0xFFDCDEE0), // Main middle aluminum body
    Color(0xFFC7CACD), // Warm lower mid-tone
    Color(0xFFA6A9AC)  // Lower-right subtle dark vignette lip
)

// Legacy Classic Palettes for flat vertical lighting
val ClassicMetallicDarkGradient = listOf(
    Color(0xFF5E6164), // Bright top reflection
    Color(0xFF808488), // Upper mid-tone highlight
    Color(0xFF4A4D50), // Center baseline gray
    Color(0xFF323538), // Bottom lower mid-tone gray
    Color(0xFF242628)  // Bottom subtle dark gray
)

val ClassicMetallicLightGradient = listOf(
    Color(0xFFF7F8FA), // Bright top reflection
    Color(0xFFFFFFFF), // Upper pure white glow
    Color(0xFFDCDEE0), // Main middle aluminum body
    Color(0xFFC7CACD), // Warm lower mid-tone
    Color(0xFFA6A9AC)  // Bottom subtle dark vignette lip
)

@Language("AGSL")
private const val METALLIC_GRAIN_AGSL = """
    uniform shader inputTexture;
    uniform float grainIntensity; // Controls visibility of fine noise

    // Pseudo-random number generator for fine frosted grain
    float rand(vec2 co) {
        return fract(sin(dot(co, vec2(12.9898, 78.233))) * 43758.5453);
    }

    vec4 main(vec2 coords) {
        vec4 color = inputTexture.eval(coords);
        // Generate fine frosted grain noise mapped between -0.5 and 0.5
        float noise = (rand(coords) - 0.5) * grainIntensity;
        // Add noise uniformly to RGB channels
        return vec4(color.rgb + vec3(noise), color.a);
    }
"""

/**
 * Custom layout using Compose low-level graphics Canvas APIs and AGSL runtime shaders
 * supporting both 3D Claymorphic and Classic Metallic iPod chassis rendering styles.
 *
 * @param modifier Custom modifier for layout sizing and positioning.
 * @param isLightMode Toggle between Silver (Light) and Space Gray (Dark) metallic chassis styles.
 * @param chassisStyle Defines whether to draw 3D Claymorphic or Classic Metallic chassis graphics.
 * @param cornerRadius Corner radius for the 3D claymorphic rounded chassis corners (default: 28.dp).
 * @param content Optional composable content to render over the background layer.
 */
@Composable
fun MetallicBackground(
    modifier: Modifier = Modifier,
    isLightMode: Boolean = false,
    chassisStyle: ChassisStyle = ChassisStyle.CLAYMORPHIC_3D,
    cornerRadius: Dp = 28.dp,
    content: (@Composable () -> Unit)? = null
) {
    val isClaymorphic = chassisStyle == ChassisStyle.CLAYMORPHIC_3D
    val colors = if (isClaymorphic) {
        if (isLightMode) MetallicLightGradient else MetallicDarkGradient
    } else {
        if (isLightMode) ClassicMetallicLightGradient else ClassicMetallicDarkGradient
    }

    // AGSL Shader configuration for API 33+ (Android 13 Tiramisu)
    val metallicRenderEffect = remember(isLightMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val runtimeShader = RuntimeShader(METALLIC_GRAIN_AGSL)
            runtimeShader.setFloatUniform("grainIntensity", if (isLightMode) 0.035f else 0.050f)
            RenderEffect.createRuntimeShaderEffect(runtimeShader, "inputTexture").asComposeRenderEffect()
        } else {
            null
        }
    }

    val shapeModifier = if (isClaymorphic) Modifier.clip(RoundedCornerShape(cornerRadius)) else Modifier

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(shapeModifier)
    ) {
        // Background Surface canvas layer
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (metallicRenderEffect != null) {
                        Modifier.graphicsLayer {
                            renderEffect = metallicRenderEffect
                        }
                    } else {
                        Modifier
                    }
                )
                .drawWithCache {
                    val w = size.width
                    val h = size.height

                    // Pre-Tiramisu noise path generators
                    val brightNoisePath = Path()
                    val darkNoisePath = Path()
                    val isTiramisu = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU

                    if (!isTiramisu) {
                        val cols = (w / 4f).toInt().coerceIn(20, 100)
                        val rows = (h / 4f).toInt().coerceIn(30, 150)
                        val colStep = w / cols
                        val rowStep = h / rows

                        for (r in 0 until rows) {
                            for (c in 0 until cols) {
                                val hash = ((r * 92821) xor (c * 38609)) and 0x7FFFFFFF
                                val mod = hash % 100
                                if (mod < 32) {
                                    val px = c * colStep + (hash % 4)
                                    val py = r * rowStep + ((hash / 4) % 4)
                                    val isBright = (mod % 2) == 0
                                    val targetPath = if (isBright) brightNoisePath else darkNoisePath
                                    targetPath.addOval(Rect(center = Offset(px, py), radius = 0.8f))
                                }
                            }
                        }
                    }

                    if (isClaymorphic) {
                        // --- 3D CLAYMORPHIC CHASSIS RENDERING PIPELINE ---
                        val cornerRadiusPx = cornerRadius.toPx().coerceAtLeast(1f)
                        val chassisRoundRect = RoundRect(
                            rect = Rect(0f, 0f, w, h),
                            cornerRadius = CornerRadius(cornerRadiusPx)
                        )
                        val chassisPath = Path().apply { addRoundRect(chassisRoundRect) }

                        val baseGradientBrush = Brush.linearGradient(
                            colors = colors,
                            start = Offset(0f, 0f),
                            end = Offset(w, h)
                        )

                        val topLeftCornerHighlightBrush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isLightMode) 0.55f else 0.35f),
                                Color.White.copy(alpha = if (isLightMode) 0.20f else 0.10f),
                                Color.Transparent
                            ),
                            center = Offset(0f, 0f),
                            radius = cornerRadiusPx * 3.8f
                        )

                        val bottomRightCornerShadowBrush = Brush.radialGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = if (isLightMode) 0.38f else 0.55f),
                                Color.Black.copy(alpha = if (isLightMode) 0.15f else 0.25f),
                                Color.Transparent
                            ),
                            center = Offset(w, h),
                            radius = cornerRadiusPx * 3.8f
                        )

                        val topRightCornerShadowBrush = Brush.radialGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = if (isLightMode) 0.18f else 0.28f),
                                Color.Transparent
                            ),
                            center = Offset(w, 0f),
                            radius = cornerRadiusPx * 2.8f
                        )

                        val bottomLeftCornerHighlightBrush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isLightMode) 0.22f else 0.12f),
                                Color.Transparent
                            ),
                            center = Offset(0f, h),
                            radius = cornerRadiusPx * 2.8f
                        )

                        val topInnerHighlightBrush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isLightMode) 0.45f else 0.25f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = cornerRadiusPx * 1.5f
                        )

                        val leftInnerHighlightBrush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isLightMode) 0.40f else 0.22f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = cornerRadiusPx * 1.5f
                        )

                        val bottomInnerShadowBrush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = if (isLightMode) 0.25f else 0.40f)
                            ),
                            startY = h - cornerRadiusPx * 1.5f,
                            endY = h
                        )

                        val rightInnerShadowBrush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = if (isLightMode) 0.25f else 0.40f)
                            ),
                            startX = w - cornerRadiusPx * 1.5f,
                            endX = w
                        )

                        val rimStrokeWidth = 1.5.dp.toPx()
                        val rimBevelBrush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = if (isLightMode) 0.90f else 0.60f),
                                Color.White.copy(alpha = if (isLightMode) 0.40f else 0.20f),
                                Color.Black.copy(alpha = if (isLightMode) 0.35f else 0.55f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(w, h)
                        )

                        onDrawBehind {
                            drawPath(path = chassisPath, brush = baseGradientBrush)

                            drawPath(path = chassisPath, brush = topLeftCornerHighlightBrush)
                            drawPath(path = chassisPath, brush = bottomRightCornerShadowBrush)
                            drawPath(path = chassisPath, brush = topRightCornerShadowBrush)
                            drawPath(path = chassisPath, brush = bottomLeftCornerHighlightBrush)

                            drawPath(path = chassisPath, brush = topInnerHighlightBrush)
                            drawPath(path = chassisPath, brush = leftInnerHighlightBrush)
                            drawPath(path = chassisPath, brush = bottomInnerShadowBrush)
                            drawPath(path = chassisPath, brush = rightInnerShadowBrush)

                            drawPath(
                                path = chassisPath,
                                brush = rimBevelBrush,
                                style = Stroke(width = rimStrokeWidth)
                            )

                            if (!isTiramisu) {
                                drawPath(
                                    path = brightNoisePath,
                                    color = Color.White.copy(alpha = if (isLightMode) 0.05f else 0.06f)
                                )
                                drawPath(
                                    path = darkNoisePath,
                                    color = Color.Black.copy(alpha = if (isLightMode) 0.06f else 0.08f)
                                )
                            }
                        }
                    } else {
                        // --- CLASSIC METALLIC CHASSIS RENDERING PIPELINE (PREVIOUS IMPLEMENTATION) ---
                        val baseGradientBrush = Brush.verticalGradient(
                            colors = colors,
                            startY = 0f,
                            endY = h
                        )

                        val topShadowBrush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = if (isLightMode) 0.28f else 0.42f),
                                Color.Black.copy(alpha = if (isLightMode) 0.08f else 0.15f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = h * 0.12f
                        )

                        val bottomShadowBrush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = if (isLightMode) 0.06f else 0.10f),
                                Color.Black.copy(alpha = if (isLightMode) 0.18f else 0.26f)
                            ),
                            startY = h * 0.85f,
                            endY = h
                        )

                        val sideShadowWidth = w * 0.10f
                        val leftShadowBrush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = if (isLightMode) 0.22f else 0.35f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = sideShadowWidth
                        )

                        val rightShadowBrush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = if (isLightMode) 0.22f else 0.35f)
                            ),
                            startX = w - sideShadowWidth,
                            endX = w
                        )

                        val maxDim = maxOf(w, h)
                        val cornerVignetteBrush = Brush.radialGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Transparent,
                                Color.Black.copy(alpha = if (isLightMode) 0.25f else 0.38f)
                            ),
                            center = Offset(w / 2f, h / 2f),
                            radius = maxDim * 0.70f
                        )

                        val topRimHighlightBrush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = if (isLightMode) 0.85f else 0.40f),
                                Color.White.copy(alpha = if (isLightMode) 1.00f else 0.60f),
                                Color.White.copy(alpha = if (isLightMode) 0.85f else 0.40f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = w
                        )

                        onDrawBehind {
                            drawRect(brush = baseGradientBrush)
                            drawRect(brush = topShadowBrush)
                            drawRect(brush = bottomShadowBrush)
                            drawRect(brush = leftShadowBrush)
                            drawRect(brush = rightShadowBrush)
                            drawRect(brush = cornerVignetteBrush)

                            drawRect(
                                brush = topRimHighlightBrush,
                                topLeft = Offset.Zero,
                                size = Size(w, 2.dp.toPx())
                            )

                            if (!isTiramisu) {
                                drawPath(
                                    path = brightNoisePath,
                                    color = Color.White.copy(alpha = if (isLightMode) 0.05f else 0.06f)
                                )
                                drawPath(
                                    path = darkNoisePath,
                                    color = Color.Black.copy(alpha = if (isLightMode) 0.06f else 0.08f)
                                )
                            }
                        }
                    }
                }
        )

        content?.invoke()
    }
}

/**
 * Overload allowing [ThemePreset] driven metallic backgrounds for MyPod theme engine.
 * Automatically chooses between 3D Claymorphic and Classic Metallic pipelines based on [ThemePreset.chassisStyle].
 */
@Composable
fun MetallicBackground(
    themePreset: ThemePreset,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    content: (@Composable () -> Unit)? = null
) {
    val isLightMode = themePreset == ThemePreset.SILVER || themePreset == ThemePreset.CLASSIC_SILVER
    val chassisStyle = themePreset.chassisStyle

    MetallicBackground(
        modifier = modifier,
        isLightMode = isLightMode,
        chassisStyle = chassisStyle,
        cornerRadius = cornerRadius,
        content = content
    )
}

@Preview(name = "Dark Claymorphic Metallic", showBackground = true, widthDp = 320, heightDp = 568)
@Composable
private fun PreviewMetallicDark() {
    MetallicBackground(themePreset = ThemePreset.SPACE_GRAY)
}

@Preview(name = "Classic Dark Metallic", showBackground = true, widthDp = 320, heightDp = 568)
@Composable
private fun PreviewClassicMetallicDark() {
    MetallicBackground(themePreset = ThemePreset.CLASSIC_SPACE_GRAY)
}
