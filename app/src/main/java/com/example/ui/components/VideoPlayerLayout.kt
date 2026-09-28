package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.audio.SoundManager
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.sin

/**
 * Data structures for video intel briefings, playback chapters, and tactical subtitles.
 */
data class VideoChapter(
  val title: String,
  val timestampSeconds: Int,
  val description: String = ""
)

data class VideoSubtitle(
  val startSeconds: Int,
  val endSeconds: Int,
  val speaker: String,
  val text: String
)

data class VideoItem(
  val id: String,
  val title: String,
  val durationSeconds: Int,
  val category: String = "INTEL BRIEFING",
  val classification: String = "RESTRICTED",
  val intelSummary: String = "",
  val resolution: String = "1080P // 60FPS",
  val chapters: List<VideoChapter> = emptyList(),
  val subtitles: List<VideoSubtitle> = emptyList()
)

enum class AspectRatioMode(val label: String, val ratio: Float) {
  WIDESCREEN_16_9("16:9", 16f / 9f),
  ULTRAWIDE_21_9("21:9", 21f / 9f),
  CRT_TACTICAL_4_3("4:3", 4f / 3f)
}

enum class VideoQuality(val label: String, val badge: String) {
  AUTO("Auto (Adaptive)", "AUTO"),
  SD_480P("480p Tactical", "480P"),
  HD_720P("720p Clean", "720P"),
  FHD_1080P("1080p Ultra HD", "1080P"),
  HOLO_4K("4K Sub-Ether Neural", "4K")
}

/**
 * Comprehensive Video Player Layout with HUD overlay controls, timeline scrubbing,
 * chapter navigation, animated cyberpunk visuals, closed captions, and fullscreen mode.
 */
