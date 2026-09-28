package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCard
import com.example.ui.components.StatProgressBar
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArcadeViewModel
import com.example.ui.viewmodel.GameViewModel

@Composable
fun CyberArcadeScreen(
  arcadeViewModel: ArcadeViewModel,
  gameViewModel: GameViewModel
) {
  val arcadeState by arcadeViewModel.state.collectAsState()
  val uiState by gameViewModel.uiState.collectAsState()
  var selectedDifficulty by remember { mutableStateOf("Overdrive") }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBackground)
  ) {
    if (!arcadeState.isRunning && !arcadeState.isGameOver) {
      // Arcade Lobby & Leaderboards
      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
      ) {
        item {
          CyberCard(
            borderColor = MatrixGreen,
            glowColor = MatrixGreen.copy(alpha = 0.3f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
              Text(text = "👾", fontSize = 32.sp)
              Text(
                text = "CYBER ARCADE: DRONE STRIKE",
                style = MaterialTheme.typography.titleLarge,
                color = MatrixGreen,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Defend the Oi Sub-Ether Firewall against descending rogue AI swarms in real-time.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 4.dp)
              )
            }
          }
        }

        // Difficulty Selection
        item {
          CyberCard(borderColor = BorderGlow, modifier = Modifier.fillMaxWidth()) {
            Text(
              text = "SELECT MISSION OVERDRIVE FREQUENCY",
              style = MaterialTheme.typography.labelMedium,
              color = NeonCyan,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              listOf("Standard", "Overdrive", "Frenzy").forEach { diff ->
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(CutCornerShape(4.dp))
                    .background(if (selectedDifficulty == diff) MatrixGreen.copy(alpha = 0.2f) else CyberSurfaceVariant)
                    .border(1.dp, if (selectedDifficulty == diff) MatrixGreen else BorderGlow, CutCornerShape(4.dp))
                  .clickable {
                    SoundManager.playButtonClick()
                    selectedDifficulty = diff
                  }
                  .padding(10.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = diff.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selectedDifficulty == diff) MatrixGreen else TextSecondary,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            CyberButton(
              text = "START DRONE INTERCEPT",
              onClick = {
                SoundManager.playHackChirp()
                arcadeViewModel.startGame(selectedDifficulty)
              },
              primaryColor = MatrixGreen,
              textColor = CyberBackground,
              modifier = Modifier.fillMaxWidth(),
              icon = "⚡"
            )
          }
        }

        // High Score Hall of Fame
        item {
          CyberCard(borderColor = BorderGlow, modifier = Modifier.fillMaxWidth()) {
            Text(
              text = "SUB-ETHER HALL OF FAME LEADERBOARD",
              style = MaterialTheme.typography.labelLarge,
              color = CyberGold,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              uiState.topArcadeScores.forEachIndexed { index, score ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(CyberSurfaceVariant)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                      text = "#${index + 1}",
                      style = MaterialTheme.typography.titleMedium,
                      color = if (index == 0) CyberGold else TextMuted,
                      fontFamily = FontFamily.Monospace,
                      fontWeight = FontWeight.Bold
                    )
                    Text(
                      text = score.playerName,
                      style = MaterialTheme.typography.titleSmall,
                      color = TextPrimary
                    )
                  }

                  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                      text = "${score.score} PTS",
                      style = MaterialTheme.typography.titleMedium,
                      color = MatrixGreen,
                      fontFamily = FontFamily.Monospace,
                      fontWeight = FontWeight.Bold
                    )
                    CyberBadge(text = "${score.maxCombo}x COMBO", color = CyberGold)
                  }
                }
              }
            }
          }
        }
      }
    } else {
      // Active Live Arcade Session
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 10.dp, vertical = 6.dp)
      ) {
        // Top Ticker HUD
        CyberCard(
          borderColor = MatrixGreen,
          backgroundColor = CyberSurface,
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(text = "SCORE", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Text(
                text = "${arcadeState.score}",
                style = MaterialTheme.typography.titleLarge,
                color = MatrixGreen,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Text(text = "COMBO", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Text(
                text = "${arcadeState.combo}x",
                style = MaterialTheme.typography.titleLarge,
                color = CyberGold,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }

            CyberButton(
              text = "ABORT",
              onClick = { arcadeViewModel.exitGame() },
              primaryColor = NeonCrimson,
              textColor = TextPrimary,
              modifier = Modifier.defaultMinSize(minWidth = 70.dp, minHeight = 32.dp)
            )
          }

          Spacer(modifier = Modifier.height(6.dp))

          // Firewall Integrity
          StatProgressBar(
            current = arcadeState.firewallHealth,
            max = 100,
            barColor = if (arcadeState.firewallHealth > 35) MatrixGreen else NeonCrimson,
            label = "FIREWALL INTEGRITY",
            height = 8.dp
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Intercept Canvas Area
        BoxWithConstraints(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(CyberSurface)
            .border(1.dp, MatrixGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
        ) {
          val parentWidth = maxWidth
          val parentHeight = maxHeight

          // Bottom Firewall Laser Line
          Box(
            modifier = Modifier
              .align(Alignment.BottomCenter)
              .fillMaxWidth()
              .height(4.dp)
              .background(
                Brush.horizontalGradient(
                  listOf(NeonCrimson, NeonCrimsonDim, NeonCrimson)
                )
              )
          )

          // Falling Drones
          arcadeState.activeDrones.forEach { drone ->
            val offsetX = (parentWidth - 44.dp) * drone.x
            val offsetY = (parentHeight - 44.dp) * drone.y

            Box(
              modifier = Modifier
                .offset(x = offsetX, y = offsetY)
                .size(44.dp)
                .clip(CircleShape)
                .background(drone.color.copy(alpha = 0.2f))
                .border(1.5.dp, drone.color, CircleShape)
                .clickable {
                  arcadeViewModel.tapDrone(drone.id)
                },
              contentAlignment = Alignment.Center
            ) {
              Text(text = drone.icon, fontSize = 20.sp)
            }
          }
        }
      }
    }

    // Game Over Modal
    if (arcadeState.isGameOver) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.85f))
          .padding(20.dp),
        contentAlignment = Alignment.Center
      ) {
        CyberCard(
          borderColor = MatrixGreen,
          glowColor = MatrixGreen,
          backgroundColor = CyberSurface,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
          ) {
            Text(
              text = "FIREWALL BREACH DETECTED",
              style = MaterialTheme.typography.titleLarge,
              color = NeonCrimson,
              fontWeight = FontWeight.Bold,
              textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "FINAL SCORE: ${arcadeState.score}",
              style = MaterialTheme.typography.displayMedium,
              color = MatrixGreen,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )

            Text(
              text = "MAX COMBO: ${arcadeState.maxCombo}x | NEUTRALIZED: ${arcadeState.neutralizedCount} DRONES",
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
              modifier = Modifier.padding(top = 4.dp)
            )

            // Reward Payout Card
            CyberCard(
              borderColor = BorderGlow,
              backgroundColor = CyberSurfaceVariant,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(text = "+${arcadeState.earnedCredits}", style = MaterialTheme.typography.titleMedium, color = NeonCyan, fontWeight = FontWeight.Bold)
                  Text(text = "Credits", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(text = "+${arcadeState.earnedData}", style = MaterialTheme.typography.titleMedium, color = MatrixGreen, fontWeight = FontWeight.Bold)
                  Text(text = "Data MB", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            CyberButton(
              text = "CLAIM REWARDS & RETURN",
              onClick = { arcadeViewModel.exitGame() },
              primaryColor = MatrixGreen,
              textColor = CyberBackground,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    }
  }
}
