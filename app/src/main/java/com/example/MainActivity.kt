package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize the MainViewModel using our custom Factory
                val viewModel: MainViewModel by viewModels {
                    MainViewModelFactory(application)
                }

                var currentTab by remember { mutableStateOf("Shop") }

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(VipBlack),
                    bottomBar = {
                        NavigationBar(
                            containerColor = VipDarkGray,
                            tonalElevation = 8.dp,
                            windowInsets = WindowInsets.navigationBars,
                            modifier = Modifier.testTag("main_bottom_nav")
                        ) {
                            val items = listOf(
                                NavigationItem("Shop", "Shop", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag),
                                NavigationItem("Comunidade", "Comunidade", Icons.Filled.Group, Icons.Outlined.Group),
                                NavigationItem("Fidelidade", "Fidelidade", Icons.Filled.WorkspacePremium, Icons.Outlined.WorkspacePremium),
                                NavigationItem("Afiliados", "Afiliados", Icons.Filled.Share, Icons.Outlined.Share),
                                NavigationItem("Suporte", "Suporte", Icons.Filled.Chat, Icons.Outlined.Chat),
                                NavigationItem("Admin", "Admin", Icons.Filled.AdminPanelSettings, Icons.Outlined.AdminPanelSettings)
                            )

                            items.forEach { item ->
                                val isSelected = currentTab == item.id
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentTab = item.id },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = item.label,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = VipBlack,
                                        selectedTextColor = VipGold,
                                        indicatorColor = VipGold,
                                        unselectedIconColor = VipTextGray,
                                        unselectedTextColor = VipTextGray
                                    ),
                                    modifier = Modifier.testTag("nav_item_${item.id.lowercase()}")
                                )
                            }
                        }
                    },
                    containerColor = VipBlack
                ) { innerPadding ->
                    // Ambient gradient background for deep luxury atmosphere
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(VipBlack, VipDarkGray, VipBlack)
                                )
                            )
                    ) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                (fadeIn() + scaleIn(initialScale = 0.98f))
                                    .togetherWith(fadeOut())
                            },
                            label = "TabTransition"
                        ) { targetTab ->
                            when (targetTab) {
                                "Shop" -> ShopScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                                )
                                "Comunidade" -> CommunityScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                                )
                                "Fidelidade" -> LoyaltyScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                                )
                                "Afiliados" -> AffiliateScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                                )
                                "Suporte" -> SupportChatScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                                )
                                "Admin" -> AdminScreen(
                                    viewModel = viewModel,
                                    modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class NavigationItem(
    val id: String,
    val label: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val unselectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)
