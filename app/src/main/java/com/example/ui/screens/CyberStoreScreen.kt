package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.data.model.ItemRarity
import com.example.data.model.ItemType
import com.example.ui.components.CyberBadge
import com.example.ui.components.CyberButton
import com.example.ui.components.CyberCard
import com.example.ui.theme.*
import com.example.ui.viewmodel.GameViewModel
import java.util.UUID

data class StorePackage(
  val id: String,
  val title: String,
  val priceUsd: String,
  val costDouble: Double,
  val credits: Int,
  val dataMb: Int,
  val isVip: Boolean = false,
  val bonusGear: ItemEntity? = null,
  val icon: String = "💎",
  val themeColor: Color = NeonCyan,
  val tag: String? = null
)

@Composable
fun CyberStoreScreen(
  viewModel: GameViewModel
) {
  val uiState by viewModel.uiState.collectAsState()
  val profile = uiState.playerProfile

  var pendingPackage by remember { mutableStateOf<StorePackage?>(null) }
  var isProcessingPurchase by remember { mutableStateOf(false) }

  val storePackages = remember {
    listOf(
      StorePackage(
        id = "pkg_starter",
        title = "RECON DATA RUNNER PACK",
        priceUsd = "$0.99",
        costDouble = 0.99,
        credits = 1200,
        dataMb = 600,
        icon = "💎",
        themeColor = NeonCyan,
        tag = "POPULAR"
      ),
      StorePackage(
        id = "pkg_arms",
        title = "SPECIAL FORCES ARMS CACHE",
        priceUsd = "$2.99",
        costDouble = 2.99,
        credits = 4500,
        dataMb = 2200,
        bonusGear = ItemEntity(
          id = "store_item_${UUID.randomUUID().toString().take(6)}",
          name = "Singularity Hyper-Katana",
          itemType = ItemType.WEAPON.name,
          rarity = ItemRarity.EPIC.name,
          atkBonus = 45,
          critBonus = 0.20f,
          flavorText = "Military-grade thermal blade commissioned for high-priority surgical assassinations."
        ),
        icon = "🗡️",
        themeColor = ElectricPurple,
        tag = "BEST VALUE"
      ),
      StorePackage(
        id = "pkg_overlord_vault",
        title = "COMMANDER OVERLORD VAULT",
        priceUsd = "$9.99",
        costDouble = 9.99,
        credits = 18000,
        dataMb = 9000,
        bonusGear = ItemEntity(
          id = "store_item_${UUID.randomUUID().toString().take(6)}",
          name = "Aegis Dreadnought Kinetic Rig",
          itemType = ItemType.ARMOR.name,
          rarity = ItemRarity.LEGENDARY.name,
          defBonus = 55,
          hpBonus = 160,
          flavorText = "Experimental sovereign fortress armor capable of withstanding heavy orbital bombardments."
        ),
        icon = "👑",
        themeColor = CyberGold,
        tag = "OVERLORD TIER"
      ),
      StorePackage(
        id = "pkg_vip_pass",
        title = "PERMANENT VIP OVERLORD ACCESS",
        priceUsd = "$4.99",
        costDouble = 4.99,
        credits = 6000,
        dataMb = 3000,
        isVip = true,
        icon = "⚡",
        themeColor = MatrixGreen,
        tag = "PERMANENT UNLOCK"
      )
    )
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
      // Header
      item {
        CyberCard(borderColor = CyberGold, modifier = Modifier.fillMaxWidth()) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "SUB-ETHER BLACK MARKET",
                style = MaterialTheme.typography.titleMedium,
                color = CyberGold,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Procure tactical credits, data matrices, and prototype weapons.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 12.sp
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              CyberBadge(text = "${profile?.cyberCredits ?: 0} CR", color = NeonCyan)
              CyberBadge(text = "${profile?.tacticalData ?: 0} MB", color = MatrixGreen)
            }
          }
        }
      }

      // Store Packages
      items(storePackages.size) { index ->
        val pkg = storePackages[index]
        CyberCard(
          borderColor = pkg.themeColor,
          glowColor = pkg.themeColor.copy(alpha = 0.25f),
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
                  .size(46.dp)
                  .clip(CutCornerShape(4.dp))
                  .background(CyberSurfaceVariant)
                  .border(1.dp, pkg.themeColor, CutCornerShape(4.dp)),
                contentAlignment = Alignment.Center
              ) {
                Text(text = pkg.icon, fontSize = 24.sp)
              }

              Column {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  Text(
                    text = pkg.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                  )
                  if (pkg.tag != null) {
                    CyberBadge(text = pkg.tag, color = pkg.themeColor)
                  }
                }

                Text(
                  text = "+${pkg.credits} Cyber Credits  •  +${pkg.dataMb} Tactical Data",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MatrixGreen,
                  fontFamily = FontFamily.Monospace,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )

                if (pkg.bonusGear != null) {
                  Text(
                    text = "🎁 BONUS: [${pkg.bonusGear.rarity}] ${pkg.bonusGear.name}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CyberGold,
                    fontSize = 10.sp
                  )
                }
              }
            }

            CyberButton(
              text = pkg.priceUsd,
              onClick = {
                SoundManager.playButtonClick()
                pendingPackage = pkg
              },
              primaryColor = pkg.themeColor,
              textColor = if (pkg.themeColor == CyberGold || pkg.themeColor == NeonCyan || pkg.themeColor == MatrixGreen) CyberBackground else TextPrimary,
              modifier = Modifier.defaultMinSize(minWidth = 75.dp, minHeight = 36.dp)
            )
          }
        }
      }
    }

    // Purchase Terminal Checkout Modal
    if (pendingPackage != null) {
      val pkg = pendingPackage!!
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.85f))
          .padding(20.dp),
        contentAlignment = Alignment.Center
      ) {
        CyberCard(
          borderColor = pkg.themeColor,
          glowColor = pkg.themeColor,
          backgroundColor = CyberSurface,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
          ) {
            Text(
              text = "CYBERNETIC PAYMENT TERMINAL",
              style = MaterialTheme.typography.titleMedium,
              color = pkg.themeColor,
              fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = pkg.title,
              style = MaterialTheme.typography.titleLarge,
              color = TextPrimary,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            CyberCard(
              borderColor = BorderGlow,
              backgroundColor = CyberSurfaceVariant,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(text = "ITEMIZED TRANSACTION REQUISITION:", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                Text(text = "• +${pkg.credits} Cyber Credits", color = NeonCyan, fontWeight = FontWeight.Bold)
                Text(text = "• +${pkg.dataMb} Tactical Data Matrix", color = MatrixGreen, fontWeight = FontWeight.Bold)
                if (pkg.bonusGear != null) {
                  Text(text = "• +[${pkg.bonusGear.rarity}] ${pkg.bonusGear.name}", color = CyberGold, fontWeight = FontWeight.Bold)
                }
                if (pkg.isVip) {
                  Text(text = "• Permanent VIP Overlord Protocol", color = MatrixGreen, fontWeight = FontWeight.Bold)
                }
                Text(text = "Total Charged: ${pkg.priceUsd}", color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              CyberButton(
                text = "CANCEL",
                onClick = { pendingPackage = null },
                primaryColor = CyberSurfaceHigh,
                textColor = TextPrimary,
                modifier = Modifier.weight(1f)
              )

              CyberButton(
                text = "AUTHORIZE",
                onClick = {
                  SoundManager.playLevelUp()
                  viewModel.purchaseStorePackage(
                    credits = pkg.credits,
                    data = pkg.dataMb,
                    cost = pkg.costDouble,
                    isVip = pkg.isVip,
                    bonusItem = pkg.bonusGear
                  )
                  pendingPackage = null
                },
                primaryColor = pkg.themeColor,
                textColor = if (pkg.themeColor == CyberGold || pkg.themeColor == NeonCyan || pkg.themeColor == MatrixGreen) CyberBackground else TextPrimary,
                modifier = Modifier.weight(1f),
                icon = "💳"
              )
            }
          }
        }
      }
    }
  }
}
