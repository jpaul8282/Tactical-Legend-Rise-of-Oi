package com.example.ui.navigation

sealed class Screen(val route: String, val title: String, val icon: String) {
  object WarRoom : Screen("war_room", "War Room", "🛡️")
  object Squad : Screen("squad", "Oi Squad", "👥")
  object Campaign : Screen("campaign", "Campaign", "🗺️")
  object Armory : Screen("armory", "Armory & Forge", "🔬")
  object Arcade : Screen("arcade", "Cyber Arcade", "👾")
  object Store : Screen("store", "Black Market", "🛒")
  object Cinema : Screen("cinema", "Holo Cinema", "🎬")
  object Combat : Screen("combat/{missionId}", "Tactical Combat", "⚔️") {
    fun createRoute(missionId: String) = "combat/$missionId"
  }
}
