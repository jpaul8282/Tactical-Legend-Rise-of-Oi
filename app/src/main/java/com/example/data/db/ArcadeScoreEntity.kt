package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "arcade_scores")
data class ArcadeScoreEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val playerName: String,
  val score: Int,
  val maxCombo: Int,
  val difficulty: String,
  val earnedCredits: Int,
  val earnedData: Int,
  val timestamp: Long = System.currentTimeMillis()
)
