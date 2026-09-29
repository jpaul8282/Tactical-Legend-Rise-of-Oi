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

  @Test
  fun floatingDamage_initializationAndTypes() {
    val normalDmg = com.example.data.model.FloatingDamage(
      gridX = 2,
      gridY = 3,
      text = "-35",
      isCrit = false
    )
    assertEquals(com.example.data.model.CombatDamageType.NORMAL, normalDmg.damageType)
    assertEquals("-35", normalDmg.text)

    val critDmg = com.example.data.model.FloatingDamage(
      gridX = 4,
      gridY = 5,
      text = "💥 CRIT -80",
      isCrit = true
    )
    assertEquals(com.example.data.model.CombatDamageType.CRITICAL, critDmg.damageType)
    assertTrue(critDmg.isCrit)
  }

  @Test
  fun codexTab_hasExpectedCategories() {
    val tabs = com.example.ui.components.CodexTab.entries
    assertEquals(6, tabs.size)
    assertTrue(tabs.contains(com.example.ui.components.CodexTab.LORE))
    assertTrue(tabs.contains(com.example.ui.components.CodexTab.RULES))
    assertTrue(tabs.contains(com.example.ui.components.CodexTab.OPERATIVES))
    assertTrue(tabs.contains(com.example.ui.components.CodexTab.ENEMIES))
    assertTrue(tabs.contains(com.example.ui.components.CodexTab.SECURITY))
    assertTrue(tabs.contains(com.example.ui.components.CodexTab.FEATURES))
  }

  @Test
  fun coverSystem_directionalFullCover_behindPillar() {
    val tiles = mapOf(
      Pair(3, 4) to com.example.data.model.TileType.BLOCKED
    )
    val envObjects = listOf(
      com.example.data.model.EnvironmentObject(
        name = "Structural Pillar",
        type = com.example.data.model.EnvironmentObjectType.STRUCTURAL_PILLAR,
        gridX = 3,
        gridY = 4
      )
    )

    // Defender at (3, 5), Attacker at (3, 2). Obstacle at (3, 4) lies directly between them!
    val cover = com.example.data.model.CoverCalculator.calculateCover(
      defenderX = 3,
      defenderY = 5,
      attackerX = 3,
      attackerY = 2,
      tiles = tiles,
      envObjects = envObjects
    )

    assertEquals(com.example.data.model.CoverType.FULL, cover.type)
    assertEquals(25, cover.defBonus)
    assertEquals(1.0f, cover.critReduction, 0.01f)
    assertEquals(0.45f, cover.damageReductionPct, 0.01f)
    assertFalse(cover.isFlanked)
  }

  @Test
  fun coverSystem_directionalHalfCover_behindBarricade() {
    val tiles = mapOf(
      Pair(5, 5) to com.example.data.model.TileType.COVER
    )
    val envObjects = listOf(
      com.example.data.model.EnvironmentObject(
        name = "Energy Barricade Alpha",
        type = com.example.data.model.EnvironmentObjectType.ENERGY_BARRICADE,
        gridX = 5,
        gridY = 5,
        coverDefBonus = 12
      )
    )

    // Defender at (5, 6), Attacker at (5, 3). Barricade at (5, 5) is in front of defender towards attacker
    val cover = com.example.data.model.CoverCalculator.calculateCover(
      defenderX = 5,
      defenderY = 6,
      attackerX = 5,
      attackerY = 3,
      tiles = tiles,
      envObjects = envObjects
    )

    assertEquals(com.example.data.model.CoverType.HALF, cover.type)
    assertEquals(14, cover.defBonus)
    assertEquals(0.5f, cover.critReduction, 0.01f)
    assertEquals(0.25f, cover.damageReductionPct, 0.01f)
    assertFalse(cover.isFlanked)
  }

  @Test
  fun coverSystem_flankingDetected_whenAttackedFromSide() {
    val tiles = mapOf(
      Pair(3, 4) to com.example.data.model.TileType.BLOCKED
    )
    val envObjects = listOf(
      com.example.data.model.EnvironmentObject(
        name = "Structural Pillar",
        type = com.example.data.model.EnvironmentObjectType.STRUCTURAL_PILLAR,
        gridX = 3,
        gridY = 4
      )
    )

    // Defender at (3, 5), obstacle at (3, 4) (North).
    // Attacker at (7, 5) (East flank). Obstacle is not between them!
    val cover = com.example.data.model.CoverCalculator.calculateCover(
      defenderX = 3,
      defenderY = 5,
      attackerX = 7,
      attackerY = 5,
      tiles = tiles,
      envObjects = envObjects
    )

    assertEquals(com.example.data.model.CoverType.NONE, cover.type)
    assertTrue(cover.isFlanked)
  }

  @Test
  fun coverSystem_openField_returnsNoCoverAndNotFlanked() {
    val cover = com.example.data.model.CoverCalculator.calculateCover(
      defenderX = 2,
      defenderY = 2,
      attackerX = 6,
      attackerY = 6,
      tiles = emptyMap(),
      envObjects = emptyList()
    )

    assertEquals(com.example.data.model.CoverType.NONE, cover.type)
    assertEquals(0, cover.defBonus)
    assertFalse(cover.isFlanked)
  }
}

