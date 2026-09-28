package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Visual HUD component that displays the current combat turn/round number
 * and clearly indicates whose turn it is (Player Squad vs. Hostile Enemy AI)
 * with animated cybernetic telemetry, glowing status beacons, and tactical metadata.
 *
 * @param currentTurn The current round / turn count (1-indexed).
 * @param isPlayerTurn True if it is currently the Player's turn, false if Enemy AI's turn.
 * @param modifier Custom modifier for styling and layout.
 * @param playerUnitCount Number of active/alive player squad members (optional).
 * @param enemyUnitCount Number of active/alive enemy hostiles (optional).
 * @param missionTitle Optional mission or sector title for top telemetry.
 * @param phaseName Optional sub-phase description (e.g., "COMMAND PHASE", "TARGETING").
 */
@Composable
fun TurnIndicatorHUD(
  currentTurn: Int,
  isPlayerTurn: Boolean,
  modifier: Modifier = Modifier,
  playerUnitCount: Int? = null,
  enemyUnitCount: Int? = null,
  missionTitle: String? = null,
  phaseName: String? = null
) {
  // Pulsing animation for the active faction beacon
  val infiniteTransition = rememberInfiniteTransition(label = "hud_turn_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 0.85f,
    targetValue = 1.18f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "beacon_pulse_scale"
  )
  val pulseAlpha by infiniteTransition.animateFloat(
    initialValue = 0.35f,
    targetValue = 0.95f,
    animationSpec = infiniteRepeatable(
      animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "beacon_pulse_alpha"
  )

  // Animated color transitions for active faction
  val primaryAccent by animateColorAsState(
    targetValue = if (isPlayerTurn) MatrixGreen else NeonCrimson,
    animationSpec = tween(400),
    label = "hud_primary_accent"
  )
  val secondaryAccent by animateColorAsState(
    targetValue = if (isPlayerTurn) NeonCyan else CyberAmber,
    animationSpec = tween(400),
    label = "hud_secondary_accent"
  )
  val bgGradientStart by animateColorAsState(
    targetValue = if (isPlayerTurn) Color(0xFF0F1F1D) else Color(0xFF260F17),
    animationSpec = tween(400),
    label = "hud_bg_start"
  )

  val cutShape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .testTag("turn_indicator_hud")
      .clip(cutShape)
      .background(
        Brush.horizontalGradient(
          colors = listOf(
            bgGradientStart,
            CyberSurface,
            CyberSurfaceVariant
          )
        )
      )
      .border(1.dp, primaryAccent.copy(alpha = 0.75f), cutShape)
      .drawBehind {
        // Subtle cyber top highlight rule
        drawLine(
          brush = Brush.horizontalGradient(
            colors = listOf(
              primaryAccent.copy(alpha = 0.8f),
              secondaryAccent.copy(alpha = 0.4f),
              Color.Transparent
            )
          ),
          start = Offset(0f, 0f),
          end = Offset(size.width, 0f),
          strokeWidth = 2.dp.toPx()
        )
      }
      .padding(horizontal = 12.dp, vertical = 8.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // LEFT SECTION: Turn / Round Indicator & Mission Context
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Stylized Turn Number Box
        Box(
          modifier = Modifier
            .testTag("turn_number_display")
            .clip(CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp))
            .background(CyberSurfaceHigh)
            .border(1.dp, CyberGold.copy(alpha = 0.8f), CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
              text = "ROUND",
              style = MaterialTheme.typography.labelSmall,
              color = CyberGold,
              fontSize = 7.sp,
              fontWeight = FontWeight.Black,
              letterSpacing = 1.sp,
              fontFamily = FontFamily.Monospace
            )
            AnimatedContent(
              targetState = currentTurn,
              transitionSpec = {
                (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
              },
              label = "turn_number_anim"
            ) { turn ->
              Text(
                text = turn.toString().padStart(2, '0'),
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
              )
            }
          }
        }

        // Mission / Sector / Phase Metadata
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
          Text(
            text = missionTitle ?: "TACTICAL GRID ENGAGEMENT",
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            maxLines = 1
          )
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Text(
              text = "PHASE:",
              style = MaterialTheme.typography.labelSmall,
              color = TextMuted,
              fontSize = 9.sp,
              fontFamily = FontFamily.Monospace
            )
            Text(
              text = phaseName ?: if (isPlayerTurn) "COMMAND & EXECUTION" else "AI COMPUTATION",
              style = MaterialTheme.typography.labelSmall,
              color = secondaryAccent,
              fontSize = 9.sp,
              fontWeight = FontWeight.SemiBold,
              fontFamily = FontFamily.Monospace
            )
          }
        }
      }

      // RIGHT SECTION: Whose Turn It Is (Player Squad vs Enemy Hostile AI)
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.testTag("turn_faction_display")
      ) {
        // Squad vs Enemy Telemetry Counters (if provided)
        if (playerUnitCount != null || enemyUnitCount != null) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(3.dp))
              .background(CyberBackground.copy(alpha = 0.6f))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            if (playerUnitCount != null) {
              Text(
                text = "🛡️ $playerUnitCount",
                style = MaterialTheme.typography.labelSmall,
                color = MatrixGreen,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
            if (playerUnitCount != null && enemyUnitCount != null) {
              Text(
                text = "/",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontSize = 9.sp
              )
            }
            if (enemyUnitCount != null) {
              Text(
                text = "👾 $enemyUnitCount",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCrimson,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // Active Turn Badge with Glowing Tactical Beacon
        AnimatedContent(
          targetState = isPlayerTurn,
          transitionSpec = {
            (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
          },
          label = "turn_faction_pill_anim"
        ) { isPlayer ->
          Box(
            modifier = Modifier
              .testTag("turn_status_pill")
              .clip(CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp))
              .background(
                if (isPlayer) MatrixGreen.copy(alpha = 0.18f)
                else NeonCrimson.copy(alpha = 0.22f)
              )
              .border(
                width = 1.dp,
                color = if (isPlayer) MatrixGreen else NeonCrimson,
                shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp)
              )
              .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              // Pulsing Radar Beacon
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .scale(pulseScale)
                  .clip(CircleShape)
                  .background(
                    if (isPlayer) MatrixGreen.copy(alpha = pulseAlpha)
                    else NeonCrimson.copy(alpha = pulseAlpha)
                  )
              )

              Column(horizontalAlignment = Alignment.Start) {
                Text(
                  text = if (isPlayer) "PLAYER SQUAD" else "HOSTILE ENEMY",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isPlayer) MatrixGreen else NeonCrimson,
                  fontWeight = FontWeight.Black,
                  fontSize = 9.sp,
                  letterSpacing = 0.8.sp,
                  fontFamily = FontFamily.Monospace
                )
                Text(
                  text = if (isPlayer) "ACTIVE INITIATIVE" else "ENEMY TURN",
                  style = MaterialTheme.typography.labelSmall,
                  color = TextPrimary.copy(alpha = 0.85f),
                  fontWeight = FontWeight.Bold,
                  fontSize = 8.sp,
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
