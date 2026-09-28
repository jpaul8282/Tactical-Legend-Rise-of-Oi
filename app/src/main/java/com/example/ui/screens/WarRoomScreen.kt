package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.audio.SoundManager
import com.example.data.model.HeroClass
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCard
import com.example.ui.components.CyberOutlineButton
import com.example.ui.components.GameCodexDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@Composable
fun WarRoomScreen(
  viewModel: GameViewModel,
  onNavigateToCampaign: () -> Unit,
  onNavigateToSquad: () -> Unit,
  onNavigateToArmory: () -> Unit,
  onNavigateToArcade: () -> Unit,
  onNavigateToStore: () -> Unit,
  onNavigateToCinema: () -> Unit,
  onLaunchQuickBattle: (String) -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()
  val profile = uiState.playerProfile
  val deployedOps = uiState.operatives.filter { it.isDeployed }
  val squadPower = viewModel.getSquadCombatPower()
  var showCodex by remember { mutableStateOf(false) }

  Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .background(CyberBackground)
        .padding(horizontal = 14.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
    // 1. Hero Banner & Commander Telemetry
    item {
      CyberCard(
        borderColor = NeonCyan,
        backgroundColor = CyberSurface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(CutCornerShape(topStart = 6.dp, topEnd = 6.dp))
        ) {
          Image(
            painter = painterResource(id = R.drawable.bg_tactical_banner),
            contentDescription = "Tactical War Room",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  listOf(Color.Transparent, CyberSurface.copy(alpha = 0.95f))
                )
              )
          )
          // Commander Telemetry Overlay
          Column(
            modifier = Modifier
              .align(Alignment.BottomStart)
              .padding(12.dp)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = profile?.commanderName ?: "Commander Oi",
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
              )
              if (profile?.isVipPassActive == true) {
                CyberBadge(text = "VIP OVERLORD", color = CyberGold)
              } else {
                CyberBadge(text = "SECTOR COMMAND", color = NeonCyan)
              }
            }
            Text(
              text = "TACTICAL GRID DEFENSE PROTOCOL ACTIVE",
              style = MaterialTheme.typography.labelSmall,
              color = MatrixGreen,
              letterSpacing = 1.sp
            )
          }

          // Top-right quick Codex Access Button
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(8.dp)
          ) {
            CyberOutlineButton(
              text = "DOSSIER",
              onClick = {
                SoundManager.playButtonClick()
                showCodex = true
              },
              borderColor = CyberGold,
              textColor = CyberGold,
              icon = "📖",
              testTag = "banner_codex_button"
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Currency Counters
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(CyberSurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Credits
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "💎", fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
            Column {
              Text(text = "CYBER CREDITS", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Text(
                text = "${profile?.cyberCredits ?: 0}",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
          }

          // Tactical Data
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "💾", fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
            Column {
              Text(text = "TACTICAL DATA", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Text(
                text = "${profile?.tacticalData ?: 0}",
                style = MaterialTheme.typography.titleMedium,
                color = MatrixGreen,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
          }

          // Combat Power
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "⚡", fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
            Column {
              Text(text = "SQUAD PWR", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Text(
                text = "$squadPower",
                style = MaterialTheme.typography.titleMedium,
                color = CyberGold,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // 2. Story Dossier & Game Codex Card
    item {
      CyberCard(
        borderColor = CyberGold,
        glowColor = CyberGold.copy(alpha = 0.4f),
        backgroundColor = CyberSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(text = "📖", fontSize = 18.sp, modifier = Modifier.padding(end = 6.dp))
              Text(
                text = "GAME DOSSIER // CODEX",
                style = MaterialTheme.typography.titleMedium,
                color = CyberGold,
                fontWeight = FontWeight.Bold
              )
            }
            Text(
              text = "Access classified lore on the 'Oi' resistance, 10x10 tactical rules, operative profiles, and enemy threats.",
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 3.dp, end = 8.dp)
            )
          }

          CyberOutlineButton(
            text = "INTEL",
            onClick = {
              SoundManager.playButtonClick()
              showCodex = true
            },
            borderColor = CyberGold,
            textColor = CyberGold,
            icon = "⚡",
            testTag = "open_dossier_button"
          )
        }
      }
    }

    // 3. Primary Action: Deploy Tactical Combat
    item {
      CyberCard(
        borderColor = NeonCrimson,
        glowColor = NeonCrimson,
        backgroundColor = CyberSurfaceHigh.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(text = "⚔️", fontSize = 20.sp, modifier = Modifier.padding(end = 6.dp))
              Text(
                text = "CAMPAIGN WAR THEATRE",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCrimson,
                fontWeight = FontWeight.Bold
              )
            }
            Text(
              text = "Engage hostile corporate forces across 5 sectors in turn-based tactical grid combat.",
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
              modifier = Modifier.padding(top = 4.dp, end = 8.dp)
            )
          }

          CyberButton(
            text = "DEPLOY",
            onClick = onNavigateToCampaign,
            primaryColor = NeonCrimson,
            textColor = TextPrimary,
            testTag = "deploy_campaign_button"
          )
        }
      }
    }

    // 3. Active Squad Mini Overview
    item {
      CyberCard(borderColor = BorderGlow, modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "DEPLOYED 'OI' OPERATIVES (${deployedOps.size}/4)",
            style = MaterialTheme.typography.labelLarge,
            color = NeonCyan,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "MANAGE ROSTER >",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.clickable {
              SoundManager.playButtonClick()
              onNavigateToSquad()
            }
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          deployedOps.take(4).forEach { op ->
            val heroCls = HeroClass.fromString(op.heroClass)
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp))
                .background(CyberSurfaceVariant)
                .border(1.dp, heroCls.primaryColor.copy(alpha = 0.6f), CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp))
                .clickable {
                  SoundManager.playButtonClick()
                  onNavigateToSquad()
                }
                .padding(8.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val opPortrait = com.example.ui.components.UnitPortraits.getOperativePortraitByClassName(op.heroClass, op.name)
                Image(
                  painter = painterResource(id = opPortrait),
                  contentDescription = op.name,
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CutCornerShape(4.dp))
                    .border(0.5.dp, heroCls.primaryColor, CutCornerShape(4.dp)),
                  contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = op.name.split(" ").first(),
                  style = MaterialTheme.typography.labelSmall,
                  color = TextPrimary,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1
                )
                Text(
                  text = "LV.${op.level}",
                  style = MaterialTheme.typography.labelSmall,
                  color = heroCls.primaryColor,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }
    }

    // 4. Tactical Operations Hub (Grid 2x2)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "OPERATIONAL HUBS & FACILITIES",
          style = MaterialTheme.typography.labelLarge,
          color = TextSecondary,
          fontWeight = FontWeight.Bold
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Armory & Nanite Forge
          CyberCard(
            borderColor = BorderGlow,
            modifier = Modifier
              .weight(1f)
              .clickable {
                SoundManager.playButtonClick()
                onNavigateToArmory()
              }
          ) {
            Text(text = "🔬", fontSize = 24.sp)
            Text(
              text = "NANITE FORGE",
              style = MaterialTheme.typography.titleMedium,
              color = NeonCyan,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(top = 4.dp)
            )
            Text(
              text = "Synthesize legendary weapons and armor.",
              style = MaterialTheme.typography.bodyMedium,
              color = TextMuted,
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 2.dp)
            )
          }

          // Cyber Arcade
          CyberCard(
            borderColor = BorderGlow,
            modifier = Modifier
              .weight(1f)
              .clickable {
                SoundManager.playButtonClick()
                onNavigateToArcade()
              }
          ) {
            Text(text = "👾", fontSize = 24.sp)
            Text(
              text = "CYBER ARCADE",
              style = MaterialTheme.typography.titleMedium,
              color = MatrixGreen,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(top = 4.dp)
            )
            Text(
              text = "Hack rogue drones and earn high scores.",
              style = MaterialTheme.typography.bodyMedium,
              color = TextMuted,
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 2.dp)
            )
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Black Market
          CyberCard(
            borderColor = BorderGlow,
            modifier = Modifier
              .weight(1f)
              .clickable {
                SoundManager.playButtonClick()
                onNavigateToStore()
              }
          ) {
            Text(text = "🛒", fontSize = 24.sp)
            Text(
              text = "BLACK MARKET",
              style = MaterialTheme.typography.titleMedium,
              color = CyberGold,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(top = 4.dp)
            )
            Text(
              text = "Credit vaults and exclusive prototype gear.",
              style = MaterialTheme.typography.bodyMedium,
              color = TextMuted,
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 2.dp)
            )
          }

          // Holo Cinema
          CyberCard(
            borderColor = BorderGlow,
            modifier = Modifier
              .weight(1f)
              .clickable {
                SoundManager.playButtonClick()
                onNavigateToCinema()
              }
          ) {
            Text(text = "🎬", fontSize = 24.sp)
            Text(
              text = "HOLO CINEMA",
              style = MaterialTheme.typography.titleMedium,
              color = ElectricPurple,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(top = 4.dp)
            )
            Text(
              text = "Watch briefing feeds and earn credit rewards.",
              style = MaterialTheme.typography.bodyMedium,
              color = TextMuted,
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 2.dp)
            )
          }
        }
      }
    }

    // 5. Live Sector Intel Ticker
    item {
      CyberCard(
        borderColor = BorderGlow,
        backgroundColor = CyberSurfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(text = "📡", fontSize = 16.sp, modifier = Modifier.padding(end = 8.dp))
          Column {
            Text(
              text = "SUB-ETHER INTELLIGENCE FEED",
              style = MaterialTheme.typography.labelSmall,
              color = NeonCyan,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Arasaka security lockdown escalating in Sector 7. Commander Varrus deploying heavy sentinels.",
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
              fontSize = 12.sp
            )
          }
        }
      }
    }
    }

    // Classified Game Codex & Story Dossier Dialog
    if (showCodex) {
      GameCodexDialog(onDismiss = { showCodex = false })
    }
  }
}
