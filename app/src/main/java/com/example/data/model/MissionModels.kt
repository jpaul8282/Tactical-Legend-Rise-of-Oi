package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.CyberGold
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCyan

enum class MissionDifficulty(val label: String, val color: Color) {
  EASY("Recruit", MatrixGreen),
  MEDIUM("Veteran", NeonCyan),
  HARD("Overdrive", CyberGold),
  BOSS("Boss Encounter", NeonCrimson);

  companion object {
    fun fromString(value: String): MissionDifficulty {
      return entries.find { it.name.equals(value, ignoreCase = true) } ?: EASY
    }
  }
}
