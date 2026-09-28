package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "campaign_missions")
data class MissionEntity(
  @PrimaryKey val id: String,
  val chapter: Int,
  val missionNumber: Int,
  val title: String,
  val location: String,
  val difficulty: String,
  val recommendedPower: Int,
  val rewardCredits: Int,
  val rewardData: Int,
  val rewardXp: Int,
  val intel: String,
  val isUnlocked: Boolean = false,
  val starsEarned: Int = 0,
  val isBossEncounter: Boolean = false,
  val bossName: String? = null
)
