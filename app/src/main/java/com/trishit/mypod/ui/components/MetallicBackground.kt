package com.trishit.mypod.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trishit.mypod.data.theme.ThemePreset
import org.intellij.lang.annotations.Language

// --- METALLIC COLOR PALETTES (Recreating reference images) ---
val MetallicDarkGradient = listOf(
    Color(0xFF5E6164), // Bright top reflection
    Color(0xFF808488), // Upper mid-tone highlight
    Color(0xFF4A4D50), // Center baseline gray
    Color(0xFF323538), // Bottom lower mid-tone gray
    Color(0xFF242628)  // Bottom subtle dark gray
)

val MetallicLightGradient = listOf(
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
 * to perfectly recreate the metallic gradient, perimeter edge vignette, top chamfer highlight,
 * and sandblasted anodized metal noise texture.
 *
 * @param modifier Custom modifier for layout sizing and positioning.
 * @param isLightMode Toggle between Silver (Light) and Space Gray (Dark) metallic chassis styles.
 * @param content Optional composable content to render over the background layer.
 */
@Composable
fun MetallicBackground(
    modifier: Modifier = Modifier,
    isLightMode: Boolean = false,
    content: (@Composable () -> Unit)? = null
) {
    val colors = if (isLightMode) MetallicLightGradient else MetallicDarkGradient

    // AGSL Shader configuration for API 33+ (Android 13 Tiramisu)
    val metallicRenderEffect = remember(isLightMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val runtimeShader = RuntimeShader(METALLIC_GRAIN_AGSL)
            // Intensity tuned for anodized finish (0.035 for silver, 0.050 for dark)
            runtimeShader.setFloatUniform("grainIntensity", if (isLightMode) 0.035f else 0.050f)
            RenderEffect.createRuntimeShaderEffect(runtimeShader, "inputTexture").asComposeRenderEffect()
        } else {
            null // Fallback to Canvas drawWithCache noise paths on older APIs
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
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

                    // 1. Structural vertical linear illumination gradient
                    val baseGradientBrush = Brush.verticalGradient(
                        colors = colors,
                        startY = 0f,
                        endY = h
                    )

                    // 2. Top Perimeter Inner Edge Shadow (Top Bezel Depth)
                    val topShadowBrush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = if (isLightMode) 0.28f else 0.42f),
                            Color.Black.copy(alpha = if (isLightMode) 0.08f else 0.15f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = h * 0.12f
                    )

                    // 3. Bottom Perimeter Inner Edge Shadow (Subtle Vignette Lip)
                    val bottomShadowBrush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = if (isLightMode) 0.06f else 0.10f),
                            Color.Black.copy(alpha = if (isLightMode) 0.18f else 0.26f)
                        ),
                        startY = h * 0.85f,
                        endY = h
                    )

                    // 4. Left & Right Perimeter Side Edge Shadows
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

                    // 5. Smooth Corner Radial Vignette
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

                    // 6. Top Specular Bevel Chamfer Highlight
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

                    // 7. Fallback fine grain noise paths for pre-Tiramisu APIs
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

                    onDrawBehind {
                        // Draw structural vertical gradient
                        drawRect(brush = baseGradientBrush)

                        // Draw perimeter vignette edge shadow layers
                        drawRect(brush = topShadowBrush)
                        drawRect(brush = bottomShadowBrush)
                        drawRect(brush = leftShadowBrush)
                        drawRect(brush = rightShadowBrush)
                        drawRect(brush = cornerVignetteBrush)

                        // Draw top chamfer rim line
                        drawRect(
                            brush = topRimHighlightBrush,
                            topLeft = Offset.Zero,
                            size = Size(w, 2.dp.toPx())
                        )

                        // Draw fallback noise paths if not running AGSL shader
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
        )

        // Render child composables over background surface
        content?.invoke()
    }
}

/**
 * Overload allowing [ThemePreset] driven metallic backgrounds for MyPod theme engine.
 */
@Composable
fun MetallicBackground(
    themePreset: ThemePreset,
    modifier: Modifier = Modifier,
    content: (@Composable () -> Unit)? = null
) {
    val isLightMode = themePreset == ThemePreset.SILVER
    val customColors = remember(themePreset) {
        when (themePreset) {
            ThemePreset.SILVER -> MetallicLightGradient
            ThemePreset.SPACE_GRAY -> MetallicDarkGradient
            else -> listOf(
                themePreset.bodyPrimary.copy(alpha = 0.9f),
                themePreset.bodyPrimary,
                themePreset.bodySecondary,
                themePreset.bodySecondary.copy(alpha = 0.95f),
                Color.Black.copy(alpha = 0.85f)
            )
        }
    }

    val metallicRenderEffect = remember(themePreset) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val runtimeShader = RuntimeShader(METALLIC_GRAIN_AGSL)
            runtimeShader.setFloatUniform("grainIntensity", if (isLightMode) 0.035f else 0.050f)
            RenderEffect.createRuntimeShaderEffect(runtimeShader, "inputTexture").asComposeRenderEffect()
        } else {
            null
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
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

                    val baseGradientBrush = Brush.verticalGradient(
                        colors = customColors,
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
                    }
                }
        )

        content?.invoke()
    }
}

@Preview(name = "Dark Metallic Background", showBackground = true, widthDp = 320, heightDp = 568)
@Composable
private fun PreviewMetallicDark() {
    MetallicBackground(isLightMode = false)
}

@Preview(name = "Light Metallic Background", showBackground = true, widthDp = 320, heightDp = 568)
@Composable
private fun PreviewMetallicLight() {
    MetallicBackground(isLightMode = true)
}
