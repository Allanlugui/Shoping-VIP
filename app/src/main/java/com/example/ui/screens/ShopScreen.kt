package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.CartItem
import com.example.data.Product
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val products by viewModel.products.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var activeProductForDetail by remember { mutableStateOf<Product?>(null) }
    var showCartSheet by remember { mutableStateOf(false) }
    var checkoutSuccessByAmount by remember { mutableStateOf<Double?>(null) }

    // Autocomplete suggestions
    var showSuggestions by remember { mutableStateOf(false) }
    val autocompleteSuggestions = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else listOf("Tênis Nike", "Ultraboost", "Kindle", "AirPods", "Puma", "Caixa JBL")
            .filter { it.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            if (cartItems.isNotEmpty()) {
                BadgedBox(
                    badge = {
                        Badge(containerColor = VipAccentRed) {
                            Text(
                                text = cartItems.sumOf { it.quantity }.toString(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp, bottom = 8.dp)
                ) {
                    FloatingActionButton(
                        onClick = { showCartSheet = true },
                        containerColor = VipGold,
                        contentColor = VipBlack,
                        shape = CircleShape,
                        modifier = Modifier.testTag("cart_fab")
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Carrinho")
                    }
                }
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "VIP SHOPPING",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = VipGold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Curadoria Premium Personal Shopper",
                        fontSize = 12.sp,
                        color = VipTextGray
                    )
                }

                if (userProfile.isVip) {
                    Surface(
                        color = VipGold.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, VipGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = VipGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("VIP", color = VipGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Search Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .zIndex(2f)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        viewModel.setSearchQuery(it)
                        showSuggestions = it.isNotBlank()
                    },
                    placeholder = { Text("Buscar marca, produto, categoria...", color = VipTextGray) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar", tint = VipGold) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Limpar", tint = VipTextGray)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input"),
                    shape = RoundedCornerShape(24.dp),
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

                // Autocomplete Suggestions Dropdown
                if (showSuggestions && autocompleteSuggestions.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 58.dp)
                            .shadow(8.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = VipCardGray)
                    ) {
                        Column {
                            autocompleteSuggestions.forEach { suggestion ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.setSearchQuery(suggestion)
                                            showSuggestions = false
                                        }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.TrendingUp, contentDescription = null, tint = VipGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(suggestion, color = VipWhite, fontSize = 14.sp)
                                }
                                HorizontalDivider(color = VipDarkGray)
                            }
                        }
                    }
                }
            }

            // Main Scrollable Area
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Banner Carousel (Hero Banner)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.vip_banner),
                        contentDescription = "VIP Hero Banner",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Dim Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, VipBlack.copy(alpha = 0.85f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = "DESCONTOS DE ATÉ 50%",
                            color = VipGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Outlet Nike & Adidas com cupons secretos ativos",
                            color = VipWhite,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Categories Row
                Text(
                    text = "Categorias",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = VipWhite,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.setCategoryFilter(null) },
                            label = { Text("Todos") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VipGold,
                                selectedLabelColor = VipBlack,
                                containerColor = VipCardGray,
                                labelColor = VipWhite
                            ),
                            border = null
                        )
                    }
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { viewModel.setCategoryFilter(cat) },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VipGold,
                                selectedLabelColor = VipBlack,
                                containerColor = VipCardGray,
                                labelColor = VipWhite
                            ),
                            border = null
                        )
                    }
                }

                // Product Grid Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Curadoria de Produtos (${products.size})",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VipWhite
                    )

                    if (selectedCategory != null) {
                        TextButton(onClick = { viewModel.setCategoryFilter(null) }) {
                            Text("Limpar", color = VipGold)
                        }
                    }
                }

                if (products.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Inbox,
                            contentDescription = null,
                            tint = VipTextGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Nenhum produto encontrado.",
                            color = VipTextGray,
                            fontSize = 14.sp
                        )
                    }
                } else {
                    // Custom non-nested Grid list utilizing Row columns to avoid nested scroll issue
                    val chunkedProducts = products.chunked(2)
                    chunkedProducts.forEach { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { product ->
                                Box(modifier = Modifier.weight(1f)) {
                                    ProductItemCard(
                                        product = product,
                                        onProductClick = { activeProductForDetail = product },
                                        onWhatsAppClick = {
                                            openWhatsApp(context, product)
                                        }
                                    )
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(100.dp)) // padding for bottom nav
            }
        }
    }

    // PRODUCT DETAIL DIALOG
    activeProductForDetail?.let { product ->
        ProductDetailDialog(
            product = product,
            onDismiss = { activeProductForDetail = null },
            onAddToCart = { qty, size, color ->
                viewModel.addToCart(product, qty, size, color)
                activeProductForDetail = null
                Toast.makeText(context, "Produto adicionado!", Toast.LENGTH_SHORT).show()
            },
            onBuyWhatsApp = {
                openWhatsApp(context, product)
            }
        )
    }

    // CART SHEET / DIALOG
    if (showCartSheet) {
        CartCheckoutDialog(
            viewModel = viewModel,
            onDismiss = { showCartSheet = false },
            onCheckoutSuccess = { amount ->
                checkoutSuccessByAmount = amount
                showCartSheet = false
            }
        )
    }

    // CHECKOUT SUCCESS OVERLAY
    checkoutSuccessByAmount?.let { amount ->
        CheckoutSuccessDialog(
            amount = amount,
            pointsEarned = amount.toInt(),
            onDismiss = { checkoutSuccessByAmount = null }
        )
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    onProductClick: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick() }
            .testTag("product_card_${product.id}"),
        colors = CardDefaults.cardColors(containerColor = VipCardGray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            // Product Image Placeholder with Badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(VipDarkGray)
            ) {
                // Determine icon based on brand/image
                val iconRes = when (product.brand.lowercase()) {
                    "nike" -> Icons.Default.SportsBasketball
                    "adidas" -> Icons.Default.SportsHandball
                    "apple" -> Icons.Default.Headphones
                    "amazon" -> Icons.Default.MenuBook
                    else -> Icons.Default.ShoppingBag
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            iconRes,
                            contentDescription = null,
                            tint = VipGold.copy(alpha = 0.4f),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            product.brand.uppercase(),
                            color = VipGold.copy(alpha = 0.5f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Promo Badge
                if (product.promoPrice != null) {
                    val discountPercent = ((product.price - product.promoPrice) / product.price * 100).toInt()
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .background(VipAccentGreen, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Text(
                            text = "-$discountPercent%",
                            color = VipBlack,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Rating
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .background(VipBlack.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .align(Alignment.BottomEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = VipGold,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        product.rating.toString(),
                        color = VipWhite,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Info Column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = product.storeName,
                    fontSize = 10.sp,
                    color = VipGold,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = product.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = VipWhite,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Price display
                if (product.promoPrice != null) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "R$ ${String.format("%.2f", product.promoPrice)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = VipAccentGreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "R$ ${String.format("%.2f", product.price)}",
                            fontSize = 10.sp,
                            textDecoration = TextDecoration.LineThrough,
                            color = VipTextGray
                        )
                    }
                } else {
                    Text(
                        text = "R$ ${String.format("%.2f", product.price)}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = VipWhite
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Buy via Whatsapp Button
                OutlinedButton(
                    onClick = { onWhatsAppClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = VipAccentGreen
                    ),
                    border = BorderStroke(1.dp, VipAccentGreen.copy(alpha = 0.5f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = VipAccentGreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailDialog(
    product: Product,
    onDismiss: () -> Unit,
    onAddToCart: (quantity: Int, size: String, color: String) -> Unit,
    onBuyWhatsApp: () -> Unit
) {
    val sizes = product.sizeOptions.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    val colors = product.colorOptions.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    var selectedSize by remember { mutableStateOf(sizes.firstOrNull() ?: "Único") }
    var selectedColor by remember { mutableStateOf(colors.firstOrNull() ?: "Padrão") }
    var quantity by remember { mutableIntStateOf(1) }

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight()
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, VipGold.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
            color = VipDarkGray
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header Image Display
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(VipCardGray)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = VipGold,
                                modifier = Modifier.size(54.dp)
                            )
                            Text(
                                product.brand,
                                color = VipGold,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(VipBlack.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = VipWhite)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Store & Title
                Text(
                    text = product.storeName,
                    fontSize = 12.sp,
                    color = VipGold,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = product.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = VipWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "SKU: ${product.sku}  |  Estoque: ${product.stock} un",
                    fontSize = 11.sp,
                    color = VipTextGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Price Row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (product.promoPrice != null) {
                        Text(
                            text = "R$ ${String.format("%.2f", product.promoPrice)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = VipAccentGreen
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "R$ ${String.format("%.2f", product.price)}",
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.LineThrough,
                            color = VipTextGray
                        )
                    } else {
                        Text(
                            text = "R$ ${String.format("%.2f", product.price)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = VipWhite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                Text(
                    text = "Descrição",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = VipWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    fontSize = 13.sp,
                    color = VipTextGray,
                    lineHeight = 18.sp
                )

                // Select Size Options
                if (sizes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Tamanho", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VipWhite)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sizes) { size ->
                            val isSelected = selectedSize == size
                            Surface(
                                modifier = Modifier
                                    .clickable { selectedSize = size }
                                    .border(
                                        1.dp,
                                        if (isSelected) VipGold else VipCardGray,
                                        RoundedCornerShape(8.dp)
                                    ),
                                color = if (isSelected) VipGold.copy(alpha = 0.1f) else VipCardGray,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = size,
                                    color = if (isSelected) VipGold else VipWhite,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Select Color Options
                if (colors.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cor", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VipWhite)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(colors) { color ->
                            val isSelected = selectedColor == color
                            Surface(
                                modifier = Modifier
                                    .clickable { selectedColor = color }
                                    .border(
                                        1.dp,
                                        if (isSelected) VipGold else VipCardGray,
                                        RoundedCornerShape(8.dp)
                                    ),
                                color = if (isSelected) VipGold.copy(alpha = 0.1f) else VipCardGray,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = color,
                                    color = if (isSelected) VipGold else VipWhite,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Quantity Selector
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quantidade", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = VipWhite)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            modifier = Modifier.background(VipCardGray, CircleShape).size(32.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null, tint = VipWhite, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = quantity.toString(),
                            color = VipWhite,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        IconButton(
                            onClick = { if (quantity < product.stock) quantity++ },
                            modifier = Modifier.background(VipCardGray, CircleShape).size(32.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = VipWhite, modifier = Modifier.size(16.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Actions Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Buy via WhatsApp button
                    Button(
                        onClick = onBuyWhatsApp,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VipAccentGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = VipBlack)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("WhatsApp", color = VipBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // Add to Cart button
                    Button(
                        onClick = { onAddToCart(quantity, selectedSize, selectedColor) },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(48.dp)
                            .testTag("add_to_cart_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = VipGold),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AddShoppingCart, contentDescription = null, tint = VipBlack)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("No Carrinho", color = VipBlack, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartCheckoutDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit,
    onCheckoutSuccess: (Double) -> Unit
) {
    val context = LocalContext.current
    val cartItems by viewModel.cartItems.collectAsState()
    val products by viewModel.products.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var selectedPaymentMethod by remember { mutableStateOf("PIX") }
    var couponText by remember { mutableStateOf("") }
    var discountAmount by remember { mutableStateOf(0.0) }
    var couponAppliedMessage by remember { mutableStateOf<String?>(null) }

    // Calc totals
    var subTotal = 0.0
    val itemsWithProducts = cartItems.mapNotNull { item ->
        val prod = products.find { it.id == item.productId }
        if (prod != null) {
            val priceToUse = prod.promoPrice ?: prod.price
            subTotal += priceToUse * item.quantity
            Pair(item, prod)
        } else null
    }

    // VIP Cashback calculation
    val cashbackRate = if (userProfile.isVip) 0.10 else 0.03
    val cashbackValue = subTotal * cashbackRate

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(24.dp)),
            color = VipDarkGray
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Seu Carrinho VIP", color = VipGold, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                        navigationIcon = {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = VipWhite)
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = VipDarkGray)
                    )
                },
                containerColor = VipDarkGray
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp)
                ) {
                    if (cartItems.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.RemoveShoppingCart, contentDescription = null, tint = VipTextGray, modifier = Modifier.size(64.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Seu carrinho está vazio.", color = VipTextGray)
                        }
                    } else {
                        // Cart List
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            itemsWithProducts.forEach { (item, product) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .background(VipCardGray, RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.ShoppingBag,
                                        contentDescription = null,
                                        tint = VipGold,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(product.name, color = VipWhite, fontSize = 14.sp, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("Cor: ${item.color} | Tam: ${item.size}", color = VipTextGray, fontSize = 11.sp)
                                        Text("R$ ${String.format("%.2f", product.promoPrice ?: product.price)}", color = VipAccentGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    
                                    // Quantity Selector
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                if (item.quantity > 1) viewModel.updateCartQuantity(item.id, item.quantity - 1)
                                                else viewModel.removeFromCart(item.id)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = null, tint = VipWhite)
                                        }
                                        Text(item.quantity.toString(), color = VipWhite, modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold)
                                        IconButton(
                                            onClick = {
                                                viewModel.updateCartQuantity(item.id, item.quantity + 1)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = VipWhite)
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Coupons & Gift Area
                            Text("Cupom de Desconto", fontWeight = FontWeight.Bold, color = VipWhite, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = couponText,
                                    onValueChange = { couponText = it },
                                    placeholder = { Text("Cupom (ex: NIKE30EXTRA)", color = VipTextGray, fontSize = 12.sp) },
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = VipGold,
                                        unfocusedBorderColor = VipCardGray,
                                        focusedTextColor = VipWhite,
                                        unfocusedTextColor = VipWhite,
                                        focusedContainerColor = VipCardGray,
                                        unfocusedContainerColor = VipCardGray
                                    ),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (couponText.trim().uppercase() == "NIKE30EXTRA") {
                                            discountAmount = subTotal * 0.3
                                            couponAppliedMessage = "Cupom de 30% EXTRA aplicado com sucesso!"
                                        } else if (couponText.trim().uppercase() == "MLPRIME100") {
                                            discountAmount = 100.0
                                            couponAppliedMessage = "Desconto flat de R$100,00 aplicado!"
                                        } else {
                                            discountAmount = 0.0
                                            couponAppliedMessage = "Cupom inválido ou expirado."
                                        }
                                    },
                                    modifier = Modifier.weight(0.8f).height(54.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = VipGold),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Aplicar", color = VipBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                            couponAppliedMessage?.let {
                                Text(it, color = if (discountAmount > 0) VipAccentGreen else VipAccentRed, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Payment Method
                            Text("Método de Pagamento", fontWeight = FontWeight.Bold, color = VipWhite, fontSize = 14.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            val methods = listOf("PIX", "Mercado Pago", "Cartão", "Google Pay")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                methods.forEach { method ->
                                    val isSelected = selectedPaymentMethod == method
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedPaymentMethod = method }
                                            .border(1.dp, if (isSelected) VipGold else VipCardGray, RoundedCornerShape(8.dp)),
                                        color = if (isSelected) VipGold.copy(alpha = 0.1f) else VipCardGray,
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(method, color = if (isSelected) VipGold else VipWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), textAlign = TextAlign.Center)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Order Summary
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = VipCardGray,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Subtotal", color = VipTextGray, fontSize = 13.sp)
                                        Text("R$ ${String.format("%.2f", subTotal)}", color = VipWhite, fontSize = 13.sp)
                                    }
                                    if (discountAmount > 0) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text("Desconto", color = VipAccentRed, fontSize = 13.sp)
                                            Text("- R$ ${String.format("%.2f", discountAmount)}", color = VipAccentRed, fontSize = 13.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Frete", color = VipTextGray, fontSize = 13.sp)
                                        Text(if (userProfile.isVip || subTotal >= 250.0) "GRÁTIS (VIP)" else "R$ 15,90", color = VipAccentGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Cashback estimado (${if (userProfile.isVip) "10% VIP" else "3%"})", color = VipGold, fontSize = 12.sp)
                                        Text("+ R$ ${String.format("%.2f", cashbackValue)}", color = VipGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                    HorizontalDivider(color = VipDarkGray, modifier = Modifier.padding(vertical = 8.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                        Text("Total", color = VipWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        val totalWithShipping = subTotal - discountAmount + (if (userProfile.isVip || subTotal >= 250.0) 0.0 else 15.90)
                                        Text("R$ ${String.format("%.2f", maxOf(0.0, totalWithShipping))}", color = VipAccentGreen, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Checkout Button
                        val finalTotal = maxOf(0.0, subTotal - discountAmount + (if (userProfile.isVip || subTotal >= 250.0) 0.0 else 15.90))
                        Button(
                            onClick = {
                                viewModel.checkoutCart(selectedPaymentMethod, couponText, discountAmount)
                                onCheckoutSuccess(finalTotal)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .testTag("submit_checkout_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = VipAccentGreen),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = VipBlack)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Confirmar e Pagar R$ ${String.format("%.2f", finalTotal)}", color = VipBlack, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckoutSuccessDialog(
    amount: Double,
    pointsEarned: Int,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = { onDismiss() }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .border(2.dp, VipGold, RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = VipDarkGray),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success Badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(VipAccentGreen.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = VipAccentGreen, modifier = Modifier.size(40.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Compra Aprovada!", color = VipWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Seu pedido de R$ ${String.format("%.2f", amount)} foi processado com sucesso.",
                    color = VipTextGray,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Points earned Card
                Surface(
                    color = VipCardGray,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("MÓDULO FIDELIDADE", fontSize = 10.sp, color = VipGold, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("+$pointsEarned pontos acumulados!", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VipGold)
                        Text("1 Real gasto = 1 Ponto", fontSize = 10.sp, color = VipTextGray)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VipGold),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Excelente!", color = VipBlack, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// WhatsApp utility helper
fun openWhatsApp(context: android.content.Context, product: Product) {
    try {
        val phoneNumber = "5511999999999" // Mock Personal Shopper Whatsapp
        val message = "Olá Personal Shopper VIP! Gostaria de comprar o produto:\n\n" +
                "📦 *${product.name}*\n" +
                "🏷️ SKU: ${product.sku}\n" +
                "🏬 Loja: ${product.storeName}\n" +
                "💰 Valor: R$ ${String.format("%.2f", product.promoPrice ?: product.price)}\n\n" +
                "Por favor, verifique disponibilidade e frete!"
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Link WhatsApp gerado! (WhatsApp não instalado)", Toast.LENGTH_SHORT).show()
    }
}
