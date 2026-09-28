package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory_items")
data class ItemEntity(
  @PrimaryKey val id: String,
  val name: String,
  val itemType: String,
  val rarity: String,
  val atkBonus: Int = 0,
  val defBonus: Int = 0,
  val hpBonus: Int = 0,
  val critBonus: Float = 0.0f,
  val isEquipped: Boolean = false,
  val equippedToOperativeId: String? = null,
  val flavorText: String
)
