package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
  @PrimaryKey val id: Int = 1,
  val commanderName: String = "Commander Oi",
  val cyberCredits: Int = 2500,
  val tacticalData: Int = 1200,
  val playerLevel: Int = 1,
  val playerXp: Int = 0,
  val isVipPassActive: Boolean = false,
  val totalBattlesWon: Int = 0,
  val totalDronesNeutralized: Int = 0
)
