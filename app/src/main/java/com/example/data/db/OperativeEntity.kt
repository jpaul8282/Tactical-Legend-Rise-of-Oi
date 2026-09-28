package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operatives")
data class OperativeEntity(
  @PrimaryKey val id: String,
  val name: String,
  val heroClass: String,
  val roleTitle: String,
  val level: Int = 1,
  val currentXp: Int = 0,
  val maxXp: Int = 100,
  val baseHp: Int,
  val baseAtk: Int,
  val baseDef: Int,
  val critRate: Float,
  val speed: Int,
  val specialAbilityName: String,
  val specialAbilityDesc: String,
  val specialAbilityCooldown: Int = 3,
  val isDeployed: Boolean = true,
  val equippedWeaponId: String? = null,
  val equippedArmorId: String? = null,
  val equippedCoreId: String? = null,
  val equippedChipId: String? = null,
  val avatarColorHex: Long = 0xFF00E5FF,
  val loreBriefing: String
)