@Composable
fun VideoPlayerLayout(
  video: VideoItem,
  modifier: Modifier = Modifier,
  onBack: (() -> Unit)? = null,
  onVideoCompleted: (() -> Unit)? = null
) {
  var isPlaying by remember(video.id) { mutableStateOf(true) }
  var currentPositionSeconds by remember(video.id) { mutableStateOf(0) }
  var isScrubbing by remember { mutableStateOf(false) }
  var scrubPreviewSeconds by remember { mutableStateOf(0) }
  var showControls by remember { mutableStateOf(true) }
  var isMuted by remember { mutableStateOf(false) }
  var volumeLevel by remember { mutableFloatStateOf(0.85f) }
  var playbackSpeed by remember { mutableFloatStateOf(1.0f) }
  var selectedQuality by remember { mutableStateOf(VideoQuality.FHD_1080P) }
  var aspectRatioMode by remember { mutableStateOf(AspectRatioMode.WIDESCREEN_16_9) }
  var subtitlesEnabled by remember { mutableStateOf(true) }
  var isControlsLocked by remember { mutableStateOf(false) }
  var isFullscreen by remember { mutableStateOf(false) }
  var isBuffering by remember { mutableStateOf(false) }
  var showSpeedMenu by remember { mutableStateOf(false) }
  var showQualityMenu by remember { mutableStateOf(false) }

  // Auto-hide controls timer
  LaunchedEffect(showControls, isPlaying, isControlsLocked) {
    if (showControls && isPlaying && !isControlsLocked && !isScrubbing) {
      delay(4000)
      showControls = false
    }
  }

  // Playback timer ticker with playback speed support
  LaunchedEffect(isPlaying, video.id, playbackSpeed) {
    while (isActive && isPlaying) {
      val delayMs = (1000L / playbackSpeed).toLong().coerceAtLeast(100L)
      delay(delayMs)
      if (currentPositionSeconds < video.durationSeconds) {
        currentPositionSeconds++
      } else {
        isPlaying = false
        onVideoCompleted?.invoke()
      }
    }
  }

  // Current subtitle calculation
  val currentDisplaySubtitle = remember(currentPositionSeconds, video.subtitles, subtitlesEnabled) {
    if (!subtitlesEnabled) null
    else {
      video.subtitles.find { sub ->
        currentPositionSeconds in sub.startSeconds..sub.endSeconds
      }
    }
  }

  // Main Container
  Box(
    modifier = modifier
      .fillMaxWidth()
      .clip(CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
      .border(1.dp, if (isPlaying) NeonCyan.copy(alpha = 0.5f) else BorderGlow, CutCornerShape(topStart = 8.dp, bottomEnd = 8.dp))
      .background(CyberBackground)
      .testTag("video_player_layout")
  ) {
    VideoPlayerContent(
      video = video,
      isPlaying = isPlaying,
      currentPositionSeconds = if (isScrubbing) scrubPreviewSeconds else currentPositionSeconds,
      aspectRatio = aspectRatioMode.ratio,
      showControls = showControls,
      isMuted = isMuted,
      volumeLevel = volumeLevel,
      playbackSpeed = playbackSpeed,
      selectedQuality = selectedQuality,
      aspectRatioMode = aspectRatioMode,
      subtitlesEnabled = subtitlesEnabled,
      isControlsLocked = isControlsLocked,
      isFullscreen = false,
      isBuffering = isBuffering,
      currentSubtitle = currentDisplaySubtitle,
      showSpeedMenu = showSpeedMenu,
      showQualityMenu = showQualityMenu,
      onToggleControls = {
        if (!isControlsLocked) {
          showControls = !showControls
        }
      },
      onPlayPauseToggle = {
        SoundManager.playButtonClick()
        isPlaying = !isPlaying
        showControls = true
      },
      onSeekRelative = { delta ->
        SoundManager.playButtonClick()
        currentPositionSeconds = (currentPositionSeconds + delta).coerceIn(0, video.durationSeconds)
        showControls = true
      },
      onSeekTo = { target ->
        currentPositionSeconds = target.coerceIn(0, video.durationSeconds)
      },
      onScrubStart = {
        isScrubbing = true
        scrubPreviewSeconds = currentPositionSeconds
      },
      onScrubValueChange = { preview ->
        scrubPreviewSeconds = preview
      },
      onScrubEnd = { finalPos ->
        isScrubbing = false
        currentPositionSeconds = finalPos.coerceIn(0, video.durationSeconds)
      },
      onToggleMute = {
        SoundManager.playButtonClick()
        isMuted = !isMuted
      },
      onVolumeChange = { newVol ->
        volumeLevel = newVol
        if (newVol > 0f) isMuted = false
      },
      onSpeedSelect = { speed ->
        SoundManager.playButtonClick()
        playbackSpeed = speed
        showSpeedMenu = false
      },
      onQualitySelect = { quality ->
        SoundManager.playButtonClick()
        selectedQuality = quality
        showQualityMenu = false
      },
      onAspectRatioToggle = {
        SoundManager.playButtonClick()
        aspectRatioMode = when (aspectRatioMode) {
          AspectRatioMode.WIDESCREEN_16_9 -> AspectRatioMode.ULTRAWIDE_21_9
          AspectRatioMode.ULTRAWIDE_21_9 -> AspectRatioMode.CRT_TACTICAL_4_3
          AspectRatioMode.CRT_TACTICAL_4_3 -> AspectRatioMode.WIDESCREEN_16_9
        }
      },
      onToggleSubtitles = {
        SoundManager.playButtonClick()
        subtitlesEnabled = !subtitlesEnabled
      },
      onToggleLock = {
        SoundManager.playButtonClick()
        isControlsLocked = !isControlsLocked
        if (isControlsLocked) showControls = false
      },
      onToggleFullscreen = {
        SoundManager.playButtonClick()
        isFullscreen = true
      },
      onBack = onBack,
      onDismissMenus = {
        showSpeedMenu = false
        showQualityMenu = false
      },
      onShowSpeedMenu = { showSpeedMenu = true },
      onShowQualityMenu = { showQualityMenu = true }
    )
  }

  // Fullscreen Dialog Mode
  if (isFullscreen) {
    Dialog(
      onDismissRequest = { isFullscreen = false },
      properties = DialogProperties(
        usePlatformDefaultWidth = false,
        decorFitsSystemWindows = false
      )
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black)
          .testTag("fullscreen_video_player")
      ) {
        VideoPlayerContent(
          video = video,
          isPlaying = isPlaying,
          currentPositionSeconds = if (isScrubbing) scrubPreviewSeconds else currentPositionSeconds,
          aspectRatio = aspectRatioMode.ratio,
          showControls = showControls,
          isMuted = isMuted,
          volumeLevel = volumeLevel,
          playbackSpeed = playbackSpeed,
          selectedQuality = selectedQuality,
          aspectRatioMode = aspectRatioMode,
          subtitlesEnabled = subtitlesEnabled,
          isControlsLocked = isControlsLocked,
          isFullscreen = true,
          isBuffering = isBuffering,
          currentSubtitle = currentDisplaySubtitle,
          showSpeedMenu = showSpeedMenu,
          showQualityMenu = showQualityMenu,
          onToggleControls = {
            if (!isControlsLocked) {
              showControls = !showControls
            }
          },
          onPlayPauseToggle = {
            SoundManager.playButtonClick()
            isPlaying = !isPlaying
            showControls = true
          },
          onSeekRelative = { delta ->
            SoundManager.playButtonClick()
            currentPositionSeconds = (currentPositionSeconds + delta).coerceIn(0, video.durationSeconds)
            showControls = true
          },
          onSeekTo = { target ->
            currentPositionSeconds = target.coerceIn(0, video.durationSeconds)
          },
          onScrubStart = {
            isScrubbing = true
            scrubPreviewSeconds = currentPositionSeconds
          },
          onScrubValueChange = { preview ->
            scrubPreviewSeconds = preview
          },
          onScrubEnd = { finalPos ->
            isScrubbing = false
            currentPositionSeconds = finalPos.coerceIn(0, video.durationSeconds)
          },
          onToggleMute = {
            SoundManager.playButtonClick()
            isMuted = !isMuted
          },
          onVolumeChange = { newVol ->
            volumeLevel = newVol
            if (newVol > 0f) isMuted = false
          },
          onSpeedSelect = { speed ->
            SoundManager.playButtonClick()
            playbackSpeed = speed
            showSpeedMenu = false
          },
          onQualitySelect = { quality ->
            SoundManager.playButtonClick()
            selectedQuality = quality
            showQualityMenu = false
          },
          onAspectRatioToggle = {
            SoundManager.playButtonClick()
            aspectRatioMode = when (aspectRatioMode) {
              AspectRatioMode.WIDESCREEN_16_9 -> AspectRatioMode.ULTRAWIDE_21_9
              AspectRatioMode.ULTRAWIDE_21_9 -> AspectRatioMode.CRT_TACTICAL_4_3
              AspectRatioMode.CRT_TACTICAL_4_3 -> AspectRatioMode.WIDESCREEN_16_9
            }
          },
          onToggleSubtitles = {
            SoundManager.playButtonClick()
            subtitlesEnabled = !subtitlesEnabled
          },
          onToggleLock = {
            SoundManager.playButtonClick()
            isControlsLocked = !isControlsLocked
            if (isControlsLocked) showControls = false
          },
          onToggleFullscreen = {
            SoundManager.playButtonClick()
            isFullscreen = false
          },
          onBack = { isFullscreen = false },
          onDismissMenus = {
            showSpeedMenu = false
            showQualityMenu = false
          },
          onShowSpeedMenu = { showSpeedMenu = true },
          onShowQualityMenu = { showQualityMenu = true }
        )
      }
    }
  }
}

