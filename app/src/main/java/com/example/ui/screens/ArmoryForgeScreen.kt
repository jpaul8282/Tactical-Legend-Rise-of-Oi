package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SoundManager
import com.example.data.model.ItemRarity
import com.example.data.model.ItemType
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@Composable
fun ArmoryForgeScreen(
  viewModel: GameViewModel
) {
  val uiState by viewModel.uiState.collectAsState()
  var selectedTab by remember { mutableStateOf(0) } // 0 = Armory, 1 = Forge
  var selectedFilterType by remember { mutableStateOf<ItemType?>(null) }

  // Forge Synthesizer State
  var forgeType by remember { mutableStateOf(ItemType.WEAPON) }
  var forgeRarity by remember { mutableStateOf(ItemRarity.RARE) }

  val profile = uiState.playerProfile

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBackground)
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 14.dp)
    ) {
      // Header & Tabs
      Spacer(modifier = Modifier.height(12.dp))
      CyberCard(borderColor = NeonCyan, modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = if (selectedTab == 0) "CYBER ARMORY VAULT" else "NANITE FORGE SYNTHESIZER",
              style = MaterialTheme.typography.titleMedium,
              color = NeonCyan,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Credits: ${profile?.cyberCredits ?: 0} CR | Data: ${profile?.tacticalData ?: 0} MB",
              style = MaterialTheme.typography.bodyMedium,
              color = TextMuted,
              fontSize = 11.sp
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CyberButton(
              text = "VAULT",
              onClick = { selectedTab = 0 },
              primaryColor = if (selectedTab == 0) NeonCyan else CyberSurfaceHigh,
              textColor = if (selectedTab == 0) CyberBackground else TextPrimary,
              modifier = Modifier.defaultMinSize(minWidth = 70.dp, minHeight = 34.dp)
            )
            CyberButton(
              text = "FORGE",
              onClick = { selectedTab = 1 },
              primaryColor = if (selectedTab == 1) MatrixGreen else CyberSurfaceHigh,
              textColor = if (selectedTab == 1) CyberBackground else TextPrimary,
              modifier = Modifier.defaultMinSize(minWidth = 70.dp, minHeight = 34.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      if (selectedTab == 0) {
        // Tab 1: Armory Vault
        // Filter Chips
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          FilterChipItem(
            label = "ALL",
            isSelected = selectedFilterType == null,
            onClick = { selectedFilterType = null }
          )
          ItemType.entries.forEach { type ->
            FilterChipItem(
              label = type.label.uppercase(),
              isSelected = selectedFilterType == type,
              onClick = { selectedFilterType = type }
            )
          }
        }

        val filteredItems = if (selectedFilterType == null) {
          uiState.items
        } else {
          uiState.items.filter { it.itemType == selectedFilterType?.name }
        }

        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(bottom = 90.dp)
        ) {
          items(filteredItems) { item ->
            val rarityEnum = ItemRarity.fromString(item.rarity)
            val typeEnum = ItemType.fromString(item.itemType)
            val equippedOp = uiState.operatives.find { it.id == item.equippedToOperativeId }

            CyberCard(
              borderColor = rarityEnum.color,
              glowColor = if (rarityEnum == ItemRarity.LEGENDARY || rarityEnum == ItemRarity.EPIC) rarityEnum.color.copy(alpha = 0.4f) else null,
              backgroundColor = CyberSurface,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                  modifier = Modifier.weight(1f)
                ) {
                  Box(
                    modifier = Modifier
                      .size(42.dp)
                      .clip(CutCornerShape(4.dp))
                      .background(CyberSurfaceVariant)
                      .border(1.dp, rarityEnum.color, CutCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(text = typeEnum.icon, fontSize = 22.sp)
                  }

                  Column {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                      )
                      CyberBadge(text = rarityEnum.label, color = rarityEnum.color)
                    }

                    Text(
                      text = item.flavorText,
                      style = MaterialTheme.typography.bodyMedium,
                      color = TextMuted,
                      fontSize = 11.sp,
                      maxLines = 1
                    )

                    // Stats row
                    Row(
                      horizontalArrangement = Arrangement.spacedBy(10.dp),
                      modifier = Modifier.padding(top = 4.dp)
                    ) {
                      if (item.atkBonus > 0) Text(text = "ATK +${item.atkBonus}", color = NeonCrimson, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                      if (item.defBonus > 0) Text(text = "DEF +${item.defBonus}", color = ElectricBlue, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                      if (item.hpBonus > 0) Text(text = "HP +${item.hpBonus}", color = MatrixGreen, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                      if (item.critBonus > 0) Text(text = "CRIT +${(item.critBonus * 100).toInt()}%", color = CyberGold, style = MaterialTheme.typography.labelSmall, fontFamily = FontFamily.Monospace)
                    }
                  }
                }

                if (equippedOp != null) {
                  CyberBadge(text = "ON ${equippedOp.name.split(" ").first()}", color = MatrixGreen)
                } else {
                  CyberBadge(text = "VAULT", color = TextMuted)
                }
              }
            }
          }
        }
      } else {
        // Tab 2: Nanite Forge Synthesizer
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(12.dp),
          contentPadding = PaddingValues(bottom = 90.dp)
        ) {
          item {
            CyberCard(
              borderColor = MatrixGreen,
              glowColor = MatrixGreen.copy(alpha = 0.3f),
              backgroundColor = CyberSurface,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "1. SELECT ITEM BLUEPRINT",
                style = MaterialTheme.typography.labelLarge,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                ItemType.entries.forEach { type ->
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(CutCornerShape(4.dp))
                      .background(if (forgeType == type) MatrixGreen.copy(alpha = 0.2f) else CyberSurfaceVariant)
                      .border(1.dp, if (forgeType == type) MatrixGreen else BorderGlow, CutCornerShape(4.dp))
                      .clickable {
                        SoundManager.playButtonClick()
                        forgeType = type
                      }
                      .padding(8.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text(text = type.icon, fontSize = 20.sp)
                      Text(
                        text = type.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (forgeType == type) MatrixGreen else TextSecondary,
                        fontSize = 10.sp
                      )
                    }
                  }
                }
              }
            }
          }

          item {
            CyberCard(
              borderColor = forgeRarity.color,
              backgroundColor = CyberSurface,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "2. SELECT SYNTHESIS RARITY TIER",
                style = MaterialTheme.typography.labelLarge,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
              ) {
                ItemRarity.entries.forEach { rarity ->
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(CutCornerShape(4.dp))
                      .background(if (forgeRarity == rarity) rarity.color.copy(alpha = 0.2f) else CyberSurfaceVariant)
                      .border(1.dp, if (forgeRarity == rarity) rarity.color else BorderGlow, CutCornerShape(4.dp))
                      .clickable {
                        SoundManager.playButtonClick()
                        forgeRarity = rarity
                      }
                      .padding(8.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                      Text(
                        text = rarity.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = rarity.color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                      )
                      Text(
                        text = "${rarity.statMultiplier}x PWR",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        fontSize = 9.sp
                      )
                    }
                  }
                }
              }
            }
          }

          // Cost Summary & Synthesize Action
          item {
            val canAfford = (profile?.cyberCredits ?: 0) >= forgeRarity.forgeCreditCost && (profile?.tacticalData ?: 0) >= forgeRarity.forgeDataCost

            CyberCard(
              borderColor = if (canAfford) MatrixGreen else NeonCrimson,
              backgroundColor = CyberSurfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column {
                  Text(
                    text = "SYNTHESIS RESOURCE REQUIREMENTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                  )
                  Text(
                    text = "${forgeRarity.forgeCreditCost} Cyber Credits + ${forgeRarity.forgeDataCost} Tactical Data",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (canAfford) MatrixGreen else NeonCrimson,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                  )
                }

                CyberButton(
                  text = if (uiState.isForgeAnimating) "SYNTHESIZING..." else "FORGE ITEM",
                  onClick = {
                    SoundManager.playForgeSuccess()
                    viewModel.forgeItem(forgeRarity, forgeType)
                  },
                  primaryColor = if (canAfford) MatrixGreen else TextMuted,
                  textColor = CyberBackground,
                  enabled = canAfford && !uiState.isForgeAnimating,
                  icon = "⚡"
                )
              }
            }
          }
        }
      }
    }

    // Modal Forged Item Reveal Dialog
    if (uiState.lastForgedItem != null) {
      val item = uiState.lastForgedItem!!
      val rarity = ItemRarity.fromString(item.rarity)
      val type = ItemType.fromString(item.itemType)

      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.85f))
          .padding(20.dp),
        contentAlignment = Alignment.Center
      ) {
        CyberCard(
          borderColor = rarity.color,
          glowColor = rarity.color,
          backgroundColor = CyberSurface,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
          ) {
            Text(
              text = "SYNTHESIS COMPLETE!",
              style = MaterialTheme.typography.titleLarge,
              color = MatrixGreen,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Box(
              modifier = Modifier
                .size(64.dp)
                .clip(CutCornerShape(8.dp))
                .background(CyberSurfaceVariant)
                .border(2.dp, rarity.color, CutCornerShape(8.dp)),
              contentAlignment = Alignment.Center
            ) {
              Text(text = type.icon, fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = item.name,
              style = MaterialTheme.typography.titleLarge,
              color = TextPrimary,
              fontWeight = FontWeight.Bold
            )
            CyberBadge(text = rarity.label, color = rarity.color)

            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = item.flavorText,
              style = MaterialTheme.typography.bodyMedium,
              color = TextSecondary,
              fontSize = 12.sp
            )

            // Stat Summary Card
            CyberCard(
              borderColor = BorderGlow,
              backgroundColor = CyberSurfaceVariant,
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
              ) {
                if (item.atkBonus > 0) Text(text = "ATK +${item.atkBonus}", color = NeonCrimson, fontWeight = FontWeight.Bold)
                if (item.defBonus > 0) Text(text = "DEF +${item.defBonus}", color = ElectricBlue, fontWeight = FontWeight.Bold)
                if (item.hpBonus > 0) Text(text = "HP +${item.hpBonus}", color = MatrixGreen, fontWeight = FontWeight.Bold)
              }
            }

            Spacer(modifier = Modifier.height(8.dp))
            CyberButton(
              text = "STORE IN ARMORY VAULT",
              onClick = { viewModel.clearLastForgedItem() },
              primaryColor = NeonCyan,
              textColor = CyberBackground,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    }
  }
}

@Composable
fun FilterChipItem(
  label: String,
  isSelected: Boolean,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .clip(CutCornerShape(4.dp))
      .background(if (isSelected) NeonCyan.copy(alpha = 0.2f) else CyberSurface)
      .border(1.dp, if (isSelected) NeonCyan else BorderGlow, CutCornerShape(4.dp))
      .clickable {
        SoundManager.playButtonClick()
        onClick()
      }
      .padding(horizontal = 8.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = if (isSelected) NeonCyan else TextSecondary,
      fontWeight = FontWeight.Bold,
      fontSize = 10.sp
    )
  }
}
