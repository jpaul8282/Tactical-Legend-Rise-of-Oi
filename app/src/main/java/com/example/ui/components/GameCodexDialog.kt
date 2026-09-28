package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.audio.SoundManager
import com.example.ui.theme.*

enum class CodexTab(val title: String, val icon: String) {
  LORE("STORY & LORE", "📜"),
  RULES("COMBAT GUIDE", "⚔️"),
  OPERATIVES("OPERATIVES", "👥"),
  ENEMIES("ENEMY INTEL", "🤖"),
  FEATURES("SYSTEMS", "🔬")
}

/**
 * High-tech Cyberpunk Game Dossier & Story Codex Dialog.
 * Provides complete game description, world narrative, tactical combat rules,
 * operative classes, enemy threat matrix, and facility systems.
 */
@Composable
fun GameCodexDialog(
  onDismiss: () -> Unit
) {
  var selectedTab by remember { mutableStateOf(CodexTab.LORE) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.85f))
        .padding(horizontal = 12.dp, vertical = 24.dp),
      contentAlignment = Alignment.Center
    ) {
      CyberCard(
        borderColor = NeonCyan,
        glowColor = NeonCyan,
        backgroundColor = CyberSurface,
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight(0.92f)
          .testTag("game_codex_dialog")
      ) {
        // Dialog Top Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "💾", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
            Column {
              Text(
                text = "GAME DOSSIER // CODEX",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
              )
              Text(
                text = "TACTICAL LEGEND: RISE OF OI // CLASSIFIED INTEL",
                style = MaterialTheme.typography.labelSmall,
                color = MatrixGreen,
                fontSize = 10.sp
              )
            }
          }

          IconButton(
            onClick = {
              SoundManager.playButtonClick()
              onDismiss()
            },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close Codex",
              tint = TextMuted
            )
          }
        }

        // Tab Selector Row
        ScrollableTabRow(
          selectedTabIndex = selectedTab.ordinal,
          containerColor = CyberSurfaceVariant,
          contentColor = NeonCyan,
          edgePadding = 4.dp,
          indicator = {},
          divider = {},
          modifier = Modifier
            .fillMaxWidth()
            .clip(CutCornerShape(4.dp))
            .border(1.dp, BorderGlow, CutCornerShape(4.dp))
        ) {
          CodexTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            Tab(
              selected = isSelected,
              onClick = {
                SoundManager.playButtonClick()
                selectedTab = tab
              },
              modifier = Modifier
                .background(if (isSelected) NeonCyan.copy(alpha = 0.18f) else Color.Transparent)
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Text(text = tab.icon, fontSize = 14.sp)
                Text(
                  text = tab.title,
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isSelected) NeonCyan else TextMuted,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                  fontSize = 11.sp
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content Area Based on Tab
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
        ) {
          when (selectedTab) {
            CodexTab.LORE -> LoreSection()
            CodexTab.RULES -> RulesSection()
            CodexTab.OPERATIVES -> OperativesSection()
            CodexTab.ENEMIES -> EnemiesSection()
            CodexTab.FEATURES -> FeaturesSection()
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Bottom Dismiss Button
        CyberButton(
          text = "CLOSE DOSSIER",
          onClick = {
            SoundManager.playButtonClick()
            onDismiss()
          },
          primaryColor = NeonCyan,
          modifier = Modifier.fillMaxWidth(),
          testTag = "codex_close_button"
        )
      }
    }
  }
}

