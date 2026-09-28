package com.example

import com.example.ui.components.VideoChapter
import com.example.ui.components.VideoItem
import com.example.ui.components.VideoSubtitle
import com.example.ui.components.formatDuration
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun formatDuration_formatsCorrectly() {
    assertEquals("00:00", formatDuration(0))
    assertEquals("00:45", formatDuration(45))
    assertEquals("01:30", formatDuration(90))
    assertEquals("10:05", formatDuration(605))
  }

  @Test
  fun videoItem_chaptersAndSubtitles_validateRanges() {
    val video = VideoItem(
      id = "test_vid",
      title = "Test Briefing",
      durationSeconds = 60,
      chapters = listOf(
        VideoChapter("Chapter 1", 0),
        VideoChapter("Chapter 2", 30)
      ),
      subtitles = listOf(
        VideoSubtitle(0, 15, "COMMANDER", "Alpha in position."),
        VideoSubtitle(16, 40, "AI", "Telemetry acquired.")
      )
    )

    assertEquals(60, video.durationSeconds)
    assertEquals(2, video.chapters.size)
    assertTrue(video.chapters[1].timestampSeconds < video.durationSeconds)

    val currentSub = video.subtitles.find { 20 in it.startSeconds..it.endSeconds }
    assertNotNull(currentSub)
    assertEquals("AI", currentSub?.speaker)
  }
}

