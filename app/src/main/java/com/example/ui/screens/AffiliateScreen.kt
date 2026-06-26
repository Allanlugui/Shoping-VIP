package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun AffiliateScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val affiliateData by viewModel.affiliateDashboard.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(bottom = 16.dp)) {
            Text(
                text = "PROGRAMA DE AFILIADOS",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = VipGold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "Indique clientes e ganhe comissão em dinheiro e cupons",
                fontSize = 12.sp,
                color = VipTextGray
            )
        }

        // 1. Referral Link Panel
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
                .testTag("referral_link_card"),
            colors = CardDefaults.cardColors(containerColor = VipCardGray),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Seu Link Exclusivo de Afiliado",
                    fontSize = 13.sp,
                    color = VipGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    color = VipDarkGray,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = userProfile.referralLink,
                            color = VipWhite,
                            fontSize = 12.sp,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(userProfile.referralLink))
                                Toast.makeText(context, "Link copiado para a área de transferência!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Link", tint = VipGold, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Actions buttons (Share and simulate click)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val phoneNumber = "" // Any contact
                            val text = "Olá! Dá uma olhada nesse convite para o Shopping VIP. Você recebe ofertas exclusivas com preço de ponta de estoque e eu ainda ganho pontos! Cadastre-se através do meu link:\n${userProfile.referralLink}"
                            val uri = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(text)}")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Link gerado para compartilhar!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1.2f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VipAccentGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = VipBlack, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enviar p/ WhatsApp", color = VipBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    // Interactive click simulator to let user test how affiliates are tracked!
                    Button(
                        onClick = {
                            viewModel.addClickToAffiliate()
                            viewModel.triggerSimulatedPushNotification(
                                "🔗 Novo clique rastreado!",
                                "Alguém clicou no seu link de afiliado e está navegando no Shopping VIP."
                            )
                            Toast.makeText(context, "Clique simulado! Verifique o painel abaixo.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VipGold.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, VipGold.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.TouchApp, contentDescription = null, tint = VipGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simular Clique", color = VipGold, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // 2. Dashboard metrics grid (Cliques, Conversões, Vendas, Lucro)
        Text(
            text = "Painel do Afiliado",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = VipWhite,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        // Using standard rows and columns for stable display
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                MetricCard("Cliques", affiliateData.clicks.toString(), "Acessos ao link", Icons.Default.TouchApp, VipGold)
            }
            Box(modifier = Modifier.weight(1f)) {
                MetricCard("Conversões", affiliateData.conversions.toString(), "Vendas indicadas", Icons.Default.CheckCircle, VipAccentGreen)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                MetricCard("Vendas Totais", "R$ ${String.format("%.2f", affiliateData.salesAmount)}", "Valor indicado", Icons.Default.TrendingUp, VipWhite)
            }
            Box(modifier = Modifier.weight(1f)) {
                MetricCard("Seus Ganhos (10%)", "R$ ${String.format("%.2f", affiliateData.earnings)}", "Saldo resgatável", Icons.Default.MonetizationOn, VipGold)
            }
        }

        // 3. Referred Sales Stream
        Text(
            text = "Histórico Recente de Indicações",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = VipWhite,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = VipCardGray),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val mockReferredSales = listOf(
                    ReferredSale("Carlos M.", "Tênis Nike Air Max 90", "R$ 599,90", "R$ 59,99", "Aprovado"),
                    ReferredSale("Mariana S.", "AirPods Pro Geração 2", "R$ 1.599,00", "R$ 159,90", "Aprovado"),
                    ReferredSale("Felipe L.", "Caixa JBL Charge 5", "R$ 749,00", "R$ 74,90", "Aprovado")
                )

                mockReferredSales.forEach { sale ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VipDarkGray, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(VipGold.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = VipGold, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(sale.buyerName, color = VipWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(sale.productName, color = VipTextGray, fontSize = 10.sp)
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Comissão", color = VipTextGray, fontSize = 9.sp)
                            Text("+ ${sale.commissionAmount}", color = VipAccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Rules Area
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = VipCardGray),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Como funciona o Programa de Afiliados?", fontWeight = FontWeight.Bold, color = VipGold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                val rules = listOf(
                    "Indique o link: Compartilhe o seu link com amigos e familiares.",
                    "Venda registrada: Seus indicados ganham 10% de desconto na primeira compra.",
                    "Comissão rápida: Você ganha 10% de comissão em dinheiro sobre o valor total do pedido.",
                    "Acúmulo de pontos: Além do dinheiro, você ganha +100 pontos de fidelidade para cada conversão!"
                )
                rules.forEach { rule ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.FiberManualRecord, contentDescription = null, tint = VipGold, modifier = Modifier.size(8.dp).padding(top = 4.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(rule, color = VipTextGray, fontSize = 11.sp, lineHeight = 16.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    desc: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = VipCardGray),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = VipTextGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, color = color, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(desc, color = VipTextGray, fontSize = 9.sp)
        }
    }
}

data class ReferredSale(
    val buyerName: String,
    val productName: String,
    val totalSpent: String,
    val commissionAmount: String,
    val status: String
)