@Composable
private fun LoreSection() {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      // Visual Header Banner
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(130.dp)
          .clip(CutCornerShape(6.dp))
          .border(1.dp, BorderGlow, CutCornerShape(6.dp))
      ) {
        Image(
          painter = painterResource(id = R.drawable.playstore_tactical_briefing_1790618367880),
          contentDescription = "Tactical Briefing",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                listOf(Color.Transparent, CyberSurface.copy(alpha = 0.95f))
              )
            )
        )
        Column(
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(10.dp)
        ) {
          CyberBadge(text = "NEO-KYOTO // 2088 AD", color = CyberGold)
          Text(
            text = "TACTICAL LEGEND: RISE OF OI",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
          )
          Text(
            text = "Lead the Cyber-Rebellion. Command the Grid. Overthrow the Megacorps.",
            style = MaterialTheme.typography.labelSmall,
            color = NeonCyan,
            fontSize = 11.sp
          )
        }
      }
    }

    item {
      CyberCard(
        borderColor = NeonCyan,
        backgroundColor = CyberSurfaceVariant.copy(alpha = 0.5f)
      ) {
        Text(
          text = "THE SETTING & SYNOPSIS",
          style = MaterialTheme.typography.titleSmall,
          color = NeonCyan,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = stringResource(id = R.string.game_story_synopsis),
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
          fontSize = 12.sp,
          lineHeight = 18.sp
        )
      }
    }

    item {
      CyberCard(
        borderColor = MatrixGreen,
        backgroundColor = CyberSurfaceVariant.copy(alpha = 0.5f)
      ) {
        Text(
          text = "THE 'OI' RESISTANCE CELL",
          style = MaterialTheme.typography.titleSmall,
          color = MatrixGreen,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Born in the neon underbelly of the city, 'Oi' is an underground coalition of rogue cyborg mercenaries, outlaw netrunners, disavowed combat medics, and exiled cyber-samurai. United under your command, 'Oi' infiltrates corporate fortresses to liberate stolen biometric data cores and cripple the corporate war machine.",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
          fontSize = 12.sp,
          lineHeight = 18.sp
        )
      }
    }

    item {
      CyberCard(
        borderColor = NeonCrimson,
        backgroundColor = CyberSurfaceVariant.copy(alpha = 0.5f)
      ) {
        Text(
          text = "THE CORPORATE ARASAKA HEGEMONY",
          style = MaterialTheme.typography.titleSmall,
          color = NeonCrimson,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Arasaka Security Cartels enforce total control using automated sentry drones, cybernetic shock troops, and ruthless commanders. Commander Varrus pilots the colossal Apex War Titan mech, orchestrating sector-wide purges against all insurgent activity.",
          style = MaterialTheme.typography.bodyMedium,
          color = TextSecondary,
          fontSize = 12.sp,
          lineHeight = 18.sp
        )
      }
    }
  }
}

@Composable
private fun RulesSection() {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(110.dp)
          .clip(CutCornerShape(6.dp))
          .border(1.dp, BorderGlow, CutCornerShape(6.dp))
      ) {
        Image(
          painter = painterResource(id = R.drawable.playstore_grid_combat_1790618340061),
          contentDescription = "Grid Combat",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize()
        )
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                listOf(Color.Transparent, CyberSurface.copy(alpha = 0.9f))
              )
            )
        )
        Text(
          text = "TACTICAL GRID COMBAT PROTOCOLS",
          style = MaterialTheme.typography.titleMedium,
          color = NeonCyan,
          fontWeight = FontWeight.Bold,
          modifier = Modifier
            .align(Alignment.BottomStart)
            .padding(10.dp)
        )
      }
    }

    val combatRules = listOf(
      Triple(
        "10x10 ISOMETRIC GRID",
        "Engagements take place on an isometric 10x10 map with coordinate-based positioning (A-J, 1-10). Tile terrain includes high vantage points, plasma hazard zones, and defensive cover.",
        NeonCyan
      ),
      Triple(
        "ACTION POINT (AP) ECONOMY",
        "Every operative begins their turn with 4 Action Points (AP). Moving costs 1 AP per tile. Standard attacks cost 3 AP. Powerful special abilities cost 4 AP. Plan your turns meticulously.",
        MatrixGreen
      ),
      Triple(
        "SHIELDS & DAMAGE ABSORPTION",
        "Units possess rechargeable energy shields. Incoming attacks are 100% absorbed by active shields before compromising the unit's core HP. Break enemy shields to inflict lethal hull damage.",
        ElectricBlue
      ),
      Triple(
        "CRITICAL STRIKES & BLADE SLASHES",
        "Striking from advantageous positions or wielding precision weaponry triggers critical strikes (1.5x damage). Melee Samurai attacks execute lethal blade slashes with kinetic particle bursts.",
        CyberGold
      ),
      Triple(
        "KINETIC FX & COMBAT BADGES",
        "Every attack triggers real-time visual shockwaves, radial spark shards, and color-coded damage badges indicating critical strikes, shield breaks, blade slashes, and EMP stuns.",
        NeonCrimson
      )
    )

    combatRules.forEach { (title, description, color) ->
      item {
        CyberCard(
          borderColor = color.copy(alpha = 0.6f),
          backgroundColor = CyberSurfaceVariant.copy(alpha = 0.4f)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = title,
              style = MaterialTheme.typography.titleSmall,
              color = color,
              fontWeight = FontWeight.Bold
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
          )
        }
      }
    }
  }
}

