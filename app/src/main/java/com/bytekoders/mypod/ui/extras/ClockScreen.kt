package com.bytekoders.mypod.ui.extras

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bytekoders.mypod.ui.components.WheelEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.cos
import kotlin.math.sin

data class WorldCity(
    val cityName: String,
    val timeZoneId: String,
    val country: String
)

@Composable
fun ClockScreen(
    wheelEvents: SharedFlow<WheelEvent>,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // Stopwatch State
    var stopwatchRunning by remember { mutableStateOf(false) }
    var stopwatchTimeMs by remember { mutableLongStateOf(0L) }

    val cities = remember {
        listOf(
            WorldCity("Cupertino", "America/Los_Angeles", "USA"),
            WorldCity("New York", "America/New_York", "USA"),
            WorldCity("London", "Europe/London", "UK"),
            WorldCity("Tokyo", "Asia/Tokyo", "Japan"),
            WorldCity("Sydney", "Australia/Sydney", "Australia")
        )
    }

    var selectedCityIdx by remember { mutableIntStateOf(0) }

    // Clock update ticker
    LaunchedEffect(Unit) {
        while (true) {
            currentTimeMs = System.currentTimeMillis()
            delay(100L)
        }
    }

    // Stopwatch ticker
    LaunchedEffect(stopwatchRunning) {
        var lastTime = System.currentTimeMillis()
        while (stopwatchRunning) {
            delay(16L)
            val now = System.currentTimeMillis()
            stopwatchTimeMs += (now - lastTime)
            lastTime = now
        }
    }

    // Collect Wheel Events
    LaunchedEffect(Unit) {
        wheelEvents.collect { event ->
            when (event) {
                is WheelEvent.Scroll -> {
                    var next = selectedCityIdx + event.detents
                    while (next < 0) next += cities.size
                    selectedCityIdx = next % cities.size
                }
                is WheelEvent.SelectPress -> {
                    // Toggle or reset stopwatch
                    if (stopwatchRunning) {
                        stopwatchRunning = false
                    } else if (stopwatchTimeMs > 0) {
                        stopwatchTimeMs = 0L
                    } else {
                        stopwatchRunning = true
                    }
                }
                is WheelEvent.PlayPausePress -> {
                    stopwatchRunning = !stopwatchRunning
                }
                is WheelEvent.MenuPress -> {
                    onExit()
                }
                else -> {}
            }
        }
    }

    val timeFormat = remember { SimpleDateFormat("h:mm:ss a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()) }
    val timeStr = timeFormat.format(Date(currentTimeMs))
    val dateStr = dateFormat.format(Date(currentTimeMs))

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Main Digital & Analog Clock Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF2B3A4A), Color(0xFF1E2733))
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Analog Clock Face
                Canvas(modifier = Modifier.size(44.dp)) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)

                    // Dial background
                    drawCircle(color = Color.White, radius = radius, center = center)
                    drawCircle(color = Color(0xFF1E2733), radius = radius, center = center, style = Stroke(2f))

                    // Hands
                    val cal = Calendar.getInstance().apply { timeInMillis = currentTimeMs }
                    val seconds = cal.get(Calendar.SECOND)
                    val minutes = cal.get(Calendar.MINUTE)
                    val hours = cal.get(Calendar.HOUR)

                    val secAngle = Math.toRadians((seconds * 6 - 90).toDouble())
                    val minAngle = Math.toRadians((minutes * 6 - 90).toDouble())
                    val hourAngle = Math.toRadians(((hours * 30) + (minutes * 0.5) - 90))

                    // Hour Hand
                    drawLine(
                        color = Color.Black,
                        start = center,
                        end = Offset(
                            (center.x + cos(hourAngle) * radius * 0.5f).toFloat(),
                            (center.y + sin(hourAngle) * radius * 0.5f).toFloat()
                        ),
                        strokeWidth = 3f
                    )

                    // Minute Hand
                    drawLine(
                        color = Color.Black,
                        start = center,
                        end = Offset(
                            (center.x + cos(minAngle) * radius * 0.75f).toFloat(),
                            (center.y + sin(minAngle) * radius * 0.75f).toFloat()
                        ),
                        strokeWidth = 2f
                    )

                    // Second Hand
                    drawLine(
                        color = Color(0xFFE53935),
                        start = center,
                        end = Offset(
                            (center.x + cos(secAngle) * radius * 0.85f).toFloat(),
                            (center.y + sin(secAngle) * radius * 0.85f).toFloat()
                        ),
                        strokeWidth = 1f
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Digital Time & Date
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = timeStr,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = dateStr,
                        fontSize = 9.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // World Clock Carousel / Selection
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = "WORLD CLOCK",
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF555555),
                modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
            )

            cities.forEachIndexed { idx, city ->
                val isSelected = idx == selectedCityIdx
                val cityFormat = remember(city.timeZoneId) {
                    SimpleDateFormat("h:mm a", Locale.getDefault()).apply {
                        timeZone = TimeZone.getTimeZone(city.timeZoneId)
                    }
                }
                val cityTime = cityFormat.format(Date(currentTimeMs))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(22.dp)
                        .background(
                            if (isSelected) {
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF3F82DB), Color(0xFF104192))
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(Color.White, Color(0xFFF5F5F5))
                                )
                            },
                            shape = RoundedCornerShape(3.dp)
                        )
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${city.cityName}, ${city.country}",
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF222222)
                        )
                        Text(
                            text = cityTime,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else Color(0xFF1E5BB5)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
            }
        }

        // Stopwatch Widget Footer
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
                .border(1.dp, Color(0xFF94A3B8), RoundedCornerShape(6.dp))
                .padding(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "STOPWATCH",
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF475569)
                    )
                    val mins = (stopwatchTimeMs / 60000L)
                    val secs = (stopwatchTimeMs % 60000L) / 1000L
                    val millis = (stopwatchTimeMs % 1000L) / 10L
                    val stopStr = String.format(Locale.getDefault(), "%02d:%02d.%02d", mins, secs, millis)

                    Text(
                        text = stopStr,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (stopwatchRunning) Color(0xFF166534) else Color(0xFF1E293B)
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            color = if (stopwatchRunning) Color(0xFFDC2626) else Color(0xFF16A34A),
                            shape = CircleShape
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (stopwatchRunning) "PAUSE" else if (stopwatchTimeMs > 0) "RESET" else "START",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
