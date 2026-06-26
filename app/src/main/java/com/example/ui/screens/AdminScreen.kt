package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun AdminScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var activeAdminTab by remember { mutableStateOf("Dashboard") } // Dashboard, CRM, Automações, Importar, Marketplace

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Column(modifier = Modifier.padding(bottom = 12.dp)) {
            Text(
                text = "PAINEL ADMINISTRATIVO",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = VipGold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Controle de vendas, CRM de clientes, importação em lote e automações",
                fontSize = 11.sp,
                color = VipTextGray
            )
        }

        // Subtabs scrollable row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf("Dashboard", "CRM Clientes", "Automações", "Importar Lote", "Marketplace")
            items(tabs) { tab ->
                val isSelected = activeAdminTab == tab
                Surface(
                    modifier = Modifier
                        .clickable { activeAdminTab = tab }
                        .border(1.dp, if (isSelected) VipGold else VipCardGray, RoundedCornerShape(10.dp)),
                    color = if (isSelected) VipGold.copy(alpha = 0.15f) else VipCardGray,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = tab,
                        color = if (isSelected) VipGold else VipWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = VipCardGray, modifier = Modifier.padding(vertical = 12.dp))

        // RENDERING TABS
        when (activeAdminTab) {
            "Dashboard" -> DashboardSubTab(viewModel)
            "CRM Clientes" -> CrmSubTab(userProfile, viewModel)
            "Automações" -> AutomationsSubTab()
            "Importar Lote" -> ImporterSubTab(viewModel)
            "Marketplace" -> MarketplaceSubTab()
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

// 1. DASHBOARD SUBTAB
@Composable
fun DashboardSubTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    
    Text("Métricas Comerciais (Mês Atual)", fontWeight = FontWeight.Bold, color = VipWhite, fontSize = 14.sp)
    Spacer(modifier = Modifier.height(8.dp))

    // 4 metrics grid
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            MetricCard("Faturamento Total", "R$ 45.890,00", "+14.2% s/ mês ant.", Icons.Default.TrendingUp, VipAccentGreen)
        }
        Box(modifier = Modifier.weight(1f)) {
            MetricCard("Pedidos Concluídos", "184 un", "Ticket médio R$249", Icons.Default.CheckCircle, VipWhite)
        }
    }
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(modifier = Modifier.weight(1f)) {
            MetricCard("Clientes Ativos", "92 usuários", "Frequência: 1.8x", Icons.Default.People, VipGold)
        }
        Box(modifier = Modifier.weight(1f)) {
            MetricCard("ROI de Campanhas", "4.8x lucro", "Meta superada", Icons.Default.Campaign, VipGold)
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Best Selling Products List
    Text("Produtos Mais Vendidos", fontWeight = FontWeight.Bold, color = VipWhite, fontSize = 14.sp)
    Spacer(modifier = Modifier.height(8.dp))

    Card(
        colors = CardDefaults.cardColors(containerColor = VipCardGray),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val topProducts = listOf(
                Pair("Tênis Nike Air Max 90 Premium", "42 vendidos • Faturamento R$ 25.195"),
                Pair("Apple AirPods Pro (2ª Geração)", "18 vendidos • Faturamento R$ 28.782"),
                Pair("Tênis Adidas Ultraboost 22", "15 vendidos • Faturamento R$ 11.998")
            )
            topProducts.forEachIndexed { index, (name, stats) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("#${index + 1}", color = VipGold, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, modifier = Modifier.width(28.dp))
                    Column {
                        Text(name, color = VipWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(stats, color = VipTextGray, fontSize = 10.sp)
                    }
                }
                if (index < topProducts.size - 1) {
                    HorizontalDivider(color = VipDarkGray, modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // PDF/Excel export actions simulation
    Button(
        onClick = {
            Toast.makeText(context, "Métricas exportadas com sucesso em PDF e Excel!", Toast.LENGTH_LONG).show()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = VipGold),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.Download, contentDescription = null, tint = VipBlack)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Exportar Relatório Mensal (Excel / PDF)", color = VipBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

// 2. CRM SUBTAB
@Composable
fun CrmSubTab(userProfile: com.example.data.UserProfile, viewModel: MainViewModel) {
    val context = LocalContext.current
    var searchQueryCrm by remember { mutableStateOf("") }

    Text("Módulo CRM: Relacionamento com Clientes", fontWeight = FontWeight.Bold, color = VipWhite, fontSize = 14.sp)
    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = searchQueryCrm,
        onValueChange = { searchQueryCrm = it },
        placeholder = { Text("Filtrar por nome, cidade ou nível...", color = VipTextGray, fontSize = 12.sp) },
        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = VipGold, modifier = Modifier.size(16.dp)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VipGold,
            unfocusedBorderColor = VipCardGray,
            focusedContainerColor = VipCardGray,
            unfocusedContainerColor = VipCardGray,
            focusedTextColor = VipWhite,
            unfocusedTextColor = VipWhite
        ),
        singleLine = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    val crmClients = listOf(
        CrmClient("Ana Silva (Você)", userProfile.email, userProfile.level, "R$ ${String.format("%.2f", userProfile.totalSpent)}", "São Paulo", "Hoje", if (userProfile.isVip) "Cliente VIP" else "Cliente Ativo"),
        CrmClient("Carlos Eduardo", "carlos.edu@gmail.com", "Ouro", "R$ 5.420,00", "Rio de Janeiro", "2 dias atrás", "Cliente Recorrente"),
        CrmClient("Julia Mendonça", "julia.m@hotmail.com", "Diamante", "R$ 12.500,00", "Belo Horizonte", "Ontem", "Cliente VIP"),
        CrmClient("Rodrigo Alencar", "rodrigo82@yahoo.com", "Bronze", "R$ 399,90", "Porto Alegre", "15 dias atrás", "Cliente Inativo")
    ).filter { it.name.contains(searchQueryCrm, ignoreCase = true) || it.city.contains(searchQueryCrm, ignoreCase = true) }

    crmClients.forEach { client ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = VipCardGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(client.name, color = VipWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(client.email, color = VipTextGray, fontSize = 10.sp)
                    }

                    // Classification Badge
                    val labelColor = when (client.label) {
                        "Cliente VIP" -> Color(0xFF00E5FF)
                        "Cliente Recorrente" -> VipGold
                        "Cliente Inativo" -> VipAccentRed
                        else -> VipTextGray
                    }
                    Box(
                        modifier = Modifier
                            .background(labelColor.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(client.label, color = labelColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = VipDarkGray)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("TOTAL GASTO", color = VipTextGray, fontSize = 8.sp)
                        Text(client.totalSpent, color = VipAccentGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Column {
                        Text("CIDADE", color = VipTextGray, fontSize = 8.sp)
                        Text(client.city, color = VipWhite, fontSize = 11.sp)
                    }
                    Column {
                        Text("ÚLTIMO ACESSO", color = VipTextGray, fontSize = 8.sp)
                        Text(client.lastLogin, color = VipWhite, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Actions for specific client
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            viewModel.triggerSimulatedPushNotification(
                                "🎁 Cupom Especial Enviado!",
                                "Olá ${client.name.split(" ")[0]}, preparamos um cupom de 20% OFF extra para você reativar suas compras!"
                            )
                            Toast.makeText(context, "Cupom reativador disparado para ${client.name}!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).height(28.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = VipGold),
                        border = BorderStroke(1.dp, VipGold.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Disparar Cupom Reativador", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 3. AUTOMATIONS SUBTAB
@Composable
fun AutomationsSubTab() {
    val context = LocalContext.current
    var abandonCartToggle by remember { mutableStateOf(true) }
    var inactiveUserToggle by remember { mutableStateOf(true) }
    var birthdayGiftToggle by remember { mutableStateOf(true) }
    var firstPurchaseToggle by remember { mutableStateOf(true) }
    var postSalesReviewToggle by remember { mutableStateOf(false) }

    Text("Configurações de Automação de Marketing CRM", fontWeight = FontWeight.Bold, color = VipWhite, fontSize = 14.sp)
    Spacer(modifier = Modifier.height(4.dp))
    Text("Defina as regras automáticas de engajamento do cliente", color = VipTextGray, fontSize = 10.sp)
    Spacer(modifier = Modifier.height(12.dp))

    val automations = listOf(
        AutomationItem("Carrinho Abandonado", "Dispara push e WhatsApp após 1h alertando sobre itens no carrinho com frete grátis extra.", abandonCartToggle, { abandonCartToggle = it }),
        AutomationItem("Cliente Inativo (+15 dias)", "Envia cupom exclusivo VIP de 20% de desconto para reengajamento do usuário.", inactiveUserToggle, { inactiveUserToggle = it }),
        AutomationItem("Presente de Aniversário", "Dispara e-mail e push parabenizando o cliente e concedendo 500 pontos de fidelidade extras.", birthdayGiftToggle, { birthdayGiftToggle = it }),
        AutomationItem("Desconto de Boas-Vindas", "Ativa o cupom BEMVINDO10 automaticamente para a primeira compra do novo cadastro.", firstPurchaseToggle, { firstPurchaseToggle = it }),
        AutomationItem("Avaliação Pós-Venda (48h)", "Envia push solicitando feedback sobre o produto entregue em troca de 50 pontos.", postSalesReviewToggle, { postSalesReviewToggle = it })
    )

    automations.forEach { auto ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = VipCardGray),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1.8f)) {
                    Text(auto.title, color = VipWhite, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(auto.description, color = VipTextGray, fontSize = 10.sp, lineHeight = 14.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = auto.checkedState,
                    onCheckedChange = {
                        auto.onToggle(it)
                        val txt = if (it) "Automação ativada!" else "Automação desativada."
                        Toast.makeText(context, txt, Toast.LENGTH_SHORT).show()
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = VipBlack,
                        checkedTrackColor = VipGold,
                        uncheckedTrackColor = VipDarkGray
                    ),
                    modifier = Modifier.weight(0.5f)
                )
            }
        }
    }
}

// 4. IMPORTER SUBTAB (CSV / XML)
@Composable
fun ImporterSubTab(viewModel: MainViewModel) {
    val context = LocalContext.current
    var formatChoice by remember { mutableStateOf("CSV") }
    var rawTextData by remember { mutableStateOf("") }
    var importStatusMsg by remember { mutableStateOf<String?>(null) }

    // Preset examples for quick testing!
    val csvPreset = """Name, Brand, Category, SKU, Stock, Price, StoreName
Tênis Air Jordan 1 Retro, Nike, Calçados, NK-AJ1-RET, 5, 1499.90, Nike Outlet
Smartwatch Apple Watch S8, Apple, Eletrônicos, APL-W8, 12, 3299.00, Mercado Livre Prime
Camiseta Puma Active Training, Puma, Vestuário, PM-ACT-TR, 45, 129.90, Centauro Esportes"""

    val xmlPreset = """<products>
  <product>
    <name>Caixa Sony SRS-XB13</name>
    <brand>Sony</brand>
    <category>Eletrônicos</category>
    <sku>SNY-XB13</sku>
    <stock>18</stock>
    <price>399.00</price>
    <store>Amazon VIP</store>
  </product>
  <product>
    <name>Moletom Nike Fleece Sport</name>
    <brand>Nike</brand>
    <category>Vestuário</category>
    <sku>NK-FL-SPT</sku>
    <stock>22</stock>
    <price>299.90</price>
    <store>Nike Outlet</store>
  </product>
</products>"""

    // Auto load template based on selected format
    LaunchedEffect(formatChoice) {
        rawTextData = if (formatChoice == "CSV") csvPreset else xmlPreset
    }

    Text("Importação de Produtos em Massa", fontWeight = FontWeight.Bold, color = VipWhite, fontSize = 14.sp)
    Spacer(modifier = Modifier.height(4.dp))
    Text("Selecione o formato e insira os dados para cadastrar múltiplos produtos de uma vez.", color = VipTextGray, fontSize = 10.sp)
    Spacer(modifier = Modifier.height(12.dp))

    // Format choices
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("CSV", "XML").forEach { format ->
            val isSelected = formatChoice == format
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { formatChoice = format }
                    .border(1.dp, if (isSelected) VipGold else VipCardGray, RoundedCornerShape(8.dp)),
                color = if (isSelected) VipGold.copy(alpha = 0.15f) else VipCardGray,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "$format / Excel",
                    color = if (isSelected) VipGold else VipWhite,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 10.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Input text field
    OutlinedTextField(
        value = rawTextData,
        onValueChange = { rawTextData = it },
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .testTag("bulk_importer_text"),
        placeholder = { Text("Cole os dados aqui...", color = VipTextGray, fontSize = 11.sp) },
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VipGold,
            unfocusedBorderColor = VipCardGray,
            focusedContainerColor = VipCardGray,
            unfocusedContainerColor = VipCardGray,
            focusedTextColor = VipWhite,
            unfocusedTextColor = VipWhite
        ),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
    )

    Spacer(modifier = Modifier.height(12.dp))

    Button(
        onClick = {
            val result = viewModel.importProductsFromText(rawTextData, formatChoice)
            importStatusMsg = result
            Toast.makeText(context, result, Toast.LENGTH_LONG).show()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("submit_import_button"),
        colors = ButtonDefaults.buttonColors(containerColor = VipAccentGreen),
        shape = RoundedCornerShape(12.dp)
    ) {
        Icon(Icons.Default.CloudUpload, contentDescription = null, tint = VipBlack)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Iniciar Importação Automática", color = VipBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }

    importStatusMsg?.let { msg ->
        Spacer(modifier = Modifier.height(8.dp))
        Text(msg, color = if (msg.startsWith("Sucesso")) VipAccentGreen else VipAccentRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

// 5. MARKETPLACE FUTURE SUBTAB
@Composable
fun MarketplaceSubTab() {
    Text("Módulo Vendedores Externos (Marketplace)", fontWeight = FontWeight.Bold, color = VipWhite, fontSize = 14.sp)
    Spacer(modifier = Modifier.height(4.dp))
    Text("Estruturação para expansão de sellers e lojistas parceiros", color = VipTextGray, fontSize = 10.sp)
    Spacer(modifier = Modifier.height(16.dp))

    Card(
        colors = CardDefaults.cardColors(containerColor = VipCardGray),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.Store, contentDescription = null, tint = VipGold, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Em Breve: Área do Vendedor Parceiro", color = VipWhite, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "O Shopping VIP já está pré-arquitetado para suportar múltiplos sellers de forma transparente. Com o Marketplace ativo, cada lojista terá:\n\n" +
                "• Painel de controle próprio para cadastro de produtos e gestão de estoque.\n" +
                "• Gestão inteligente de pedidos e geração de etiquetas de envio.\n" +
                "• Relatórios completos de faturamento com split de pagamento automático.\n" +
                "• Comissões do Shopping VIP calculadas em tempo real com base no faturamento do seller.",
                color = VipTextGray,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}

data class CrmClient(
    val name: String,
    val email: String,
    val level: String,
    val totalSpent: String,
    val city: String,
    val lastLogin: String,
    val label: String
)

data class AutomationItem(
    val title: String,
    val description: String,
    val checkedState: Boolean,
    val onToggle: (Boolean) -> Unit
)
