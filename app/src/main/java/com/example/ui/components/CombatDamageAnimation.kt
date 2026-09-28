package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CombatDamageType
import com.example.data.model.FloatingDamage
import com.example.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Data representation of an individual kinetic spark/particle radiating from damage impact.
 */
private data class ImpactParticle(
  val angle: Float,
  val maxDistance: Float,
  val size: Float,
  val color: Color,
  val isStar: Boolean,
  val delayRatio: Float
)

/**
 * Animated overlay for displaying dynamic particles and floating combat text animations
 * when units deal damage to each other during combat and attack phases.
 */
@Composable
fun AnimatedDamageOverlay(
  floatingDamages: List<FloatingDamage>,
  gridWidth: Int,
  gridHeight: Int,
  showCoordinates: Boolean,
  modifier: Modifier = Modifier,
  onDismissDamage: ((String) -> Unit)? = null
) {
  BoxWithConstraints(
    modifier = modifier
      .fillMaxSize()
      .padding(
        start = if (showCoordinates) 18.dp else 0.dp,
        top = if (showCoordinates) 14.dp else 0.dp
      )
  ) {
    val cellWidth = maxWidth / gridWidth
    val cellHeight = maxHeight / gridHeight

    floatingDamages.forEach { dmg ->
      key(dmg.id) {
        val centerX = cellWidth * dmg.gridX + (cellWidth / 2)
        val centerY = cellHeight * dmg.gridY + (cellHeight / 2)

        DamageImpactAndFloatingText(
          dmg = dmg,
          centerX = centerX,
          centerY = centerY,
          cellWidth = cellWidth,
          cellHeight = cellHeight,
          onAnimationFinished = {
            onDismissDamage?.invoke(dmg.id)
          }
        )
      }
    }
  }
}

/**
 * Renders both the Canvas-driven kinetic particle explosion / shockwave ring
 * and the floating text badge rising upward with spring-like scale animation.
 */
