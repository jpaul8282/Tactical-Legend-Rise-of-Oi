package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*

/**
 * A composable that renders a 10x10 tactical map using a grid layout,
 * where each tile can hold a unit or environment object.
 *
 * @param gridSize The dimension of the tactical grid (defaults to 10 for a 10x10 map).
 * @param gridWidth Custom horizontal tile count (defaults to [gridSize]).
 * @param gridHeight Custom vertical tile count (defaults to [gridSize]).
 * @param tiles Map of coordinates (x, y) to their base terrain [TileType].
 * @param units List of combat units currently deployed on the map.
 * @param environmentObjects List of environment props/objects (cover, wells, hazards, terminals, pillars).
 * @param selectedUnitId ID of the currently selected unit, if any.
 * @param selectedTile Coordinates (x, y) of the currently selected tile, if any.
 * @param reachableTiles Set of tiles within movement range.
 * @param targetableTiles Set of tiles within attack/target range.
 * @param specialTargetTiles Set of tiles within skill/special ability range.
 * @param floatingDamages Real-time floating damage / heal / status numbers.
 * @param isPlayerTurn Whether it is currently the squad's turn.
 * @param showCoordinates Whether to display tactical axis indicators (01..10, A..J).
 * @param tileSize Fixed or adaptive tile dimension.
 * @param onTileClick Callback invoked when a tile is clicked with (x, y).
 * @param onUnitClick Callback invoked when a unit is clicked.
 * @param onEnvironmentObjectClick Callback invoked when an environment object is clicked.
 */
@Composable
fun BattleGrid(
  modifier: Modifier = Modifier,
  gridSize: Int = 10,
  gridWidth: Int = gridSize,
  gridHeight: Int = gridSize,
  tiles: Map<Pair<Int, Int>, TileType> = emptyMap(),
  units: List<CombatUnit> = emptyList(),
  environmentObjects: List<EnvironmentObject> = emptyList(),
  selectedUnitId: String? = null,
  selectedTile: Pair<Int, Int>? = null,
  reachableTiles: Set<Pair<Int, Int>> = emptySet(),
  targetableTiles: Set<Pair<Int, Int>> = emptySet(),
  specialTargetTiles: Set<Pair<Int, Int>> = emptySet(),
  floatingDamages: List<FloatingDamage> = emptyList(),
  isPlayerTurn: Boolean = true,
  showCoordinates: Boolean = true,
  tileSize: Dp? = null,
  onTileClick: ((Int, Int) -> Unit)? = null,
  onUnitClick: ((CombatUnit) -> Unit)? = null,
  onEnvironmentObjectClick: ((EnvironmentObject) -> Unit)? = null,
  onDismissDamage: ((String) -> Unit)? = null
) {
  val pulseTransition = rememberInfiniteTransition(label = "grid_pulse")
  val pulseAlpha by pulseTransition.animateFloat(
    initialValue = 0.25f,
    targetValue = 0.65f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_alpha"
  )

  val colLabels = remember(gridWidth) {
    (0 until gridWidth).map { (it + 1).toString().padStart(2, '0') }
  }
  val rowLabels = remember(gridHeight) {
    (0 until gridHeight).map { ('A'.code + it).toChar().toString() }
  }

  // Fast lookups
  val unitMap = remember(units) {
    units.filter { it.isAlive }.associateBy { Pair(it.gridX, it.gridY) }
  }
  val envMap = remember(environmentObjects) {
    environmentObjects.associateBy { Pair(it.gridX, it.gridY) }
  }

  val activeBorderColor = if (isPlayerTurn) NeonCyanDim else NeonCrimsonDim

  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(8.dp))
      .background(CyberSurface)
      .border(1.dp, activeBorderColor, RoundedCornerShape(8.dp))
      .padding(6.dp)
      .testTag("battle_grid_10x10_container")
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Top Column Coordinates (01 .. 10)
      if (showCoordinates) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, bottom = 3.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          for (x in 0 until gridWidth) {
            Box(
              modifier = Modifier.weight(1f),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = colLabels[x],
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      // Grid Rows (A .. J)
      for (y in 0 until gridHeight) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .then(
              if (tileSize != null) Modifier.height(tileSize)
              else Modifier.weight(1f, fill = false)
            ),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Left Row Coordinate (A, B, C...)
          if (showCoordinates) {
            Box(
              modifier = Modifier
                .width(18.dp)
                .fillMaxHeight(),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = rowLabels.getOrElse(y) { "$y" },
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 8.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
          }

          // Row Tiles
          for (x in 0 until gridWidth) {
            val coord = Pair(x, y)
            val baseTile = tiles[coord] ?: TileType.NORMAL
            val unit = unitMap[coord]
            val envObject = envMap[coord]

            val isSelectedTile = selectedTile == coord
            val isSelectedUnit = unit != null && unit.id == selectedUnitId
            val isReachable = reachableTiles.contains(coord)
            val isTargetable = targetableTiles.contains(coord)
            val isSpecialTarget = specialTargetTiles.contains(coord)

            BattleGridTile(
              modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
                .padding(1.dp),
              gridX = x,
              gridY = y,
              tileType = baseTile,
              unit = unit,
              environmentObject = envObject,
              isSelected = isSelectedTile || isSelectedUnit,
              isReachable = isReachable,
              isTargetable = isTargetable,
              isSpecialTarget = isSpecialTarget,
              pulseAlpha = pulseAlpha,
              onClick = {
                onTileClick?.invoke(x, y)
                if (unit != null) onUnitClick?.invoke(unit)
                if (envObject != null) onEnvironmentObjectClick?.invoke(envObject)
              }
            )
          }
        }
      }
    }

    // Overlay Floating Damage & Dynamic Particle Effects
    AnimatedDamageOverlay(
      floatingDamages = floatingDamages,
      gridWidth = gridWidth,
      gridHeight = gridHeight,
      showCoordinates = showCoordinates,
      onDismissDamage = onDismissDamage
    )
  }
}

