package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCyan

enum class HeroClass(
  val displayName: String,
  val roleTitle: String,
  val primaryColor: Color,
  val baseIcon: String,
  val moveRange: Int,
  val attackRange: Int,
  val specialAbilityName: String,
  val specialAbilityDesc: String
) {
  VANGUARD(
    displayName = "Vanguard",
    roleTitle = "Frontline Tank",
    primaryColor = ElectricBlue,
    baseIcon = "🛡️",
    moveRange = 2,
    attackRange = 1,
    specialAbilityName = "Plasma Barrier",
    specialAbilityDesc = "Deploys an overshield absorbing +60 damage and taunting nearby threats."
  ),
  SNIPER(
    displayName = "Sniper",
    roleTitle = "Long-Range Marksman",
    primaryColor = NeonCrimson,
    baseIcon = "🎯",
    moveRange = 2,
    attackRange = 3,
    specialAbilityName = "Overcharge Shot",
    specialAbilityDesc = "Channels an armor-piercing shot dealing 2.5x critical damage."
  ),
  CIPHER(
    displayName = "Cipher",
    roleTitle = "Electronic Hacker",
    primaryColor = NeonCyan,
    baseIcon = "⚡",
    moveRange = 3,
    attackRange = 2,
    specialAbilityName = "EMP Pulse",
    specialAbilityDesc = "Unleashes an electronic disruption field that stuns and drains enemy barriers."
  ),
  MEDIC(
    displayName = "Medic",
    roleTitle = "Combat Field Doctor",
    primaryColor = MatrixGreen,
    baseIcon = "💉",
    moveRange = 3,
    attackRange = 1,
    specialAbilityName = "Nanite Surge",
    specialAbilityDesc = "Injects microscopic repair nanites, restoring +70 HP to adjacent squadmates."
  ),
  SAMURAI(
    displayName = "Samurai",
    roleTitle = "Melee Assassin",
    primaryColor = ElectricPurple,
    baseIcon = "⚔️",
    moveRange = 3,
    attackRange = 1,
    specialAbilityName = "Blade Dance",
    specialAbilityDesc = "Executes rapid thermal strikes dealing devastating multi-target damage."
  );

  companion object {
    fun fromString(value: String): HeroClass {
      return entries.find { it.name.equals(value, ignoreCase = true) } ?: VANGUARD
    }
  }
}
