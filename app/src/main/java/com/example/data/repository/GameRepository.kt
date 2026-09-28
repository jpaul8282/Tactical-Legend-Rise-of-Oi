package com.example.data.repository

import com.example.data.db.*
import com.example.data.model.HeroClass
import com.example.data.model.ItemRarity
import com.example.data.model.ItemType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class GameRepository(private val gameDao: GameDao) {
  val allOperatives: Flow<List<OperativeEntity>> = gameDao.getAllOperatives()
  val allItems: Flow<List<ItemEntity>> = gameDao.getAllItems()
  val allMissions: Flow<List<MissionEntity>> = gameDao.getAllMissions()
  val playerProfile: Flow<PlayerProfileEntity?> = gameDao.getPlayerProfile()
  val topArcadeScores: Flow<List<ArcadeScoreEntity>> = gameDao.getTopArcadeScores()

  init {
    CoroutineScope(Dispatchers.IO).launch {
      seedInitialDataIfNeeded()
    }
  }

  private suspend fun seedInitialDataIfNeeded() {
    val existingProfile = gameDao.getPlayerProfile().firstOrNull()
    if (existingProfile == null) {
      // 1. Seed Player Profile
      val defaultProfile = PlayerProfileEntity(
        id = 1,
        commanderName = "Commander Oi",
        cyberCredits = 3000,
        tacticalData = 1500,
        playerLevel = 1,
        playerXp = 40,
        isVipPassActive = false,
        totalBattlesWon = 0,
        totalDronesNeutralized = 12
      )
      gameDao.insertPlayerProfile(defaultProfile)

      // 2. Seed Default Items
      val defaultItems = listOf(
        ItemEntity(
          id = "wp_01",
          name = "Thermal Arc Blade",
          itemType = ItemType.WEAPON.name,
          rarity = ItemRarity.EPIC.name,
          atkBonus = 32,
          critBonus = 0.12f,
          isEquipped = true,
          equippedToOperativeId = "kael_blade",
          flavorText = "Superheated plasma edge capable of slicing through composite tank armor."
        ),
        ItemEntity(
          id = "wp_02",
          name = "Rail-Spike Mark IV",
          itemType = ItemType.WEAPON.name,
          rarity = ItemRarity.RARE.name,
          atkBonus = 24,
          critBonus = 0.18f,
          isEquipped = true,
          equippedToOperativeId = "lyra_swift",
          flavorText = "Electromagnetic sniper rifle delivering hyper-velocity depleted uranium rounds."
        ),
        ItemEntity(
          id = "ar_01",
          name = "Aegis Kinetic Weave",
          itemType = ItemType.ARMOR.name,
          rarity = ItemRarity.EPIC.name,
          defBonus = 26,
          hpBonus = 65,
          isEquipped = true,
          equippedToOperativeId = "jax_vane",
          flavorText = "Reinforced carbon-nanotube weave designed for point-blank assault containment."
        ),
        ItemEntity(
          id = "cr_01",
          name = "Neural Overdrive Core",
          itemType = ItemType.NANITE_CORE.name,
          rarity = ItemRarity.RARE.name,
          atkBonus = 10,
          defBonus = 8,
          hpBonus = 30,
          isEquipped = true,
          equippedToOperativeId = "zero_kai",
          flavorText = "Bio-synthetic processor core multiplying cognitive tactical reaction speeds."
        ),
        ItemEntity(
          id = "ch_01",
          name = "Sub-Ether Cipher Array",
          itemType = ItemType.CIPHER_CHIP.name,
          rarity = ItemRarity.LEGENDARY.name,
          atkBonus = 20,
          defBonus = 15,
          hpBonus = 45,
          critBonus = 0.10f,
          isEquipped = false,
          equippedToOperativeId = null,
          flavorText = "Black-market military crypto processor that intercepts adversary targeting packets."
        ),
        ItemEntity(
          id = "wp_03",
          name = "Heavy Pulse Carbine",
          itemType = ItemType.WEAPON.name,
          rarity = ItemRarity.COMMON.name,
          atkBonus = 14,
          isEquipped = false,
          equippedToOperativeId = null,
          flavorText = "Standard corporate security burst rifle."
        ),
        ItemEntity(
          id = "ar_02",
          name = "Nano-Mesh Vest",
          itemType = ItemType.ARMOR.name,
          rarity = ItemRarity.COMMON.name,
          defBonus = 12,
          hpBonus = 25,
          isEquipped = false,
          equippedToOperativeId = null,
          flavorText = "Lightweight flexible protective tactical gear."
        )
      )
      gameDao.insertItems(defaultItems)

      // 3. Seed Operatives
      val defaultOperatives = listOf(
        OperativeEntity(
          id = "jax_vane",
          name = "Jax 'Aegis' Vane",
          heroClass = HeroClass.VANGUARD.name,
          roleTitle = "Frontline Heavy Tank",
          level = 2,
          currentXp = 45,
          maxXp = 120,
          baseHp = 240,
          baseAtk = 30,
          baseDef = 25,
          critRate = 0.05f,
          speed = 10,
          specialAbilityName = "Plasma Barrier",
          specialAbilityDesc = "Deploys an overshield absorbing +60 damage and taunting nearby threats.",
          isDeployed = true,
          equippedArmorId = "ar_01",
          avatarColorHex = 0xFF0070FF,
          loreBriefing = "Former Arasaka heavy riot captain. Abandoned the corporate police state to lead the Oi vanguard."
        ),
        OperativeEntity(
          id = "lyra_swift",
          name = "Lyra 'Viper' Swift",
          heroClass = HeroClass.SNIPER.name,
          roleTitle = "Long-Range Marksman",
          level = 2,
          currentXp = 60,
          maxXp = 120,
          baseHp = 130,
          baseAtk = 56,
          baseDef = 12,
          critRate = 0.25f,
          speed = 14,
          specialAbilityName = "Overcharge Shot",
          specialAbilityDesc = "Channels an armor-piercing shot dealing 2.5x critical damage.",
          isDeployed = true,
          equippedWeaponId = "wp_02",
          avatarColorHex = 0xFFFF2A6D,
          loreBriefing = "Ex-orbital scout sniper. Holds the underworld record for longest confirmed cyber-drone neutralization."
        ),
        OperativeEntity(
          id = "zero_kai",
          name = "Zero 'Ghost' Kai",
          heroClass = HeroClass.CIPHER.name,
          roleTitle = "Electronic Hacker",
          level = 2,
          currentXp = 30,
          maxXp = 120,
          baseHp = 150,
          baseAtk = 42,
          baseDef = 15,
          critRate = 0.12f,
          speed = 16,
          specialAbilityName = "EMP Pulse",
          specialAbilityDesc = "Unleashes an electronic disruption field that stuns and drains enemy barriers.",
          isDeployed = true,
          equippedCoreId = "cr_01",
          avatarColorHex = 0xFF00E5FF,
          loreBriefing = "Prodigy netrunner who breached the central military mainframe and liberated the Oi tactical network."
        ),
        OperativeEntity(
          id = "elena_vance",
          name = "Dr. Elena Vance",
          heroClass = HeroClass.MEDIC.name,
          roleTitle = "Combat Field Doctor",
          level = 1,
          currentXp = 15,
          maxXp = 100,
          baseHp = 155,
          baseAtk = 24,
          baseDef = 16,
          critRate = 0.08f,
          speed = 12,
          specialAbilityName = "Nanite Surge",
          specialAbilityDesc = "Injects microscopic repair nanites, restoring +70 HP to adjacent squadmates.",
          isDeployed = true,
          avatarColorHex = 0xFF05FF69,
          loreBriefing = "Chief biomedical nanite engineer who synthesizes cellular regeneration serums under live fire."
        ),
        OperativeEntity(
          id = "kael_blade",
          name = "Kael 'Ronin' Blade",
          heroClass = HeroClass.SAMURAI.name,
          roleTitle = "Melee Assassin",
          level = 1,
          currentXp = 80,
          maxXp = 100,
          baseHp = 180,
          baseAtk = 48,
          baseDef = 18,
          critRate = 0.18f,
          speed = 18,
          specialAbilityName = "Blade Dance",
          specialAbilityDesc = "Executes rapid thermal strikes dealing devastating multi-target damage.",
          isDeployed = false,
          equippedWeaponId = "wp_01",
          avatarColorHex = 0xFFBD00FF,
          loreBriefing = "Wandering blade master carrying ancient martial code reinforced with thermal mono-molecular edge blades."
        )
      )
      gameDao.insertOperatives(defaultOperatives)

      // 4. Seed Missions
      val defaultMissions = listOf(
        MissionEntity(
          id = "mission_101",
          chapter = 1,
          missionNumber = 1,
          title = "Sector 7 Outpost Infiltration",
          location = "Neo-Tokyo Outer Edge",
          difficulty = "EASY",
          recommendedPower = 400,
          rewardCredits = 300,
          rewardData = 120,
          rewardXp = 80,
          intel = "Secure the perimeter relay terminal to restore squad satellite communications.",
          isUnlocked = true,
          starsEarned = 3,
          isBossEncounter = false
        ),
        MissionEntity(
          id = "mission_102",
          chapter = 1,
          missionNumber = 2,
          title = "Sub-Level 4 Smuggling Docks",
          location = "Sub-Level 4 Smuggling Hub",
          difficulty = "MEDIUM",
          recommendedPower = 650,
          rewardCredits = 500,
          rewardData = 250,
          rewardXp = 140,
          intel = "Intercept the smuggled military Nanite Core prototypes before export by cartel syndicates.",
          isUnlocked = true,
          starsEarned = 0,
          isBossEncounter = false
        ),
        MissionEntity(
          id = "mission_103",
          chapter = 1,
          missionNumber = 3,
          title = "Arasaka Tower Spire Raid",
          location = "High-Tier Corporate HQ",
          difficulty = "BOSS",
          recommendedPower = 950,
          rewardCredits = 1000,
          rewardData = 550,
          rewardXp = 300,
          intel = "Infiltrate the executive penthouse and neutralize Commander Varrus before the lockdown.",
          isUnlocked = false,
          starsEarned = 0,
          isBossEncounter = true,
          bossName = "Commander Varrus"
        ),
        MissionEntity(
          id = "mission_104",
          chapter = 2,
          missionNumber = 1,
          title = "Orbital Data Array Breach",
          location = "Low Earth Orbit Facility",
          difficulty = "HARD",
          recommendedPower = 1350,
          rewardCredits = 1400,
          rewardData = 700,
          rewardXp = 450,
          intel = "Overload the neural satellite uplink and capture master tactical AI protocols.",
          isUnlocked = false,
          starsEarned = 0,
          isBossEncounter = false
        ),
        MissionEntity(
          id = "mission_105",
          chapter = 2,
          missionNumber = 2,
          title = "Nexus AI Core Overdrive",
          location = "Deep Sub-Core Server 01",
          difficulty = "BOSS",
          recommendedPower = 1800,
          rewardCredits = 2500,
          rewardData = 1200,
          rewardXp = 800,
          intel = "Face the Sovereign Tactical Overlord AI in the ultimate cyber battle of attrition.",
          isUnlocked = false,
          starsEarned = 0,
          isBossEncounter = true,
          bossName = "Rogue Sovereign AI Core"
        )
      )
      gameDao.insertMissions(defaultMissions)

      // 5. Seed Arcade Scores
      val defaultArcadeScores = listOf(
        ArcadeScoreEntity(playerName = "Commander Oi", score = 14800, maxCombo = 28, difficulty = "Overdrive", earnedCredits = 740, earnedData = 296),
        ArcadeScoreEntity(playerName = "Top Operative", score = 12400, maxCombo = 24, difficulty = "Overdrive", earnedCredits = 620, earnedData = 248),
        ArcadeScoreEntity(playerName = "Jax 'Aegis'", score = 8900, maxCombo = 18, difficulty = "Standard", earnedCredits = 445, earnedData = 178),
        ArcadeScoreEntity(playerName = "Zero 'Ghost'", score = 6200, maxCombo = 12, difficulty = "Standard", earnedCredits = 310, earnedData = 124)
      )
      defaultArcadeScores.forEach { gameDao.insertArcadeScore(it) }
    }
  }

  suspend fun toggleOperativeDeployment(operativeId: String) {
    val operative = gameDao.getOperativeById(operativeId) ?: return
    gameDao.updateOperative(operative.copy(isDeployed = !operative.isDeployed))
  }

  suspend fun levelUpOperative(operativeId: String): Boolean {
    val operative = gameDao.getOperativeById(operativeId) ?: return false
    val profile = gameDao.getPlayerProfile().firstOrNull() ?: return false
    val creditCost = operative.level * 200
    val dataCost = operative.level * 80

    if (profile.cyberCredits < creditCost || profile.tacticalData < dataCost) {
      return false
    }

    // Deduct currencies
    gameDao.updatePlayerProfile(
      profile.copy(
        cyberCredits = profile.cyberCredits - creditCost,
        tacticalData = profile.tacticalData - dataCost
      )
    )

    // Upgrade operative stats
    val newLevel = operative.level + 1
    val updated = operative.copy(
      level = newLevel,
      baseHp = (operative.baseHp * 1.12f).toInt(),
      baseAtk = (operative.baseAtk * 1.12f).toInt(),
      baseDef = (operative.baseDef * 1.10f).toInt(),
      currentXp = 0,
      maxXp = (operative.maxXp * 1.25f).toInt()
    )
    gameDao.updateOperative(updated)
    return true
  }

  suspend fun equipItem(operativeId: String, itemId: String) {
    val operative = gameDao.getOperativeById(operativeId) ?: return
    val items = gameDao.getAllItems().firstOrNull() ?: return
    val targetItem = items.find { it.id == itemId } ?: return

    // Unequip currently equipped item of this type on this operative if exists
    val currentEquippedId = when (ItemType.fromString(targetItem.itemType)) {
      ItemType.WEAPON -> operative.equippedWeaponId
      ItemType.ARMOR -> operative.equippedArmorId
      ItemType.NANITE_CORE -> operative.equippedCoreId
      ItemType.CIPHER_CHIP -> operative.equippedChipId
    }

    if (currentEquippedId != null) {
      val oldItem = items.find { it.id == currentEquippedId }
      if (oldItem != null) {
        gameDao.updateItem(oldItem.copy(isEquipped = false, equippedToOperativeId = null))
      }
    }

    // Unequip target item from any previous owner
    if (targetItem.isEquipped && targetItem.equippedToOperativeId != null && targetItem.equippedToOperativeId != operativeId) {
      val prevOwner = gameDao.getOperativeById(targetItem.equippedToOperativeId)
      if (prevOwner != null) {
        val unequippedOwner = when (ItemType.fromString(targetItem.itemType)) {
          ItemType.WEAPON -> prevOwner.copy(equippedWeaponId = null)
          ItemType.ARMOR -> prevOwner.copy(equippedArmorId = null)
          ItemType.NANITE_CORE -> prevOwner.copy(equippedCoreId = null)
          ItemType.CIPHER_CHIP -> prevOwner.copy(equippedChipId = null)
        }
        gameDao.updateOperative(unequippedOwner)
      }
    }

    // Equip to current operative
    gameDao.updateItem(targetItem.copy(isEquipped = true, equippedToOperativeId = operativeId))
    val updatedOperative = when (ItemType.fromString(targetItem.itemType)) {
      ItemType.WEAPON -> operative.copy(equippedWeaponId = itemId)
      ItemType.ARMOR -> operative.copy(equippedArmorId = itemId)
      ItemType.NANITE_CORE -> operative.copy(equippedCoreId = itemId)
      ItemType.CIPHER_CHIP -> operative.copy(equippedChipId = itemId)
    }
    gameDao.updateOperative(updatedOperative)
  }

  suspend fun unequipItem(itemId: String) {
    val items = gameDao.getAllItems().firstOrNull() ?: return
    val item = items.find { it.id == itemId } ?: return
    val ownerId = item.equippedToOperativeId

    gameDao.updateItem(item.copy(isEquipped = false, equippedToOperativeId = null))

    if (ownerId != null) {
      val operative = gameDao.getOperativeById(ownerId)
      if (operative != null) {
        val updated = when (ItemType.fromString(item.itemType)) {
          ItemType.WEAPON -> operative.copy(equippedWeaponId = null)
          ItemType.ARMOR -> operative.copy(equippedArmorId = null)
          ItemType.NANITE_CORE -> operative.copy(equippedCoreId = null)
          ItemType.CIPHER_CHIP -> operative.copy(equippedChipId = null)
        }
        gameDao.updateOperative(updated)
      }
    }
  }

  suspend fun forgeItem(rarity: ItemRarity, itemType: ItemType): ItemEntity? {
    val profile = gameDao.getPlayerProfile().firstOrNull() ?: return null
    if (profile.cyberCredits < rarity.forgeCreditCost || profile.tacticalData < rarity.forgeDataCost) {
      return null
    }

    // Deduct forge costs
    gameDao.updatePlayerProfile(
      profile.copy(
        cyberCredits = profile.cyberCredits - rarity.forgeCreditCost,
        tacticalData = profile.tacticalData - rarity.forgeDataCost
      )
    )

    // Generate stats based on rarity and type
    val multiplier = rarity.statMultiplier
    val baseStat = Random.nextInt(15, 25)
    val atk = if (itemType == ItemType.WEAPON || itemType == ItemType.NANITE_CORE || itemType == ItemType.CIPHER_CHIP) (baseStat * multiplier).toInt() else 0
    val def = if (itemType == ItemType.ARMOR || itemType == ItemType.NANITE_CORE || itemType == ItemType.CIPHER_CHIP) ((baseStat - 4).coerceAtLeast(6) * multiplier).toInt() else 0
    val hp = if (itemType == ItemType.ARMOR || itemType == ItemType.NANITE_CORE) (baseStat * 3 * multiplier).toInt() else 0
    val crit = if (itemType == ItemType.WEAPON || itemType == ItemType.CIPHER_CHIP) (0.05f * multiplier) else 0.0f

    val prefixes = listOf("Quantum", "Vortex", "Apex", "Singularity", "Shadow", "Chrono", "Hyper", "Specter")
    val nouns = when (itemType) {
      ItemType.WEAPON -> listOf("Thermal Edge", "Plasma Katana", "Pulse Cannon", "Viper Rifle")
      ItemType.ARMOR -> listOf("Carbon Weave", "Kinetic Plate", "Aegis Cloak", "Refractor Rig")
      ItemType.NANITE_CORE -> listOf("Overclock Matrix", "Flux Reactor", "Synapse Node", "Warp Core")
      ItemType.CIPHER_CHIP -> listOf("Logic Array", "Breaker Subroutine", "Ghost Key", "Zero Protocol")
    }

    val name = "${prefixes.random()} ${nouns.random()}"
    val newItem = ItemEntity(
      id = "item_${UUID.randomUUID().toString().take(8)}",
      name = name,
      itemType = itemType.name,
      rarity = rarity.name,
      atkBonus = atk,
      defBonus = def,
      hpBonus = hp,
      critBonus = crit,
      isEquipped = false,
      equippedToOperativeId = null,
      flavorText = "Synthesized in the Nanite Forge with $rarity tier tactical matrices."
    )

    gameDao.insertItem(newItem)
    return newItem
  }

  suspend fun purchaseStoreItem(
    creditsGranted: Int,
    dataGranted: Int,
    isVip: Boolean = false,
    bonusItem: ItemEntity? = null
  ) {
    val profile = gameDao.getPlayerProfile().firstOrNull() ?: return
    val updated = profile.copy(
      cyberCredits = profile.cyberCredits + creditsGranted,
      tacticalData = profile.tacticalData + dataGranted,
      isVipPassActive = if (isVip) true else profile.isVipPassActive
    )
    gameDao.updatePlayerProfile(updated)

    if (bonusItem != null) {
      gameDao.insertItem(bonusItem)
    }
  }

  suspend fun recordArcadeGame(score: Int, maxCombo: Int, difficulty: String) {
    val earnedCredits = (score * 0.05f).toInt().coerceAtLeast(20)
    val earnedData = (score * 0.02f).toInt().coerceAtLeast(10)

    val arcadeScore = ArcadeScoreEntity(
      playerName = "Commander Oi",
      score = score,
      maxCombo = maxCombo,
      difficulty = difficulty,
      earnedCredits = earnedCredits,
      earnedData = earnedData
    )
    gameDao.insertArcadeScore(arcadeScore)

    val profile = gameDao.getPlayerProfile().firstOrNull() ?: return
    val updated = profile.copy(
      cyberCredits = profile.cyberCredits + earnedCredits,
      tacticalData = profile.tacticalData + earnedData,
      totalDronesNeutralized = profile.totalDronesNeutralized + (score / 100)
    )
    gameDao.updatePlayerProfile(updated)
  }

  suspend fun completeMission(missionId: String, stars: Int): Boolean {
    val mission = gameDao.getMissionById(missionId) ?: return false
    val profile = gameDao.getPlayerProfile().firstOrNull() ?: return false

    // Update mission status
    val updatedMission = mission.copy(
      starsEarned = maxOf(mission.starsEarned, stars)
    )
    gameDao.updateMission(updatedMission)

    // Unlock next mission
    val all = gameDao.getAllMissions().firstOrNull() ?: emptyList()
    val currentIndex = all.indexOfFirst { it.id == missionId }
    if (currentIndex != -1 && currentIndex + 1 < all.size) {
      val nextMission = all[currentIndex + 1]
      gameDao.updateMission(nextMission.copy(isUnlocked = true))
    }

    // Grant player rewards
    val updatedProfile = profile.copy(
      cyberCredits = profile.cyberCredits + mission.rewardCredits,
      tacticalData = profile.tacticalData + mission.rewardData,
      playerXp = profile.playerXp + mission.rewardXp,
      totalBattlesWon = profile.totalBattlesWon + 1
    )
    gameDao.updatePlayerProfile(updatedProfile)

    // Also grant XP to deployed operatives
    val deployed = gameDao.getDeployedOperatives().firstOrNull() ?: emptyList()
    deployed.forEach { op ->
      val newXp = op.currentXp + mission.rewardXp
      if (newXp >= op.maxXp) {
        val newLvl = op.level + 1
        gameDao.updateOperative(
          op.copy(
            level = newLvl,
            currentXp = newXp - op.maxXp,
            maxXp = (op.maxXp * 1.25f).toInt(),
            baseHp = (op.baseHp * 1.12f).toInt(),
            baseAtk = (op.baseAtk * 1.12f).toInt(),
            baseDef = (op.baseDef * 1.10f).toInt()
          )
        )
      } else {
        gameDao.updateOperative(op.copy(currentXp = newXp))
      }
    }
    return true
  }

  suspend fun claimVideoReward(amount: Int = 50) {
    val profile = gameDao.getPlayerProfile().firstOrNull() ?: return
    gameDao.updatePlayerProfile(profile.copy(cyberCredits = profile.cyberCredits + amount))
  }
}
