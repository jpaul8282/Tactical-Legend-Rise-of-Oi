package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.SoundManager
import com.example.data.db.MissionEntity
import com.example.data.model.MissionDifficulty
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@Composable
fun CampaignMapScreen(
  viewModel: GameViewModel,
  onLaunchMission: (String) -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()
  val squadPower = viewModel.getSquadCombatPower()
  var briefingMission by remember { mutableStateOf<MissionEntity?>(null) }
  var showCodex by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBackground)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
      // Header
      item {
        CyberCard(borderColor = NeonCrimson, modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "CAMPAIGN WAR SECTORS",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCrimson,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Reclaim cyber city sectors from rogue syndicates.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 12.sp
              )
            }

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              CyberOutlineButton(
                text = "CODEX",
                icon = "📖",
                onClick = {
                  SoundManager.playButtonClick()
                  showCodex = true
                },
                borderColor = CyberGold,
                textColor = CyberGold,
                testTag = "campaign_codex_button"
              )
              CyberBadge(
                text = "PWR $squadPower",
                color = CyberGold,
                backgroundColor = CyberGold.copy(alpha = 0.15f)
              )
            }
          }
        }
      }

      // Missions
      items(uiState.missions) { mission ->
        val diffEnum = MissionDifficulty.fromString(mission.difficulty)
        val isLocked = !mission.isUnlocked

        CyberCard(
          borderColor = if (isLocked) BorderGlow else if (mission.isBossEncounter) NeonCrimson else NeonCyan,
          glowColor = if (mission.isBossEncounter && !isLocked) NeonCrimson.copy(alpha = 0.35f) else null,
          backgroundColor = if (isLocked) CyberSurface.copy(alpha = 0.5f) else CyberSurface,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            // Title & Stars
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                Text(
                  text = if (mission.isBossEncounter) "👹" else "📍",
                  fontSize = 18.sp
                )
                Text(
                  text = mission.title,
                  style = MaterialTheme.typography.titleMedium,
                  color = if (isLocked) TextMuted else TextPrimary,
                  fontWeight = FontWeight.Bold
                )
              }

              if (!isLocked) {
                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                  for (i in 1..3) {
                    Text(
                      text = if (i <= mission.starsEarned) "⭐" else "☆",
                      color = CyberGold,
                      fontSize = 14.sp
                    )
                  }
                }
              } else {
                CyberBadge(text = "LOCKED", color = TextMuted)
              }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle
            Text(
              text = "CH.${mission.chapter} // SEC.${mission.missionNumber} — ${mission.location.uppercase()}",
              style = MaterialTheme.typography.labelSmall,
              color = if (isLocked) TextMuted else NeonCyan,
              fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Intel Debrief & Threat Intel
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              val threatPortrait = if (mission.isBossEncounter) {
                UnitPortraits.APEX_TITAN_BOSS
              } else if (mission.difficulty.equals("HARD", true) || mission.difficulty.equals("NIGHTMARE", true)) {
                UnitPortraits.CYBORG_ENFORCER
              } else {
                UnitPortraits.DRONE_STRIKER
              }

              androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(id = threatPortrait),
                contentDescription = "Threat Intel",
                modifier = Modifier
                  .size(38.dp)
                  .clip(CutCornerShape(4.dp))
                  .border(
                    width = 1.dp,
                    color = if (mission.isBossEncounter) NeonCrimson else BorderGlow,
                    shape = CutCornerShape(4.dp)
                  ),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
              )

              Text(
                text = mission.intel,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isLocked) TextMuted else TextSecondary,
                fontSize = 11.5.sp,
                modifier = Modifier.weight(1f)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Rewards & Launch Strike Row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              // Badges
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CyberBadge(text = diffEnum.label, color = diffEnum.color)
                CyberBadge(
                  text = "REC PWR ${mission.recommendedPower}",
                  color = if (squadPower >= mission.recommendedPower) MatrixGreen else CyberAmber
                )
              }

              if (!isLocked) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  CyberOutlineButton(
                    text = "BRIEFING",
                    onClick = {
                      SoundManager.playButtonClick()
                      briefingMission = mission
                    },
                    borderColor = NeonCyan,
                    textColor = NeonCyan,
                    icon = "📹"
                  )

                  CyberButton(
                    text = "STRIKE",
                    onClick = {
                      SoundManager.playButtonClick()
                      onLaunchMission(mission.id)
                    },
                    primaryColor = if (mission.isBossEncounter) NeonCrimson else NeonCyan,
                    textColor = if (mission.isBossEncounter) TextPrimary else CyberBackground,
                    icon = "⚔️"
                  )
                }
              }
            }
          }
        }
      }
    }

    // Modal Video Briefing Dialog using VideoPlayerLayout
    briefingMission?.let { mission ->
      val briefingVideo = remember(mission.id) {
        VideoItem(
          id = "briefing_${mission.id}",
          title = "Operation Briefing: ${mission.title}",
          durationSeconds = 45,
          category = "SECTOR RECON",
          classification = if (mission.isBossEncounter) "TOP SECRET // BOSS TARGET" else "CONFIDENTIAL",
          intelSummary = mission.intel,
          chapters = listOf(
            VideoChapter("Perimeter Infiltration", 0, "Initial grid scan"),
            VideoChapter("Hostile Formations", 15, "Enemy positions identified"),
            VideoChapter("Tactical Objectives", 30, "Primary core sabotage"),
            VideoChapter("Extraction Coordinates", 40, "Evac protocol")
          ),
          subtitles = listOf(
            VideoSubtitle(0, 12, "COMMANDER OI", "Tactical grid scan online for ${mission.location}. Note all enemy patrol sectors."),
            VideoSubtitle(13, 26, "AI AVALON", "Hostiles detected with high armor rating. Deploy EMP or resonance blades."),
            VideoSubtitle(27, 45, "COMMANDER OI", "Command directive confirmed. Launch strike when ready, operatives.")
          )
        )
      }

      Dialog(
        onDismissRequest = { briefingMission = null },
        properties = DialogProperties(usePlatformDefaultWidth = false)
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f))
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          CyberCard(
            borderColor = NeonCyan,
            glowColor = NeonCyan.copy(alpha = 0.4f),
            backgroundColor = CyberSurface,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "TACTICAL VIDEO BRIEFING",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
              )
              CyberBadge(text = mission.difficulty, color = CyberGold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Embedded Video Player Layout
            VideoPlayerLayout(
              video = briefingVideo,
              modifier = Modifier.fillMaxWidth(),
              onBack = { briefingMission = null }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              CyberOutlineButton(
                text = "CLOSE FEED",
                onClick = { briefingMission = null },
                borderColor = BorderGlow,
                textColor = TextSecondary,
                modifier = Modifier.weight(1f)
              )
              CyberButton(
                text = "PROCEED TO COMBAT",
                onClick = {
                  val missionId = mission.id
                  briefingMission = null
                  onLaunchMission(missionId)
                },
                primaryColor = NeonCrimson,
                textColor = TextPrimary,
                icon = "⚔️",
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }
    }

    if (showCodex) {
      GameCodexDialog(onDismiss = { showCodex = false })
    }
  }
}
