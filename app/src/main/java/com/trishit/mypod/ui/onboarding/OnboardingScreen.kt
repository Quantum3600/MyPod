package com.trishit.mypod.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Navigation
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trishit.mypod.ui.components.WheelEvent
import kotlinx.coroutines.flow.SharedFlow

data class OnboardingSlide(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val headerColor: List<Color>,
    val description: String,
    val bulletPoints: List<Pair<String, String>>
)

@Composable
fun OnboardingScreen(
    wheelEvents: SharedFlow<WheelEvent>,
    onCompleteOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    val slides = remember {
        listOf(
            OnboardingSlide(
                title = "WELCOME TO MYPOD",
                subtitle = "Classic Music Player",
                icon = Icons.Rounded.MusicNote,
                headerColor = listOf(Color(0xFF1E5BB5), Color(0xFF104192)),
                description = "Experience your music collection on a pixel-accurate iPod Classic with 3D Cover Flow and click wheel haptics.",
                bulletPoints = listOf(
                    "📱 Pixel-Accurate UI" to "Skinned as a classic iPod with custom chassis themes.",
                    "🎧 Local & Storage Playback" to "Play MediaStore library & SAF storage audio files.",
                    "🪞 Interactive 3D Visualizer" to "Tilt-responsive album art & 3D Cover Flow."
                )
            ),
            OnboardingSlide(
                title = "CLICK WHEEL CONTROLS",
                subtitle = "Rotary Touch Input",
                icon = Icons.Rounded.Navigation,
                headerColor = listOf(Color(0xFF334155), Color(0xFF1E293B)),
                description = "Navigate every screen using the tactile iPod Click Wheel:",
                bulletPoints = listOf(
                    "🔄 Scroll Wheel" to "Swipe clockwise / counter-clockwise to navigate lists.",
                    "🔘 Center Button" to "Select item, play song, or confirm action.",
                    "📋 MENU Button" to "Go back to previous screen. Hold to return root.",
                    "⏯️ Play / Pause" to "Quick play/pause track or trigger app actions."
                )
            ),
            OnboardingSlide(
                title = "MUSIC & STORAGE",
                subtitle = "Audio Sources",
                icon = Icons.Rounded.LibraryMusic,
                headerColor = listOf(Color(0xFF0F766E), Color(0xFF115E59)),
                description = "Multiple audio sources seamlessly unified under one player:",
                bulletPoints = listOf(
                    "🎵 MediaStore Scanner" to "Auto-scans device audio, artists, albums & genres.",
                    "📁 Storage Explorer" to "Browse MP3, FLAC, M4A, WAV audio folders directly.",
                    "🔀 Shuffle All" to "Instant 1-tap shuffle of your entire song collection."
                )
            ),
            OnboardingSlide(
                title = "NOW PLAYING & LYRICS",
                subtitle = "Synced Lyrics & 3D Art",
                icon = Icons.Rounded.AutoAwesome,
                headerColor = listOf(Color(0xFF6D28D9), Color(0xFF5B21B6)),
                description = "Rich visual playback experience for every track:",
                bulletPoints = listOf(
                    "🎤 LRCLIB Synced Lyrics" to "Auto-scrolling lyrics with active line highlight.",
                    "🪞 Parallax 3D Artwork" to "Accelerometer-driven tilt perspective & specular gloss.",
                    "💿 3D Cover Flow" to "Interactive 3D album carousel visualizer."
                )
            ),
            OnboardingSlide(
                title = "EXTRAS & RETRO GAMES",
                subtitle = "Arcade & AI Chat",
                icon = Icons.Rounded.SportsEsports,
                headerColor = listOf(Color(0xFFC2410C), Color(0xFF9A3412)),
                description = "Classic iPod mini-games and useful gimmicks:",
                bulletPoints = listOf(
                    "🎮 5 Retro Mini-Games" to "Brick Breaker, Snake, Solitaire, Parachute, Music Quiz.",
                    "🎙️ Voice Recorder & Camera" to "Record voice memos & snap photos on the LCD.",
                    "🤖 Gemini AI Voice Chat" to "Voice & text responses powered by Gemini AI."
                )
            ),
            OnboardingSlide(
                title = "YOU'RE ALL SET!",
                subtitle = "Ready to Play",
                icon = Icons.Rounded.CheckCircle,
                headerColor = listOf(Color(0xFF15803D), Color(0xFF166534)),
                description = "Your iPod Classic is ready. Press Center button to start listening!",
                bulletPoints = listOf(
                    "✨ Custom Themes" to "Customize chassis color in Settings.",
                    "❤️ Favorites & Playlists" to "1-tap heart button on Now Playing.",
                    "📖 User Guide" to "Revisit this guide anytime from Settings or About."
                )
            )
        )
    }

    var currentSlideIdx by remember { mutableIntStateOf(0) }

    // Handle Click Wheel Navigation
    LaunchedEffect(currentSlideIdx, slides.size) {
        wheelEvents.collect { event ->
            when (event) {
                is WheelEvent.Scroll -> {
                    if (event.detents > 0) {
                        if (currentSlideIdx < slides.lastIndex) {
                            currentSlideIdx++
                        }
                    } else if (event.detents < 0) {
                        if (currentSlideIdx > 0) {
                            currentSlideIdx--
                        }
                    }
                }
                is WheelEvent.SelectPress, is WheelEvent.PlayPausePress -> {
                    if (currentSlideIdx < slides.lastIndex) {
                        currentSlideIdx++
                    } else {
                        onCompleteOnboarding()
                    }
                }
                is WheelEvent.NextPress -> {
                    if (currentSlideIdx < slides.lastIndex) {
                        currentSlideIdx++
                    }
                }
                is WheelEvent.PrevPress -> {
                    if (currentSlideIdx > 0) {
                        currentSlideIdx--
                    }
                }
                is WheelEvent.MenuPress -> {
                    onCompleteOnboarding()
                }
            }
        }
    }

    val activeSlide = slides[currentSlideIdx]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Slide Content
        AnimatedContent(
            targetState = activeSlide,
            transitionSpec = {
                (slideInHorizontally { width -> width } + fadeIn()).togetherWith(
                    slideOutHorizontally { width -> -width } + fadeOut()
                ).using(SizeTransform(clip = false))
            },
            label = "slide_transition",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { slide ->
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Slide Header Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(colors = slide.headerColor),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = slide.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column {
                                Text(
                                    text = slide.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = slide.subtitle,
                                    fontSize = 9.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Page count pill
                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${currentSlideIdx + 1}/${slides.size}",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Slide Body Description & Bullet Points
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White, RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = slide.description,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1E293B),
                            lineHeight = 13.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            slide.bulletPoints.forEach { (heading, body) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8FAFC), RoundedCornerShape(4.dp))
                                        .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                        .padding(4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column {
                                        Text(
                                            text = heading,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = body,
                                            fontSize = 8.5.sp,
                                            color = Color(0xFF475569),
                                            lineHeight = 11.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Bottom action button on final slide or prompt
                        if (currentSlideIdx == slides.lastIndex) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = onCompleteOnboarding,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(28.dp)
                            ) {
                                Text(
                                    text = "GET STARTED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Page Indicator Dots & Control Guidance
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                .padding(vertical = 4.dp, horizontal = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Page Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    slides.indices.forEach { idx ->
                        Box(
                            modifier = Modifier
                                .size(if (idx == currentSlideIdx) 7.dp else 5.dp)
                                .background(
                                    color = if (idx == currentSlideIdx) Color(0xFF1E5BB5) else Color(0xFF94A3B8),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                Text(
                    text = if (currentSlideIdx == slides.lastIndex) "Center: GET STARTED" else "Center: NEXT | Menu: SKIP",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
            }
        }
    }
}