/**
 * Individual Tactical Tile within the BattleGrid.
 * Renders base terrain styling, environmental objects, tactical units, and targeting overlays.
 */
@Composable
fun BattleGridTile(
  modifier: Modifier = Modifier,
  gridX: Int,
  gridY: Int,
  tileType: TileType,
  unit: CombatUnit?,
  environmentObject: EnvironmentObject?,
  isSelected: Boolean,
  isReachable: Boolean,
  isTargetable: Boolean,
  isSpecialTarget: Boolean,
  pulseAlpha: Float,
  onClick: () -> Unit
) {
  // Tile background color calculation
  val backgroundColor = when {
    isTargetable -> NeonCrimson.copy(alpha = 0.35f * (pulseAlpha + 0.5f))
    isSpecialTarget -> ElectricPurple.copy(alpha = 0.35f * (pulseAlpha + 0.5f))
    isReachable -> NeonCyan.copy(alpha = 0.28f * (pulseAlpha + 0.5f))
    isSelected -> CyberGold.copy(alpha = 0.25f)
    environmentObject != null -> when (environmentObject.type) {
      EnvironmentObjectType.ENERGY_BARRICADE -> Color(0xFF162D4A)
      EnvironmentObjectType.NANITE_WELL -> MatrixGreen.copy(alpha = 0.18f)
      EnvironmentObjectType.EMP_HAZARD -> NeonCrimson.copy(alpha = 0.20f)
      EnvironmentObjectType.HACK_TERMINAL -> NeonCyan.copy(alpha = 0.20f)
      EnvironmentObjectType.SUPPLY_CRATE -> CyberGold.copy(alpha = 0.18f)
      EnvironmentObjectType.DEFENSE_TURRET -> ElectricPurple.copy(alpha = 0.20f)
      EnvironmentObjectType.PLASMA_VENT -> NeonCrimson.copy(alpha = 0.25f)
      EnvironmentObjectType.REINFORCED_BUNKER -> Color(0xFF1C2533)
      EnvironmentObjectType.REPAIR_STATION -> MatrixGreen.copy(alpha = 0.22f)
      EnvironmentObjectType.STRUCTURAL_PILLAR -> Color(0xFF141924)
    }
    tileType == TileType.COVER -> Color(0xFF162D4A)
    tileType == TileType.NANITE_WELL -> MatrixGreen.copy(alpha = 0.15f)
    tileType == TileType.EMP_HAZARD -> NeonCrimson.copy(alpha = 0.15f)
    tileType == TileType.BLOCKED -> Color(0xFF141924)
    else -> CyberSurfaceVariant
  }

  // Tile border color & thickness
  val (borderColor, borderWidth) = when {
    isSelected -> CyberGold to 2.dp
    isTargetable -> NeonCrimson to 1.5.dp
    isSpecialTarget -> ElectricPurple to 1.5.dp
    isReachable -> NeonCyan to 1.5.dp
    environmentObject != null -> environmentObject.themeColor.copy(alpha = 0.8f) to 1.dp
    tileType == TileType.COVER -> ElectricBlue.copy(alpha = 0.7f) to 1.dp
    tileType == TileType.NANITE_WELL -> MatrixGreen.copy(alpha = 0.7f) to 1.dp
    tileType == TileType.EMP_HAZARD -> NeonCrimson.copy(alpha = 0.7f) to 1.dp
    tileType == TileType.BLOCKED -> BorderGlow.copy(alpha = 0.5f) to 0.8.dp
    else -> BorderGlow.copy(alpha = 0.4f) to 0.5.dp
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(3.dp))
      .background(backgroundColor)
      .border(borderWidth, borderColor, RoundedCornerShape(3.dp))
      .clickable(onClick = onClick)
      .testTag("battle_grid_tile_${gridX}_${gridY}"),
    contentAlignment = Alignment.Center
  ) {
    // 1. Environmental Object or Base Terrain Prop
    if (environmentObject != null) {
      EnvironmentObjectView(
        obj = environmentObject,
        hasUnitOnTop = unit != null
      )
    } else if (unit == null) {
      // Default tile type watermark icon
      when (tileType) {
        TileType.COVER -> Text(text = "🛡️", fontSize = 10.sp, modifier = Modifier.alpha(0.6f))
        TileType.NANITE_WELL -> Text(text = "🔮", fontSize = 10.sp, modifier = Modifier.alpha(0.6f))
        TileType.EMP_HAZARD -> Text(text = "⚡", fontSize = 10.sp, modifier = Modifier.alpha(0.6f))
        TileType.BLOCKED -> Text(text = "🏛️", fontSize = 10.sp, modifier = Modifier.alpha(0.4f))
        TileType.NORMAL -> {
          // Subtle grid dot
          Box(
            modifier = Modifier
              .size(2.dp)
              .clip(CircleShape)
              .background(BorderGlow.copy(alpha = 0.3f))
          )
        }
      }
    }

    // 2. Unit Token on Tile
    if (unit != null) {
      CombatUnitToken(
        unit = unit,
        isSelected = isSelected
      )
    }

    // 3. Tactical Reticle / Movement Waypoint Overlay
    if (isTargetable && unit != null) {
      // Crosshair corner brackets
      Box(
        modifier = Modifier
          .fillMaxSize()
          .border(1.dp, NeonCrimson.copy(alpha = 0.8f), CutCornerShape(2.dp))
      )
    } else if (isReachable && unit == null && environmentObject?.type != EnvironmentObjectType.STRUCTURAL_PILLAR) {
      // Animated Pulsing Movement Waypoint Marker
      Box(
        modifier = Modifier
          .size(10.dp)
          .clip(CircleShape)
          .background(NeonCyan.copy(alpha = 0.15f * pulseAlpha + 0.10f))
          .border(1.dp, NeonCyan.copy(alpha = pulseAlpha + 0.35f), CircleShape),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(4.dp)
            .clip(CircleShape)
            .background(NeonCyan)
        )
      }
    }
  }
}

