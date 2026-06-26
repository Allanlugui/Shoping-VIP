package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE category = :category ORDER BY id DESC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    fun getProductById(id: Int): Flow<Product?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Int)

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' OR storeName LIKE '%' || :query || '%'")
    fun searchProducts(query: String): Flow<List<Product>>
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY followersCount DESC")
    fun getAllChannels(): Flow<List<Channel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannel(channel: Channel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<Channel>)

    @Query("UPDATE channels SET isFollowing = :isFollowing, followersCount = followersCount + :offset WHERE id = :channelId")
    suspend fun updateChannelFollowStatus(channelId: Int, isFollowing: Boolean, offset: Int)
}

@Dao
interface DealPostDao {
    @Query("SELECT * FROM deal_posts ORDER BY timestamp DESC")
    fun getAllPosts(): Flow<List<DealPost>>

    @Query("SELECT * FROM deal_posts WHERE channelId = :channelId ORDER BY timestamp DESC")
    fun getPostsForChannel(channelId: Int): Flow<List<DealPost>>

    @Query("SELECT * FROM deal_posts WHERE isSaved = 1 ORDER BY timestamp DESC")
    fun getSavedPosts(): Flow<List<DealPost>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: DealPost)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPosts(posts: List<DealPost>)

    @Query("UPDATE deal_posts SET isLiked = :isLiked, likesCount = likesCount + :offset WHERE id = :postId")
    suspend fun updateLikeStatus(postId: Int, isLiked: Boolean, offset: Int)

    @Query("UPDATE deal_posts SET isSaved = :isSaved WHERE id = :postId")
    suspend fun updateSaveStatus(postId: Int, isSaved: Boolean)

    @Query("UPDATE deal_posts SET commentsCount = commentsCount + 1 WHERE id = :postId")
    suspend fun incrementCommentCount(postId: Int)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Int): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items ORDER BY id DESC")
    fun getCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItem)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateCartItemQuantity(id: Int, quantity: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface UserDao {
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)

    @Query("UPDATE user_profile SET totalSpent = :totalSpent, points = :points, level = :level, isVip = :isVip WHERE id = 1")
    suspend fun updateUserProfileSpent(totalSpent: Double, points: Int, level: String, isVip: Boolean)
}

@Dao
interface AffiliateDao {
    @Query("SELECT * FROM affiliate_dashboard WHERE id = 1")
    fun getDashboard(): Flow<AffiliateDashboard?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDashboard(dashboard: AffiliateDashboard)

    @Query("UPDATE affiliate_dashboard SET clicks = clicks + :clicksOffset, conversions = conversions + :conversionsOffset, salesAmount = salesAmount + :salesOffset, earnings = earnings + :earningsOffset WHERE id = 1")
    suspend fun updateDashboardMetrics(clicksOffset: Int, conversionsOffset: Int, salesOffset: Double, earningsOffset: Double)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getMessages(): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getNotifications(): Flow<List<NotificationItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Int)
}