@Composable
private fun DamageImpactAndFloatingText(
  dmg: FloatingDamage,
  centerX: Dp,
  centerY: Dp,
  cellWidth: Dp,
  cellHeight: Dp,
  onAnimationFinished: () -> Unit
) {
  val animProgress = remember { Animatable(0f) }
  val density = LocalDensity.current

  LaunchedEffect(dmg.id) {
    animProgress.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 1150, easing = LinearEasing)
    )
    onAnimationFinished()
  }

  val progress = animProgress.value

  // Generate deterministic particle bundle for this impact event
  val particleCount = if (dmg.isCrit || dmg.damageType == CombatDamageType.CRITICAL) 22 else 14
  val particles = remember(dmg.id) {
    val baseColor = dmg.color
    val accentColors = when (dmg.damageType) {
      CombatDamageType.CRITICAL -> listOf(CyberGold, Color(0xFFFFF176), NeonCrimson, Color.White)
      CombatDamageType.BLADE_SLASH -> listOf(NeonCrimson, Color(0xFFFF5252), ElectricBlue, Color.White)
      CombatDamageType.SHIELD_BREAK -> listOf(NeonCyan, ElectricBlue, MatrixGreen, Color.White)
      CombatDamageType.EMP_SHOCK -> listOf(ElectricPurple, NeonCyan, Color(0xFFE040FB), Color.White)
      CombatDamageType.HEAL -> listOf(MatrixGreen, Color(0xFF69F0AE), CyberGold, Color.White)
      CombatDamageType.NORMAL -> listOf(NeonCrimson, Color(0xFFFF8A80), CyberGold, Color.White)
    }

    val maxDistPx = with(density) { (cellWidth * 0.95f).toPx() }
    val rng = Random(dmg.id.hashCode())

    (0 until particleCount).map { i ->
      val angle = (rng.nextFloat() * 2f * PI.toFloat())
      val dist = (rng.nextFloat() * 0.65f + 0.35f) * maxDistPx
      val size = rng.nextFloat() * 4.5f + 2f
      val color = accentColors[rng.nextInt(accentColors.size)]
      val isStar = rng.nextFloat() < 0.35f
      val delayRatio = rng.nextFloat() * 0.15f
      ImpactParticle(angle, dist, size, color, isStar, delayRatio)
    }
  }

  // 1. Kinetic Particle & Shockwave Ring Canvas (centered on unit tile)
  Canvas(
    modifier = Modifier
      .fillMaxSize()
  ) {
    val cx = centerX.toPx()
    val cy = centerY.toPx()

    // A. Expanding Shockwave Ring Pulse
    val shockwaveProgress = (progress * 1.5f).coerceAtMost(1f)
    if (shockwaveProgress < 1f) {
      val shockwaveRadius = 6.dp.toPx() + (36.dp.toPx() * shockwaveProgress)
      val shockwaveAlpha = ((1f - shockwaveProgress) * 0.85f).coerceIn(0f, 1f)
      val strokeThickness = (2.5.dp.toPx() * (1f - shockwaveProgress)).coerceAtLeast(0.5f)

      drawCircle(
        color = dmg.color.copy(alpha = shockwaveAlpha),
        radius = shockwaveRadius,
        center = Offset(cx, cy),
        style = Stroke(width = strokeThickness)
      )
    }

    // B. Melee / Blade Slash Razor Streak
    if (dmg.damageType == CombatDamageType.BLADE_SLASH) {
      val slashProgress = (progress * 2.2f).coerceAtMost(1f)
      if (slashProgress < 1f) {
        val slashAlpha = (1f - slashProgress).coerceIn(0f, 1f)
        val slashLength = 32.dp.toPx() * slashProgress
        val startOffset = Offset(cx - slashLength, cy - slashLength * 0.7f)
        val endOffset = Offset(cx + slashLength, cy + slashLength * 0.7f)

        // Core slash beam
        drawLine(
          color = Color.White.copy(alpha = slashAlpha),
          start = startOffset,
          end = endOffset,
          strokeWidth = 3.dp.toPx() * (1f - slashProgress),
          cap = StrokeCap.Round
        )
        // Outer colored laser glow
        drawLine(
          color = dmg.color.copy(alpha = slashAlpha * 0.8f),
          start = startOffset,
          end = endOffset,
          strokeWidth = 6.dp.toPx() * (1f - slashProgress),
          cap = StrokeCap.Round
        )
      }
    }

    // C. Radial Shards and Kinetic Sparks
    particles.forEach { p ->
      val adjustedT = ((progress - p.delayRatio) / (1f - p.delayRatio)).coerceIn(0f, 1f)
      if (adjustedT > 0f) {
        // Easing deceleration: fast initial burst, decelerating float
        val easedT = 1f - (1f - adjustedT) * (1f - adjustedT)
        val currentDist = p.maxDistance * easedT
        val px = cx + cos(p.angle) * currentDist
        val py = cy + sin(p.angle) * currentDist
        val pAlpha = ((1f - adjustedT) * 1.1f).coerceIn(0f, 1f)
        val currentSize = p.size * (1f - adjustedT * 0.45f)

        if (p.isStar) {
          // Draw 4-point sparkle cross
          val arm = currentSize * 1.6f
          drawLine(
            color = p.color.copy(alpha = pAlpha),
            start = Offset(px - arm, py),
            end = Offset(px + arm, py),
            strokeWidth = 1.8.dp.toPx() * (1f - adjustedT),
            cap = StrokeCap.Round
          )
          drawLine(
            color = p.color.copy(alpha = pAlpha),
            start = Offset(px, py - arm),
            end = Offset(px, py + arm),
            strokeWidth = 1.8.dp.toPx() * (1f - adjustedT),
            cap = StrokeCap.Round
          )
          drawCircle(
            color = Color.White.copy(alpha = pAlpha),
            radius = currentSize * 0.4f,
            center = Offset(px, py)
          )
        } else {
          // Glow halo
          drawCircle(
            color = p.color.copy(alpha = pAlpha * 0.6f),
            radius = currentSize * 1.7f,
            center = Offset(px, py)
          )
          // Bright inner core
          drawCircle(
            color = Color.White.copy(alpha = pAlpha),
            radius = currentSize * 0.7f,
            center = Offset(px, py)
          )
        }
      }
    }
  }

  // 2. Floating Combat Text Badge
  // Scale curve: 0.35 -> punchy 1.35 bounce -> settle 1.0 -> gentle drift
  val textScale = when {
    progress < 0.15f -> 0.35f + (progress / 0.15f) * 1.0f // 0.35 -> 1.35
    progress < 0.30f -> 1.35f - ((progress - 0.15f) / 0.15f) * 0.35f // 1.35 -> 1.0
    else -> 1.0f - ((progress - 0.30f) / 0.70f) * 0.08f // 1.0 -> 0.92
  }

  // Y-Rise displacement: from tile center upward to -46.dp
  val floatOffsetY = (progress * 46f).dp

  // Alpha fade: instantly appears, stays crisp, then fades out in the final 35%
  val textAlpha = when {
    progress < 0.08f -> progress / 0.08f
    progress < 0.65f -> 1.0f
    else -> ((1.0f - progress) / 0.35f).coerceIn(0f, 1f)
  }

  val badgeBackgroundBrush = when (dmg.damageType) {
    CombatDamageType.CRITICAL -> Brush.horizontalGradient(
      listOf(Color(0xFF2E2005), Color(0xFF42280A), Color(0xFF2E2005))
    )
    CombatDamageType.BLADE_SLASH -> Brush.horizontalGradient(
      listOf(Color(0xFF330B12), Color(0xFF4A101A), Color(0xFF330B12))
    )
    CombatDamageType.SHIELD_BREAK -> Brush.horizontalGradient(
      listOf(Color(0xFF0A2238), Color(0xFF0F3254), Color(0xFF0A2238))
    )
    CombatDamageType.EMP_SHOCK -> Brush.horizontalGradient(
      listOf(Color(0xFF270E38), Color(0xFF3B1554), Color(0xFF270E38))
    )
    CombatDamageType.HEAL -> Brush.horizontalGradient(
      listOf(Color(0xFF0B2D1B), Color(0xFF114227), Color(0xFF0B2D1B))
    )
    CombatDamageType.NORMAL -> Brush.horizontalGradient(
      listOf(CyberBackground.copy(alpha = 0.94f), Color(0xFF261017))
    )
  }

  Box(
    modifier = Modifier
      .offset(
        x = centerX - 42.dp,
        y = centerY - (cellHeight / 2) - 10.dp - floatOffsetY
      )
      .width(84.dp),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .scale(textScale)
        .alpha(textAlpha)
        .shadow(
          elevation = if (dmg.isCrit) 8.dp else 4.dp,
          shape = CutCornerShape(3.dp),
          ambientColor = dmg.color,
          spotColor = dmg.color
        )
        .clip(CutCornerShape(3.dp))
        .background(badgeBackgroundBrush)
        .border(
          width = if (dmg.isCrit) 1.5.dp else 1.dp,
          color = if (dmg.isCrit) CyberGold else dmg.color.copy(alpha = 0.9f),
          shape = CutCornerShape(3.dp)
        )
        .padding(horizontal = 6.dp, vertical = 2.5.dp)
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
      ) {
        Text(
          text = dmg.text,
          style = MaterialTheme.typography.labelSmall,
          color = if (dmg.isCrit) Color.White else dmg.color,
          fontFamily = FontFamily.Monospace,
          fontWeight = FontWeight.ExtraBold,
          fontSize = if (dmg.isCrit) 11.5.sp else 10.sp,
          letterSpacing = 0.5.sp
        )
      }
    }
  }
}