@Composable
private fun OperativesSection() {
  val operatives = listOf(
    OperativeCodexItem(
      name = "Jax Vane",
      heroClass = "VANGUARD",
      role = "Heavy Defense / Assault Tank",
      portraitRes = UnitPortraits.TACTICAL_OPERATIVE,
      badgeColor = ElectricBlue,
      stats = "HP: 160 | Shield: 60 | Move: 3 | Range: 2",
      ability = "Plasma Barrier: Deploys an overcharged energy shield, adding +60 shield points instantly.",
      description = "Frontline juggernaut clad in reinforced titanium power armor. Specializes in drawing hostile fire and holding choke points."
    ),
    OperativeCodexItem(
      name = "Cipher Nyx",
      heroClass = "CIPHER / NETRUNNER",
      role = "Deep-Net Electronic Warfare",
      portraitRes = UnitPortraits.CYBER_NETRUNNER,
      badgeColor = ElectricPurple,
      stats = "HP: 110 | Shield: 40 | Move: 4 | Range: 4",
      ability = "EMP Overload: Infiltrates enemy drone logic, stunning targets and neutralizing shields.",
      description = "Prodigy hacker equipped with neural cyberdecks. Capable of disabling automated sentries and intercepting corporate transmissions."
    ),
    OperativeCodexItem(
      name = "Ghost Chen",
      heroClass = "SNIPER",
      role = "Extreme Range Precision Recon",
      portraitRes = UnitPortraits.TACTICAL_OPERATIVE,
      badgeColor = MatrixGreen,
      stats = "HP: 100 | Shield: 30 | Move: 3 | Range: 6",
      ability = "Deadly Precision: Charges magnetic rails for a guaranteed critical strike with armor piercing.",
      description = "Ex-corporate marksman equipped with a high-caliber anti-materiel railgun. Dominates long sightlines."
    ),
    OperativeCodexItem(
      name = "Dr. Aris Vance",
      heroClass = "MEDIC",
      role = "Nanite Biogel Support",
      portraitRes = UnitPortraits.CYBER_NETRUNNER,
      badgeColor = NeonCyan,
      stats = "HP: 120 | Shield: 45 | Move: 3 | Range: 3",
      ability = "Nanite Revive: Deploys airborne medical nanobots that restore +50 HP to damaged squadmates.",
      description = "Former biomedical researcher who created self-replicating healing nanites to sustain field operatives in hostile zones."
    ),
    OperativeCodexItem(
      name = "Zero Kai",
      heroClass = "SAMURAI",
      role = "High-Mobility Cyber-Infiltrator",
      portraitRes = UnitPortraits.TACTICAL_OPERATIVE,
      badgeColor = CyberGold,
      stats = "HP: 130 | Shield: 35 | Move: 5 | Range: 1",
      ability = "Shadow Slash: Dashes through defenses, striking with an energized monomolecular katana.",
      description = "Swift urban ghost armed with high-frequency blades. Flanks entrenched positions and eliminates priority targets."
    )
  )

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    items(operatives.size) { index ->
      val op = operatives[index]
      CyberCard(
        borderColor = op.badgeColor.copy(alpha = 0.7f),
        backgroundColor = CyberSurfaceVariant.copy(alpha = 0.5f)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Image(
            painter = painterResource(id = op.portraitRes),
            contentDescription = op.name,
            modifier = Modifier
              .size(60.dp)
              .clip(CutCornerShape(4.dp))
              .border(1.dp, op.badgeColor, CutCornerShape(4.dp)),
            contentScale = ContentScale.Crop
          )
          Column(modifier = Modifier.weight(1f)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = op.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
              )
              CyberBadge(text = op.heroClass, color = op.badgeColor)
            }
            Text(
              text = op.role,
              style = MaterialTheme.typography.labelSmall,
              color = op.badgeColor,
              fontWeight = FontWeight.SemiBold
            )
            Text(
              text = op.stats,
              style = MaterialTheme.typography.labelSmall,
              color = TextMuted,
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp
            )
          }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = op.description,
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary,
          fontSize = 11.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "⚡ ${op.ability}",
          style = MaterialTheme.typography.labelSmall,
          color = CyberGold,
          fontSize = 10.sp
        )
      }
    }
  }
}

