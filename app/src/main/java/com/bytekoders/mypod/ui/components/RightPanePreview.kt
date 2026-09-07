package com.bytekoders.mypod.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.bytekoders.mypod.navigation.RightPaneContent
import com.bytekoders.mypod.source.NowPlayingState

@Composable
fun RightPanePreview(
    content: RightPaneContent,
    modifier: Modifier = Modifier,
    nowPlayingState: NowPlayingState = NowPlayingState()
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF9FAFB),
                        Color(0xFFEDEDF0)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when (content) {
            is RightPaneContent.DefaultArtwork -> DefaultArtworkCard(nowPlayingState)
            is RightPaneContent.MusicCategory -> CategoryCard(
                icon = Icons.Rounded.MusicNote,
                title = content.categoryName
            )
            is RightPaneContent.ThemePreview -> ThemePreviewCard(content)
            is RightPaneContent.GamePreview -> GamePreviewCard(content)
            is RightPaneContent.ExternalLinkPreview -> ExternalLinkCard(content)
            is RightPaneContent.ActionPreview -> ActionCard(content)
        }
    }
}

@Composable
private fun DefaultArtworkCard(nowPlayingState: NowPlayingState) {
    val track = nowPlayingState.currentTrack
    if (track != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(6.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2C3E50),
                                Color(0xFF4CA1AF)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (track.artUri != null) {
                    AsyncImage(
                        model = track.artUri,
                        contentDescription = track.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Rounded.MusicNote,
                        contentDescription = "Music",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(44.dp)
                    )
                }

                // Gloss Reflection
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height * 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (nowPlayingState.isPlaying) Color(0xFF2E7D32) else Color(0xFFC62828))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (nowPlayingState.isPlaying) "▶ Playing" else "⏸ Paused",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = track.title,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF111111),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = track.artist,
                fontSize = 9.sp,
                color = Color(0xFF555555),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .shadow(6.dp, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF2C3E50),
                                Color(0xFF4CA1AF)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.MusicNote,
                    contentDescription = "Music",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(44.dp)
                )

                // Gloss Reflection
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f),
                                Color.Transparent
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height * 0.5f)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "MyPod Classic",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = Color(0xFF333333)
            )
            Text(
                text = "No Track Playing",
                fontSize = 9.5.sp,
                color = Color(0xFF777777)
            )
        }
    }
}

@Composable
private fun CategoryCard(icon: ImageVector, title: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E88E5),
                            Color(0xFF1565C0)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFF222222),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ThemePreviewCard(content: RightPaneContent.ThemePreview) {
    val preset = content.preset
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {
        // Mini iPod Body Swatch
        Box(
            modifier = Modifier
                .size(60.dp, 84.dp)
                .shadow(6.dp, RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            preset.bodyPrimary,
                            preset.bodySecondary
                        )
                    )
                )
                .padding(6.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                // Mini Screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color.Black)
                        .padding(1.dp)
                        .background(preset.screenBackground)
                )

                // Mini Click Wheel
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(preset.wheelColor),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(preset.centerButtonColor)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = preset.displayName,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color(0xFF222222),
            textAlign = TextAlign.Center
        )
        Text(
            text = "Press Center to apply",
            fontSize = 9.sp,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GamePreviewCard(content: RightPaneContent.GamePreview) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .shadow(4.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFFF9800),
                            Color(0xFFF57C00)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.SportsEsports,
                contentDescription = content.gameTitle,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = content.gameTitle,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color(0xFF111111),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = content.description,
            fontSize = 8.5.sp,
            color = Color(0xFF555555),
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ExternalLinkCard(content: RightPaneContent.ExternalLinkPreview) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF673AB7),
                            Color(0xFF512DA8)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = content.title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = content.title,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color(0xFF222222)
        )
        Text(
            text = content.url,
            fontSize = 8.5.sp,
            color = Color(0xFF1E88E5),
            fontFamily = FontFamily.Monospace,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ActionCard(content: RightPaneContent.ActionPreview) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(3.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF607D8B),
                            Color(0xFF455A64)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = content.title,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = content.title,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = Color(0xFF222222)
        )
        Text(
            text = content.description,
            fontSize = 8.5.sp,
            color = Color(0xFF555555),
            textAlign = TextAlign.Center,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}
