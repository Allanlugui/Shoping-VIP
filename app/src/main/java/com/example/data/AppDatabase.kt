package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        Product::class,
        Channel::class,
        DealPost::class,
        Comment::class,
        CartItem::class,
        UserProfile::class,
        AffiliateDashboard::class,
        ChatMessage::class,
        NotificationItem::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun channelDao(): ChannelDao
    abstract fun dealPostDao(): DealPostDao
    abstract fun commentDao(): CommentDao
    abstract fun cartDao(): CartDao
    abstract fun userDao(): UserDao
    abstract fun affiliateDao(): AffiliateDao
    abstract fun chatDao(): ChatDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shopping_vip_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
