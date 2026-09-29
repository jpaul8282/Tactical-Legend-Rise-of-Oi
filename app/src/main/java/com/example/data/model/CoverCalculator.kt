package com.example.data.model

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Tactical Cover Calculation Engine.
 * Evaluates directional cover bonuses, line-of-sight obstacle interception,
 * flanking angles, and defensive damage mitigation for the 10x10 battle grid.
 */
object CoverCalculator {

  /**
   * Checks whether a specific grid coordinate contains a physical obstacle that provides cover.
   * Returns a Pair of (isObstacle, CoverType).
   */
  fun getObstacleAt(
    x: Int,
    y: Int,
    tiles: Map<Pair<Int, Int>, TileType>,
    envObjects: List<EnvironmentObject>
  ): Pair<Boolean, CoverType> {
    val coord = Pair(x, y)
    val tile = tiles[coord]

    // Check environment objects first
    val env = envObjects.find { it.gridX == x && it.gridY == y }
    if (env != null) {
      return when (env.type) {
        EnvironmentObjectType.STRUCTURAL_PILLAR,
        EnvironmentObjectType.REINFORCED_BUNKER -> Pair(true, CoverType.FULL)

        EnvironmentObjectType.ENERGY_BARRICADE,
        EnvironmentObjectType.DEFENSE_TURRET -> Pair(true, CoverType.HALF)

        else -> {
          if (env.effectType == EnvironmentEffectType.PASSAGE_BLOCK) {
            Pair(true, CoverType.FULL)
          } else if (env.coverDefBonus >= 20) {
            Pair(true, CoverType.FULL)
          } else if (env.coverDefBonus > 0) {
            Pair(true, CoverType.HALF)
          } else {
            Pair(false, CoverType.NONE)
          }
        }
      }
    }

    // Check base tile type
    return when (tile) {
      TileType.BLOCKED -> Pair(true, CoverType.FULL)
      TileType.COVER -> Pair(true, CoverType.HALF)
      else -> Pair(false, CoverType.NONE)
    }
  }

  /**
   * Computes the directional cover that [defenderX, defenderY] receives against an attack
   * coming from [attackerX, attackerY].
   */
  fun calculateCover(
    defenderX: Int,
    defenderY: Int,
    attackerX: Int,
    attackerY: Int,
    tiles: Map<Pair<Int, Int>, TileType>,
    envObjects: List<EnvironmentObject>
  ): CoverResult {
    val distToAttacker = abs(attackerX - defenderX) + abs(attackerY - defenderY)

    // Point-blank / melee range: no obstacle can intervene directly between adjacent tiles
    if (distToAttacker <= 1) {
      // Check if defender is occupying a bunker or cover tile
      val (isDirectCover, directType) = getObstacleAt(defenderX, defenderY, tiles, envObjects)
      if (isDirectCover && directType != CoverType.NONE) {
        return CoverResult(
          type = CoverType.HALF,
          defBonus = 8,
          critReduction = 0.25f,
          damageReductionPct = 0.15f,
          obstacleName = "Entrenched Position"
        )
      }
      return CoverResult(type = CoverType.NONE)
    }

    var bestCover = CoverType.NONE
    var protectingObstacleName: String? = null
    var protectingObstacleCoord: Pair<Int, Int>? = null
    var hasAnyAdjacentObstacle = false

    // 1. Check adjacent tiles around defender (North, South, East, West, and diagonals)
    val adjacentOffsets = listOf(
      Pair(0, -1),  // North
      Pair(0, 1),   // South
      Pair(-1, 0),  // West
      Pair(1, 0),   // East
      Pair(-1, -1), // NW
      Pair(1, -1),  // NE
      Pair(-1, 1),  // SW
      Pair(1, 1)    // SE
    )

    for (offset in adjacentOffsets) {
      val ox = defenderX + offset.first
      val oy = defenderY + offset.second

      val (isObstacle, coverType) = getObstacleAt(ox, oy, tiles, envObjects)
      if (isObstacle) {
        hasAnyAdjacentObstacle = true
        // Check if this obstacle is positioned between defender and attacker
        val distObstacleToAttacker = abs(attackerX - ox) + abs(attackerY - oy)
        val dotProduct = (offset.first * (attackerX - defenderX)) + (offset.second * (attackerY - defenderY))

        // If the obstacle is closer to the attacker and angled towards the attacker's vector
        if (distObstacleToAttacker < distToAttacker && dotProduct > 0) {
          if (coverType == CoverType.FULL) {
            val env = envObjects.find { it.gridX == ox && it.gridY == oy }
            protectingObstacleName = env?.name ?: "Structural Pillar"
            protectingObstacleCoord = Pair(ox, oy)
            bestCover = CoverType.FULL
            break // Full cover is the highest possible cover
          } else if (coverType == CoverType.HALF && bestCover == CoverType.NONE) {
            val env = envObjects.find { it.gridX == ox && it.gridY == oy }
            protectingObstacleName = env?.name ?: "Energy Barricade"
            protectingObstacleCoord = Pair(ox, oy)
            bestCover = CoverType.HALF
          }
        }
      }
    }

    // 2. If no adjacent directional cover was found, check intervening line-of-sight raycast
    if (bestCover == CoverType.NONE) {
      val lineTiles = getBresenhamLine(defenderX, defenderY, attackerX, attackerY)
      // Exclude defender and attacker tiles
      val intervening = lineTiles.filter { it != Pair(defenderX, defenderY) && it != Pair(attackerX, attackerY) }

      for (coord in intervening) {
        val (isObstacle, coverType) = getObstacleAt(coord.first, coord.second, tiles, envObjects)
        if (isObstacle) {
          val env = envObjects.find { it.gridX == coord.first && it.gridY == coord.second }
          protectingObstacleName = env?.name ?: "Intervening Barrier"
          protectingObstacleCoord = coord
          if (coverType == CoverType.FULL) {
            bestCover = CoverType.FULL
            break
          } else if (coverType == CoverType.HALF && bestCover == CoverType.NONE) {
            bestCover = CoverType.HALF
          }
        }
      }
    }

    // 3. Check if defender is directly occupying a cover tile (e.g. Energy Barricade tile)
    if (bestCover == CoverType.NONE) {
      val (isDirectCover, directType) = getObstacleAt(defenderX, defenderY, tiles, envObjects)
      if (isDirectCover && directType != CoverType.NONE) {
        bestCover = CoverType.HALF
        protectingObstacleName = "Fortified Deck"
        protectingObstacleCoord = Pair(defenderX, defenderY)
      }
    }

    // Detect flanking: defender has an obstacle nearby, but attacker fired from an unprotected angle
    val isFlanked = bestCover == CoverType.NONE && hasAnyAdjacentObstacle

    return CoverResult(
      type = bestCover,
      defBonus = bestCover.defBonus,
      critReduction = bestCover.critReduction,
      damageReductionPct = bestCover.damageReductionPct,
      obstacleName = protectingObstacleName,
      isFlanked = isFlanked,
      obstacleCoord = protectingObstacleCoord
    )
  }

