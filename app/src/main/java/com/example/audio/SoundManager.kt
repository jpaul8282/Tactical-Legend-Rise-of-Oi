package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

object SoundManager {
  private val scope = CoroutineScope(Dispatchers.Default)
  private const val SAMPLE_RATE = 22050

  private fun playPcm(generator: (sampleIndex: Int, totalSamples: Int) -> Short, durationMs: Int) {
    scope.launch {
      try {
        val totalSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalSamples)
        for (i in 0 until totalSamples) {
          buffer[i] = generator(i, totalSamples)
        }

        val track = AudioTrack.Builder()
          .setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_GAME)
              .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
              .build()
          )
          .setAudioFormat(
            AudioFormat.Builder()
              .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
              .setSampleRate(SAMPLE_RATE)
              .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
              .build()
          )
          .setBufferSizeInBytes(buffer.size * 2)
          .setTransferMode(AudioTrack.MODE_STATIC)
          .build()

        track.write(buffer, 0, buffer.size)
        track.play()

        // Auto release track after duration
        kotlinx.coroutines.delay(durationMs.toLong() + 100)
        track.release()
      } catch (e: Exception) {
        // Silently handle audio engine fallback
      }
    }
  }

  fun playButtonClick() {
    playPcm({ i, total ->
      val freq = 800.0
      val env = 1.0 - (i.toDouble() / total)
      val sample = sin(2.0 * PI * i * freq / SAMPLE_RATE) * env
      (sample * Short.MAX_VALUE * 0.3).toInt().toShort()
    }, durationMs = 50)
  }

  fun playLaserShoot() {
    playPcm({ i, total ->
      val progress = i.toDouble() / total
      val freq = 900.0 - (progress * 650.0) // 900Hz to 250Hz drop
      val env = 1.0 - progress
      val sample = sin(2.0 * PI * i * freq / SAMPLE_RATE) * env
      (sample * Short.MAX_VALUE * 0.4).toInt().toShort()
    }, durationMs = 120)
  }

  fun playSlash() {
    playPcm({ i, total ->
      val progress = i.toDouble() / total
      val noise = (Random.nextDouble() * 2.0 - 1.0)
      val env = (1.0 - progress) * (1.0 - progress)
      (noise * env * Short.MAX_VALUE * 0.45).toInt().toShort()
    }, durationMs = 140)
  }

  fun playShield() {
    playPcm({ i, total ->
      val progress = i.toDouble() / total
      val freq = 300.0 + (progress * 400.0)
      val env = sin(progress * PI)
      val sample = sin(2.0 * PI * i * freq / SAMPLE_RATE) * env
      (sample * Short.MAX_VALUE * 0.4).toInt().toShort()
    }, durationMs = 220)
  }

  fun playEmp() {
    playPcm({ i, total ->
      val progress = i.toDouble() / total
      val noise = (Random.nextDouble() * 2.0 - 1.0) * 0.6
      val lowFreq = sin(2.0 * PI * i * 110.0 / SAMPLE_RATE) * 0.4
      val env = 1.0 - progress
      ((noise + lowFreq) * env * Short.MAX_VALUE * 0.5).toInt().toShort()
    }, durationMs = 280)
  }

  fun playHackChirp() {
    playPcm({ i, total ->
      val progress = i.toDouble() / total
      val freq = if (progress < 0.5) 1200.0 else 1800.0
      val sample = sin(2.0 * PI * i * freq / SAMPLE_RATE)
      (sample * Short.MAX_VALUE * 0.35).toInt().toShort()
    }, durationMs = 90)
  }

  fun playLevelUp() {
    playPcm({ i, total ->
      val progress = i.toDouble() / total
      val noteIndex = (progress * 4).toInt().coerceIn(0, 3)
      val freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50) // C5, E5, G5, C6
      val freq = freqs[noteIndex]
      val sample = sin(2.0 * PI * i * freq / SAMPLE_RATE)
      (sample * Short.MAX_VALUE * 0.4).toInt().toShort()
    }, durationMs = 380)
  }

  fun playForgeSuccess() {
    playPcm({ i, total ->
      val progress = i.toDouble() / total
      val freq = 440.0 + sin(progress * PI * 4.0) * 120.0 + (progress * 500.0)
      val sample = sin(2.0 * PI * i * freq / SAMPLE_RATE)
      (sample * Short.MAX_VALUE * 0.4).toInt().toShort()
    }, durationMs = 320)
  }

  fun playVictory() {
    playPcm({ i, total ->
      val progress = i.toDouble() / total
      val noteIndex = (progress * 5).toInt().coerceIn(0, 4)
      val freqs = doubleArrayOf(587.33, 739.99, 880.0, 1174.66, 1479.98) // D5, F#5, A5, D6, F#6
      val freq = freqs[noteIndex]
      val sample = sin(2.0 * PI * i * freq / SAMPLE_RATE)
      (sample * Short.MAX_VALUE * 0.45).toInt().toShort()
    }, durationMs = 500)
  }

  fun playDefeat() {
    playPcm({ i, total ->
      val progress = i.toDouble() / total
      val noteIndex = (progress * 3).toInt().coerceIn(0, 2)
      val freqs = doubleArrayOf(440.0, 415.30, 370.0) // A4, G#4, F#4
      val freq = freqs[noteIndex]
      val sample = sin(2.0 * PI * i * freq / SAMPLE_RATE) * (1.0 - (progress * 0.5))
      (sample * Short.MAX_VALUE * 0.4).toInt().toShort()
    }, durationMs = 450)
  }
}
