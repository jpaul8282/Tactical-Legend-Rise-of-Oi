package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@Composable
fun HoloCinemaScreen(
  viewModel: GameViewModel,
  onNavigateBack: () -> Unit = {}
) {
  BackHandler { onNavigateBack() }

  val briefings = remember {
    listOf(
      VideoItem(
        id = "vid_01",
        title = "Rise of Oi: Sub-Ether Tactical Briefing",
        durationSeconds = 48,
        category = "RECONNAISSANCE",
        classification = "TOP SECRET",
        intelSummary = "Orbital satellite telemetry tracking Arasaka patrol paths and drone swarm security along the Sector 7 corridor.",
        resolution = "1080P // 60FPS",
        chapters = listOf(
          VideoChapter("Orbit Drop & Insertion", 0, "Initial infiltration point"),
          VideoChapter("Drone Route Detection", 15, "Perimeter security sweeps"),
          VideoChapter("Security Matrix Overload", 30, "Sub-ether frequency jamming"),
          VideoChapter("Extraction Beacon Primed", 42, "Evac transport rendezvous")
        ),
        subtitles = listOf(
          VideoSubtitle(0, 10, "COMMANDER OI", "All operatives, we have orbital satellite feed established over Sector 7."),
          VideoSubtitle(11, 22, "AI AVALON", "Scanning enemy grid... Arasaka heavy surveillance drones detected on perimeter."),
          VideoSubtitle(23, 35, "VALKYRIE 01", "Jamming their sub-ether frequencies now. Strike window is opening."),
          VideoSubtitle(36, 48, "COMMANDER OI", "Execute tactical protocol. Secure the nanite core and move to extraction.")
        )
      ),
      VideoItem(
        id = "vid_02",
        title = "Nanite Core Weaponization Protocols",
        durationSeconds = 60,
        category = "WEAPON MATRIX",
        classification = "CONFIDENTIAL",
        intelSummary = "Field guide detailing kinetic nano-weaves and thermal edge plasma calibration for legendary squad equipment.",
        resolution = "4K HOLO // 60FPS",
        chapters = listOf(
          VideoChapter("Nanite Infusion Phase", 0, "Core material synthesis"),
          VideoChapter("Resonance Blade Sharpening", 20, "Thermal edge calibration"),
          VideoChapter("Kinetic Armor Calibration", 40, "Plasma bolt deflection"),
          VideoChapter("Overclock Verification", 55, "Final weapon testing")
        ),
        subtitles = listOf(
          VideoSubtitle(0, 15, "CHIEF FORGE TECH", "Commencing nanite injection into standard cyber katana edge."),
          VideoSubtitle(16, 32, "AI AVALON", "Thermal resonance peaking at 4,200 Kelvin. Armor piercing increased 300%."),
          VideoSubtitle(33, 48, "CHIEF FORGE TECH", "Kinetic dispersion weave active. Deflects incoming plasma bolts."),
          VideoSubtitle(49, 60, "COMMANDER OI", "Authorize forge blueprint distribution to all active squad barracks.")
        )
      ),
      VideoItem(
        id = "vid_03",
        title = "Sovereign AI Overlord Sub-Core Analysis",
        durationSeconds = 75,
        category = "THREAT DOSSIER",
        classification = "RESTRICTED",
        intelSummary = "Deep scan vulnerabilities and harmonic frequency analysis of the autonomous Nexus AI defensive shielding.",
        resolution = "1080P // 60FPS",
        chapters = listOf(
          VideoChapter("Nexus Deep Scan", 0, "Mainframe telemetry"),
          VideoChapter("Shield Harmonics", 25, "4.2 second oscillation gap"),
          VideoChapter("Cyber Warhead Lock", 50, "Synchronized EMP targeting"),
          VideoChapter("Core Breach Simulated", 68, "Defensive collapse")
        ),
        subtitles = listOf(
          VideoSubtitle(0, 18, "AI AVALON", "Decrypting mainframe architecture of the rogue Nexus AI core."),
          VideoSubtitle(19, 38, "OPERATIVE ZERO", "Shielding is multi-layered, but oscillates every 4.2 seconds."),
          VideoSubtitle(39, 56, "COMMANDER OI", "Time our EMP burst with their harmonic cycle to breach defenses."),
          VideoSubtitle(57, 75, "AI AVALON", "Target locked. Lethal vulnerability window confirmed at Sector 5.")
        )
      ),
      VideoItem(
        id = "vid_04",
        title = "Sector 9 Neon Underworld Ambush Telemetry",
        durationSeconds = 55,
        category = "COMBAT LOG",
        classification = "CLASSIFIED",
        intelSummary = "Tactical playback of squad counter-offensive during an ambush by rogue syndicate operatives in the underground depot.",
        resolution = "1080P // 60FPS",
        chapters = listOf(
          VideoChapter("Thermal Radar Contact", 0, "Underground freight movement"),
          VideoChapter("Syndicate Ambush", 18, "Hostile crossfire engaged"),
          VideoChapter("EMP Counter-Attack", 36, "Grid disruption pulse"),
          VideoChapter("Sector Secured", 50, "All bogeys neutralized")
        ),
        subtitles = listOf(
          VideoSubtitle(0, 12, "RECON DRONE", "Multiple hostiles emerging from subterranean freight tunnels."),
          VideoSubtitle(13, 28, "VALKYRIE 01", "Hostiles have heavy shields! Requesting immediate air support."),
          VideoSubtitle(29, 44, "COMMANDER OI", "Deploying tactical grid barricades. Focus fire on the drone leader."),
          VideoSubtitle(45, 55, "AI AVALON", "All hostile signatures neutralized. Sector 9 secured.")
        )
      )
    )
  }

  var selectedVideo by remember { mutableStateOf(briefings.first()) }
  var hasClaimedReward by remember { mutableStateOf(false) }
  var selectedCategoryFilter by remember { mutableStateOf("ALL") }

  val categories = remember {
    listOf("ALL", "RECONNAISSANCE", "WEAPON MATRIX", "THREAT DOSSIER", "COMBAT LOG")
  }

  val filteredBriefings = remember(selectedCategoryFilter) {
    if (selectedCategoryFilter == "ALL") briefings
    else briefings.filter { it.category == selectedCategoryFilter }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBackground)
      .testTag("holo_cinema_screen")
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 14.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      contentPadding = PaddingValues(top = 10.dp, bottom = 95.dp)
    ) {
      // Screen Header with Back Navigation
      item {
        CyberCard(borderColor = ElectricPurple, modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              IconButton(
                onClick = {
                  SoundManager.playButtonClick()
                  onNavigateBack()
                },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Back to War Room",
                  tint = ElectricPurple
                )
              }

              Column {
                Text(
                  text = "HOLO CINEMA INTEL FEEDS",
                  style = MaterialTheme.typography.titleMedium,
                  color = ElectricPurple,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "Tactical video player & classified intelligence telemetry.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = TextSecondary,
                  fontSize = 11.sp
                )
              }
            }

            CyberBadge(text = "LIVE SATELLITE", color = MatrixGreen)
          }
        }
      }

      // PRIMARY COMPONENT: VIDEO PLAYER LAYOUT
      item {
        CyberCard(
          borderColor = NeonCyan,
          glowColor = NeonCyan.copy(alpha = 0.3f),
          backgroundColor = Color(0xFF060910),
          modifier = Modifier.fillMaxWidth()
        ) {
          // Dedicated Video Player Layout
          VideoPlayerLayout(
            video = selectedVideo,
            modifier = Modifier.fillMaxWidth(),
            onBack = onNavigateBack,
            onVideoCompleted = {
              // Video completed callback
            }
          )

          Spacer(modifier = Modifier.height(10.dp))

          // Video Meta & Classification Bar
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = selectedVideo.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = selectedVideo.intelSummary,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Key Intel Chapters
          if (selectedVideo.chapters.isNotEmpty()) {
            Text(
              text = "INTEL KEYFRAME CHAPTERS",
              style = MaterialTheme.typography.labelSmall,
              color = CyberGold,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(6.dp))

            LazyRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              items(selectedVideo.chapters) { chapter ->
                Box(
                  modifier = Modifier
                    .clip(CutCornerShape(4.dp))
                    .background(CyberSurfaceVariant)
                    .border(1.dp, BorderGlow, CutCornerShape(4.dp))
                    .clickable {
                      SoundManager.playButtonClick()
                    }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Text(
                      text = formatDuration(chapter.timestampSeconds),
                      style = MaterialTheme.typography.labelSmall,
                      color = NeonCyan,
                      fontFamily = FontFamily.Monospace,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = chapter.title,
                      style = MaterialTheme.typography.labelSmall,
                      color = TextPrimary,
                      fontSize = 11.sp
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Claim Intel Reward Action
          CyberButton(
            text = if (hasClaimedReward) "INTEL REWARD CLAIMED ✓" else "WATCH & CLAIM +50 CYBER CREDITS",
            onClick = {
              if (!hasClaimedReward) {
                SoundManager.playLevelUp()
                viewModel.claimCinemaReward()
                hasClaimedReward = true
              }
            },
            primaryColor = if (hasClaimedReward) CyberSurfaceHigh else MatrixGreen,
            textColor = if (hasClaimedReward) TextMuted else CyberBackground,
            enabled = !hasClaimedReward,
            modifier = Modifier.fillMaxWidth(),
            icon = "🎁",
            testTag = "claim_video_reward_button"
          )
        }
      }

      // Category Filter Chips
      item {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(categories) { category ->
            val isSelected = category == selectedCategoryFilter
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CyberSurfaceVariant)
                .border(
                  1.dp,
                  if (isSelected) NeonCyan else BorderGlow,
                  RoundedCornerShape(16.dp)
                )
                .clickable {
                  SoundManager.playButtonClick()
                  selectedCategoryFilter = category
                }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = category,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) NeonCyan else TextSecondary,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 10.sp
              )
            }
          }
        }
      }

      // Available Video Intel Feeds Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "ENCRYPTED INTEL CHANNELS (${filteredBriefings.size})",
            style = MaterialTheme.typography.labelLarge,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "TAP TO STREAM",
            style = MaterialTheme.typography.labelSmall,
            color = NeonCyan,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
          )
        }
      }

      // Briefings Playlist Items
      filteredBriefings.forEach { briefing ->
        item {
          val isCurrent = briefing.id == selectedVideo.id
          CyberCard(
            borderColor = if (isCurrent) NeonCyan else BorderGlow,
            glowColor = if (isCurrent) NeonCyan.copy(alpha = 0.3f) else null,
            backgroundColor = if (isCurrent) CyberSurfaceVariant else CyberSurface,
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                SoundManager.playButtonClick()
                selectedVideo = briefing
                hasClaimedReward = false
              }
              .testTag("briefing_item_${briefing.id}")
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
              ) {
                // Video thumbnail badge with play state
                Box(
                  modifier = Modifier
                    .size(46.dp)
                    .clip(CutCornerShape(4.dp))
                    .background(if (isCurrent) NeonCyan.copy(alpha = 0.2f) else CyberBackground)
                    .border(
                      1.dp,
                      if (isCurrent) NeonCyan else BorderGlow,
                      CutCornerShape(4.dp)
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = if (isCurrent) "▶️" else "📹",
                    fontSize = 20.sp
                  )
                }

                Column {
                  Text(
                    text = briefing.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isCurrent) NeonCyan else TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                  )
                  Text(
                    text = briefing.intelSummary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 2
                  )
                  Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    CyberBadge(
                      text = briefing.category,
                      color = when (briefing.category) {
                        "RECONNAISSANCE" -> NeonCyan
                        "WEAPON MATRIX" -> MatrixGreen
                        "THREAT DOSSIER" -> NeonCrimson
                        else -> ElectricPurple
                      },
                      modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                      text = "${briefing.chapters.size} Chapters",
                      style = MaterialTheme.typography.labelSmall,
                      color = TextSecondary,
                      fontSize = 10.sp,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }
              }

              Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                CyberBadge(
                  text = formatDuration(briefing.durationSeconds),
                  color = TextSecondary
                )
                Text(
                  text = briefing.classification,
                  style = MaterialTheme.typography.labelSmall,
                  color = if (briefing.classification == "TOP SECRET") NeonCrimson else TextMuted,
                  fontSize = 9.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        }
      }
    }
  }
}
