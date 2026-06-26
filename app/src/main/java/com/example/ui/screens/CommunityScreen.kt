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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Channel
import com.example.data.Comment
import com.example.data.DealPost
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val channels by viewModel.channels.collectAsState()
    val dealPosts by viewModel.dealPosts.collectAsState()
    val savedDealPosts by viewModel.savedDealPosts.collectAsState()

    var activeTab by remember { mutableStateOf("Feed") } // Feed or Salvos
    var activeChannelFilterId by remember { mutableStateOf<Int?>(null) }
    var activePostForComments by remember { mutableStateOf<DealPost?>(null) }

    val filteredPosts = remember(dealPosts, activeTab, activeChannelFilterId, savedDealPosts) {
        val baseList = if (activeTab == "Feed") dealPosts else savedDealPosts
        if (activeChannelFilterId == null) baseList
        else baseList.filter { it.channelId == activeChannelFilterId }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COMUNIDADE VIP",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = VipGold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Canais oficiais de monitoramento de promoções",
                        fontSize = 12.sp,
                        color = VipTextGray
                    )
                }
            }

            // Tabs selector (Feed vs Saved)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Feed", "Salvos").forEach { tab ->
                    val isSelected = activeTab == tab
                    Surface(
                        modifier = Modifier
                            .clickable { activeTab = tab }
                            .border(1.dp, if (isSelected) VipGold else VipCardGray, RoundedCornerShape(12.dp)),
                        color = if (isSelected) VipGold.copy(alpha = 0.15f) else VipCardGray,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (tab == "Salvos") "📌 $tab" else "🔥 $tab",
                            color = if (isSelected) VipGold else VipWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            // Horizontal Channel List (Stories format)
            Text(
                text = "Canais Oficiais",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = VipWhite,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    // All channels filter
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { activeChannelFilterId = null }
                            .width(72.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (activeChannelFilterId == null) VipGold.copy(alpha = 0.2f) else VipCardGray,
                                    CircleShape
                                )
                                .border(
                                    2.dp,
                                    if (activeChannelFilterId == null) VipGold else Color.Transparent,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AllInclusive,
                                contentDescription = null,
                                tint = if (activeChannelFilterId == null) VipGold else VipWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Ver Todos",
                            color = VipWhite,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                items(channels) { channel ->
                    val isSelected = activeChannelFilterId == channel.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { activeChannelFilterId = channel.id }
                            .width(72.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(VipCardGray, CircleShape)
                                .border(
                                    2.dp,
                                    if (isSelected) VipGold else if (channel.isFollowing) VipGold.copy(alpha = 0.5f) else Color.Transparent,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Render custom icon per channel
                            val iconRes = when (channel.logoUrl) {
                                "nike" -> Icons.Default.SportsBasketball
                                "adidas" -> Icons.Default.SportsHandball
                                "centauro" -> Icons.Default.FitnessCenter
                                "amazon" -> Icons.Default.MenuBook
                                "mercadolivre" -> Icons.Default.LocalShipping
                                "shopee" -> Icons.Default.ShoppingBag
                                else -> Icons.Default.Group
                            }
                            Icon(
                                iconRes,
                                contentDescription = null,
                                tint = if (isSelected) VipGold else VipWhite,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            channel.name,
                            color = VipWhite,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        
                        // Follow Button Badge
                        Text(
                            text = if (channel.isFollowing) "Seguindo" else "Seguir",
                            color = if (channel.isFollowing) VipTextGray else VipGold,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    viewModel.toggleFollowChannel(channel.id, !channel.isFollowing)
                                }
                                .background(
                                    if (channel.isFollowing) VipDarkGray else VipGold.copy(alpha = 0.1f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = VipCardGray)

            // Feed of Posts
            if (filteredPosts.isEmpty()) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.PostAdd,
                        contentDescription = null,
                        tint = VipTextGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        if (activeTab == "Salvos") "Nenhuma promoção salva ainda."
                        else "Nenhuma promoção publicada neste canal.",
                        color = VipTextGray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredPosts) { post ->
                        val postChannel = channels.find { it.id == post.channelId }
                        DealPostCard(
                            post = post,
                            channel = postChannel,
                            onLikeClick = { viewModel.toggleLikePost(post.id, !post.isLiked) },
                            onSaveClick = { viewModel.toggleSavePost(post.id, !post.isSaved) },
                            onCommentClick = { activePostForComments = post },
                            onCopyCouponClick = { code ->
                                clipboardManager.setText(AnnotatedString(code))
                                Toast.makeText(context, "Cupom '$code' copiado!", Toast.LENGTH_SHORT).show()
                            },
                            onShareClick = {
                                val intent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, "Olha essa oferta VIP! ${post.title}\n${post.description}\nLink: ${post.externalLink ?: "Acesse no app!"}")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(intent, "Compartilhar Promoção"))
                            }
                        )
                    }
                }
            }
        }
    }

    // COMMENTS BOTTOM SHEET / DIALOG
    activePostForComments?.let { post ->
        val commentsList by viewModel.getCommentsForPost(post.id).collectAsState(initial = emptyList())
        CommentsDialog(
            post = post,
            comments = commentsList,
            onDismiss = { activePostForComments = null },
            onPostComment = { content ->
                viewModel.addComment(post.id, content)
            }
        )
    }
}

