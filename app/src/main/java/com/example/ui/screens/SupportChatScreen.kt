package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.data.ChatMessage
import com.example.data.NotificationItem
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun SupportChatScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val chatMessages by viewModel.chatMessages.collectAsState()
    val notifications by viewModel.notifications.collectAsState()

    var chatTextInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Sub-sections selector (Chat Suporte vs Notificações)
    var activeSubSection by remember { mutableStateOf("Chat") }

    // Scroll to bottom on new messages
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ATENDIMENTO & NOTIFICAÇÕES",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = VipGold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Personal Shoppers e avisos importantes",
                    fontSize = 11.sp,
                    color = VipTextGray
                )
            }
        }

        // Toggle buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("Chat", "Notificações").forEach { sub ->
                val isSelected = activeSubSection == sub
                val badgeCount = if (sub == "Notificações") notifications.filter { !it.isRead }.size else 0

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { activeSubSection = sub }
                        .border(1.dp, if (isSelected) VipGold else VipCardGray, RoundedCornerShape(12.dp)),
                    color = if (isSelected) VipGold.copy(alpha = 0.15f) else VipCardGray,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (sub == "Chat") Icons.Default.ChatBubbleOutline else Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = if (isSelected) VipGold else VipWhite,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = sub,
                            color = if (isSelected) VipGold else VipWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (badgeCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Badge(containerColor = VipAccentRed) {
                                Text(badgeCount.toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = VipCardGray, modifier = Modifier.padding(bottom = 8.dp))

        // CHAT SECTION
        if (activeSubSection == "Chat") {
            Column(modifier = Modifier.weight(1f)) {
                // Online Banner
                Surface(
                    color = VipCardGray,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(VipAccentGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Personal Shopper Online (Canal 1)", color = VipWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.weight(1f))
                        Text("Média de resposta: 1 min", color = VipTextGray, fontSize = 9.sp)
                    }
                }

                // Messages LazyColumn
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(chatMessages) { msg ->
                        val isUser = msg.sender == "user"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            if (!isUser) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(VipGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.SupportAgent, contentDescription = null, tint = VipBlack, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isUser) VipGold else VipCardGray
                                ),
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isUser) 16.dp else 0.dp,
                                    bottomEnd = if (isUser) 0.dp else 16.dp
                                ),
                                modifier = Modifier
                                    .widthIn(max = 280.dp)
                                    .testTag("chat_bubble_${msg.id}")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = msg.text,
                                        color = if (isUser) VipBlack else VipWhite,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Entregue",
                                        color = if (isUser) VipBlack.copy(alpha = 0.5f) else VipTextGray,
                                        fontSize = 8.sp,
                                        modifier = Modifier.align(Alignment.End)
                                    )
                                }
                            }

                            if (isUser) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(VipGold.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = VipGold, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Typing input panel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Simulated audio and file attachment buttons
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Anexo de imagem simulado!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.background(VipCardGray, CircleShape)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Simulate image upload", tint = VipGold)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Gravação de áudio simulada!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.background(VipCardGray, CircleShape)
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = "Simulate audio", tint = VipGold)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = chatTextInput,
                        onValueChange = { chatTextInput = it },
                        placeholder = { Text("Escreva ao Personal Shopper...", color = VipTextGray, fontSize = 13.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_text"),
                        shape = RoundedCornerShape(24.dp),
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
                    IconButton(
                        onClick = {
                            if (chatTextInput.isNotBlank()) {
                                viewModel.sendChatMessage(chatTextInput)
                                chatTextInput = ""
                            }
                        },
                        modifier = Modifier
                            .background(VipGold, CircleShape)
                            .testTag("send_chat_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send Message", tint = VipBlack, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // NOTIFICATIONS TRAYS SECTION
        else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Central de Avisos (${notifications.size})", color = VipWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                if (notifications.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.NotificationsOff, contentDescription = null, tint = VipTextGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nenhum aviso ou notificação recebida.", color = VipTextGray)
                    }
                } else {
                    notifications.forEach { item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { viewModel.markNotificationRead(item.id) },
                            color = if (item.isRead) VipCardGray.copy(alpha = 0.5f) else VipCardGray,
                            shape = RoundedCornerShape(12.dp),
                            border = if (!item.isRead) BorderStroke(1.dp, VipGold.copy(alpha = 0.4f)) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(if (item.isRead) VipDarkGray else VipGold.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (item.isRead) Icons.Default.Drafts else Icons.Default.MarkEmailUnread,
                                        contentDescription = null,
                                        tint = if (item.isRead) VipTextGray else VipGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, color = VipWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(item.description, color = VipTextGray, fontSize = 11.sp, lineHeight = 16.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Rastreado agora", color = VipTextGray, fontSize = 8.sp)
                                }

                                if (!item.isRead) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(VipGold, CircleShape)
                                            .align(Alignment.CenterVertically)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(100.dp))
    }
}
