package com.trishit.mypod.data.lyrics

import java.util.regex.Pattern

object LrcParser {

    // Regex for matching timestamp patterns like [mm:ss.xx], [m:ss.xx], [mm:ss:xx], or [mm:ss]
    private val TIMESTAMP_PATTERN = Pattern.compile("\\[(\\d{1,3}):(\\d{2})(?:[.:](\\d{1,3}))?]")
    private val OFFSET_PATTERN = Pattern.compile("^\\[offset:\\s*([+-]?\\d+)]$", Pattern.CASE_INSENSITIVE)
    private val HEADER_PATTERN = Pattern.compile("^\\[(ar|ti|al|by|length|re|ve):.*]$", Pattern.CASE_INSENSITIVE)
    private val INLINE_WORD_TAG_PATTERN = Pattern.compile("<\\d{1,3}:\\d{2}(?:[.:]\\d{1,3})?>")

    fun parse(lrcContent: String?): List<LrcLine> {
        if (lrcContent.isNullOrBlank()) return emptyList()

        val lines = lrcContent.lines()
        val result = mutableListOf<LrcLine>()
        var globalOffsetMs = 0L

        // First pass: extract header offset if present
        for (line in lines) {
            val trimmed = line.trim()
            val offsetMatcher = OFFSET_PATTERN.matcher(trimmed)
            if (offsetMatcher.matches()) {
                globalOffsetMs = offsetMatcher.group(1)?.toLongOrNull() ?: 0L
                break
            }
        }

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue

            // Skip header metadata tags
            if (HEADER_PATTERN.matcher(trimmedLine).matches() || OFFSET_PATTERN.matcher(trimmedLine).matches()) {
                continue
            }

            val matcher = TIMESTAMP_PATTERN.matcher(trimmedLine)
            val timestamps = mutableListOf<Long>()

            while (matcher.find()) {
                val minStr = matcher.group(1) ?: "0"
                val secStr = matcher.group(2) ?: "0"
                val msStr = matcher.group(3)

                val minutes = minStr.toLongOrNull() ?: 0L
                val seconds = secStr.toLongOrNull() ?: 0L

                val millis = when (msStr?.length ?: 0) {
                    0 -> 0L
                    1 -> (msStr!!.toLongOrNull() ?: 0L) * 100
                    2 -> (msStr!!.toLongOrNull() ?: 0L) * 10
                    3 -> msStr!!.toLongOrNull() ?: 0L
                    else -> 0L
                }

                val totalMs = ((minutes * 60 + seconds) * 1000) + millis + globalOffsetMs
                timestamps.add(totalMs.coerceAtLeast(0L))
            }

            if (timestamps.isNotEmpty()) {
                // Extract text after all timestamp brackets and remove inline word timing tags
                var lyricText = matcher.replaceAll("")
                lyricText = INLINE_WORD_TAG_PATTERN.matcher(lyricText).replaceAll("").trim()

                for (ts in timestamps) {
                    result.add(LrcLine(timestampMs = ts, text = lyricText))
                }
            }
        }

        return result.sortedBy { it.timestampMs }
    }
}
