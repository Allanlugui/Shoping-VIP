package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val brand: String,
    val category: String,
    val sku: String,
    val stock: Int,
    val price: Double,
    val promoPrice: Double? = null,
    val imageUrl: String,
    val isFeatured: Boolean = false,
    val sizeOptions: String = "", // e.g. "P, M, G" or "38, 40, 42"
    val colorOptions: String = "", // e.g. "Preto, Branco, Vermelho"
    val rating: Float = 4.5f,
    val storeName: String = "",
    val videoUrl: String? = null
)

@Entity(tableName = "channels")
data class Channel(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val logoUrl: String,
    val followersCount: Int = 1200,
    val isFollowing: Boolean = false
)

@Entity(tableName = "deal_posts")
data class DealPost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val channelId: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
    val couponCode: String? = null,
    val externalLink: String? = null,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "comments")
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val postId: Int,
    val userName: String,
    val userAvatar: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productId: Int,
    val quantity: Int,
    val size: String,
    val color: String
)

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1, // Fixed ID for local single user
    val name: String = "VIP Client",
    val email: String = "client@shoppingvip.com",
    val level: String = "Bronze", // Bronze, Prata, Ouro, Diamante
    val totalSpent: Double = 0.0,
    val points: Int = 0,
    val isVip: Boolean = false,
    val referralLink: String = "https://shoppingvip.app/invite/vipclient1"
)

@Entity(tableName = "affiliate_dashboard")
data class AffiliateDashboard(
    @PrimaryKey val id: Int = 1, // Fixed ID
    val clicks: Int = 45,
    val conversions: Int = 12,
    val salesAmount: Double = 3450.0,
    val earnings: Double = 345.0
)

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "user", "admin", "support"
    val text: String,
    val imageUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)
