package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCyan

enum class TileType(val label: String, val defBonus: Int, val description: String) {
  NORMAL("Standard Deck", 0, "Flat cyber combat surface."),
  COVER("Energy Barricade", 12, "Provides +12 DEF cover against attacks."),
  NANITE_WELL("Nanite Well", 0, "Stepping here restores +15 Shield."),
  EMP_HAZARD("EMP Ground Fault", -5, "Unstable grid tile that deals 10 electric dmg."),
  BLOCKED("Structural Pillar", 0, "Impassable obstacle.")
}

enum class EnvironmentObjectType(val displayName: String, val defaultIcon: String) {
  ENERGY_BARRICADE("Energy Barricade", "🛡️"),
  NANITE_WELL("Nanite Well", "🔮"),
  EMP_HAZARD("EMP Ground Fault", "⚡"),
  STRUCTURAL_PILLAR("Structural Pillar", "🏛️"),
  HACK_TERMINAL("Uplink Terminal", "💾"),
  SUPPLY_CRATE("Munitions Cache", "📦"),
  DEFENSE_TURRET("Automated Sentry", "🔫"),
  PLASMA_VENT("Plasma Geyser", "🔥"),
  REINFORCED_BUNKER("Titanium Bunker", "🧱"),
  REPAIR_STATION("Sub-Ether Med-Station", "🧪")
}

enum class EnvironmentEffectType {
  NONE,
  COVER_DEFENSE,
  SHIELD_RESTORE,
  ELECTRIC_DAMAGE,
  LOOT_REWARD,
  TERMINAL_HACK,
  PASSAGE_BLOCK
}

data class EnvironmentObject(
  val id: String = java.util.UUID.randomUUID().toString(),
  val name: String,
  val type: EnvironmentObjectType,
  val gridX: Int,
  val gridY: Int,
  val icon: String = type.defaultIcon,
  val hp: Int? = null,
  val maxHp: Int? = null,
  val isDestructible: Boolean = false,
  val isInteractable: Boolean = true,
  val description: String = "",
  val themeColor: Color = NeonCyan,
  val coverDefBonus: Int = 0,
  val effectType: EnvironmentEffectType = EnvironmentEffectType.NONE
)

enum class UnitFaction {
  PLAYER_OPERATIVE,
  ENEMY_HOSTILE
}

data class CombatUnit(
  val id: String,
  val name: String,
  val faction: UnitFaction,
  val heroClass: HeroClass?,
  val maxHp: Int,
  var currentHp: Int,
  var maxShield: Int = 0,
  var currentShield: Int = 0,
  val atk: Int,
  val def: Int,
  val critRate: Float,
  val moveRange: Int,
  val attackRange: Int,
  var gridX: Int,
  var gridY: Int,
  var actionPoints: Int = 2,
  var maxActionPoints: Int = 2,
  var isStunned: Boolean = false,
  var isTaunted: Boolean = false,
  var specialCooldown: Int = 0,
  val avatarIcon: String,
  val themeColor: Color,
  val isBoss: Boolean = false,
  val portraitResId: Int? = null
) {
  val isAlive: Boolean get() = currentHp > 0
}

data class CombatLog(
  val id: String = java.util.UUID.randomUUID().toString(),
  val timestamp: Long = System.currentTimeMillis(),
  val message: String,
  val color: Color = NeonCyan
)

enum class CombatDamageType {
  NORMAL,
  CRITICAL,
  SHIELD_BREAK,
  EMP_SHOCK,
  HEAL,
  BLADE_SLASH
}

data class FloatingDamage(
  val gridX: Int,
  val gridY: Int,
  val text: String,
  val color: Color = NeonCrimson,
  val isCrit: Boolean = false,
  val damageType: CombatDamageType = if (isCrit) CombatDamageType.CRITICAL else CombatDamageType.NORMAL,
  val timestamp: Long = System.currentTimeMillis(),
  val id: String = java.util.UUID.randomUUID().toString()
)

enum class BattlePhase {
  SELECT_UNIT,
  ACTION_MOVE,
  ACTION_ATTACK,
  ACTION_SPECIAL,
  ENEMY_TURN,
  VICTORY,
  DEFEAT
}

enum class CoverType(
  val displayName: String,
  val icon: String,
  val defBonus: Int,
  val critReduction: Float,
  val damageReductionPct: Float,
  val description: String
) {
  NONE(
    displayName = "Exposed",
    icon = "⚠️",
    defBonus = 0,
    critReduction = 0f,
    damageReductionPct = 0f,
    description = "No cover protection. Full damage and critical hit vulnerability."
  ),
  HALF(
    displayName = "Half Cover",
    icon = "🛡️",
    defBonus = 14,
    critReduction = 0.5f,
    damageReductionPct = 0.25f,
    description = "+14 DEF bonus, 25% damage mitigation, and -50% enemy crit chance."
  ),
  FULL(
    displayName = "Full Cover",
    icon = "🏰",
    defBonus = 25,
    critReduction = 1.0f,
    damageReductionPct = 0.45f,
    description = "+25 DEF bonus, 45% damage mitigation, and complete immunity to critical hits."
  )
}

data class CoverResult(
  val type: CoverType,
  val defBonus: Int = type.defBonus,
  val critReduction: Float = type.critReduction,
  val damageReductionPct: Float = type.damageReductionPct,
  val obstacleName: String? = null,
  val isFlanked: Boolean = false,
  val obstacleCoord: Pair<Int, Int>? = null
)
