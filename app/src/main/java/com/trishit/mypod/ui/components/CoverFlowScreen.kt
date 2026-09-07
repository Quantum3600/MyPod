package com.trishit.mypod.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.trishit.mypod.R
import com.trishit.mypod.source.AlbumInfo
import kotlin.math.abs

@Composable
fun CoverFlowScreen(
    albums: List<AlbumInfo>,
    selectedIndex: Int,
    onAlbumSelect: (AlbumInfo) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeAlbums = if (albums.isEmpty()) {
        listOf(
            AlbumInfo(id = "def1", name = "Classic Classics", artist = "iPod Heritage", trackCount = 12),
            AlbumInfo(id = "def2", name = "Retro Hits", artist = "Apple Nostalgia", trackCount = 15),
            AlbumInfo(id = "def3", name = "Click Wheel Vibes", artist = "MyPod Studio", trackCount = 10),
            AlbumInfo(id = "def4", name = "Skeuomorphic Dreams", artist = "Cupertino Sound", trackCount = 8)
        )
    } else {
        albums
    }

    val safeIndex = selectedIndex.coerceIn(0, activeAlbums.lastIndex)
    val currentAlbum = activeAlbums[safeIndex]
    val density = LocalDensity.current.density

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E2026),
                        Color(0xFF121317),
                        Color(0xFF08080A)
                    )
                )
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Label Indicator
        Text(
            text = "COVER FLOW",
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF888899),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Center 3D Cover Flow Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Render albums in range [safeIndex - 4, safeIndex + 4]
            val range = (safeIndex - 4)..(safeIndex + 4)
            for (index in range) {
                if (index in activeAlbums.indices) {
                    val album = activeAlbums[index]
                    val offsetFromCenter = index - safeIndex

                    val animOffset by animateFloatAsState(
                        targetValue = offsetFromCenter.toFloat(),
                        animationSpec = spring(stiffness = 350f, dampingRatio = 0.8f),
                        label = "coverOffset"
                    )

                    val rotY = when {
                        animOffset == 0f -> 0f
                        animOffset < 0f -> 62f
                        else -> -62f
                    }

                    val scaleVal = if (abs(animOffset) < 0.1f) 1.15f else 0.82f
                    val zIndexVal = 100f - abs(animOffset) * 10f

                    // Spacing calculation
                    val baseSpacingDp = 48.dp
                    val centerExtraDp = 40.dp
                    val rawXOffset = if (animOffset == 0f) {
                        0.dp
                    } else if (animOffset < 0f) {
                        baseSpacingDp * animOffset - centerExtraDp
                    } else {
                        baseSpacingDp * animOffset + centerExtraDp
                    }

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(x = (rawXOffset.value * density).toInt(), y = 0) }
                            .zIndex(zIndexVal)
                            .size(100.dp)
                            .graphicsLayer {
                                rotationY = rotY
                                scaleX = scaleVal
                                scaleY = scaleVal
                                cameraDistance = 14f * density
                            }
                            .clickable {
                                onAlbumSelect(album)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Main Artwork Card
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .shadow(6.dp, RoundedCornerShape(4.dp))
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(0.5.dp, Color(0xFF555566), RoundedCornerShape(4.dp))
                                    .background(Color(0xFF22222E)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!album.artUri.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = album.artUri,
                                        contentDescription = album.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.placeholder),
                                        contentDescription = album.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                if (offsetFromCenter != 0) {
                                    // Side item darkening overlay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.35f))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Reflection below card
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(18.dp)
                                    .graphicsLayer {
                                        scaleY = -1f
                                    }
                                    .drawWithContent {
                                        drawContent()
                                        drawRect(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color(0xFF121317)
                                                )
                                            ),
                                            blendMode = BlendMode.SrcOver
                                        )
                                    }
                                    .graphicsLayer { alpha = 0.3f }
                            ) {
                                if (!album.artUri.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = album.artUri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Image(
                                        painter = painterResource(id = R.drawable.placeholder),
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Selected Album Info Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = currentAlbum.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = currentAlbum.artist,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFAAAABB),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "${currentAlbum.trackCount} Songs • Album ${safeIndex + 1} of ${activeAlbums.size}",
                fontSize = 8.5.sp,
                color = Color(0xFF777788)
            )
        }
    }
}