  /**
   * Calculates the general ambient cover of a tile [x, y] against the nearest active enemy units.
   * Useful for showing shield indicators on movement reachable tiles and unit tokens.
   */
  fun getAmbientCover(
    x: Int,
    y: Int,
    unitFaction: UnitFaction,
    units: List<CombatUnit>,
    tiles: Map<Pair<Int, Int>, TileType>,
    envObjects: List<EnvironmentObject>
  ): CoverType {
    val hostileFaction = if (unitFaction == UnitFaction.PLAYER_OPERATIVE) {
      UnitFaction.ENEMY_HOSTILE
    } else {
      UnitFaction.PLAYER_OPERATIVE
    }

    val activeEnemies = units.filter { it.faction == hostileFaction && it.isAlive }
    if (activeEnemies.isEmpty()) return CoverType.NONE

    // Find the closest enemy to this position
    val closestEnemy = activeEnemies.minByOrNull {
      abs(it.gridX - x) + abs(it.gridY - y)
    } ?: return CoverType.NONE

    val result = calculateCover(
      defenderX = x,
      defenderY = y,
      attackerX = closestEnemy.gridX,
      attackerY = closestEnemy.gridY,
      tiles = tiles,
      envObjects = envObjects
    )

    return result.type
  }

  /**
   * Generates grid tile coordinates along the ray between (x0, y0) and (x1, y1)
   * using Bresenham's line algorithm.
   */
  fun getBresenhamLine(x0: Int, y0: Int, x1: Int, y1: Int): List<Pair<Int, Int>> {
    val line = mutableListOf<Pair<Int, Int>>()
    val dx = abs(x1 - x0)
    val dy = abs(y1 - y0)
    val sx = if (x0 < x1) 1 else -1
    val sy = if (y0 < y1) 1 else -1
    var err = dx - dy

    var curX = x0
    var curY = y0

    while (true) {
      line.add(Pair(curX, curY))
      if (curX == x1 && curY == y1) break
      val e2 = 2 * err
      if (e2 > -dy) {
        err -= dy
        curX += sx
      }
      if (e2 < dx) {
        err += dx
        curY += sy
      }
    }
    return line
  }
}
