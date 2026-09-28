package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.audio.SoundManager
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ArcadeViewModel
import com.example.ui.viewmodel.GameViewModel
import com.example.ui.viewmodel.TacticalCombatViewModel

class MainActivity : ComponentActivity() {
  private val gameViewModel: GameViewModel by viewModels()
  private val combatViewModel: TacticalCombatViewModel by viewModels()
  private val arcadeViewModel: ArcadeViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      TacticalLegendTheme {
        TacticalLegendApp(
          gameViewModel = gameViewModel,
          combatViewModel = combatViewModel,
          arcadeViewModel = arcadeViewModel
        )
      }
    }
  }
}

@Composable
fun TacticalLegendApp(
  gameViewModel: GameViewModel,
  combatViewModel: TacticalCombatViewModel,
  arcadeViewModel: ArcadeViewModel
) {
  val navController = rememberNavController()
  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val isCombatScreen = currentRoute?.startsWith("combat/") == true

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBackground),
    containerColor = CyberBackground,
    bottomBar = {
      if (!isCombatScreen) {
        CyberBottomNavigationBar(navController = navController, currentRoute = currentRoute)
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      NavHost(
        navController = navController,
        startDestination = Screen.WarRoom.route,
        modifier = Modifier.fillMaxSize()
      ) {
        composable(Screen.WarRoom.route) {
          WarRoomScreen(
            viewModel = gameViewModel,
            onNavigateToCampaign = { navController.navigate(Screen.Campaign.route) },
            onNavigateToSquad = { navController.navigate(Screen.Squad.route) },
            onNavigateToArmory = { navController.navigate(Screen.Armory.route) },
            onNavigateToArcade = { navController.navigate(Screen.Arcade.route) },
            onNavigateToStore = { navController.navigate(Screen.Store.route) },
            onNavigateToCinema = { navController.navigate(Screen.Cinema.route) },
            onLaunchQuickBattle = { missionId ->
              navController.navigate(Screen.Combat.createRoute(missionId))
            }
          )
        }

        composable(Screen.Squad.route) {
          SquadManagerScreen(
            viewModel = gameViewModel,
            onNavigateToCampaign = { navController.navigate(Screen.Campaign.route) }
          )
        }

        composable(Screen.Campaign.route) {
          CampaignMapScreen(
            viewModel = gameViewModel,
            onLaunchMission = { missionId ->
              navController.navigate(Screen.Combat.createRoute(missionId))
            }
          )
        }

        composable(
          route = Screen.Combat.route,
          arguments = listOf(navArgument("missionId") { type = NavType.StringType })
        ) { backStackEntry ->
          val missionId = backStackEntry.arguments?.getString("missionId") ?: "mission_101"
          TacticalCombatScreen(
            missionId = missionId,
            viewModel = combatViewModel,
            onExitCombat = { navController.popBackStack() }
          )
        }

        composable(Screen.Armory.route) {
          ArmoryForgeScreen(viewModel = gameViewModel)
        }

        composable(Screen.Arcade.route) {
          CyberArcadeScreen(
            arcadeViewModel = arcadeViewModel,
            gameViewModel = gameViewModel
          )
        }

        composable(Screen.Store.route) {
          CyberStoreScreen(viewModel = gameViewModel)
        }

        composable(Screen.Cinema.route) {
          HoloCinemaScreen(
            viewModel = gameViewModel,
            onNavigateBack = { navController.popBackStack() }
          )
        }
      }
    }
  }
}

@Composable
fun CyberBottomNavigationBar(
  navController: NavHostController,
  currentRoute: String?
) {
  val primaryItems = listOf(
    Screen.WarRoom,
    Screen.Squad,
    Screen.Campaign,
    Screen.Armory,
    Screen.Arcade,
    Screen.Store
  )

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .background(CyberSurface)
      .border(width = 1.dp, color = BorderGlow, shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
      .padding(horizontal = 4.dp, vertical = 6.dp)
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceAround,
      verticalAlignment = Alignment.CenterVertically
    ) {
      primaryItems.forEach { screen ->
        val isSelected = currentRoute == screen.route
        val activeColor = when (screen) {
          Screen.WarRoom -> NeonCyan
          Screen.Squad -> ElectricBlue
          Screen.Campaign -> NeonCrimson
          Screen.Armory -> MatrixGreen
          Screen.Arcade -> MatrixGreen
          Screen.Store -> CyberGold
          else -> NeonCyan
        }

        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier
            .clip(CutCornerShape(4.dp))
            .background(if (isSelected) activeColor.copy(alpha = 0.15f) else Color.Transparent)
            .clickable {
              if (currentRoute != screen.route) {
                SoundManager.playButtonClick()
                navController.navigate(screen.route) {
                  popUpTo(Screen.WarRoom.route) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              }
            }
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .testTag("nav_item_${screen.route}")
        ) {
          Text(text = screen.icon, fontSize = 18.sp)
          Text(
            text = screen.title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) activeColor else TextMuted,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 9.sp
          )
        }
      }
    }
  }
}

