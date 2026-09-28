package com.example.ui.components

import com.example.R
import com.example.data.model.HeroClass

/**
 * High-definition visual asset mappings for playable Operatives and Enemy factions.
 */
object UnitPortraits {
  val TACTICAL_OPERATIVE = R.drawable.img_char_tactical_operative_1790619050031
  val CYBER_NETRUNNER = R.drawable.img_char_cyber_netrunner_1790619064344
  val DRONE_STRIKER = R.drawable.img_enemy_drone_striker_1790619081101
  val CYBORG_ENFORCER = R.drawable.img_enemy_cyborg_enforcer_1790619094942
  val APEX_TITAN_BOSS = R.drawable.img_enemy_apex_titan_1790619109989

  fun getOperativePortrait(heroClass: HeroClass?, name: String = ""): Int {
    return when (heroClass) {
      HeroClass.CIPHER, HeroClass.MEDIC -> CYBER_NETRUNNER
      HeroClass.VANGUARD, HeroClass.SNIPER, HeroClass.SAMURAI -> TACTICAL_OPERATIVE
      null -> if (name.contains("Ghost", true) || name.contains("Nyx", true) || name.contains("Chen", true)) CYBER_NETRUNNER else TACTICAL_OPERATIVE
    }
  }

  fun getOperativePortraitByClassName(heroClassName: String, name: String = ""): Int {
    return when (heroClassName.uppercase()) {
      "CIPHER", "MEDIC" -> CYBER_NETRUNNER
      "VANGUARD", "SNIPER", "SAMURAI" -> TACTICAL_OPERATIVE
      else -> if (name.contains("Ghost", true) || name.contains("Nyx", true) || name.contains("Chen", true)) CYBER_NETRUNNER else TACTICAL_OPERATIVE
    }
  }
}