/**
 * Inner core video viewport rendering with animated holographic canvas,
 * scanlines, transport controls, and HUD widgets.
 */
@Composable
private fun VideoPlayerContent(
  video: VideoItem,
  isPlaying: Boolean,
  currentPositionSeconds: Int,
  aspectRatio: Float,
  showControls: Boolean,
  isMuted: Boolean,
  volumeLevel: Float,
  playbackSpeed: Float,
  selectedQuality: VideoQuality,
  aspectRatioMode: AspectRatioMode,
  subtitlesEnabled: Boolean,
  isControlsLocked: Boolean,
  isFullscreen: Boolean,
  isBuffering: Boolean,
  currentSubtitle: VideoSubtitle?,
  showSpeedMenu: Boolean,
  showQualityMenu: Boolean,
  onToggleControls: () -> Unit,
  onPlayPauseToggle: () -> Unit,
  onSeekRelative: (Int) -> Unit,
  onSeekTo: (Int) -> Unit,
  onScrubStart: () -> Unit,
  onScrubValueChange: (Int) -> Unit,
  onScrubEnd: (Int) -> Unit,
  onToggleMute: () -> Unit,
  onVolumeChange: (Float) -> Unit,
  onSpeedSelect: (Float) -> Unit,
  onQualitySelect: (VideoQuality) -> Unit,
  onAspectRatioToggle: () -> Unit,
  onToggleSubtitles: () -> Unit,
  onToggleLock: () -> Unit,
  onToggleFullscreen: () -> Unit,
  onBack: (() -> Unit)?,
  onDismissMenus: () -> Unit,
  onShowSpeedMenu: () -> Unit,
  onShowQualityMenu: () -> Unit
) {
  val infiniteTransition = rememberInfiniteTransition(label = "video_animations")
  val scanlineAnim by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(2400, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "scanlines"
  )
  val radarAngle by infiniteTransition.animateFloat(
    initialValue = 0f,
    targetValue = 360f,
    animationSpec = infiniteRepeatable(
      animation = tween(4000, easing = LinearEasing),
      repeatMode = RepeatMode.Restart
    ),
    label = "radar"
  )

  Box(
    modifier = Modifier
      .then(
        if (isFullscreen) Modifier.fillMaxSize()
        else Modifier
          .fillMaxWidth()
          .aspectRatio(aspectRatio)
      )
      .background(Color(0xFF04070D))
      .pointerInput(Unit) {
        detectTapGestures(
          onTap = { onToggleControls() },
          onDoubleTap = { offset ->
            val isRight = offset.x > size.width / 2
            onSeekRelative(if (isRight) 10 else -10)
          }
        )
      },
    contentAlignment = Alignment.Center
  ) {
    // 1. Procedural Cyberpunk Tactical Video Stream Canvas
    CyberVideoCanvas(
      isPlaying = isPlaying,
      currentPositionSeconds = currentPositionSeconds,
      scanlinePhase = scanlineAnim,
      radarAngle = radarAngle,
      videoTitle = video.title,
      modifier = Modifier.fillMaxSize()
    )

    // 2. Camera HUD Frame Overlay (Corners, live ticker, classification)
    VideoFrameHudOverlay(
      video = video,
      isPlaying = isPlaying,
      currentSeconds = currentPositionSeconds,
      selectedQuality = selectedQuality
    )

    // 3. Subtitles / Closed Captions Layer
    if (currentSubtitle != null) {
      Box(
        modifier = Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = if (showControls) 82.dp else 22.dp)
          .padding(horizontal = 24.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(Color(0xCC080D18))
          .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
          .padding(horizontal = 12.dp, vertical = 6.dp)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "[${currentSubtitle.speaker}]",
            style = MaterialTheme.typography.labelSmall,
            color = MatrixGreen,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
          )
          Text(
            text = currentSubtitle.text,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            fontSize = 12.sp
          )
        }
      }
    }

    // 4. Interactive Overlay Controls (Animated fade in/out)
    AnimatedVisibility(
      visible = showControls && !isControlsLocked,
      enter = fadeIn(animationSpec = tween(180)),
      exit = fadeOut(animationSpec = tween(220)),
      modifier = Modifier.fillMaxSize()
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.55f))
      ) {
        // TOP HUD CONTROLS BAR
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .background(
              Brush.verticalGradient(
                listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
              )
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
          ) {
            if (onBack != null || isFullscreen) {
              IconButton(
                onClick = {
                  SoundManager.playButtonClick()
                  onBack?.invoke()
                },
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                  contentDescription = "Back",
                  tint = NeonCyan
                )
              }
            }

            Column(modifier = Modifier.padding(start = 4.dp)) {
              Text(
                text = video.title,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                CyberBadge(
                  text = video.classification,
                  color = NeonCrimson,
                  backgroundColor = NeonCrimson.copy(alpha = 0.2f)
                )
                Text(
                  text = "${video.category} • ${selectedQuality.badge}",
                  style = MaterialTheme.typography.labelSmall,
                  color = TextSecondary,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }

          // Top Action Buttons
          Row(verticalAlignment = Alignment.CenterVertically) {
            // Lock controls button
            IconButton(
              onClick = onToggleLock,
              modifier = Modifier.size(36.dp)
            ) {
              Icon(
                imageVector = Icons.Default.LockOpen,
                contentDescription = "Lock Controls",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
              )
            }

            // Aspect ratio toggle
            IconButton(
              onClick = onAspectRatioToggle,
              modifier = Modifier.size(36.dp)
            ) {
              Text(
                text = aspectRatioMode.label,
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
              )
            }

            // Quality selector button
            Box {
              IconButton(
                onClick = onShowQualityMenu,
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.HighQuality,
                  contentDescription = "Video Quality",
                  tint = NeonCyan,
                  modifier = Modifier.size(18.dp)
                )
              }

              DropdownMenu(
                expanded = showQualityMenu,
                onDismissRequest = onDismissMenus,
                modifier = Modifier.background(CyberSurfaceVariant)
              ) {
                VideoQuality.values().forEach { quality ->
                  DropdownMenuItem(
                    text = {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = quality.label,
                          color = if (quality == selectedQuality) NeonCyan else TextPrimary,
                          fontSize = 12.sp
                        )
                        if (quality == selectedQuality) {
                          Text(text = "✓", color = NeonCyan, fontSize = 12.sp)
                        }
                      }
                    },
                    onClick = { onQualitySelect(quality) }
                  )
                }
              }
            }
          }
        }

        // CENTER QUICK TRANSPORT CONTROLS
        Row(
          modifier = Modifier.align(Alignment.Center),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
          // -10s Seek Rewind
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(CyberSurfaceVariant.copy(alpha = 0.75f))
              .border(1.dp, BorderGlow, CircleShape)
              .clickable { onSeekRelative(-10) },
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Replay10,
                contentDescription = "Rewind 10 Seconds",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = "-10s",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 9.sp
              )
            }
          }

          // Hero Play/Pause Button
          Box(
            modifier = Modifier
              .size(62.dp)
              .clip(CircleShape)
              .background(
                Brush.radialGradient(
                  listOf(NeonCyan.copy(alpha = 0.9f), ElectricBlue.copy(alpha = 0.95f))
                )
              )
              .border(2.dp, NeonCyan, CircleShape)
              .clickable { onPlayPauseToggle() },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (isPlaying) "Pause" else "Play",
              tint = CyberBackground,
              modifier = Modifier.size(34.dp)
            )
          }

          // +10s Seek Forward
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(CyberSurfaceVariant.copy(alpha = 0.75f))
              .border(1.dp, BorderGlow, CircleShape)
              .clickable { onSeekRelative(10) },
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Default.Forward10,
                contentDescription = "Forward 10 Seconds",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = "+10s",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontSize = 9.sp
              )
            }
          }
        }

        // BOTTOM SCRUBBER & MEDIA BAR
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .background(
              Brush.verticalGradient(
                listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f))
              )
            )
            .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
          // Interactive Scrubber Slider with Chapter Markers
          VideoScrubberSlider(
            currentSeconds = currentPositionSeconds,
            totalSeconds = video.durationSeconds,
            chapters = video.chapters,
            onScrubStart = onScrubStart,
            onScrubChange = onScrubValueChange,
            onScrubEnd = onScrubEnd
          )

          // Media controls bottom row
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Left: Time elapsed / Total duration
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = formatDuration(currentPositionSeconds),
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "/",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
              )
              Text(
                text = formatDuration(video.durationSeconds),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontFamily = FontFamily.Monospace
              )
            }

            // Right: Audio, Speed, CC, and Fullscreen
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              // Mute / Unmute
              IconButton(
                onClick = onToggleMute,
                modifier = Modifier.size(34.dp)
              ) {
                Icon(
                  imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                  contentDescription = if (isMuted) "Unmute" else "Mute",
                  tint = if (isMuted) NeonCrimson else TextPrimary,
                  modifier = Modifier.size(18.dp)
                )
              }

              // Subtitles (CC) Toggle
              IconButton(
                onClick = onToggleSubtitles,
                modifier = Modifier.size(34.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Subtitles,
                  contentDescription = "Closed Captions",
                  tint = if (subtitlesEnabled) NeonCyan else TextMuted,
                  modifier = Modifier.size(18.dp)
                )
              }

              // Playback Speed Selector
              Box {
                IconButton(
                  onClick = onShowSpeedMenu,
                  modifier = Modifier.size(34.dp)
                ) {
                  Text(
                    text = "${playbackSpeed}x",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (playbackSpeed != 1.0f) CyberGold else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                  )
                }

                DropdownMenu(
                  expanded = showSpeedMenu,
                  onDismissRequest = onDismissMenus,
                  modifier = Modifier.background(CyberSurfaceVariant)
                ) {
                  listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { speed ->
                    DropdownMenuItem(
                      text = {
                        Text(
                          text = "${speed}x Speed",
                          color = if (speed == playbackSpeed) NeonCyan else TextPrimary,
                          fontSize = 12.sp
                        )
                      },
                      onClick = { onSpeedSelect(speed) }
                    )
                  }
                }
              }

              // Fullscreen Toggle
              IconButton(
                onClick = onToggleFullscreen,
                modifier = Modifier.size(34.dp)
              ) {
                Icon(
                  imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                  contentDescription = if (isFullscreen) "Exit Fullscreen" else "Fullscreen",
                  tint = NeonCyan,
                  modifier = Modifier.size(20.dp)
                )
              }
            }
          }
        }
      }
    }

    // 5. Controls Locked Badge Indicator (shows only when controls are locked)
    if (isControlsLocked) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .padding(12.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.7f))
          .border(1.dp, NeonCrimson, CircleShape)
          .clickable { onToggleLock() }
          .padding(8.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = "Unlock Controls",
          tint = NeonCrimson,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}