/**
 * Renders an environmental object with cyber accents, icon, and optional health/status indicators.
 */
@Composable
fun EnvironmentObjectView(
  obj: EnvironmentObject,
  hasUnitOnTop: Boolean
) {
  val iconScale = if (hasUnitOnTop) 0.65f else 1f
  val iconAlpha = if (hasUnitOnTop) 0.45f else 0.9f

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier
      .fillMaxSize()
      .padding(1.dp)
  ) {
    Text(
      text = obj.icon,
      fontSize = if (hasUnitOnTop) 8.sp else 12.sp,
      modifier = Modifier
        .scale(iconScale)
        .alpha(iconAlpha)
    )

    // Mini Durability / Status bar for destructible objects
    if (!hasUnitOnTop && obj.isDestructible && obj.hp != null && obj.maxHp != null) {
      val ratio = (obj.hp.toFloat() / obj.maxHp).coerceIn(0f, 1f)
      Box(
        modifier = Modifier
          .width(18.dp)
          .height(2.dp)
          .clip(RoundedCornerShape(1.dp))
          .background(Color(0xFF1E2838))
      ) {
        Box(
          modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(ratio)
            .background(obj.themeColor)
        )
      }
    }
  }
}

/**
 * Renders a combat unit token on a grid tile, including health bar, shield bar,
 * avatar icon, action points, and status condition badges.
 */
