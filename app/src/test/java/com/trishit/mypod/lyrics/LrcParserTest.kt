package com.trishit.mypod.lyrics

import com.trishit.mypod.data.lyrics.LrcParser
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
    fun testOffsetHeaderAndWordTimingTags() {
        val lrc = """
            [offset:500]
            [00:05.00]Hello, <00:05.30>it's <00:05.60>me
        """.trimIndent()

        val lines = LrcParser.parse(lrc)

        assertEquals(1, lines.size)
        assertEquals(5500L, lines[0].timestampMs) // 5000ms + 500ms offset
        assertEquals("Hello, it's me", lines[0].text)
    }

    @Test
    fun testExampleSongBohemianRhapsody() {
        val lrc = """
            [ar:Queen]
            [ti:Bohemian Rhapsody]
            [00:00.00]Is this the real life?
            [00:04.50]Is this just fantasy?
            [00:09.20]Caught in a landside
            [00:12.10]No escape from reality
        """.trimIndent()

        val lines = LrcParser.parse(lrc)

        assertEquals(4, lines.size)
        assertEquals("Is this the real life?", lines[0].text)
        assertEquals(0L, lines[0].timestampMs)
        assertEquals("Is this just fantasy?", lines[1].text)
        assertEquals(4500L, lines[1].timestampMs)
        assertEquals("Caught in a landside", lines[2].text)
        assertEquals(9200L, lines[2].timestampMs)
        assertEquals("No escape from reality", lines[3].text)
        assertEquals(12100L, lines[3].timestampMs)
    }

    @Test
    fun testExampleSongAdeleHello() {
        val lrc = """
            [00:01.05]Hello, it's me
            [00:06.12]I was wondering if after all these years you'd like to meet
            [00:12.80]To go over everything
        """.trimIndent()

        val lines = LrcParser.parse(lrc)

        assertEquals(3, lines.size)
        assertEquals(1050L, lines[0].timestampMs)
        assertEquals("Hello, it's me", lines[0].text)
        assertEquals(6120L, lines[1].timestampMs)
        assertEquals(12800L, lines[2].timestampMs)
    }

    @Test
    fun testExampleSongColdplayYellow() {
        val lrc = """
            [00:27.50]Look at the stars
            [00:31.20]Look how they shine for you
            [00:36.80]And everything you do
            [00:41.00]Yeah, they were all yellow
        """.trimIndent()

        val lines = LrcParser.parse(lrc)

        assertEquals(4, lines.size)
        assertEquals(27500L, lines[0].timestampMs)
        assertEquals("Look at the stars", lines[0].text)
        assertEquals(31200L, lines[1].timestampMs)
        assertEquals("Look how they shine for you", lines[1].text)
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