@Composable
fun DealPostCard(
    post: DealPost,
    channel: Channel?,
    onLikeClick: () -> Unit,
    onSaveClick: () -> Unit,
    onCommentClick: () -> Unit,
    onCopyCouponClick: (String) -> Unit,
    onShareClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("deal_post_${post.id}"),
        colors = CardDefaults.cardColors(containerColor = VipCardGray),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Channel Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Circular channel logo placeholder
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(VipDarkGray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Storefront,
                        contentDescription = null,
                        tint = VipGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = channel?.name ?: "Personal Shopper VIP",
                        color = VipWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Oferta Relâmpago • Ativa",
                        color = VipAccentGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Bookmark icon
                IconButton(onClick = onSaveClick) {
                    Icon(
                        imageVector = if (post.isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = "Save Offer",
                        tint = if (post.isSaved) VipGold else VipTextGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Content
            Text(
                text = post.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = VipWhite,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = post.description,
                fontSize = 13.sp,
                color = VipTextGray,
                lineHeight = 18.sp
            )

            // Coupon Code Banner (if exists)
            if (!post.couponCode.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(VipDarkGray, RoundedCornerShape(12.dp))
                        .border(BorderStroke(1.dp, VipGold.copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalActivity, contentDescription = null, tint = VipGold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("CUPOM DISPONÍVEL", fontSize = 9.sp, color = VipTextGray, fontWeight = FontWeight.Bold)
                            Text(post.couponCode, fontSize = 16.sp, color = VipGold, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Button(
                        onClick = { onCopyCouponClick(post.couponCode) },
                        colors = ButtonDefaults.buttonColors(containerColor = VipGold),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Copiar", color = VipBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // External link button
            post.externalLink?.let { link ->
                val context = LocalContext.current
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(38.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VipGold.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, VipGold.copy(alpha = 0.4f))
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Launch, contentDescription = null, tint = VipGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ir para Loja Oficial", color = VipGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Interactions Bar (Like, Comment, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Likes
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onLikeClick() }
                ) {
                    Icon(
                        imageVector = if (post.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (post.isLiked) VipAccentRed else VipTextGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        post.likesCount.toString(),
                        color = VipTextGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Comments
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCommentClick() }
                ) {
                    Icon(
                        Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = VipTextGray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        post.commentsCount.toString(),
                        color = VipTextGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Share
                IconButton(onClick = onShareClick, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = VipTextGray,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CommentsDialog(
    post: DealPost,
    comments: List<Comment>,
    onDismiss: () -> Unit,
    onPostComment: (String) -> Unit
) {
    var textInput by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .border(1.dp, VipGold.copy(alpha = 0.3f), RoundedCornerShape(20.dp)),
            colors = CardDefaults.cardColors(containerColor = VipDarkGray),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Comentários (${comments.size})", color = VipWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = VipWhite)
                    }
                }

                HorizontalDivider(color = VipCardGray, modifier = Modifier.padding(vertical = 8.dp))

                // List of Comments
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (comments.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Ninguém comentou ainda. Seja o primeiro!", color = VipTextGray, fontSize = 13.sp)
                            }
                        }
                    } else {
                        items(comments) { comment ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(VipCardGray, RoundedCornerShape(12.dp))
                                    .padding(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .background(VipGold.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = VipGold, modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(comment.userName, color = VipGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("Agora", color = VipTextGray, fontSize = 9.sp)
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(comment.content, color = VipWhite, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // Add comment row
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Escreva um comentário...", color = VipTextGray, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
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
                            if (textInput.isNotBlank()) {
                                onPostComment(textInput)
                                textInput = ""
                            }
                        },
                        modifier = Modifier.background(VipGold, CircleShape)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, tint = VipBlack, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
