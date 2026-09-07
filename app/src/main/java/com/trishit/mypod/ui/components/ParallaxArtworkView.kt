package com.trishit.mypod.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.trishit.mypod.R

@Composable
fun ParallaxArtworkView(
    artUri: String?,
    modifier: Modifier = Modifier,
    sensorPitch: Float = 0f,
    sensorRoll: Float = 0f,
    onClick: (() -> Unit)? = null
) {
    var dragX by remember { mutableFloatStateOf(0f) }
    var dragY by remember { mutableFloatStateOf(0f) }

    val targetRotX = (sensorPitch * 0.7f + dragY).coerceIn(-35f, 35f)
    val targetRotY = (sensorRoll * 0.7f + dragX).coerceIn(-35f, 35f)

    val animatedRotX by animateFloatAsState(
        targetValue = targetRotX,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
        label = "rotX"
    )
    val animatedRotY by animateFloatAsState(
        targetValue = targetRotY,
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.75f),
        label = "rotY"
    )

    val density = LocalDensity.current.density

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxHeight()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        dragX = 0f
                        dragY = 0f
                    },
                    onDragCancel = {
                        dragX = 0f
                        dragY = 0f
                    }
                ) { change, dragAmount ->
                    change.consume()
                    dragX += dragAmount.x * 0.2f
                    dragY -= dragAmount.y * 0.2f
                }
            }
    ) {
        // Main 3D Artwork Card
        Box(
            modifier = Modifier
                .weight(1f, fill = false)
                .aspectRatio(1f, matchHeightConstraintsFirst = true)
                .graphicsLayer {
                    rotationX = animatedRotX
                    rotationY = animatedRotY
                    cameraDistance = 16f * density
                }
                .shadow(8.dp, RoundedCornerShape(6.dp))
                .clip(RoundedCornerShape(6.dp))
                .border(1.dp, Color(0xFFCCCCCC), RoundedCornerShape(6.dp))
                .background(Color(0xFFDDDDDD)),
            contentAlignment = Alignment.Center
        ) {
            if (!artUri.isNullOrEmpty()) {
                AsyncImage(
                    model = artUri,
                    contentDescription = "3D Album Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.placeholder),
                    contentDescription = "Placeholder Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Glass Reflection Gloss Sweep
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent,
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Ground Reflection Mirror Glass Effect
        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(20.dp)
                .graphicsLayer {
                    rotationX = animatedRotX
                    rotationY = animatedRotY
                    scaleY = -1f // Invert vertically
                    cameraDistance = 16f * density
                }
                .drawWithContent {
                    drawContent()
                    // Apply linear fade gradient mask over inverted reflection
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xFFF2F4F7).copy(alpha = 0.7f),
                                Color(0xFFE5E8ED)
                            )
                        ),
                        blendMode = BlendMode.SrcOver
                    )
                }
                .clip(RoundedCornerShape(4.dp))
                .background(Color.White.copy(alpha = 0.15f))
        ) {
            if (!artUri.isNullOrEmpty()) {
                AsyncImage(
                    model = artUri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 0.35f }
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.placeholder),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { alpha = 0.35f }
                )
            }
        }
    }
}
