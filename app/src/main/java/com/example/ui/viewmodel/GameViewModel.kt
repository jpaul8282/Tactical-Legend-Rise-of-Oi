package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.*
import com.example.data.model.HeroClass
import com.example.data.model.ItemRarity
import com.example.data.model.ItemType
import com.example.data.repository.GameRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class Quadruple<A, B, C, D>(
  val first: A,
  val second: B,
  val third: C,
  val fourth: D
)

data class GameUiState(
  val playerProfile: PlayerProfileEntity? = null,
  val operatives: List<OperativeEntity> = emptyList(),
  val items: List<ItemEntity> = emptyList(),
  val missions: List<MissionEntity> = emptyList(),
  val topArcadeScores: List<ArcadeScoreEntity> = emptyList(),
  val lastForgedItem: ItemEntity? = null,
  val feedbackMessage: String? = null,
  val isForgeAnimating: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
  private val repository: GameRepository

  init {
    val db = AppDatabase.getDatabase(application)
    repository = GameRepository(db.gameDao())
  }

  private val _feedbackMessage = MutableStateFlow<String?>(null)
  private val _lastForgedItem = MutableStateFlow<ItemEntity?>(null)
  private val _isForgeAnimating = MutableStateFlow(false)

  private val dbState = combine(
    repository.playerProfile,
    repository.allOperatives,
    repository.allItems,
    repository.allMissions
  ) { profile, ops, items, missions ->
    Quadruple(profile, ops, items, missions)
  }

  private val sessionState = combine(
    repository.topArcadeScores,
    _lastForgedItem,
    _feedbackMessage,
    _isForgeAnimating
  ) { scores, forgedItem, feedback, isForging ->
    Quadruple(scores, forgedItem, feedback, isForging)
  }

  val uiState: StateFlow<GameUiState> = combine(dbState, sessionState) { db, session ->
    GameUiState(
      playerProfile = db.first,
      operatives = db.second,
      items = db.third,
      missions = db.fourth,
      topArcadeScores = session.first,
      lastForgedItem = session.second,
      feedbackMessage = session.third,
      isForgeAnimating = session.fourth
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = GameUiState()
  )

  fun getSquadCombatPower(): Int {
    val ops = uiState.value.operatives.filter { it.isDeployed }
    val items = uiState.value.items
    var totalPower = 0
    ops.forEach { op ->
      var opPower = (op.baseHp / 2) + (op.baseAtk * 3) + (op.baseDef * 2) + (op.level * 40)
      // add gear bonus
      listOfNotNull(op.equippedWeaponId, op.equippedArmorId, op.equippedCoreId, op.equippedChipId).forEach { gearId ->
        val gear = items.find { it.id == gearId }
        if (gear != null) {
          opPower += (gear.atkBonus * 3) + (gear.defBonus * 2) + (gear.hpBonus / 2)
        }
      }
      totalPower += opPower
    }
    return totalPower
  }

  fun toggleOperativeDeployment(operativeId: String) {
    viewModelScope.launch {
      val deployedCount = uiState.value.operatives.count { it.isDeployed }
      val target = uiState.value.operatives.find { it.id == operativeId }
      if (target != null && !target.isDeployed && deployedCount >= 4) {
        _feedbackMessage.value = "Maximum 4 operatives can be deployed in active squad!"
        return@launch
      }
      if (target != null && target.isDeployed && deployedCount <= 1) {
        _feedbackMessage.value = "Active squad must have at least 1 operative!"
        return@launch
      }
      repository.toggleOperativeDeployment(operativeId)
    }
  }

  fun levelUpOperative(operativeId: String) {
    viewModelScope.launch {
      val success = repository.levelUpOperative(operativeId)
      if (success) {
        _feedbackMessage.value = "Operative promoted to next combat tier!"
      } else {
        _feedbackMessage.value = "Insufficient Cyber Credits or Tactical Data for level up!"
      }
    }
  }

  fun equipItem(operativeId: String, itemId: String) {
    viewModelScope.launch {
      repository.equipItem(operativeId, itemId)
      _feedbackMessage.value = "Tactical gear calibrated and equipped."
    }
  }

  fun unequipItem(itemId: String) {
    viewModelScope.launch {
      repository.unequipItem(itemId)
      _feedbackMessage.value = "Gear removed to tactical vault."
    }
  }

  fun forgeItem(rarity: ItemRarity, itemType: ItemType) {
    viewModelScope.launch {
      _isForgeAnimating.value = true
      _lastForgedItem.value = null
      kotlinx.coroutines.delay(1200) // Synth effect
      val forged = repository.forgeItem(rarity, itemType)
      _isForgeAnimating.value = false
      if (forged != null) {
        _lastForgedItem.value = forged
        _feedbackMessage.value = "Synthesized [${forged.rarity}] ${forged.name}!"
      } else {
        _feedbackMessage.value = "Insufficient Nanite resources in Cyber Vault!"
      }
    }
  }

  fun purchaseStorePackage(credits: Int, data: Int, cost: Double, isVip: Boolean = false, bonusItem: ItemEntity? = null) {
    viewModelScope.launch {
      repository.purchaseStoreItem(credits, data, isVip, bonusItem)
      _feedbackMessage.value = "Transaction confirmed: +$credits Credits, +$data Data credited!"
    }
  }

  fun claimCinemaReward() {
    viewModelScope.launch {
      repository.claimVideoReward(50)
      _feedbackMessage.value = "Intel Holo-Briefing watched: +50 Cyber Credits claimed!"
    }
  }

  fun clearFeedback() {
    _feedbackMessage.value = null
  }

  fun clearLastForgedItem() {
    _lastForgedItem.value = null
  }
}
