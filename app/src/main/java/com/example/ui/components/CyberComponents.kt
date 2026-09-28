package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.ui.theme.*

@Composable
fun CyberCard(
  modifier: Modifier = Modifier,
  borderColor: Color = BorderGlow,
  glowColor: Color? = null,
  backgroundColor: Color = CyberSurface,
  shape: Shape = CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp),
  content: @Composable ColumnScope.() -> Unit
) {
  Card(
    modifier = modifier
      .then(
        if (glowColor != null) {
          Modifier.border(1.dp, glowColor, shape)
        } else {
          Modifier.border(1.dp, borderColor, shape)
        }
      ),
    shape = shape,
    colors = CardDefaults.cardColors(containerColor = backgroundColor)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      content = content
    )
  }
}

@Composable
fun CyberButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  primaryColor: Color = NeonCyan,
  textColor: Color = CyberBackground,
  enabled: Boolean = true,
  icon: String? = null,
  testTag: String = "cyber_button"
) {
  val shape = CutCornerShape(topStart = 6.dp, bottomEnd = 6.dp)
  val interactionSource = remember { MutableInteractionSource() }

  Box(
    modifier = modifier
      .testTag(testTag)
      .defaultMinSize(minWidth = 100.dp, minHeight = 44.dp)
      .clip(shape)
      .background(
        if (enabled) {
          Brush.horizontalGradient(listOf(primaryColor, primaryColor.copy(alpha = 0.85f)))
        } else {
          Brush.horizontalGradient(listOf(Color(0xFF2A3447), Color(0xFF1F2633)))
        }
      )
      .border(
        1.dp,
        if (enabled) primaryColor else Color(0xFF3E4C63),
        shape
      )
      .clickable(
        interactionSource = interactionSource,
        indication = null,
        enabled = enabled
      ) {
        SoundManager.playButtonClick()
        onClick()
      }
      .padding(horizontal = 14.dp, vertical = 8.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (icon != null) {
        Text(text = icon, fontSize = 16.sp, modifier = Modifier.padding(end = 6.dp))
      }
      Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = if (enabled) textColor else TextMuted,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
    }
  }
}

@Composable
fun CyberOutlineButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  borderColor: Color = NeonCyan,
  textColor: Color = NeonCyan,
  icon: String? = null,
  testTag: String = "cyber_outline_button"
) {
  val shape = CutCornerShape(topStart = 4.dp, bottomEnd = 4.dp)
  Box(
    modifier = modifier
      .testTag(testTag)
      .defaultMinSize(minWidth = 80.dp, minHeight = 38.dp)
      .clip(shape)
      .background(CyberSurfaceVariant.copy(alpha = 0.6f))
      .border(1.dp, borderColor, shape)
      .clickable {
        SoundManager.playButtonClick()
        onClick()
      }
      .padding(horizontal = 10.dp, vertical = 6.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      if (icon != null) {
        Text(text = icon, fontSize = 14.sp, modifier = Modifier.padding(end = 4.dp))
      }
      Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = textColor,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
      )
    }
  }
}

@Composable
fun CyberBadge(
  text: String,
  color: Color = NeonCyan,
  backgroundColor: Color = color.copy(alpha = 0.15f),
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .clip(RoundedCornerShape(4.dp))
      .background(backgroundColor)
      .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
      .padding(horizontal = 8.dp, vertical = 3.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text.uppercase(),
      style = MaterialTheme.typography.labelSmall,
      color = color,
      fontWeight = FontWeight.Bold,
      letterSpacing = 0.5.sp
    )
  }
}

@Composable
fun StatProgressBar(
  current: Int,
  max: Int,
  barColor: Color,
  modifier: Modifier = Modifier,
  height: Dp = 8.dp,
  label: String? = null,
  showValue: Boolean = true
) {
  val ratio = if (max > 0) (current.toFloat() / max).coerceIn(0f, 1f) else 0f

  Column(modifier = modifier) {
    if (label != null || showValue) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (label != null) {
          Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        }
        if (showValue) {
          Text(
            text = "$current / $max",
            style = MaterialTheme.typography.labelSmall,
            color = barColor,
            fontFamily = FontFamily.Monospace
          )
        }
      }
    }
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(height)
        .clip(RoundedCornerShape(2.dp))
        .background(CyberSurfaceHigh)
    ) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .fillMaxWidth(ratio)
          .background(
            Brush.horizontalGradient(
              listOf(barColor.copy(alpha = 0.7f), barColor)
            )
          )
      )
    }
  }
}
