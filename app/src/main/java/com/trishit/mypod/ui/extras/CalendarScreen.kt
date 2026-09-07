package com.trishit.mypod.ui.extras

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.trishit.mypod.ui.components.WheelEvent
import kotlinx.coroutines.flow.SharedFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class CalendarEvent(
    val title: String,
    val timeStr: String,
    val category: String
)

@Composable
fun CalendarScreen(
    wheelEvents: SharedFlow<WheelEvent>,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cal = remember { Calendar.getInstance() }
    val currentDayOfMonth = remember { cal.get(Calendar.DAY_OF_MONTH) }
    val maxDaysInMonth = remember { cal.getActualMaximum(Calendar.DAY_OF_MONTH) }

    // Month & Year header
    val monthYearFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
    val monthYearStr = remember { monthYearFormat.format(cal.time) }

    // Day of week offset for day 1
    val firstDayOfWeekOffset = remember {
        val tempCal = Calendar.getInstance().apply { set(Calendar.DAY_OF_MONTH, 1) }
        tempCal.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday, 1 = Monday, ...
    }

    var selectedDay by remember { mutableIntStateOf(currentDayOfMonth) }

    // Sample events mapping
    val eventsMap = remember {
        mapOf(
            1 to listOf(CalendarEvent("New Month Sync", "9:00 AM", "Music Library")),
            10 to listOf(CalendarEvent("iPod Special Release", "2:00 PM", "Anniversary")),
            15 to listOf(CalendarEvent("Playlist Backup", "11:30 AM", "Settings")),
            currentDayOfMonth to listOf(
                CalendarEvent("Today's Music Listening", "All Day", "Personal"),
                CalendarEvent("LRCLIB Lyrics Sync", "6:00 PM", "Library")
            ),
            25 to listOf(CalendarEvent("New Music Release Friday", "12:00 AM", "New Albums"))
        )
    }

    // Collect Wheel Events
    LaunchedEffect(Unit) {
        wheelEvents.collect { event ->
            when (event) {
                is WheelEvent.Scroll -> {
                    var next = selectedDay + event.detents
                    while (next < 1) next += maxDaysInMonth
                    while (next > maxDaysInMonth) next -= maxDaysInMonth
                    selectedDay = next
                }
                is WheelEvent.MenuPress -> {
                    onExit()
                }
                else -> {}
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F4F8))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Month / Year Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF3F82DB), Color(0xFF104192))
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(vertical = 4.dp, horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = monthYearStr.uppercase(Locale.getDefault()),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Days of Week Labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Month Grid
        val totalCells = ((maxDaysInMonth + firstDayOfWeekOffset + 6) / 7) * 7
        val rowsCount = totalCells / 7

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            for (r in 0 until rowsCount) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (c in 0 until 7) {
                        val cellIndex = r * 7 + c
                        val dayNum = cellIndex - firstDayOfWeekOffset + 1
                        val isValidDay = dayNum in 1..maxDaysInMonth
                        val isSelected = isValidDay && dayNum == selectedDay
                        val isToday = isValidDay && dayNum == currentDayOfMonth
                        val hasEvent = isValidDay && eventsMap.containsKey(dayNum)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1.2f)
                                .background(
                                    when {
                                        isSelected -> Brush.verticalGradient(
                                            colors = listOf(Color(0xFF3F82DB), Color(0xFF1E5BB5))
                                        )
                                        isToday -> Brush.verticalGradient(
                                            colors = listOf(Color(0xFFE2E8F0), Color(0xFFCBD5E1))
                                        )
                                        else -> Brush.verticalGradient(
                                            colors = listOf(Color.White, Color(0xFFFAFAFA))
                                        )
                                    },
                                    shape = RoundedCornerShape(2.dp)
                                )
                                .border(
                                    width = if (isToday && !isSelected) 1.dp else 0.dp,
                                    color = if (isToday && !isSelected) Color(0xFF1E5BB5) else Color.Transparent,
                                    shape = RoundedCornerShape(2.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isValidDay) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNum.toString(),
                                        fontSize = 9.5.sp,
                                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                        color = when {
                                            isSelected -> Color.White
                                            c == 0 || c == 6 -> Color(0xFFD32F2F) // Red for weekends
                                            else -> Color(0xFF1E293B)
                                        }
                                    )
                                    if (hasEvent) {
                                        Box(
                                            modifier = Modifier
                                                .padding(top = 1.dp)
                                                .height(2.dp)
                                                .fillMaxWidth(0.5f)
                                                .background(
                                                    if (isSelected) Color.White else Color(0xFFFFB300),
                                                    shape = RoundedCornerShape(1.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Selected Day Events Footer
        val selectedDayEvents = eventsMap[selectedDay] ?: emptyList()
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFF8FAFC))
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(4.dp))
                .padding(6.dp)
        ) {
            Column {
                Text(
                    text = "EVENTS FOR DAY $selectedDay:",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(2.dp))

                if (selectedDayEvents.isEmpty()) {
                    Text(
                        text = "No events scheduled for this day.",
                        fontSize = 9.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                } else {
                    selectedDayEvents.forEach { event ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${event.title}",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F172A),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = event.timeStr,
                                fontSize = 8.5.sp,
                                color = Color(0xFF2563EB)
                            )
                        }
                    }
                }
            }
        }
    }
}
