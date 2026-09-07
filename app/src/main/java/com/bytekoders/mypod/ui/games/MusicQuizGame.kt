package com.bytekoders.mypod.ui.games

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytekoders.mypod.source.TrackMetadata
import com.bytekoders.mypod.ui.components.WheelEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow

data class QuizQuestion(
    val correctTrack: TrackMetadata,
    val options: List<String>, // 4 choices
    val correctOptionIndex: Int
)

class MusicQuizGameState(val availableTracks: List<TrackMetadata>) {
    var roundIndex by mutableIntStateOf(0) // 0..4 (5 rounds)
    var score by mutableIntStateOf(0)
    var timeRemainingSec by mutableFloatStateOf(10.0f)

    var currentQuestion by mutableStateOf<QuizQuestion?>(null)
    var selectedOptionIndex by mutableIntStateOf(0)
    var answerSubmitted by mutableStateOf(false)
    var isCorrect by mutableStateOf(false)
    var isQuizOver by mutableStateOf(false)

    val fallbackTracks = listOf(
        TrackMetadata("fb1", "Hotel California", "Eagles", "Hotel California", durationMs = 390000L),
        TrackMetadata("fb2", "Bohemian Rhapsody", "Queen", "A Night at the Opera", durationMs = 354000L),
        TrackMetadata("fb3", "Billie Jean", "Michael Jackson", "Thriller", durationMs = 294000L),
        TrackMetadata("fb4", "Sweet Child O' Mine", "Guns N' Roses", "Appetite for Destruction", durationMs = 356000L),
        TrackMetadata("fb5", "Smells Like Teen Spirit", "Nirvana", "Nevermind", durationMs = 301000L),
        TrackMetadata("fb6", "Shape of You", "Ed Sheeran", "÷", durationMs = 233000L),
        TrackMetadata("fb7", "Blinding Lights", "The Weeknd", "After Hours", durationMs = 200000L),
        TrackMetadata("fb8", "Stay", "Kid LAROI & Justin Bieber", "F*CK LOVE 3", durationMs = 141000L)
    )

    init {
        resetQuiz()
    }

    fun resetQuiz() {
        roundIndex = 0
        score = 0
        isQuizOver = false
        loadNextQuestion()
    }

    fun loadNextQuestion() {
        if (roundIndex >= 5) {
            isQuizOver = true
            return
        }

        val pool = if (availableTracks.size >= 4) availableTracks else fallbackTracks
        val shuffledPool = pool.shuffled()

        val target = shuffledPool[0]
        val distractors = shuffledPool.subList(1, 4)

        val allChoices = (listOf(target) + distractors).shuffled()
        val correctIdx = allChoices.indexOf(target)

        val optionsList = allChoices.map { "${it.title} — ${it.artist}" }

        currentQuestion = QuizQuestion(
            correctTrack = target,
            options = optionsList,
            correctOptionIndex = correctIdx
        )

        selectedOptionIndex = 0
        answerSubmitted = false
        isCorrect = false
        timeRemainingSec = 10.0f
    }

    fun moveOption(detents: Int) {
        if (answerSubmitted || isQuizOver) return
        var next = selectedOptionIndex + detents
        while (next < 0) next += 4
        selectedOptionIndex = next % 4
    }

    fun submitOrNext() {
        if (isQuizOver) {
            resetQuiz()
            return
        }

        if (answerSubmitted) {
            roundIndex++
            loadNextQuestion()
        } else {
            val q = currentQuestion ?: return
            answerSubmitted = true
            isCorrect = (selectedOptionIndex == q.correctOptionIndex)
            if (isCorrect) {
                val timeBonus = (timeRemainingSec * 10).toInt()
                score += 100 + timeBonus
            }
        }
    }

    fun updateTimerTick() {
        if (answerSubmitted || isQuizOver) return

        timeRemainingSec -= 0.1f
        if (timeRemainingSec <= 0f) {
            timeRemainingSec = 0f
            answerSubmitted = true
            isCorrect = false
        }
    }
}

@Composable
fun MusicQuizScreen(
    wheelEvents: SharedFlow<WheelEvent>,
    tracks: List<TrackMetadata> = emptyList(),
    onPlaySnippet: ((TrackMetadata) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val gameState = remember(tracks) { MusicQuizGameState(tracks) }

    // Collect Wheel Events
    LaunchedEffect(Unit) {
        wheelEvents.collect { event ->
            when (event) {
                is WheelEvent.Scroll -> gameState.moveOption(event.detents)
                is WheelEvent.SelectPress -> gameState.submitOrNext()
                else -> {}
            }
        }
    }

    // Play snippet when question loads
    LaunchedEffect(gameState.currentQuestion) {
        gameState.currentQuestion?.correctTrack?.let { track ->
            onPlaySnippet?.invoke(track)
        }
    }

    // Timer Loop
    LaunchedEffect(gameState.answerSubmitted, gameState.isQuizOver) {
        while (!gameState.answerSubmitted && !gameState.isQuizOver) {
            delay(100L)
            gameState.updateTimerTick()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1B2A38))
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MUSIC QUIZ",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFB300)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "ROUND ${gameState.roundIndex + 1}/5",
                fontSize = 10.sp,
                color = Color.LightGray
            )
            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
            Text(
                text = "SCORE: ${gameState.score}",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (gameState.isQuizOver) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "QUIZ COMPLETED!",
                    color = Color(0xFFFFB300),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "FINAL SCORE: ${gameState.score}",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Press CENTER to Play Again",
                    color = Color.LightGray,
                    fontSize = 12.sp
                )
            }
        } else {
            val q = gameState.currentQuestion
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 22.dp, start = 8.dp, end = 8.dp, bottom = 4.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Timer Indicator
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Guess the Song!  ⏱️ ${String.format("%.1f", gameState.timeRemainingSec)}s",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color(0xFF334155), RoundedCornerShape(2.dp))
                    ) {
                        val fraction = (gameState.timeRemainingSec / 10.0f).coerceIn(0f, 1f)
                        val barColor = if (fraction > 0.3f) Color(0xFF4CAF50) else Color(0xFFE53935)
                        Box(
                            modifier = Modifier
                                .fillMaxSize(fraction)
                                .background(barColor, RoundedCornerShape(2.dp))
                        )
                    }
                }

                // 4 Choices List
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    q?.options?.forEachIndexed { index, optionText ->
                        val isSelected = index == gameState.selectedOptionIndex
                        val isCorrectChoice = index == q.correctOptionIndex

                        val bgColor = when {
                            gameState.answerSubmitted && isCorrectChoice -> Color(0xFF2E7D32) // Green for correct
                            gameState.answerSubmitted && isSelected && !gameState.isCorrect -> Color(0xFFC62828) // Red for wrong
                            isSelected -> Color(0xFF1E5BB5) // Active scroll selection
                            else -> Color(0xFF243447)
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .background(bgColor, RoundedCornerShape(4.dp))
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) Color.White else Color.Transparent,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = "${('A' + index)}.  $optionText",
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Bottom Status / Prompt
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (gameState.answerSubmitted) {
                        Text(
                            text = if (gameState.isCorrect) "CORRECT! 🎉  Press CENTER for Next" else "WRONG! ❌  Press CENTER for Next",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (gameState.isCorrect) Color(0xFF81C784) else Color(0xFFE57373)
                        )
                    } else {
                        Text(
                            text = "Spin wheel to select • Press CENTER to answer",
                            fontSize = 9.5.sp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}
