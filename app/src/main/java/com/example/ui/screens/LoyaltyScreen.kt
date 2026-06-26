package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun LoyaltyScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()

    val currentPoints = userProfile.points
    val currentSpent = userProfile.totalSpent
    val currentTier = userProfile.level
    val isVip = userProfile.isVip

    // Tier specific configs
    val tierColor = when (currentTier) {
        "Diamante" -> Color(0xFF00E5FF)
        "Ouro" -> VipGold
        "Prata" -> Color(0xFFC0C0C0)
        else -> Color(0xFFCD7F32) // Bronze
    }

    val nextTierInfo = remember(currentSpent) {
        when {
            currentSpent < 1000.0 -> Pair("Prata", 1000.0 - currentSpent)
            currentSpent < 5000.0 -> Pair("Ouro", 5000.0 - currentSpent)
            currentSpent < 10000.0 -> Pair("Diamante", 10000.0 - currentSpent)
            else -> Pair("Diamante Máximo", 0.0)
        }
    }

    val rewards = listOf(
        RewardItem("Cupom de R$ 50,00 OFF", 500, "Desconto válido para qualquer compra.", Icons.Default.LocalActivity),
        RewardItem("Frete Grátis Premium", 150, "Frete grátis em compras abaixo de R$250.", Icons.Default.LocalShipping),
        RewardItem("R$ 20,00 em Cashback", 200, "Crédito inserido direto na sua carteira.", Icons.Default.MonetizationOn),
        RewardItem("Garrafa Térmica VIP Inox", 1000, "Brinde exclusivo enviado para seu endereço.", Icons.Default.CardGiftcard),
        RewardItem("Sorteio Viagem de Compras", 800, "Bilhete para concorrer a viagem premium.", Icons.Default.FlightTakeoff)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = "PROGRAMA DE FIDELIDADE",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = VipGold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Suas compras acumulam pontos e geram recompensas",
                fontSize = 12.sp,
                color = VipTextGray
            )
        }

        // 1. Loyalty Scoreboard Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = VipCardGray),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(VipGold.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = VipGold, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Seus Pontos VIP", color = VipTextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("$currentPoints PTS", color = VipWhite, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    // Level Badge
                    Surface(
                        color = tierColor.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, tierColor),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = currentTier.uppercase(),
                            color = tierColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = VipDarkGray)
                Spacer(modifier = Modifier.height(12.dp))

                // Spent Tracker progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total gasto: R$ ${String.format("%.2f", currentSpent)}", color = VipWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (nextTierInfo.second > 0.0) {
                        Text("Próximo nível: ${nextTierInfo.first}", color = VipGold, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Progress Bar calculation
                val progress = remember(currentSpent) {
                    when {
                        currentSpent < 1000.0 -> currentSpent / 1000.0
                        currentSpent < 5000.0 -> (currentSpent - 1000.0) / 4000.0
                        currentSpent < 10000.0 -> (currentSpent - 5000.0) / 5000.0
                        else -> 1.0
                    }
                }
                LinearProgressIndicator(
                    progress = { progress.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = VipGold,
                    trackColor = VipDarkGray
                )

                if (nextTierInfo.second > 0.0) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Faltam R$ ${String.format("%.2f", nextTierInfo.second)} em compras para subir de nível!",
                        color = VipTextGray,
                        fontSize = 10.sp
                    )
                } else {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "👑 Você atingiu a categoria máxima Diamante! Parabéns!",
                        color = Color(0xFF00E5FF),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 2. VIP Membership Subscription Card (Luxurious credit-card background)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(bottom = 16.dp)
                .testTag("vip_subscription_card"),
            shape = RoundedCornerShape(20.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Background image
                Image(
                    painter = painterResource(id = R.drawable.vip_card),
                    contentDescription = "VIP Card",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Dark gold gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )

                // Card Details
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SHOPPING VIP MEMBERSHIP",
                            color = VipGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = VipGold,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isVip) "👑 ASSINANTE VIP ATIVO" else "ASSINATURA PREMIUM",
                            color = VipWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (isVip) "Seus superbenefícios estão liberados!" else "Assine por R$ 29,90/mês e libere tudo",
                            color = VipTextGray,
                            fontSize = 11.sp
                        )
                    }

                    // Subscribe action button
                    Button(
                        onClick = {
                            viewModel.toggleVipSubscription()
                            val msg = if (!isVip) "Inscrição VIP efetuada!" else "Inscrição VIP cancelada."
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .height(34.dp)
                            .testTag("vip_toggle_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isVip) VipAccentRed else VipGold
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isVip) "Cancelar VIP" else "Tornar-se VIP",
                            color = if (isVip) Color.White else VipBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Benefits of VIP Section
        Text(
            text = "Benefícios Exclusivos VIP",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = VipWhite,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = VipCardGray),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val benefits = listOf(
                    Pair("🏷️ Cupons Exclusivos", "Descontos de até 30% extras que só VIPs recebem."),
                    Pair("💰 Cashback Maior (10%)", "Receba de volta 10% de todo valor gasto (comum é 3%)."),
                    Pair("🚚 Frete Grátis Geral", "Sem valor mínimo para todo o Brasil em compras."),
                    Pair("👑 Sorteios Premium", "Participe de sorteios mensais de eletrônicos importados."),
                    Pair("⚡ Atendimento WhatsApp 24h", "Personal Shopper prioritário humano para te atender.")
                )
                benefits.forEach { (title, desc) ->
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = VipGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(title, color = VipWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(desc, color = VipTextGray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // 3. Points Redemption Shop List
        Text(
            text = "Loja de Recompensas (Troca de Pontos)",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = VipWhite,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VipCardGray),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rewards.forEach { reward ->
                    val canAfford = currentPoints >= reward.pointsPrice
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VipDarkGray, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(VipGold.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(reward.icon, contentDescription = null, tint = VipGold, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(reward.title, color = VipWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text(reward.description, color = VipTextGray, fontSize = 10.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${reward.pointsPrice} PTS", color = VipGold, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    if (canAfford) {
                                        viewModel.redeemReward(reward.pointsPrice, reward.title)
                                        Toast.makeText(context, "Resgate efetuado: ${reward.title}!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Pontos insuficientes!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (canAfford) VipAccentGreen else VipCardGray
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Text(
                                    text = "Resgatar",
                                    color = if (canAfford) VipBlack else VipTextGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

data class RewardItem(
    val title: String,
    val pointsPrice: Int,
    val description: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
