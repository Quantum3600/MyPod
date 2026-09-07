package com.trishit.mypod.data.lyrics

import java.util.regex.Pattern

object LrcParser {

    // Regex for matching timestamp patterns like [mm:ss.xx] or [mm:ss.xxx] or [hh:mm:ss.xx]
    private val TIMESTAMP_PATTERN = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{2,3}))?]")
    private val HEADER_PATTERN = Pattern.compile("^\\[(ar|ti|al|by|offset|length|re|ve):.*]$", Pattern.CASE_INSENSITIVE)

    fun parse(lrcContent: String?): List<LrcLine> {
        if (lrcContent.isNullOrBlank()) return emptyList()

        val lines = lrcContent.lines()
        val result = mutableListOf<LrcLine>()

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue

            // Skip header metadata tags like [ar:Artist], [ti:Title]
            if (HEADER_PATTERN.matcher(trimmedLine).matches()) continue

            val matcher = TIMESTAMP_PATTERN.matcher(trimmedLine)
            val timestamps = mutableListOf<Long>()

            while (matcher.find()) {
                val minStr = matcher.group(1) ?: "0"
                val secStr = matcher.group(2) ?: "0"
                val msStr = matcher.group(3) ?: "0"

                val minutes = minStr.toLongOrNull() ?: 0L
                val seconds = secStr.toLongOrNull() ?: 0L

                val millis = when (msStr.length) {
                    1 -> (msStr.toLongOrNull() ?: 0L) * 100
                    2 -> (msStr.toLongOrNull() ?: 0L) * 10
                    3 -> msStr.toLongOrNull() ?: 0L
                    else -> 0L
                }

                val totalMs = (minutes * 60 + seconds) * 1000 + millis
                timestamps.add(totalMs)
            }

            if (timestamps.isNotEmpty()) {
                // Extract text after all timestamp brackets
                val lyricText = matcher.replaceAll("").trim()
                for (ts in timestamps) {
                    result.add(LrcLine(timestampMs = ts, text = lyricText))
                }
            }
        }

        return result.sortedBy { it.timestampMs }
    }
}