/**
 * Procedural Cyberpunk Tactical Video Visualization Canvas:
 * Draws simulated drone reconnaissance visuals, scanning lines, radar grid,
 * waveform frequency visualizer, and target tracking telemetry.
 */
@Composable
private fun CyberVideoCanvas(
  isPlaying: Boolean,
  currentPositionSeconds: Int,
  scanlinePhase: Float,
  radarAngle: Float,
  videoTitle: String,
  modifier: Modifier = Modifier
) {
  Canvas(modifier = modifier) {
    val canvasWidth = size.width
    val canvasHeight = size.height

    // Deep grid lines
    val gridStep = 40f
    for (x in 0..(canvasWidth / gridStep).toInt()) {
      drawLine(
        color = Color(0xFF0F1A2E).copy(alpha = 0.4f),
        start = Offset(x * gridStep, 0f),
        end = Offset(x * gridStep, canvasHeight),
        strokeWidth = 1f
      )
    }
    for (y in 0..(canvasHeight / gridStep).toInt()) {
      drawLine(
        color = Color(0xFF0F1A2E).copy(alpha = 0.4f),
        start = Offset(0f, y * gridStep),
        end = Offset(canvasWidth, y * gridStep),
        strokeWidth = 1f
      )
    }

    // Dynamic scanning radar circle
    val centerX = canvasWidth * 0.5f
    val centerY = canvasHeight * 0.5f
    val maxRadius = minOf(canvasWidth, canvasHeight) * 0.38f

    drawCircle(
      color = NeonCyan.copy(alpha = 0.08f),
      radius = maxRadius,
      center = Offset(centerX, centerY),
      style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
    )
    drawCircle(
      color = NeonCyan.copy(alpha = 0.12f),
      radius = maxRadius * 0.65f,
      center = Offset(centerX, centerY),
      style = Stroke(width = 1f)
    )
    drawCircle(
      color = NeonCyan.copy(alpha = 0.15f),
      radius = maxRadius * 0.3f,
      center = Offset(centerX, centerY),
      style = Stroke(width = 1f)
    )

    // Radar beam sweep
    val rad = Math.toRadians(radarAngle.toDouble())
    val targetX = centerX + (maxRadius * kotlin.math.cos(rad)).toFloat()
    val targetY = centerY + (maxRadius * kotlin.math.sin(rad)).toFloat()
    drawLine(
      color = if (isPlaying) NeonCyan.copy(alpha = 0.45f) else Color.Transparent,
      start = Offset(centerX, centerY),
      end = Offset(targetX, targetY),
      strokeWidth = 2f
    )

    // Moving horizontal scanline
    val scanY = canvasHeight * scanlinePhase
    drawLine(
      brush = Brush.horizontalGradient(
        listOf(Color.Transparent, NeonCyan.copy(alpha = 0.6f), Color.Transparent)
      ),
      start = Offset(0f, scanY),
      end = Offset(canvasWidth, scanY),
      strokeWidth = 2f
    )

    // Simulated Tactical Targets / Heat Signatures
    val target1X = centerX - 60f + sin(currentPositionSeconds * 0.3) * 30f
    val target1Y = centerY - 30f + sin(currentPositionSeconds * 0.5) * 20f
    drawRect(
      color = NeonCrimson.copy(alpha = 0.7f),
      topLeft = Offset(target1X.toFloat() - 14f, target1Y.toFloat() - 14f),
      size = Size(28f, 28f),
      style = Stroke(width = 1.5f)
    )

    val target2X = centerX + 80f - sin(currentPositionSeconds * 0.4) * 25f
    val target2Y = centerY + 40f + sin(currentPositionSeconds * 0.2) * 15f
    drawCircle(
      color = MatrixGreen.copy(alpha = 0.6f),
      radius = 12f,
      center = Offset(target2X.toFloat(), target2Y.toFloat()),
      style = Stroke(width = 1.5f)
    )

    // Audio Frequency Waveform visualizer at bottom of canvas
    val barCount = 32
    val barWidth = canvasWidth / barCount
    for (i in 0 until barCount) {
      val barPhase = (i * 0.35f + currentPositionSeconds * 2f)
      val heightFactor = if (isPlaying) {
        ((sin(barPhase) + 1f) * 0.5f * 0.8f + 0.2f).coerceIn(0.1f, 1f)
      } else {
        0.1f
      }
      val barHeight = canvasHeight * 0.12f * heightFactor
      drawRect(
        color = if (isPlaying) NeonCyan.copy(alpha = 0.25f) else Color(0xFF1E293B).copy(alpha = 0.2f),
        topLeft = Offset(i * barWidth + 2f, canvasHeight - barHeight),
        size = Size(barWidth - 4f, barHeight)
      )
    }
  }
}