@Composable
fun CombatUnitToken(
  unit: CombatUnit,
  isSelected: Boolean
) {
  val isPlayer = unit.faction == UnitFaction.PLAYER_OPERATIVE
  val hpRatio = (unit.currentHp.toFloat() / unit.maxHp).coerceIn(0f, 1f)
  val shieldRatio = if (unit.maxShield > 0) {
    (unit.currentShield.toFloat() / unit.maxShield).coerceIn(0f, 1f)
  } else 0f

  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier
      .fillMaxSize()
      .padding(0.5.dp)
  ) {
    // Mini HP & Shield Bars
    Row(
      modifier = Modifier
        .fillMaxWidth(0.92f)
        .padding(bottom = 1.dp),
      horizontalArrangement = Arrangement.spacedBy(1.dp)
    ) {
      // HP Bar
      Box(
        modifier = Modifier
          .weight(1f)
          .height(2.5.dp)
          .clip(RoundedCornerShape(1.dp))
          .background(Color(0xFF1E2533))
      ) {
        Box(
          modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(hpRatio)
            .background(if (isPlayer) MatrixGreen else NeonCrimson)
        )
      }

      // Shield Bar (if unit has shields)
      if (unit.maxShield > 0) {
        Box(
          modifier = Modifier
            .weight(0.6f)
            .height(2.5.dp)
            .clip(RoundedCornerShape(1.dp))
            .background(Color(0xFF16253A))
        ) {
          Box(
            modifier = Modifier
              .fillMaxHeight()
              .fillMaxWidth(shieldRatio)
              .background(NeonCyan)
          )
        }
      }
    }

    // Avatar Token Circle
    Box(
      modifier = Modifier
        .size(if (unit.isBoss) 23.dp else 20.dp)
        .clip(CircleShape)
        .background(
          when {
            isSelected -> CyberGold.copy(alpha = 0.35f)
            isPlayer -> CyberSurfaceHigh
            unit.isBoss -> NeonCrimson.copy(alpha = 0.35f)
            else -> NeonCrimson.copy(alpha = 0.2f)
          }
        )
        .border(
          width = if (isSelected) 1.5.dp else 1.dp,
          color = when {
            isSelected -> CyberGold
            isPlayer -> unit.themeColor
            unit.isBoss -> CyberGold
            else -> NeonCrimson
          },
          shape = CircleShape
        ),
      contentAlignment = Alignment.Center
    ) {
      if (unit.portraitResId != null) {
        androidx.compose.foundation.Image(
          painter = androidx.compose.ui.res.painterResource(id = unit.portraitResId),
          contentDescription = unit.name,
          modifier = Modifier
            .fillMaxSize()
            .clip(CircleShape),
          contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
      } else {
        Text(
          text = if (unit.isBoss) "👑" else unit.avatarIcon,
          fontSize = if (unit.isBoss) 11.sp else 10.sp
        )
      }
    }

    // Action Points / Status Indicator
    if (isPlayer && unit.actionPoints > 0) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(1.dp),
        modifier = Modifier.padding(top = 0.5.dp)
      ) {
        for (i in 1..unit.actionPoints) {
          Box(
            modifier = Modifier
              .size(2.5.dp)
              .clip(CircleShape)
              .background(NeonCyan)
          )
        }
      }
    } else if (unit.isStunned) {
      Text(
        text = "⚡",
        fontSize = 7.sp,
        modifier = Modifier.alpha(0.9f)
      )
    }
  }
}

/**
 * Animated overlay for displaying dynamic particles and floating combat text animations
 * above specific tile coordinates on the 10x10 map.
 */
@Composable
fun FloatingDamageGridOverlay(
  floatingDamages: List<FloatingDamage>,
  gridWidth: Int,
  gridHeight: Int,
  showCoordinates: Boolean,
  onDismissDamage: ((String) -> Unit)? = null
) {
  AnimatedDamageOverlay(
    floatingDamages = floatingDamages,
    gridWidth = gridWidth,
    gridHeight = gridHeight,
    showCoordinates = showCoordinates,
    onDismissDamage = onDismissDamage
  )
}
