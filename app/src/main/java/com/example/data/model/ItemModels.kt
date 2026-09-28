package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.RarityCommon
import com.example.ui.theme.RarityEpic
import com.example.ui.theme.RarityLegendary
import com.example.ui.theme.RarityRare

enum class ItemType(val label: String, val icon: String) {
  WEAPON("Weapon", "🗡️"),
  ARMOR("Armor", "🛡️"),
  NANITE_CORE("Nanite Core", "🔮"),
  CIPHER_CHIP("Cipher Chip", "💾");

  companion object {
    fun fromString(value: String): ItemType {
      return entries.find { it.name.equals(value, ignoreCase = true) } ?: WEAPON
    }
  }
}

enum class ItemRarity(
  val label: String,
  val color: Color,
  val statMultiplier: Float,
  val forgeCreditCost: Int,
  val forgeDataCost: Int
) {
  COMMON("Common", RarityCommon, 1.0f, 150, 50),
  RARE("Rare", RarityRare, 1.5f, 350, 150),
  EPIC("Epic", RarityEpic, 2.2f, 750, 350),
  LEGENDARY("Legendary", RarityLegendary, 3.5f, 1500, 750);

  companion object {
    fun fromString(value: String): ItemRarity {
      return entries.find { it.name.equals(value, ignoreCase = true) } ?: COMMON
    }
  }
}