/**
 * Video frame HUD overlay with live camera telemetry, corner brackets, and recording indicators.
 */
@Composable
private fun VideoFrameHudOverlay(
  video: VideoItem,
  isPlaying: Boolean,
  currentSeconds: Int,
  selectedQuality: VideoQuality
) {
  Box(modifier = Modifier.fillMaxSize().padding(8.dp)) {
    // Top-Left: Camera ID & Live status
    Row(
      modifier = Modifier.align(Alignment.TopStart),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      Box(
        modifier = Modifier
          .size(8.dp)
          .clip(CircleShape)
          .background(if (isPlaying) NeonCrimson else TextMuted)
      )
      Text(
        text = if (isPlaying) "REC ● ORBITAL SATELLITE 09" else "PAUSED // STANDBY",
        style = MaterialTheme.typography.labelSmall,
        color = if (isPlaying) NeonCrimson else TextMuted,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        letterSpacing = 0.8.sp
      )
    }

    // Top-Right: Frame Rate & Quality
    Text(
      text = "ISO 3200 // 60 FPS // ${selectedQuality.badge}",
      style = MaterialTheme.typography.labelSmall,
      color = MatrixGreen,
      fontFamily = FontFamily.Monospace,
      fontSize = 9.sp,
      modifier = Modifier.align(Alignment.TopEnd)
    )

    // Center Crosshair
    Box(
      modifier = Modifier
        .align(Alignment.Center)
        .size(24.dp)
    ) {
      Canvas(modifier = Modifier.fillMaxSize()) {
        val stroke = 1f
        val color = Color.White.copy(alpha = 0.25f)
        drawLine(color, Offset(size.width / 2, 0f), Offset(size.width / 2, size.height), stroke)
        drawLine(color, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), stroke)
      }
    }

    // Bottom-Left: Coordinates & Telemetry
    Text(
      text = "LAT: 35.6895°N // LON: 139.6917°E // ELEV: 120M",
      style = MaterialTheme.typography.labelSmall,
      color = TextMuted,
      fontFamily = FontFamily.Monospace,
      fontSize = 8.sp,
      modifier = Modifier
        .align(Alignment.BottomStart)
        .padding(bottom = 2.dp)
    )
  }
}

