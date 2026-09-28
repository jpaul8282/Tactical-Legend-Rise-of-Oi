package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.db.AppDatabase
import com.example.data.db.MissionEntity
import com.example.data.db.OperativeEntity
import com.example.data.model.*
import com.example.data.repository.GameRepository
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

data class CombatGridState(
  val mission: MissionEntity? = null,
  val phase: BattlePhase = BattlePhase.SELECT_UNIT,
  val units: List<CombatUnit> = emptyList(),
  val environmentObjects: List<EnvironmentObject> = emptyList(),
  val selectedUnitId: String? = null,
  val selectedObjectId: String? = null,
  val gridWidth: Int = 10,
  val gridHeight: Int = 10,
  val reachableTiles: Set<Pair<Int, Int>> = emptySet(),
  val targetableTiles: Set<Pair<Int, Int>> = emptySet(),
  val specialTargetTiles: Set<Pair<Int, Int>> = emptySet(),
  val floatingDamages: List<FloatingDamage> = emptyList(),
  val combatLogs: List<CombatLog> = emptyList(),
  val currentTurn: Int = 1,
  val isPlayerTurn: Boolean = true,
  val tiles: Map<Pair<Int, Int>, TileType> = emptyMap(),
  val isBattleOver: Boolean = false,
  val didPlayerWin: Boolean = false,
  val rewardsEarned: Map<String, Int> = emptyMap()
)

class TacticalCombatViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: GameRepository

  init {
    val db = AppDatabase.getDatabase(application)
    repository = GameRepository(db.gameDao())
  }

  private val _gridState = MutableStateFlow(CombatGridState())
  val gridState: StateFlow<CombatGridState> = _gridState

  fun initMissionCombat(missionId: String) {
    viewModelScope.launch {
      val missions = repository.allMissions.firstOrNull() ?: emptyList()
      val mission = missions.find { it.id == missionId } ?: missions.firstOrNull()
      val operatives = repository.allOperatives.firstOrNull() ?: emptyList()
      val deployedOps = operatives.filter { it.isDeployed }.ifEmpty { operatives.take(3) }
      val items = repository.allItems.firstOrNull() ?: emptyList()

      // Generate 10x10 tiles
      val mapWidth = 10
      val mapHeight = 10
      val tilesMap = mutableMapOf<Pair<Int, Int>, TileType>()
      for (x in 0 until mapWidth) {
        for (y in 0 until mapHeight) {
          tilesMap[Pair(x, y)] = TileType.NORMAL
        }
      }

      // Add tactical terrain & environment objects on 10x10 map
      val envObjects = mutableListOf<EnvironmentObject>()

      // Cover barricades
      val coverPositions = listOf(
        Pair(2, 7) to "Energy Barricade Alpha",
        Pair(7, 7) to "Energy Barricade Beta",
        Pair(3, 3) to "Energy Barricade Gamma",
        Pair(6, 3) to "Energy Barricade Delta"
      )
      coverPositions.forEach { (pos, name) ->
        tilesMap[pos] = TileType.COVER
        envObjects.add(
          EnvironmentObject(
            name = name,
            type = EnvironmentObjectType.ENERGY_BARRICADE,
            gridX = pos.first,
            gridY = pos.second,
            coverDefBonus = 12,
            description = "+12 DEF cover barrier",
            themeColor = ElectricBlue
          )
        )
      }

      // Nanite Wells
      val nanitePositions = listOf(Pair(1, 5), Pair(8, 5))
      nanitePositions.forEach { pos ->
        tilesMap[pos] = TileType.NANITE_WELL
        envObjects.add(
          EnvironmentObject(
            name = "Sub-Ether Nanite Well",
            type = EnvironmentObjectType.NANITE_WELL,
            gridX = pos.first,
            gridY = pos.second,
            description = "Restores +15 Shield on contact",
            themeColor = MatrixGreen,
            effectType = EnvironmentEffectType.SHIELD_RESTORE
          )
        )
      }

      // EMP Ground Faults
      val empPositions = listOf(Pair(4, 5), Pair(5, 5), Pair(4, 2), Pair(5, 2))
      empPositions.forEach { pos ->
        tilesMap[pos] = TileType.EMP_HAZARD
        envObjects.add(
          EnvironmentObject(
            name = "EMP Discharge Fault",
            type = EnvironmentObjectType.EMP_HAZARD,
            gridX = pos.first,
            gridY = pos.second,
            description = "Deals 10 electric dmg",
            themeColor = NeonCrimson,
            effectType = EnvironmentEffectType.ELECTRIC_DAMAGE
          )
        )
      }

      // Structural Pillars
      val pillarPositions = listOf(Pair(3, 8), Pair(6, 8), Pair(2, 2), Pair(7, 2))
      pillarPositions.forEach { pos ->
        tilesMap[pos] = TileType.BLOCKED
        envObjects.add(
          EnvironmentObject(
            name = "Reinforced Security Pillar",
            type = EnvironmentObjectType.STRUCTURAL_PILLAR,
            gridX = pos.first,
            gridY = pos.second,
            description = "Impassable structural barrier",
            themeColor = BorderGlow,
            effectType = EnvironmentEffectType.PASSAGE_BLOCK
          )
        )
      }

      // Tactical Uplink Terminal & Supply Munitions Crate
      envObjects.add(
        EnvironmentObject(
          name = "Cyber Uplink Data Terminal",
          type = EnvironmentObjectType.HACK_TERMINAL,
          gridX = 0,
          gridY = 4,
          description = "Encrypted terminal holding tactical sector data",
          themeColor = NeonCyan,
          effectType = EnvironmentEffectType.TERMINAL_HACK
        )
      )
      envObjects.add(
        EnvironmentObject(
          name = "Syndicate Munitions Cache",
          type = EnvironmentObjectType.SUPPLY_CRATE,
          gridX = 9,
          gridY = 4,
          description = "Contains bonus tactical cyber credits",
          themeColor = CyberGold,
          effectType = EnvironmentEffectType.LOOT_REWARD
        )
      )

      // Build Combat Units
      val combatUnits = mutableListOf<CombatUnit>()

      // 1. Player Operatives (Spawned along bottom rows 8 & 9)
      val playerSpawnPositions = listOf(Pair(2, 9), Pair(4, 9), Pair(5, 9), Pair(7, 9))
      deployedOps.take(4).forEachIndexed { idx, op ->
        val pos = playerSpawnPositions.getOrElse(idx) { Pair(idx + 2, 9) }
        val heroCls = HeroClass.fromString(op.heroClass)
        // gear modifiers
        var bonusAtk = 0
        var bonusDef = 0
        var bonusHp = 0
        var bonusCrit = 0.0f
        listOfNotNull(op.equippedWeaponId, op.equippedArmorId, op.equippedCoreId, op.equippedChipId).forEach { gId ->
          val g = items.find { it.id == gId }
          if (g != null) {
            bonusAtk += g.atkBonus
            bonusDef += g.defBonus
            bonusHp += g.hpBonus
            bonusCrit += g.critBonus
          }
        }
        val totalHp = op.baseHp + bonusHp
        val totalAtk = op.baseAtk + bonusAtk
        val totalDef = op.baseDef + bonusDef
        val totalCrit = op.critRate + bonusCrit

        combatUnits.add(
          CombatUnit(
            id = op.id,
            name = op.name,
            faction = UnitFaction.PLAYER_OPERATIVE,
            heroClass = heroCls,
            maxHp = totalHp,
            currentHp = totalHp,
            maxShield = 40,
            currentShield = 0,
            atk = totalAtk,
            def = totalDef,
            critRate = totalCrit,
            moveRange = heroCls.moveRange + 1,
            attackRange = heroCls.attackRange + 1,
            gridX = pos.first,
            gridY = pos.second,
            actionPoints = 2,
            maxActionPoints = 2,
            avatarIcon = heroCls.baseIcon,
            themeColor = heroCls.primaryColor,
            portraitResId = com.example.ui.components.UnitPortraits.getOperativePortrait(heroCls, op.name)
          )
        )
      }

      // 2. Enemy Hostiles (Spawned across top rows 0 & 1)
      val isBoss = mission?.isBossEncounter == true
      if (isBoss) {
        // Boss Unit
        combatUnits.add(
          CombatUnit(
            id = "boss_commander",
            name = mission.bossName ?: "Apex AI Titan Mech",
            faction = UnitFaction.ENEMY_HOSTILE,
            heroClass = null,
            maxHp = 420,
            currentHp = 420,
            maxShield = 100,
            currentShield = 80,
            atk = 52,
            def = 26,
            critRate = 0.15f,
            moveRange = 3,
            attackRange = 3,
            gridX = 4,
            gridY = 1,
            avatarIcon = "👹",
            themeColor = NeonCrimson,
            isBoss = true,
            portraitResId = com.example.ui.components.UnitPortraits.APEX_TITAN_BOSS
          )
        )
        // Add escort drones & snipers
        combatUnits.add(
          CombatUnit(
            id = "drone_01",
            name = "Aegis Sentinel Drone",
            faction = UnitFaction.ENEMY_HOSTILE,
            heroClass = null,
            maxHp = 130,
            currentHp = 130,
            atk = 28,
            def = 14,
            critRate = 0.05f,
            moveRange = 3,
            attackRange = 3,
            gridX = 1,
            gridY = 0,
            avatarIcon = "🤖",
            themeColor = NeonCrimsonDim,
            portraitResId = com.example.ui.components.UnitPortraits.DRONE_STRIKER
          )
        )
        combatUnits.add(
          CombatUnit(
            id = "drone_02",
            name = "Assault Gunner Drone",
            faction = UnitFaction.ENEMY_HOSTILE,
            heroClass = null,
            maxHp = 150,
            currentHp = 150,
            atk = 34,
            def = 12,
            critRate = 0.10f,
            moveRange = 3,
            attackRange = 3,
            gridX = 8,
            gridY = 0,
            avatarIcon = "👾",
            themeColor = NeonCrimsonDim,
            portraitResId = com.example.ui.components.UnitPortraits.DRONE_STRIKER
          )
        )
        combatUnits.add(
          CombatUnit(
            id = "drone_03",
            name = "Overload Sentry Mech",
            faction = UnitFaction.ENEMY_HOSTILE,
            heroClass = null,
            maxHp = 180,
            currentHp = 180,
            atk = 38,
            def = 18,
            critRate = 0.12f,
            moveRange = 2,
            attackRange = 3,
            gridX = 5,
            gridY = 1,
            avatarIcon = "🦹",
            themeColor = NeonCrimson,
            portraitResId = com.example.ui.components.UnitPortraits.CYBORG_ENFORCER
          )
        )
      } else {
        // Standard Enemy squad
        combatUnits.add(
          CombatUnit(
            id = "enemy_01",
            name = "Rogue Syndicate Enforcer",
            faction = UnitFaction.ENEMY_HOSTILE,
            heroClass = null,
            maxHp = 160,
            currentHp = 160,
            atk = 30,
            def = 14,
            critRate = 0.08f,
            moveRange = 3,
            attackRange = 2,
            gridX = 2,
            gridY = 1,
            avatarIcon = "🦹",
            themeColor = NeonCrimson,
            portraitResId = com.example.ui.components.UnitPortraits.CYBORG_ENFORCER
          )
        )
        combatUnits.add(
          CombatUnit(
            id = "enemy_02",
            name = "Recon Cyber Drone",
            faction = UnitFaction.ENEMY_HOSTILE,
            heroClass = null,
            maxHp = 120,
            currentHp = 120,
            atk = 36,
            def = 8,
            critRate = 0.15f,
            moveRange = 4,
            attackRange = 4,
            gridX = 7,
            gridY = 0,
            avatarIcon = "🛰️",
            themeColor = NeonCrimsonDim,
            portraitResId = com.example.ui.components.UnitPortraits.DRONE_STRIKER
          )
        )
        combatUnits.add(
          CombatUnit(
            id = "enemy_03",
            name = "Cyborg Brute",
            faction = UnitFaction.ENEMY_HOSTILE,
            heroClass = null,
            maxHp = 210,
            currentHp = 210,
            atk = 26,
            def = 22,
            critRate = 0.05f,
            moveRange = 3,
            attackRange = 2,
            gridX = 4,
            gridY = 1,
            avatarIcon = "🦹",
            themeColor = NeonCrimson,
            portraitResId = com.example.ui.components.UnitPortraits.CYBORG_ENFORCER
          )
        )
      }

      val initialLog = CombatLog(
        message = "10x10 Tactical Grid Engagement initialized on sector: ${mission?.location ?: "Sector Grid"}",
        color = NeonCyan
      )

      val initialSelected = combatUnits.firstOrNull { it.faction == UnitFaction.PLAYER_OPERATIVE }
      val tempState = CombatGridState(
        mission = mission,
        phase = BattlePhase.SELECT_UNIT,
        units = combatUnits,
        environmentObjects = envObjects,
        selectedUnitId = initialSelected?.id,
        gridWidth = mapWidth,
        gridHeight = mapHeight,
        tiles = tilesMap,
        combatLogs = listOf(initialLog)
      )

      val initialReachable = if (initialSelected != null) {
        calculateReachableTiles(initialSelected, tempState)
      } else emptySet()

      _gridState.value = tempState.copy(
        phase = if (initialReachable.isNotEmpty()) BattlePhase.ACTION_MOVE else BattlePhase.SELECT_UNIT,
        reachableTiles = initialReachable
      )
    }
  }

  /**
   * Computes the set of valid reachable grid coordinates for a combat unit using BFS pathfinding.
   * Considers remaining Action Points, unit movement range, map boundaries, blocked terrain,
   * environmental obstacles, and unit collision.
   */
  fun calculateReachableTiles(unit: CombatUnit, state: CombatGridState): Set<Pair<Int, Int>> {
    if (unit.actionPoints <= 0 || unit.isStunned || !unit.isAlive) {
      return emptySet()
    }

    val range = unit.moveRange
    val reachable = mutableSetOf<Pair<Int, Int>>()
    val visited = mutableMapOf<Pair<Int, Int>, Int>()
    val queue = ArrayDeque<Pair<Pair<Int, Int>, Int>>()

    val start = Pair(unit.gridX, unit.gridY)
    queue.add(Pair(start, 0))
    visited[start] = 0

    val directions = listOf(Pair(0, 1), Pair(0, -1), Pair(1, 0), Pair(-1, 0))

    while (queue.isNotEmpty()) {
      val (currentPos, cost) = queue.removeFirst()
      if (cost < range) {
        for (dir in directions) {
          val nextX = currentPos.first + dir.first
          val nextY = currentPos.second + dir.second
          val nextPos = Pair(nextX, nextY)
          val nextCost = cost + 1

          if (nextX in 0 until state.gridWidth && nextY in 0 until state.gridHeight) {
            val tileType = state.tiles[nextPos] ?: TileType.NORMAL
            val isBlockedEnv = state.environmentObjects.any {
              it.gridX == nextX && it.gridY == nextY && it.effectType == EnvironmentEffectType.PASSAGE_BLOCK
            }
            val unitAtTile = state.units.find { it.gridX == nextX && it.gridY == nextY && it.isAlive }

            // Unit cannot move through impassable structural pillars or blocked terrain
            if (tileType != TileType.BLOCKED && !isBlockedEnv) {
              // Units cannot pass through enemy hostiles
              val isEnemyHostile = unitAtTile != null && unitAtTile.faction != unit.faction
              if (!isEnemyHostile) {
                if (nextCost < (visited[nextPos] ?: Int.MAX_VALUE)) {
                  visited[nextPos] = nextCost
                  queue.add(Pair(nextPos, nextCost))

                  // Can only stop on empty tiles (not occupied by another unit)
                  if (unitAtTile == null) {
                    reachable.add(nextPos)
                  }
                }
              }
            }
          }
        }
      }
    }
    return reachable
  }

  fun selectUnit(unitId: String) {
    val state = _gridState.value
    if (state.isBattleOver || !state.isPlayerTurn) return
    val unit = state.units.find { it.id == unitId } ?: return

    if (unit.faction == UnitFaction.PLAYER_OPERATIVE && unit.isAlive) {
      SoundManager.playButtonClick()
      val reachable = calculateReachableTiles(unit, state)

      _gridState.value = state.copy(
        selectedUnitId = unitId,
        phase = if (reachable.isNotEmpty()) BattlePhase.ACTION_MOVE else BattlePhase.SELECT_UNIT,
        reachableTiles = reachable,
        targetableTiles = emptySet(),
        specialTargetTiles = emptySet()
      )

      if (unit.actionPoints <= 0) {
        addCombatLog("${unit.name} selected. No Action Points (AP) remaining.", TextMuted)
      } else if (reachable.isEmpty()) {
        addCombatLog("${unit.name} selected. No clear movement vectors.", NeonCrimson)
      } else {
        addCombatLog("${unit.name} selected: ${reachable.size} valid movement vector(s) active.", NeonCyan)
      }
    } else if (unit.faction == UnitFaction.ENEMY_HOSTILE && unit.isAlive) {
      SoundManager.playButtonClick()
      addCombatLog("Target Scan: ${unit.name} [HP: ${unit.currentHp}/${unit.maxHp}, Move: ${unit.moveRange}, Range: ${unit.attackRange}]", NeonCrimson)
    }
  }

  fun onTileClicked(x: Int, y: Int) {
    val state = _gridState.value
    if (state.isBattleOver || !state.isPlayerTurn) return

    val clickedPos = Pair(x, y)
    val unitAtTile = state.units.find { it.gridX == x && it.gridY == y && it.isAlive }

    // 1. Clicking a player character selects them and highlights movement tiles
    if (unitAtTile != null && unitAtTile.faction == UnitFaction.PLAYER_OPERATIVE) {
      selectUnit(unitAtTile.id)
      return
    }

    // 2. Clicking a valid movement tile moves the selected unit
    if (state.reachableTiles.contains(clickedPos) && state.selectedUnitId != null) {
      executeMove(x, y)
      return
    }

    // 3. Action attack or special targeting
    when (state.phase) {
      BattlePhase.ACTION_ATTACK -> {
        if (state.targetableTiles.contains(clickedPos)) {
          val target = state.units.find { it.gridX == x && it.gridY == y && it.isAlive }
          if (target != null) {
            executeAttack(target.id)
          }
        }
      }
      BattlePhase.ACTION_SPECIAL -> {
        if (state.specialTargetTiles.contains(clickedPos)) {
          executeSpecialAbility(x, y)
        }
      }
      else -> {
        if (unitAtTile != null) {
          selectUnit(unitAtTile.id)
        }
      }
    }
  }

  fun prepareMove() {
    val state = _gridState.value
    val selectedUnit = state.units.find { it.id == state.selectedUnitId } ?: return
    if (selectedUnit.actionPoints <= 0) {
      addCombatLog("Insufficient Action Points (AP) to move!", NeonCrimson)
      return
    }

    SoundManager.playButtonClick()
    val reachable = calculateReachableTiles(selectedUnit, state)

    _gridState.value = state.copy(
      phase = BattlePhase.ACTION_MOVE,
      reachableTiles = reachable,
      targetableTiles = emptySet(),
      specialTargetTiles = emptySet()
    )
  }

  private fun executeMove(targetX: Int, targetY: Int) {
    val state = _gridState.value
    val selectedUnit = state.units.find { it.id == state.selectedUnitId } ?: return

    val tileType = state.tiles[Pair(targetX, targetY)] ?: TileType.NORMAL
    var shieldBonus = 0
    var hazardDmg = 0

    if (tileType == TileType.NANITE_WELL) {
      shieldBonus = 15
      SoundManager.playShield()
    } else if (tileType == TileType.EMP_HAZARD) {
      hazardDmg = 10
      SoundManager.playEmp()
    } else {
      SoundManager.playButtonClick()
    }

    val updatedUnits = state.units.map { u ->
      if (u.id == selectedUnit.id) {
        u.copy(
          gridX = targetX,
          gridY = targetY,
          actionPoints = u.actionPoints - 1,
          currentShield = (u.currentShield + shieldBonus).coerceAtMost(u.maxShield),
          currentHp = (u.currentHp - hazardDmg).coerceAtLeast(1)
        )
      } else u
    }

    val movedUnit = updatedUnits.find { it.id == selectedUnit.id }
    val remainingReachable = if (movedUnit != null && movedUnit.actionPoints > 0) {
      calculateReachableTiles(movedUnit, state.copy(units = updatedUnits))
    } else emptySet()

    val logs = state.combatLogs.toMutableList()
    logs.add(0, CombatLog(message = "${selectedUnit.name} moved to sector [${targetX + 1}, ${('A'.code + targetY).toChar()}].", color = NeonCyan))
    if (shieldBonus > 0) logs.add(0, CombatLog(message = "${selectedUnit.name} absorbed +15 Nanite Shield!", color = MatrixGreen))
    if (hazardDmg > 0) logs.add(0, CombatLog(message = "${selectedUnit.name} took 10 EMP Grid hazard dmg!", color = NeonCrimson))

    _gridState.value = state.copy(
      units = updatedUnits,
      phase = if (remainingReachable.isNotEmpty()) BattlePhase.ACTION_MOVE else BattlePhase.SELECT_UNIT,
      reachableTiles = remainingReachable,
      targetableTiles = emptySet(),
      specialTargetTiles = emptySet(),
      combatLogs = logs
    )
  }

  fun prepareAttack() {
    val state = _gridState.value
    val selectedUnit = state.units.find { it.id == state.selectedUnitId } ?: return
    if (selectedUnit.actionPoints <= 0) {
      addCombatLog("Insufficient Action Points (AP) to strike!", NeonCrimson)
      return
    }

    SoundManager.playButtonClick()
    val targetable = mutableSetOf<Pair<Int, Int>>()
    val range = selectedUnit.attackRange

    state.units.filter { it.faction == UnitFaction.ENEMY_HOSTILE && it.isAlive }.forEach { enemy ->
      val dist = abs(enemy.gridX - selectedUnit.gridX) + abs(enemy.gridY - selectedUnit.gridY)
      if (dist <= range) {
        targetable.add(Pair(enemy.gridX, enemy.gridY))
      }
    }

    if (targetable.isEmpty()) {
      addCombatLog("No enemy targets within attack range ($range tiles)!", CyberAmber)
    }

    _gridState.value = state.copy(
      phase = BattlePhase.ACTION_ATTACK,
      targetableTiles = targetable,
      reachableTiles = emptySet(),
      specialTargetTiles = emptySet()
    )
  }

  private fun executeAttack(targetUnitId: String) {
    val state = _gridState.value
    val attacker = state.units.find { it.id == state.selectedUnitId } ?: return
    val defender = state.units.find { it.id == targetUnitId } ?: return

    // Sound effect
    if (attacker.heroClass == HeroClass.SAMURAI) {
      SoundManager.playSlash()
    } else {
      SoundManager.playLaserShoot()
    }

    // Damage calculation
    val isCrit = Random.nextFloat() < attacker.critRate
    val critMultiplier = if (isCrit) 2.0f else 1.0f
    val tile = state.tiles[Pair(defender.gridX, defender.gridY)] ?: TileType.NORMAL
    val defValue = defender.def + tile.defBonus
    val rawDmg = max(8, ((attacker.atk * critMultiplier) - (defValue * 0.45f)).toInt())

    // Shield absorption
    var remainingDmg = rawDmg
    var newShield = defender.currentShield
    if (newShield > 0) {
      if (newShield >= remainingDmg) {
        newShield -= remainingDmg
        remainingDmg = 0
      } else {
        remainingDmg -= newShield
        newShield = 0
      }
    }

    val newHp = (defender.currentHp - remainingDmg).coerceAtLeast(0)

    val updatedUnits = state.units.map { u ->
      if (u.id == attacker.id) {
        u.copy(actionPoints = u.actionPoints - 1)
      } else if (u.id == defender.id) {
        u.copy(currentHp = newHp, currentShield = newShield)
      } else u
    }

    val isMelee = attacker.heroClass == HeroClass.SAMURAI
    val damageType = when {
      isCrit -> CombatDamageType.CRITICAL
      isMelee -> CombatDamageType.BLADE_SLASH
      defender.currentShield > 0 && remainingDmg == 0 -> CombatDamageType.SHIELD_BREAK
      else -> CombatDamageType.NORMAL
    }

    val floatingText = when {
      isCrit -> "💥 CRIT -$rawDmg"
      isMelee -> "⚔️ -$rawDmg"
      defender.currentShield > 0 && remainingDmg == 0 -> "🛡️ -$rawDmg"
      else -> "-$rawDmg"
    }

    val newFloating = FloatingDamage(
      gridX = defender.gridX,
      gridY = defender.gridY,
      text = floatingText,
      color = if (isCrit) CyberGold else NeonCrimson,
      isCrit = isCrit,
      damageType = damageType
    )

    val logs = state.combatLogs.toMutableList()
    val critText = if (isCrit) " [CRITICAL STRIKE]" else ""
    logs.add(0, CombatLog(message = "${attacker.name} attacked ${defender.name} for $rawDmg dmg$critText.", color = if (isCrit) CyberGold else NeonCrimson))
    if (!defender.isAlive || newHp <= 0) {
      logs.add(0, CombatLog(message = "Hostile ${defender.name} neutralized!", color = MatrixGreen))
    }

    _gridState.value = state.copy(
      units = updatedUnits,
      phase = BattlePhase.SELECT_UNIT,
      targetableTiles = emptySet(),
      floatingDamages = state.floatingDamages + newFloating,
      combatLogs = logs
    )

    checkBattleEnd()
  }

  fun prepareSpecial() {
    val state = _gridState.value
    val selectedUnit = state.units.find { it.id == state.selectedUnitId } ?: return
    if (selectedUnit.actionPoints <= 0) {
      addCombatLog("Insufficient AP for Special Tactical Ability!", NeonCrimson)
      return
    }
    if (selectedUnit.specialCooldown > 0) {
      addCombatLog("Ability is recharging (${selectedUnit.specialCooldown} turns remaining)!", CyberAmber)
      return
    }

    SoundManager.playButtonClick()
    val specialTiles = mutableSetOf<Pair<Int, Int>>()

    when (selectedUnit.heroClass) {
      HeroClass.VANGUARD -> {
        // Self/Adjacent shield
        specialTiles.add(Pair(selectedUnit.gridX, selectedUnit.gridY))
      }
      HeroClass.MEDIC -> {
        // Adjacent friendly tiles
        for (dx in -1..1) {
          for (dy in -1..1) {
            val tx = selectedUnit.gridX + dx
            val ty = selectedUnit.gridY + dy
            if (tx in 0..5 && ty in 0..5) specialTiles.add(Pair(tx, ty))
          }
        }
      }
      HeroClass.SNIPER -> {
        // Long range targets
        state.units.filter { it.faction == UnitFaction.ENEMY_HOSTILE && it.isAlive }.forEach {
          specialTiles.add(Pair(it.gridX, it.gridY))
        }
      }
      HeroClass.CIPHER -> {
        // 2-tile radius around enemies
        state.units.filter { it.faction == UnitFaction.ENEMY_HOSTILE && it.isAlive }.forEach {
          specialTiles.add(Pair(it.gridX, it.gridY))
        }
      }
      HeroClass.SAMURAI -> {
        // 1-tile melee adjacent enemies
        state.units.filter { it.faction == UnitFaction.ENEMY_HOSTILE && it.isAlive }.forEach { e ->
          if (abs(e.gridX - selectedUnit.gridX) <= 1 && abs(e.gridY - selectedUnit.gridY) <= 1) {
            specialTiles.add(Pair(e.gridX, e.gridY))
          }
        }
      }
      null -> {}
    }

    _gridState.value = state.copy(
      phase = BattlePhase.ACTION_SPECIAL,
      specialTargetTiles = specialTiles,
      reachableTiles = emptySet(),
      targetableTiles = emptySet()
    )
  }

  private fun executeSpecialAbility(targetX: Int, targetY: Int) {
    val state = _gridState.value
    val hero = state.units.find { it.id == state.selectedUnitId } ?: return
    val logs = state.combatLogs.toMutableList()
    val floatings = state.floatingDamages.toMutableList()

    var updatedUnits = state.units.map { it.copy() }.toMutableList()

    when (hero.heroClass) {
      HeroClass.VANGUARD -> {
        SoundManager.playShield()
        // Plasma Barrier: +60 Shield to self & taunts enemies
        updatedUnits = updatedUnits.map { u ->
          if (u.id == hero.id) {
            u.copy(
              currentShield = (u.currentShield + 60).coerceAtMost(u.maxShield + 60),
              specialCooldown = 3,
              actionPoints = u.actionPoints - 1
            )
          } else u
        }.toMutableList()
        floatings.add(FloatingDamage(hero.gridX, hero.gridY, "🛡️ +60 SHIELD", MatrixGreen, damageType = CombatDamageType.HEAL))
        logs.add(0, CombatLog(message = "${hero.name} deployed Plasma Barrier (+60 Shield)!", color = MatrixGreen))
      }
      HeroClass.MEDIC -> {
        SoundManager.playShield()
        // Nanite Surge: Heals allies in target radius
        updatedUnits = updatedUnits.map { u ->
          if (u.faction == UnitFaction.PLAYER_OPERATIVE && abs(u.gridX - targetX) <= 1 && abs(u.gridY - targetY) <= 1) {
            floatings.add(FloatingDamage(u.gridX, u.gridY, "💚 +70 HP", MatrixGreen, damageType = CombatDamageType.HEAL))
            u.copy(currentHp = (u.currentHp + 70).coerceAtMost(u.maxHp))
          } else if (u.id == hero.id) {
            u.copy(specialCooldown = 3, actionPoints = u.actionPoints - 1)
          } else u
        }.toMutableList()
        logs.add(0, CombatLog(message = "${hero.name} activated Nanite Surge, repairing squad HP!", color = MatrixGreen))
      }
      HeroClass.SNIPER -> {
        SoundManager.playLaserShoot()
        // Overcharge Shot: 2.5x critical dmg
        val target = updatedUnits.find { it.gridX == targetX && it.gridY == targetY && it.isAlive }
        if (target != null) {
          val dmg = (hero.atk * 2.5f).toInt()
          val newHp = (target.currentHp - dmg).coerceAtLeast(0)
          updatedUnits = updatedUnits.map { u ->
            if (u.id == target.id) u.copy(currentHp = newHp)
            else if (u.id == hero.id) u.copy(specialCooldown = 3, actionPoints = u.actionPoints - 1)
            else u
          }.toMutableList()
          floatings.add(FloatingDamage(target.gridX, target.gridY, "💥 -$dmg CRIT!", CyberGold, isCrit = true, damageType = CombatDamageType.CRITICAL))
          logs.add(0, CombatLog(message = "${hero.name} fired Overcharge Shot at ${target.name} for $dmg damage!", color = CyberGold))
        }
      }
      HeroClass.CIPHER -> {
        SoundManager.playEmp()
        // EMP Pulse: Stuns and drains shield
        updatedUnits = updatedUnits.map { u ->
          if (u.faction == UnitFaction.ENEMY_HOSTILE && abs(u.gridX - targetX) <= 1 && abs(u.gridY - targetY) <= 1) {
            floatings.add(FloatingDamage(u.gridX, u.gridY, "⚡ EMP STUNNED!", ElectricPurple, damageType = CombatDamageType.EMP_SHOCK))
            u.copy(currentShield = 0, isStunned = true, currentHp = (u.currentHp - 25).coerceAtLeast(0))
          } else if (u.id == hero.id) {
            u.copy(specialCooldown = 3, actionPoints = u.actionPoints - 1)
          } else u
        }.toMutableList()
        logs.add(0, CombatLog(message = "${hero.name} detonated an EMP Pulse, disabling enemy electronics!", color = ElectricPurple))
      }
      HeroClass.SAMURAI -> {
        SoundManager.playSlash()
        // Blade Dance: Strikes target with double slash
        val target = updatedUnits.find { it.gridX == targetX && it.gridY == targetY && it.isAlive }
        if (target != null) {
          val dmg = (hero.atk * 1.8f).toInt()
          val newHp = (target.currentHp - dmg).coerceAtLeast(0)
          updatedUnits = updatedUnits.map { u ->
            if (u.id == target.id) u.copy(currentHp = newHp)
            else if (u.id == hero.id) u.copy(specialCooldown = 3, actionPoints = u.actionPoints - 1)
            else u
          }.toMutableList()
          floatings.add(FloatingDamage(target.gridX, target.gridY, "⚔️ -$dmg BLADE DANCE!", NeonCrimson, isCrit = true, damageType = CombatDamageType.BLADE_SLASH))
          logs.add(0, CombatLog(message = "${hero.name} unleashed Blade Dance upon ${target.name} for $dmg damage!", color = NeonCrimson))
        }
      }
      null -> {}
    }

    _gridState.value = state.copy(
      units = updatedUnits,
      phase = BattlePhase.SELECT_UNIT,
      specialTargetTiles = emptySet(),
      floatingDamages = floatings,
      combatLogs = logs
    )

    checkBattleEnd()
  }

  fun endTurn() {
    val state = _gridState.value
    if (state.isBattleOver || !state.isPlayerTurn) return

    SoundManager.playButtonClick()
    addCombatLog("Ending Squad Turn. Hostile AI processing moves...", CyberAmber)

    viewModelScope.launch {
      // Switch to Enemy Turn
      _gridState.value = _gridState.value.copy(
        isPlayerTurn = false,
        phase = BattlePhase.ENEMY_TURN,
        reachableTiles = emptySet(),
        targetableTiles = emptySet(),
        specialTargetTiles = emptySet()
      )

      delay(600)
      executeEnemyTurn()
    }
  }

  private suspend fun executeEnemyTurn() {
    val state = _gridState.value
    var currentUnits = state.units.map { it.copy() }.toMutableList()
    val logs = state.combatLogs.toMutableList()
    val floatings = state.floatingDamages.toMutableList()

    val aliveEnemies = currentUnits.filter { it.faction == UnitFaction.ENEMY_HOSTILE && it.isAlive }

    for (enemy in aliveEnemies) {
      if (enemy.isStunned) {
        logs.add(0, CombatLog(message = "${enemy.name} is EMP STUNNED and skipped action!", color = ElectricPurple))
        currentUnits = currentUnits.map { if (it.id == enemy.id) it.copy(isStunned = false) else it }.toMutableList()
        delay(400)
        continue
      }

      val alivePlayers = currentUnits.filter { it.faction == UnitFaction.PLAYER_OPERATIVE && it.isAlive }
      if (alivePlayers.isEmpty()) break

      // Find closest player unit
      val targetPlayer = alivePlayers.minByOrNull {
        abs(it.gridX - enemy.gridX) + abs(it.gridY - enemy.gridY)
      } ?: alivePlayers.first()

      val dist = abs(targetPlayer.gridX - enemy.gridX) + abs(targetPlayer.gridY - enemy.gridY)

      var currentEnemyX = enemy.gridX
      var currentEnemyY = enemy.gridY

      // Move closer if not in attack range
      if (dist > enemy.attackRange) {
        val dx = (targetPlayer.gridX - currentEnemyX).coerceIn(-1, 1)
        val dy = (targetPlayer.gridY - currentEnemyY).coerceIn(-1, 1)
        val newX = (currentEnemyX + dx).coerceIn(0, state.gridWidth - 1)
        val newY = (currentEnemyY + dy).coerceIn(0, state.gridHeight - 1)

        val tileType = state.tiles[Pair(newX, newY)] ?: TileType.NORMAL
        val isOccupied = currentUnits.any { it.gridX == newX && it.gridY == newY && it.isAlive }

        if (tileType != TileType.BLOCKED && !isOccupied) {
          currentEnemyX = newX
          currentEnemyY = newY
          currentUnits = currentUnits.map {
            if (it.id == enemy.id) it.copy(gridX = currentEnemyX, gridY = currentEnemyY) else it
          }.toMutableList()
          _gridState.value = _gridState.value.copy(units = currentUnits)
          delay(400)
        }
      }

      // Attack player if within range
      val newDist = abs(targetPlayer.gridX - currentEnemyX) + abs(targetPlayer.gridY - currentEnemyY)
      if (newDist <= enemy.attackRange) {
        SoundManager.playLaserShoot()
        val dmg = max(6, (enemy.atk - (targetPlayer.def * 0.4f)).toInt())
        var remDmg = dmg
        var pShield = targetPlayer.currentShield
        if (pShield > 0) {
          if (pShield >= remDmg) {
            pShield -= remDmg
            remDmg = 0
          } else {
            remDmg -= pShield
            pShield = 0
          }
        }
        val pNewHp = (targetPlayer.currentHp - remDmg).coerceAtLeast(0)

        currentUnits = currentUnits.map {
          if (it.id == targetPlayer.id) it.copy(currentHp = pNewHp, currentShield = pShield) else it
        }.toMutableList()

        val isEnemyCrit = Random.nextFloat() < enemy.critRate
        val enemyDmgText = if (isEnemyCrit) "💥 CRIT -$dmg" else "-$dmg"
        val enemyDamageType = if (isEnemyCrit) CombatDamageType.CRITICAL else if (pShield > 0 && remDmg == 0) CombatDamageType.SHIELD_BREAK else CombatDamageType.NORMAL

        floatings.add(
          FloatingDamage(
            gridX = targetPlayer.gridX,
            gridY = targetPlayer.gridY,
            text = enemyDmgText,
            color = if (isEnemyCrit) CyberGold else NeonCrimson,
            isCrit = isEnemyCrit,
            damageType = enemyDamageType
          )
        )
        val enemyCritLog = if (isEnemyCrit) " [CRITICAL STRIKE]" else ""
        logs.add(0, CombatLog(message = "${enemy.name} struck ${targetPlayer.name} for $dmg damage$enemyCritLog!", color = if (isEnemyCrit) CyberGold else NeonCrimson))

        if (pNewHp <= 0) {
          logs.add(0, CombatLog(message = "⚠️ ${targetPlayer.name} was incapacitated in action!", color = NeonCrimson))
        }

        _gridState.value = _gridState.value.copy(
          units = currentUnits,
          combatLogs = logs,
          floatingDamages = floatings
        )
        delay(500)
      }
    }

    // Reset player AP and reduce cooldowns for next round
    val nextRoundUnits = currentUnits.map { u ->
      if (u.faction == UnitFaction.PLAYER_OPERATIVE) {
        u.copy(
          actionPoints = u.maxActionPoints,
          specialCooldown = max(0, u.specialCooldown - 1)
        )
      } else {
        u.copy(actionPoints = u.maxActionPoints)
      }
    }

    logs.add(0, CombatLog(message = "Round ${_gridState.value.currentTurn + 1}: Operative AP replenished.", color = NeonCyan))

    val activeSelected = nextRoundUnits.firstOrNull { it.id == _gridState.value.selectedUnitId && it.isAlive }
      ?: nextRoundUnits.firstOrNull { it.faction == UnitFaction.PLAYER_OPERATIVE && it.isAlive }

    val nextState = _gridState.value.copy(
      units = nextRoundUnits,
      isPlayerTurn = true,
      currentTurn = _gridState.value.currentTurn + 1,
      selectedUnitId = activeSelected?.id,
      combatLogs = logs
    )

    val reachable = if (activeSelected != null) {
      calculateReachableTiles(activeSelected, nextState)
    } else emptySet()

    _gridState.value = nextState.copy(
      phase = if (reachable.isNotEmpty()) BattlePhase.ACTION_MOVE else BattlePhase.SELECT_UNIT,
      reachableTiles = reachable,
      targetableTiles = emptySet(),
      specialTargetTiles = emptySet()
    )

    checkBattleEnd()
  }

  private fun checkBattleEnd() {
    val state = _gridState.value
    val aliveEnemies = state.units.filter { it.faction == UnitFaction.ENEMY_HOSTILE && it.isAlive }
    val alivePlayers = state.units.filter { it.faction == UnitFaction.PLAYER_OPERATIVE && it.isAlive }

    if (aliveEnemies.isEmpty() && !state.isBattleOver) {
      // VICTORY
      SoundManager.playVictory()
      val mission = state.mission
      val stars = if (alivePlayers.size >= 3) 3 else if (alivePlayers.size >= 2) 2 else 1
      val rewards = mapOf(
        "Credits" to (mission?.rewardCredits ?: 300),
        "Tactical Data" to (mission?.rewardData ?: 150),
        "XP" to (mission?.rewardXp ?: 100),
        "Stars" to stars
      )

      viewModelScope.launch {
        if (mission != null) {
          repository.completeMission(mission.id, stars)
        }
      }

      val logs = state.combatLogs.toMutableList()
      logs.add(0, CombatLog(message = "🏆 VICTORY ACHIEVED! Sector neutralized with $stars-Star rating.", color = MatrixGreen))

      _gridState.value = state.copy(
        isBattleOver = true,
        didPlayerWin = true,
        phase = BattlePhase.VICTORY,
        rewardsEarned = rewards,
        combatLogs = logs
      )
    } else if (alivePlayers.isEmpty() && !state.isBattleOver) {
      // DEFEAT
      SoundManager.playDefeat()
      val logs = state.combatLogs.toMutableList()
      logs.add(0, CombatLog(message = "☠️ SQUAD WIPEOUT: All operatives incapacitated.", color = NeonCrimson))

      _gridState.value = state.copy(
        isBattleOver = true,
        didPlayerWin = false,
        phase = BattlePhase.DEFEAT,
        combatLogs = logs
      )
    }
  }

  private fun addCombatLog(msg: String, color: Color) {
    val logs = _gridState.value.combatLogs.toMutableList()
    logs.add(0, CombatLog(message = msg, color = color))
    _gridState.value = _gridState.value.copy(combatLogs = logs)
  }

  fun dismissFloatingDamage(id: String) {
    _gridState.value = _gridState.value.copy(
      floatingDamages = _gridState.value.floatingDamages.filter { it.id != id }
    )
  }
}
