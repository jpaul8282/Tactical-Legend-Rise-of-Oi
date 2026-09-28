package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
  // Operatives
  @Query("SELECT * FROM operatives ORDER BY level DESC")
  fun getAllOperatives(): Flow<List<OperativeEntity>>

  @Query("SELECT * FROM operatives WHERE isDeployed = 1")
  fun getDeployedOperatives(): Flow<List<OperativeEntity>>

  @Query("SELECT * FROM operatives WHERE id = :id")
  suspend fun getOperativeById(id: String): OperativeEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOperatives(operatives: List<OperativeEntity>)

  @Update
  suspend fun updateOperative(operative: OperativeEntity)

  // Items
  @Query("SELECT * FROM inventory_items")
  fun getAllItems(): Flow<List<ItemEntity>>

  @Query("SELECT * FROM inventory_items WHERE equippedToOperativeId = :operativeId")
  suspend fun getEquippedItemsForOperative(operativeId: String): List<ItemEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItems(items: List<ItemEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertItem(item: ItemEntity)

  @Update
  suspend fun updateItem(item: ItemEntity)

  @Query("DELETE FROM inventory_items WHERE id = :id")
  suspend fun deleteItem(id: String)

  // Missions
  @Query("SELECT * FROM campaign_missions ORDER BY chapter ASC, missionNumber ASC")
  fun getAllMissions(): Flow<List<MissionEntity>>

  @Query("SELECT * FROM campaign_missions WHERE id = :id")
  suspend fun getMissionById(id: String): MissionEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMissions(missions: List<MissionEntity>)

  @Update
  suspend fun updateMission(mission: MissionEntity)

  // Player Profile
  @Query("SELECT * FROM player_profile WHERE id = 1")
  fun getPlayerProfile(): Flow<PlayerProfileEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPlayerProfile(profile: PlayerProfileEntity)

  @Update
  suspend fun updatePlayerProfile(profile: PlayerProfileEntity)

  // Arcade Scores
  @Query("SELECT * FROM arcade_scores ORDER BY score DESC LIMIT 10")
  fun getTopArcadeScores(): Flow<List<ArcadeScoreEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertArcadeScore(score: ArcadeScoreEntity)
}