@Composable
private fun EnemiesSection() {
  val enemies = listOf(
    EnemyCodexItem(
      name = "Aegis Sentinel Drone",
      faction = "ARASAKA AIR PATROL",
      threatLevel = "TIER I // RECON & HARASSMENT",
      portraitRes = UnitPortraits.DRONE_STRIKER,
      color = NeonCyan,
      stats = "HP: 60 | Shield: 20 | Move: 5 | Range: 3",
      tactics = "Autonomous quad-rotor attack drone. Moves quickly across the battlefield to surround isolated squad members. Vulnerable to Netrunner EMP pulses."
    ),
    EnemyCodexItem(
      name = "Cyborg Shock Enforcer",
      faction = "ARASAKA SECURITY SYNDICATE",
      threatLevel = "TIER II // HEAVY ASSAULT",
      portraitRes = UnitPortraits.CYBORG_ENFORCER,
      color = NeonCrimson,
      stats = "HP: 110 | Shield: 40 | Move: 3 | Range: 3",
      tactics = "Heavily augmented shock trooper wielding a rapid-fire rotary laser cannon. Advanced armor plating reduces incoming kinetic damage. Flank with Samurai."
    ),
    EnemyCodexItem(
      name = "Commander Varrus / Apex War Titan",
      faction = "HIGH COMMAND // ARASAKA SUPREMACY",
      threatLevel = "TIER III // SECTOR BOSS TITAN",
      portraitRes = UnitPortraits.APEX_TITAN_BOSS,
      color = CyberGold,
      stats = "HP: 350 | Shield: 120 | Move: 3 | Range: 5",
      tactics = "Colossal bipedal war mech commanded by Varrus. Features devastating multi-cell missile salvos, impenetrable phase barriers, and seismic stomps. Requires full squad coordination."
    )
  )

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    items(enemies.size) { index ->
      val enemy = enemies[index]
      CyberCard(
        borderColor = enemy.color.copy(alpha = 0.7f),
        backgroundColor = CyberSurfaceVariant.copy(alpha = 0.5f)
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Image(
            painter = painterResource(id = enemy.portraitRes),
            contentDescription = enemy.name,
            modifier = Modifier
              .size(64.dp)
              .clip(CutCornerShape(4.dp))
              .border(1.dp, enemy.color, CutCornerShape(4.dp)),
            contentScale = ContentScale.Crop
          )
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = enemy.name,
              style = MaterialTheme.typography.titleMedium,
              color = enemy.color,
              fontWeight = FontWeight.Bold
            )
            CyberBadge(text = enemy.threatLevel, color = enemy.color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = enemy.stats,
              style = MaterialTheme.typography.labelSmall,
              color = TextMuted,
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp
            )
          }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "TACTICAL COUNTERMEASURES:",
          style = MaterialTheme.typography.labelSmall,
          color = CyberGold,
          fontWeight = FontWeight.Bold,
          fontSize = 10.sp
        )
        Text(
          text = enemy.tactics,
          style = MaterialTheme.typography.bodySmall,
          color = TextSecondary,
          fontSize = 11.sp,
          lineHeight = 16.sp
        )
      }
    }
  }
}

@Composable
private fun FeaturesSection() {
  val features = listOf(
    Triple(
      "NANITE FORGE & ARMORY",
      "Harness synthesized nanite isotopes to construct, upgrade, and optimize cutting-edge tactical rifles, ion shotguns, vibro-blades, and ballistic shielding.",
      NeonCyan
    ),
    Triple(
      "5 SECTOR CAMPAIGN MAP",
      "Liberate five contested city sectors including Neo-Kyoto Slums, Industrial Sub-Level, Corporate Skyline, and Arasaka Citadel across increasing difficulty tiers.",
      NeonCrimson
    ),
    Triple(
      "SUB-ETHER CYBER ARCADE",
      "Engage in an intense drone interception mini-game within the sub-ether simulation deck to sharpen reflexes and earn bonus high-score tactical data.",
      MatrixGreen
    ),
    Triple(
      "BLACK MARKET & REQUISITIONS",
      "Trade surplus cyber-credits for smuggled corporate prototype tech, rare enhancement chips, and VIP clearance passes.",
      CyberGold
    ),
    Triple(
      "HOLO CINEMA BRIEFINGS",
      "Review classified audiovisual surveillance logs and intercepted holographic communiques to stay ahead of enemy troop movements.",
      ElectricPurple
    )
  )

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(10.dp)
  ) {
    features.forEach { (title, description, color) ->
      item {
        CyberCard(
          borderColor = color.copy(alpha = 0.6f),
          backgroundColor = CyberSurfaceVariant.copy(alpha = 0.4f)
        ) {
          Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = color,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 16.sp
          )
        }
      }
    }
  }
}

private data class OperativeCodexItem(
  val name: String,
  val heroClass: String,
  val role: String,
  val portraitRes: Int,
  val badgeColor: Color,
  val stats: String,
  val ability: String,
  val description: String
)

private data class EnemyCodexItem(
  val name: String,
  val faction: String,
  val threatLevel: String,
  val portraitRes: Int,
  val color: Color,
  val stats: String,
  val tactics: String
)
