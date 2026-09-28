package com.example.ui.screens

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
import com.example.data.db.ItemEntity
import com.example.data.db.OperativeEntity
import com.example.data.model.HeroClass
import com.example.data.model.ItemType
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel

@Composable
fun SquadManagerScreen(
  viewModel: GameViewModel,
  onNavigateToCampaign: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()
  var selectedOperative by remember { mutableStateOf<OperativeEntity?>(null) }
  var gearSlotToEquip by remember { mutableStateOf<ItemType?>(null) }

  val squadPower = viewModel.getSquadCombatPower()
  val deployedOps = uiState.operatives.filter { it.isDeployed }

  // Update selected operative reference when list changes
  LaunchedEffect(uiState.operatives) {
    if (selectedOperative != null) {
      selectedOperative = uiState.operatives.find { it.id == selectedOperative?.id }
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(CyberBackground)
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 90.dp)
    ) {
      // Header Banner & Squad Telemetry
      item {
        CyberCard(borderColor = NeonCyan, modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "OI SQUAD DEPLOYMENT MATRIX",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Active Squad: ${deployedOps.size}/4 Operatives Deployed",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
              )
            }

            CyberBadge(
              text = "PWR $squadPower",
              color = CyberGold,
              backgroundColor = CyberGold.copy(alpha = 0.15f)
            )
          }
        }
      }

      // Feedback message banner if any
      if (uiState.feedbackMessage != null) {
        item {
          CyberCard(
            borderColor = MatrixGreen,
            backgroundColor = MatrixGreen.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = uiState.feedbackMessage ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MatrixGreen,
                fontSize = 12.sp
              )
              Text(
                text = "✕",
                color = TextMuted,
                modifier = Modifier.clickable { viewModel.clearFeedback() }
              )
            }
          }
        }
      }

      // Operative Cards
      items(uiState.operatives) { op ->
        val heroCls = HeroClass.fromString(op.heroClass)
        CyberCard(
          borderColor = if (op.isDeployed) heroCls.primaryColor else BorderGlow,
          glowColor = if (op.isDeployed) heroCls.primaryColor.copy(alpha = 0.3f) else null,
          backgroundColor = if (op.isDeployed) CyberSurface else CyberSurface.copy(alpha = 0.7f),
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              SoundManager.playButtonClick()
              selectedOperative = op
            }
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Operative Info
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              val portraitRes = UnitPortraits.getOperativePortraitByClassName(op.heroClass, op.name)
              Box(
                modifier = Modifier
                  .size(48.dp)
                  .clip(CutCornerShape(4.dp))
                  .background(CyberSurfaceVariant)
                  .border(1.dp, heroCls.primaryColor, CutCornerShape(4.dp)),
                contentAlignment = Alignment.Center
              ) {
                androidx.compose.foundation.Image(
                  painter = androidx.compose.ui.res.painterResource(id = portraitRes),
                  contentDescription = op.name,
                  modifier = Modifier
                    .fillMaxSize()
                    .clip(CutCornerShape(4.dp)),
                  contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
              }

              Column {
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
                  CyberBadge(text = "LV.${op.level}", color = heroCls.primaryColor)
                }

                Text(
                  text = "${heroCls.displayName} • ${op.roleTitle}",
                  style = MaterialTheme.typography.bodyMedium,
                  color = TextSecondary,
                  fontSize = 12.sp
                )

                // Stat Chips
                Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.padding(top = 4.dp)
                ) {
                  Text(text = "HP ${op.baseHp}", style = MaterialTheme.typography.labelSmall, color = MatrixGreen, fontFamily = FontFamily.Monospace)
                  Text(text = "ATK ${op.baseAtk}", style = MaterialTheme.typography.labelSmall, color = NeonCrimson, fontFamily = FontFamily.Monospace)
                  Text(text = "DEF ${op.baseDef}", style = MaterialTheme.typography.labelSmall, color = ElectricBlue, fontFamily = FontFamily.Monospace)
                }
              }
            }

            // Deployment Toggle Switch
            Column(
              horizontalAlignment = Alignment.End,
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              CyberButton(
                text = if (op.isDeployed) "DEPLOYED" else "RESERVE",
                onClick = { viewModel.toggleOperativeDeployment(op.id) },
                primaryColor = if (op.isDeployed) MatrixGreen else TextMuted,
                textColor = if (op.isDeployed) CyberBackground else TextPrimary,
                modifier = Modifier.defaultMinSize(minWidth = 90.dp, minHeight = 36.dp)
              )
              Text(
                text = "INSPECT >",
                style = MaterialTheme.typography.labelSmall,
                color = NeonCyan,
                fontSize = 10.sp
              )
            }
          }
        }
      }
    }

    // Operative Spec Sheet Modal Dialog
    if (selectedOperative != null) {
      val op = selectedOperative!!
      val heroCls = HeroClass.fromString(op.heroClass)
      val profile = uiState.playerProfile
      val levelUpCostCredits = op.level * 200
      val levelUpCostData = op.level * 80

      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.85f))
          .padding(16.dp),
        contentAlignment = Alignment.Center
      ) {
        CyberCard(
          borderColor = heroCls.primaryColor,
          glowColor = heroCls.primaryColor,
          backgroundColor = CyberSurface,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              val detailPortrait = UnitPortraits.getOperativePortraitByClassName(op.heroClass, op.name)
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                androidx.compose.foundation.Image(
                  painter = androidx.compose.ui.res.painterResource(id = detailPortrait),
                  contentDescription = op.name,
                  modifier = Modifier
                    .size(54.dp)
                    .clip(CutCornerShape(6.dp))
                    .border(1.5.dp, heroCls.primaryColor, CutCornerShape(6.dp)),
                  contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
                Column {
                  Text(
                    text = op.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "${heroCls.displayName} • ${op.roleTitle}",
                    style = MaterialTheme.typography.labelMedium,
                    color = heroCls.primaryColor
                  )
                }
              }

              Text(
                text = "✕",
                fontSize = 20.sp,
                color = TextMuted,
                modifier = Modifier.clickable { selectedOperative = null }
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Lore Briefing
            CyberCard(
              borderColor = BorderGlow,
              backgroundColor = CyberSurfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(text = "CLASSIFIED OPERATIVE INTEL", style = MaterialTheme.typography.labelSmall, color = TextMuted)
              Text(
                text = op.loreBriefing,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Special Ability Info
            CyberCard(
              borderColor = ElectricPurple,
              backgroundColor = ElectricPurple.copy(alpha = 0.1f),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(
                  text = "TACTICAL SKILL: ${op.specialAbilityName.uppercase()}",
                  style = MaterialTheme.typography.labelMedium,
                  color = ElectricPurple,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "CD: 3 TURNS",
                  style = MaterialTheme.typography.labelSmall,
                  color = TextMuted
                )
              }
              Text(
                text = op.specialAbilityDesc,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimary,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Equipped Gear Slots
            Text(
              text = "TACTICAL GEAR LOADOUT",
              style = MaterialTheme.typography.labelSmall,
              color = TextSecondary,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              // Weapon Slot
              GearSlotButton(
                itemType = ItemType.WEAPON,
                itemId = op.equippedWeaponId,
                items = uiState.items,
                modifier = Modifier.weight(1f),
                onClick = { gearSlotToEquip = ItemType.WEAPON }
              )
              // Armor Slot
              GearSlotButton(
                itemType = ItemType.ARMOR,
                itemId = op.equippedArmorId,
                items = uiState.items,
                modifier = Modifier.weight(1f),
                onClick = { gearSlotToEquip = ItemType.ARMOR }
              )
              // Core Slot
              GearSlotButton(
                itemType = ItemType.NANITE_CORE,
                itemId = op.equippedCoreId,
                items = uiState.items,
                modifier = Modifier.weight(1f),
                onClick = { gearSlotToEquip = ItemType.NANITE_CORE }
              )
              // Chip Slot
              GearSlotButton(
                itemType = ItemType.CIPHER_CHIP,
                itemId = op.equippedChipId,
                items = uiState.items,
                modifier = Modifier.weight(1f),
                onClick = { gearSlotToEquip = ItemType.CIPHER_CHIP }
              )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Level Up Promotion Action
            CyberButton(
              text = "PROMOTE TO LV.${op.level + 1} ($levelUpCostCredits CR / $levelUpCostData DATA)",
              onClick = {
                SoundManager.playLevelUp()
                viewModel.levelUpOperative(op.id)
              },
              primaryColor = NeonCyan,
              textColor = CyberBackground,
              enabled = (profile?.cyberCredits ?: 0) >= levelUpCostCredits && (profile?.tacticalData ?: 0) >= levelUpCostData,
              modifier = Modifier.fillMaxWidth(),
              icon = "⬆️"
            )
          }
        }
      }
    }

    // Select Gear from Inventory Modal
    if (gearSlotToEquip != null && selectedOperative != null) {
      val slotType = gearSlotToEquip!!
      val candidateItems = uiState.items.filter { it.itemType == slotType.name }

      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.90f))
          .padding(20.dp),
        contentAlignment = Alignment.Center
      ) {
        CyberCard(
          borderColor = NeonCyan,
          backgroundColor = CyberSurface,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "EQUIP ${slotType.label.uppercase()}",
                style = MaterialTheme.typography.titleMedium,
                color = NeonCyan,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "✕",
                fontSize = 18.sp,
                color = TextMuted,
                modifier = Modifier.clickable { gearSlotToEquip = null }
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (candidateItems.isEmpty()) {
              Text(
                text = "No ${slotType.label} found in Cyber Armory vault. Visit Nanite Forge to craft!",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 12.dp)
              )
            } else {
              LazyColumn(
                modifier = Modifier.heightIn(max = 280.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                items(candidateItems) { item ->
                  CyberCard(
                    borderColor = if (item.equippedToOperativeId == selectedOperative?.id) MatrixGreen else BorderGlow,
                    backgroundColor = CyberSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                          text = item.name,
                          style = MaterialTheme.typography.titleSmall,
                          color = TextPrimary,
                          fontWeight = FontWeight.Bold
                        )
                        Text(
                          text = "ATK +${item.atkBonus} | DEF +${item.defBonus} | HP +${item.hpBonus}",
                          style = MaterialTheme.typography.labelSmall,
                          color = NeonCyan,
                          fontFamily = FontFamily.Monospace
                        )
                      }

                      if (item.equippedToOperativeId == selectedOperative?.id) {
                        CyberOutlineButton(
                          text = "UNEQUIP",
                          onClick = {
                            viewModel.unequipItem(item.id)
                            gearSlotToEquip = null
                          },
                          borderColor = NeonCrimson,
                          textColor = NeonCrimson
                        )
                      } else {
                        CyberButton(
                          text = "EQUIP",
                          onClick = {
                            viewModel.equipItem(selectedOperative!!.id, item.id)
                            gearSlotToEquip = null
                          },
                          primaryColor = NeonCyan,
                          textColor = CyberBackground,
                          modifier = Modifier.defaultMinSize(minWidth = 70.dp, minHeight = 32.dp)
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun GearSlotButton(
  itemType: ItemType,
  itemId: String?,
  items: List<ItemEntity>,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  val equippedItem = items.find { it.id == itemId }
  Box(
    modifier = modifier
      .clip(CutCornerShape(4.dp))
      .background(CyberSurfaceVariant)
      .border(1.dp, if (equippedItem != null) NeonCyan else BorderGlow, CutCornerShape(4.dp))
      .clickable {
        SoundManager.playButtonClick()
        onClick()
      }
      .padding(6.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Text(text = itemType.icon, fontSize = 18.sp)
      Text(
        text = equippedItem?.name?.split(" ")?.first() ?: slotShort(itemType),
        style = MaterialTheme.typography.labelSmall,
        color = if (equippedItem != null) NeonCyan else TextMuted,
        maxLines = 1,
        fontSize = 9.sp
      )
    }
  }
}

private fun slotShort(type: ItemType): String = when (type) {
  ItemType.WEAPON -> "Weapon"
  ItemType.ARMOR -> "Armor"
  ItemType.NANITE_CORE -> "Core"
  ItemType.CIPHER_CHIP -> "Chip"
}
