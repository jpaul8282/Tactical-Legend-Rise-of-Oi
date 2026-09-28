package com.example.ui.viewmodel

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.db.AppDatabase
import com.example.data.repository.GameRepository
import com.example.ui.theme.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

data class ArcadeDrone(
  val id: String = UUID.randomUUID().toString(),
  val x: Float, // 0.0 to 1.0
  var y: Float = 0.0f, // 0.0 (top) to 1.0 (bottom firewall)
  val speed: Float = 0.015f,
  val type: DroneType = DroneType.STANDARD,
  val points: Int = 100,
  val icon: String = "👾",
  val color: Color = NeonCrimson
)

enum class DroneType {
  STANDARD,
  SPEEDER,
  HEAVY,
  EMP_POWERUP,
  SHIELD_POWERUP
}

data class ArcadeGameState(
  val isRunning: Boolean = false,
  val isGameOver: Boolean = false,
  val score: Int = 0,
  val combo: Int = 0,
  val maxCombo: Int = 0,
  val firewallHealth: Int = 100,
  val difficulty: String = "Overdrive",
  val activeDrones: List<ArcadeDrone> = emptyList(),
  val earnedCredits: Int = 0,
  val earnedData: Int = 0,
  val neutralizedCount: Int = 0
)

class ArcadeViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: GameRepository
  private var gameLoopJob: Job? = null

  init {
    val db = AppDatabase.getDatabase(application)
    repository = GameRepository(db.gameDao())
  }

  private val _state = MutableStateFlow(ArcadeGameState())
  val state: StateFlow<ArcadeGameState> = _state

  fun startGame(difficulty: String = "Overdrive") {
    gameLoopJob?.cancel()
    _state.value = ArcadeGameState(
      isRunning = true,
      isGameOver = false,
      score = 0,
      combo = 0,
      maxCombo = 0,
      firewallHealth = 100,
      difficulty = difficulty,
      activeDrones = emptyList()
    )

    val speedMult = when (difficulty) {
      "Standard" -> 1.0f
      "Overdrive" -> 1.35f
      "Frenzy" -> 1.7f
      else -> 1.2f
    }

    gameLoopJob = viewModelScope.launch {
      var ticks = 0
      while (isActive && _state.value.isRunning && !_state.value.isGameOver) {
        delay(40) // ~25 FPS game ticker
        ticks++

        val currentState = _state.value
        if (currentState.firewallHealth <= 0) {
          endGame()
          break
        }

        // Spawn new drones periodically
        val spawnRate = when (difficulty) {
          "Standard" -> 30
          "Overdrive" -> 22
          "Frenzy" -> 16
          else -> 25
        }

        val newDrones = currentState.activeDrones.map { it.copy(y = it.y + (it.speed * speedMult)) }.toMutableList()

        if (ticks % spawnRate == 0) {
          val randType = Random.nextFloat()
          val drone = when {
            randType < 0.10f -> ArcadeDrone(
              x = Random.nextFloat().coerceIn(0.1f, 0.9f),
              type = DroneType.EMP_POWERUP,
              points = 250,
              icon = "⚡",
              color = NeonCyan,
              speed = 0.012f
            )
            randType < 0.20f -> ArcadeDrone(
              x = Random.nextFloat().coerceIn(0.1f, 0.9f),
              type = DroneType.SHIELD_POWERUP,
              points = 150,
              icon = "🛡️",
              color = MatrixGreen,
              speed = 0.010f
            )
            randType < 0.45f -> ArcadeDrone(
              x = Random.nextFloat().coerceIn(0.1f, 0.9f),
              type = DroneType.SPEEDER,
              points = 200,
              icon = "🛰️",
              color = CyberGold,
              speed = 0.022f
            )
            randType < 0.70f -> ArcadeDrone(
              x = Random.nextFloat().coerceIn(0.1f, 0.9f),
              type = DroneType.HEAVY,
              points = 300,
              icon = "👹",
              color = ElectricPurple,
              speed = 0.008f
            )
            else -> ArcadeDrone(
              x = Random.nextFloat().coerceIn(0.1f, 0.9f),
              type = DroneType.STANDARD,
              points = 100,
              icon = "👾",
              color = NeonCrimson,
              speed = 0.015f
            )
          }
          newDrones.add(drone)
        }

        // Check if drones hit the bottom firewall
        var healthLoss = 0
        var resetCombo = false
        val survivingDrones = mutableListOf<ArcadeDrone>()

        for (d in newDrones) {
          if (d.y >= 0.95f) {
            if (d.type != DroneType.EMP_POWERUP && d.type != DroneType.SHIELD_POWERUP) {
              healthLoss += 15
              resetCombo = true
              SoundManager.playDefeat()
            }
          } else {
            survivingDrones.add(d)
          }
        }

        val newHealth = (currentState.firewallHealth - healthLoss).coerceAtLeast(0)
        val newCombo = if (resetCombo) 0 else currentState.combo

        _state.value = currentState.copy(
          firewallHealth = newHealth,
          combo = newCombo,
          activeDrones = survivingDrones
        )
      }
    }
  }

  fun tapDrone(droneId: String) {
    val currentState = _state.value
    if (!currentState.isRunning || currentState.isGameOver) return

    val targetDrone = currentState.activeDrones.find { it.id == droneId } ?: return

    val newCombo = currentState.combo + 1
    val maxC = maxOf(currentState.maxCombo, newCombo)
    val comboMultiplier = (1.0f + (newCombo * 0.1f)).coerceAtMost(4.0f)
    val addedPoints = (targetDrone.points * comboMultiplier).toInt()
    val newScore = currentState.score + addedPoints

    var newHealth = currentState.firewallHealth
    val remainingDrones = currentState.activeDrones.filter { it.id != droneId }.toMutableList()

    when (targetDrone.type) {
      DroneType.EMP_POWERUP -> {
        SoundManager.playEmp()
        // EMP blast clears all drones on screen
        remainingDrones.clear()
      }
      DroneType.SHIELD_POWERUP -> {
        SoundManager.playShield()
        newHealth = (newHealth + 25).coerceAtMost(100)
      }
      else -> {
        SoundManager.playHackChirp()
      }
    }

    _state.value = currentState.copy(
      score = newScore,
      combo = newCombo,
      maxCombo = maxC,
      firewallHealth = newHealth,
      neutralizedCount = currentState.neutralizedCount + 1,
      activeDrones = remainingDrones
    )
  }

  private fun endGame() {
    val finalState = _state.value
    val creditsEarned = (finalState.score * 0.05f).toInt().coerceAtLeast(30)
    val dataEarned = (finalState.score * 0.02f).toInt().coerceAtLeast(15)

    SoundManager.playVictory()
    _state.value = finalState.copy(
      isRunning = false,
      isGameOver = true,
      earnedCredits = creditsEarned,
      earnedData = dataEarned
    )

    viewModelScope.launch {
      repository.recordArcadeGame(finalState.score, finalState.maxCombo, finalState.difficulty)
    }
  }

  fun exitGame() {
    gameLoopJob?.cancel()
    _state.value = _state.value.copy(isRunning = false, isGameOver = false)
  }
}
