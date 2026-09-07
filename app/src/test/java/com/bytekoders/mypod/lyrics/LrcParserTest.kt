package com.bytekoders.mypod.lyrics

import com.bytekoders.mypod.data.lyrics.LrcParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LrcParserTest {

    @Test
    fun testParseStandardLrc() {
        val lrc = """
            [ar:Daft Punk]
            [ti:Get Lucky]
            [al:Random Access Memories]
            [00:05.20]Like the legend of the phoenix
            [00:08.50]All ends with beginnings
            [00:12.00]What keeps the planet spinning
        """.trimIndent()

        val lines = LrcParser.parse(lrc)

        assertEquals(3, lines.size)
        assertEquals(5200L, lines[0].timestampMs)
        assertEquals("Like the legend of the phoenix", lines[0].text)

        assertEquals(8500L, lines[1].timestampMs)
        assertEquals("All ends with beginnings", lines[1].text)

        assertEquals(12000L, lines[2].timestampMs)
        assertEquals("What keeps the planet spinning", lines[2].text)
    }

    @Test
    fun testMultipleTimestampsSingleLine() {
        val lrc = """
            [00:10.00][01:30.50]We've come too far to give up who we are
        """.trimIndent()

        val lines = LrcParser.parse(lrc)

        assertEquals(2, lines.size)
        assertEquals(10000L, lines[0].timestampMs)
        assertEquals("We've come too far to give up who we are", lines[0].text)

        assertEquals(90500L, lines[1].timestampMs)
        assertEquals("We've come too far to give up who we are", lines[1].text)
    }

    @Test
    fun testEmptyOrInvalidLrc() {
        val emptyLines = LrcParser.parse(null)
        assertTrue(emptyLines.isEmpty())

        val blankLines = LrcParser.parse("   \n  \n")
        assertTrue(blankLines.isEmpty())

        val invalidLines = LrcParser.parse("Just plain text with no timestamps")
        assertTrue(invalidLines.isEmpty())
    }
}