/**
 * Custom Interactive Scrubber Slider with gradient progress, chapter markers,
 * and scrub gesture tracking.
 */
@Composable
fun VideoScrubberSlider(
  currentSeconds: Int,
  totalSeconds: Int,
  chapters: List<VideoChapter> = emptyList(),
  onScrubStart: () -> Unit,
  onScrubChange: (Int) -> Unit,
  onScrubEnd: (Int) -> Unit,
  modifier: Modifier = Modifier
) {
  val safeTotal = totalSeconds.coerceAtLeast(1)
  val progress = (currentSeconds.toFloat() / safeTotal).coerceIn(0f, 1f)

  Box(
    modifier = modifier
      .fillMaxWidth()
      .height(28.dp),
    contentAlignment = Alignment.Center
  ) {
    // Slider Track Bar
    Slider(
      value = progress,
      onValueChange = { newProgress ->
        val targetSeconds = (newProgress * safeTotal).toInt()
        onScrubStart()
        onScrubChange(targetSeconds)
      },
      onValueChangeFinished = {
        onScrubEnd(currentSeconds)
      },
      colors = SliderDefaults.colors(
        thumbColor = NeonCyan,
        activeTrackColor = NeonCyan,
        inactiveTrackColor = CyberSurfaceHigh.copy(alpha = 0.6f)
      ),
      modifier = Modifier
        .fillMaxWidth()
        .testTag("video_scrubber_slider")
    )

    // Chapter markers tick overlay
    if (chapters.isNotEmpty()) {
      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .height(12.dp)
          .padding(horizontal = 8.dp)
      ) {
        val trackWidth = size.width
        chapters.forEach { chapter ->
          val chapterProgress = (chapter.timestampSeconds.toFloat() / safeTotal).coerceIn(0f, 1f)
          val markerX = trackWidth * chapterProgress
          drawLine(
            color = CyberGold,
            start = Offset(markerX, 0f),
            end = Offset(markerX, size.height),
            strokeWidth = 2f
          )
        }
      }
    }
  }
}

/**
 * Helper to format seconds into MM:SS format.
 */
fun formatDuration(seconds: Int): String {
  val mins = (seconds % 3600) / 60
  val secs = seconds % 60
  return String.format("%02d:%02d", mins, secs)
}
