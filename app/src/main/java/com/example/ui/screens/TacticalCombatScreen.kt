package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.model.*
import com.example.ui.components.BattleGrid
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCard
import com.example.ui.components.StatProgressBar
import com.example.ui.components.TurnIndicatorHUD
import com.example.ui.theme.*
import com.example.ui.viewmodel.TacticalCombatViewModel

@Composable
fun TacticalCombatScreen(
  missionId: String,
  viewModel: TacticalCombatViewModel,
  onExitCombat: () -> Unit
) {
  val gridState by viewModel.gridState.collectAsState()

  LaunchedEffect(missionId) {
    viewModel.initMissionCombat(missionId)
  }

  val selectedUnit = gridState.units.find { it.id == gridState.selectedUnitId }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBackground)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
      // 1. Top Turn & Combat HUD Indicator
      TurnIndicatorHUD(
        currentTurn = gridState.currentTurn,
        isPlayerTurn = gridState.isPlayerTurn,
        playerUnitCount = gridState.units.count { it.faction == UnitFaction.PLAYER_OPERATIVE && it.isAlive },
        enemyUnitCount = gridState.units.count { it.faction == UnitFaction.ENEMY_HOSTILE && it.isAlive },
        missionTitle = gridState.mission?.title ?: "TACTICAL GRID ENGAGEMENT",
        phaseName = when (gridState.phase) {
          BattlePhase.ACTION_MOVE -> "MOVEMENT PHASE"
          BattlePhase.ACTION_ATTACK -> "TARGETING ENEMY"
          BattlePhase.ACTION_SPECIAL -> "SPECIAL ABILITY"
          else -> if (gridState.isPlayerTurn) "SQUAD COMMAND" else "ENEMY ACTION"
        }
      )

      Spacer(modifier = Modifier.height(6.dp))

      // 1.5 Squad Operative Quick-Selector Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        gridState.units.filter { it.faction == UnitFaction.PLAYER_OPERATIVE }.forEach { op ->
          val isSelected = op.id == gridState.selectedUnitId
          val isAlive = op.isAlive
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(4.dp))
              .background(if (isSelected) op.themeColor.copy(alpha = 0.25f) else CyberSurface)
              .border(
                if (isSelected) 1.5.dp else 0.5.dp,
                if (isSelected) op.themeColor else BorderGlow,
                RoundedCornerShape(4.dp)
              )
              .clickable(enabled = isAlive && gridState.isPlayerTurn) {
                viewModel.selectUnit(op.id)
              }
              .padding(horizontal = 4.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
              if (op.portraitResId != null && isAlive) {
                androidx.compose.foundation.Image(
                  painter = androidx.compose.ui.res.painterResource(id = op.portraitResId),
                  contentDescription = op.name,
                  modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .border(0.5.dp, op.themeColor, CircleShape),
                  contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
              } else {
                Text(text = if (isAlive) op.avatarIcon else "💀", fontSize = 12.sp)
              }
              Column {
                Text(
                  text = op.name.split(" ").firstOrNull() ?: op.name,
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isSelected) TextPrimary else TextSecondary,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 9.sp,
                  maxLines = 1
                )
                if (isAlive) {
                  Text(
                    text = "AP ${op.actionPoints} | MV ${op.moveRange}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (op.actionPoints > 0) NeonCyan else TextMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                  )
                } else {
                  Text(text = "KIA", style = MaterialTheme.typography.labelSmall, color = NeonCrimson, fontSize = 8.sp)
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 2. Interactive 10x10 Battle Grid
      BattleGrid(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1f),
        gridSize = 10,
        gridWidth = gridState.gridWidth,
        gridHeight = gridState.gridHeight,
        tiles = gridState.tiles,
        units = gridState.units,
        environmentObjects = gridState.environmentObjects,
        selectedUnitId = gridState.selectedUnitId,
        reachableTiles = gridState.reachableTiles,
        targetableTiles = gridState.targetableTiles,
        specialTargetTiles = gridState.specialTargetTiles,
        floatingDamages = gridState.floatingDamages,
        isPlayerTurn = gridState.isPlayerTurn,
        onTileClick = { x, y -> viewModel.onTileClicked(x, y) },
        onUnitClick = { unit ->
          if (unit.faction == UnitFaction.PLAYER_OPERATIVE) {
            viewModel.selectUnit(unit.id)
          }
        },
        onDismissDamage = { id -> viewModel.dismissFloatingDamage(id) }
      )

      Spacer(modifier = Modifier.height(8.dp))

      // 3. Selected Operative Control Panel
      if (selectedUnit != null && selectedUnit.faction == UnitFaction.PLAYER_OPERATIVE) {
        CyberCard(
          borderColor = selectedUnit.themeColor,
          backgroundColor = CyberSurface,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                if (selectedUnit.portraitResId != null) {
                  androidx.compose.foundation.Image(
                    painter = androidx.compose.ui.res.painterResource(id = selectedUnit.portraitResId),
                    contentDescription = selectedUnit.name,
                    modifier = Modifier
                      .size(28.dp)
                      .clip(CircleShape)
                      .border(1.dp, selectedUnit.themeColor, CircleShape),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                  )
                } else {
                  Text(text = selectedUnit.avatarIcon, fontSize = 18.sp)
                }
                Text(
                  text = selectedUnit.name,
                  style = MaterialTheme.typography.titleMedium,
                  color = TextPrimary,
                  fontWeight = FontWeight.Bold
                )
                CyberBadge(
                  text = selectedUnit.heroClass?.displayName ?: "OPERATIVE",
                  color = selectedUnit.themeColor
                )
              }

              Spacer(modifier = Modifier.height(4.dp))

              // HP & Shield Stats
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                StatProgressBar(
                  current = selectedUnit.currentHp,
                  max = selectedUnit.maxHp,
                  barColor = MatrixGreen,
                  label = "HP",
                  modifier = Modifier.weight(1f),
                  height = 6.dp
                )
                StatProgressBar(
                  current = selectedUnit.currentShield,
                  max = selectedUnit.maxShield,
                  barColor = NeonCyan,
                  label = "SHIELD",
                  modifier = Modifier.weight(1f),
                  height = 6.dp
                )
              }
            }

            // AP Counter
            Column(
              horizontalAlignment = Alignment.End,
              modifier = Modifier.padding(start = 8.dp)
            ) {
              Text(text = "ACTION POINTS", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (i in 1..selectedUnit.maxActionPoints) {
                  Box(
                    modifier = Modifier
                      .size(10.dp)
                      .clip(CircleShape)
                      .background(if (i <= selectedUnit.actionPoints) NeonCyan else CyberSurfaceHigh)
                      .border(1.dp, if (i <= selectedUnit.actionPoints) NeonCyan else BorderGlow, CircleShape)
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          // Tactical Action Buttons
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            CyberButton(
              text = "MOVE (1 AP)",
              onClick = { viewModel.prepareMove() },
              primaryColor = NeonCyan,
              textColor = CyberBackground,
              enabled = gridState.isPlayerTurn && selectedUnit.actionPoints > 0,
              modifier = Modifier.weight(1f),
              icon = "🏃"
            )

            CyberButton(
              text = "STRIKE (1 AP)",
              onClick = { viewModel.prepareAttack() },
              primaryColor = NeonCrimson,
              textColor = TextPrimary,
              enabled = gridState.isPlayerTurn && selectedUnit.actionPoints > 0,
              modifier = Modifier.weight(1f),
              icon = "🎯"
            )

            CyberButton(
              text = "SKILL",
              onClick = { viewModel.prepareSpecial() },
              primaryColor = ElectricPurple,
              textColor = TextPrimary,
              enabled = gridState.isPlayerTurn && selectedUnit.actionPoints > 0 && selectedUnit.specialCooldown == 0,
              modifier = Modifier.weight(1f),
              icon = "✨"
            )

            CyberButton(
              text = "END",
              onClick = { viewModel.endTurn() },
              primaryColor = CyberGold,
              textColor = CyberBackground,
              enabled = gridState.isPlayerTurn,
              modifier = Modifier.weight(0.9f),
              icon = "⏳"
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))

      // 4. Live Tactical Combat Log Stream
      CyberCard(
        borderColor = BorderGlow,
        backgroundColor = CyberSurfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
      ) {
        Text(
          text = "TACTICAL ENGAGEMENT LOGS",
          style = MaterialTheme.typography.labelSmall,
          color = TextSecondary,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(bottom = 4.dp)
        )
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          items(gridState.combatLogs) { log ->
            Text(
              text = "> ${log.message}",
              style = MaterialTheme.typography.bodyMedium,
              color = log.color,
              fontSize = 11.sp,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }
    }

    // 5. Victory / Defeat Overlay Modal
    if (gridState.isBattleOver) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.82f))
          .padding(20.dp),
        contentAlignment = Alignment.Center
      ) {
        CyberCard(
          borderColor = if (gridState.didPlayerWin) MatrixGreen else NeonCrimson,
          glowColor = if (gridState.didPlayerWin) MatrixGreen else NeonCrimson,
          backgroundColor = CyberSurface,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
          ) {
            Text(
              text = if (gridState.didPlayerWin) "MISSION ACCOMPLISHED" else "TACTICAL DEFEAT",
              style = MaterialTheme.typography.displayMedium,
              color = if (gridState.didPlayerWin) MatrixGreen else NeonCrimson,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )

            Text(
              text = if (gridState.didPlayerWin) "Sector neutralized and secured by the Oi squad." else "All operative signals offline. Tactical retreat advised.",
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            if (gridState.didPlayerWin) {
              // Star Rating
              val stars = gridState.rewardsEarned["Stars"] ?: 3
              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 8.dp)
              ) {
                for (i in 1..3) {
                  Text(
                    text = if (i <= stars) "⭐" else "☆",
                    fontSize = 28.sp,
                    color = CyberGold
                  )
                }
              }

              // Loot Breakdown Card
              CyberCard(
                borderColor = BorderGlow,
                backgroundColor = CyberSurfaceVariant,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 8.dp)
              ) {
                Text(
                  text = "SECTOR REWARDS CLAIMED",
                  style = MaterialTheme.typography.labelSmall,
                  color = MatrixGreen,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceAround
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "+${gridState.rewardsEarned["Credits"] ?: 300}", style = MaterialTheme.typography.titleMedium, color = NeonCyan, fontWeight = FontWeight.Bold)
                    Text(text = "Credits", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                  }
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "+${gridState.rewardsEarned["Tactical Data"] ?: 150}", style = MaterialTheme.typography.titleMedium, color = MatrixGreen, fontWeight = FontWeight.Bold)
                    Text(text = "Data", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                  }
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "+${gridState.rewardsEarned["XP"] ?: 100}", style = MaterialTheme.typography.titleMedium, color = CyberGold, fontWeight = FontWeight.Bold)
                    Text(text = "Squad XP", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            CyberButton(
              text = if (gridState.didPlayerWin) "CLAIM & RETURN TO WAR ROOM" else "RETURN TO WAR ROOM",
              onClick = onExitCombat,
              primaryColor = if (gridState.didPlayerWin) MatrixGreen else NeonCrimson,
              textColor = CyberBackground,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    }
  }
}
