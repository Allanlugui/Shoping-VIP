package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DatabaseRepository(private val db: AppDatabase) {

    val products: Flow<List<Product>> = db.productDao().getAllProducts()
    val channels: Flow<List<Channel>> = db.channelDao().getAllChannels()
    val cartItems: Flow<List<CartItem>> = db.cartDao().getCartItems()
    val userProfile: Flow<UserProfile?> = db.userDao().getUserProfile()
    val affiliateDashboard: Flow<AffiliateDashboard?> = db.affiliateDao().getDashboard()
    val chatMessages: Flow<List<ChatMessage>> = db.chatDao().getMessages()
    val notifications: Flow<List<NotificationItem>> = db.notificationDao().getNotifications()

    fun getProductById(id: Int): Flow<Product?> = db.productDao().getProductById(id)
    fun getProductsByCategory(category: String): Flow<List<Product>> = db.productDao().getProductsByCategory(category)
    fun searchProducts(query: String): Flow<List<Product>> = db.productDao().searchProducts(query)

    fun getPostsForChannel(channelId: Int): Flow<List<DealPost>> = db.dealPostDao().getPostsForChannel(channelId)
    fun getAllDealPosts(): Flow<List<DealPost>> = db.dealPostDao().getAllPosts()
    fun getSavedDealPosts(): Flow<List<DealPost>> = db.dealPostDao().getSavedPosts()

    fun getCommentsForPost(postId: Int): Flow<List<Comment>> = db.commentDao().getCommentsForPost(postId)

    // Suspend operations for database write
    suspend fun insertProduct(product: Product) = withContext(Dispatchers.IO) {
        db.productDao().insertProduct(product)
    }

    suspend fun insertProducts(products: List<Product>) = withContext(Dispatchers.IO) {
        db.productDao().insertProducts(products)
    }

    suspend fun deleteProduct(id: Int) = withContext(Dispatchers.IO) {
        db.productDao().deleteProduct(id)
    }

    suspend fun insertChannel(channel: Channel) = withContext(Dispatchers.IO) {
        db.channelDao().insertChannel(channel)
    }

    suspend fun toggleFollowChannel(channelId: Int, isFollowing: Boolean) = withContext(Dispatchers.IO) {
        val offset = if (isFollowing) 1 else -1
        db.channelDao().updateChannelFollowStatus(channelId, isFollowing, offset)
    }

    suspend fun insertDealPost(post: DealPost) = withContext(Dispatchers.IO) {
        db.dealPostDao().insertPost(post)
    }

    suspend fun toggleLikePost(postId: Int, isLiked: Boolean) = withContext(Dispatchers.IO) {
        val offset = if (isLiked) 1 else -1
        db.dealPostDao().updateLikeStatus(postId, isLiked, offset)
    }

    suspend fun toggleSavePost(postId: Int, isSaved: Boolean) = withContext(Dispatchers.IO) {
        db.dealPostDao().updateSaveStatus(postId, isSaved)
    }

    suspend fun addComment(comment: Comment) = withContext(Dispatchers.IO) {
        db.commentDao().insertComment(comment)
        db.dealPostDao().incrementCommentCount(comment.postId)
    }

    suspend fun addToCart(productId: Int, quantity: Int, size: String, color: String) = withContext(Dispatchers.IO) {
        db.cartDao().insertCartItem(CartItem(productId = productId, quantity = quantity, size = size, color = color))
    }

    suspend fun updateCartQuantity(cartItemId: Int, quantity: Int) = withContext(Dispatchers.IO) {
        db.cartDao().updateCartItemQuantity(cartItemId, quantity)
    }

    suspend fun removeFromCart(cartItemId: Int) = withContext(Dispatchers.IO) {
        db.cartDao().deleteCartItem(cartItemId)
    }

    suspend fun clearCart() = withContext(Dispatchers.IO) {
        db.cartDao().clearCart()
    }

    suspend fun saveUserProfile(profile: UserProfile) = withContext(Dispatchers.IO) {
        db.userDao().insertUserProfile(profile)
    }

    suspend fun addPurchase(amount: Double) = withContext(Dispatchers.IO) {
        val currentProfile = db.userDao().getUserProfile().firstOrNull() ?: UserProfile()
        val newSpent = currentProfile.totalSpent + amount
        val newPoints = currentProfile.points + amount.toInt() // 1 Real = 1 Ponto
        val newLevel = when {
            newSpent >= 10000.0 -> "Diamante"
            newSpent >= 5000.0 -> "Ouro"
            newSpent >= 1000.0 -> "Prata"
            else -> "Bronze"
        }
        db.userDao().insertUserProfile(
            currentProfile.copy(
                totalSpent = newSpent,
                points = newPoints,
                level = newLevel
            )
        )
        
        // Also update affiliate dashboard with simulated conversion (10% commission)
        val currentAff = db.affiliateDao().getDashboard().firstOrNull() ?: AffiliateDashboard()
        db.affiliateDao().insertDashboard(
            currentAff.copy(
                conversions = currentAff.conversions + 1,
                salesAmount = currentAff.salesAmount + amount,
                earnings = currentAff.earnings + (amount * 0.1)
            )
        )

        // Generate a simulated success notification
        db.notificationDao().insertNotification(
            NotificationItem(
                title = "🎁 Compra Aprovada! Pontos creditados!",
                description = "Você gastou R$${String.format("%.2f", amount)} e acumulou ${amount.toInt()} pontos no Programa de Fidelidade."
            )
        )
    }

    suspend fun redeemPoints(points: Int, rewardDescription: String) = withContext(Dispatchers.IO) {
        val currentProfile = db.userDao().getUserProfile().firstOrNull() ?: UserProfile()
        if (currentProfile.points >= points) {
            db.userDao().insertUserProfile(
                currentProfile.copy(points = currentProfile.points - points)
            )
            // Send notification
            db.notificationDao().insertNotification(
                NotificationItem(
                    title = "🎉 Resgate Efetuado!",
                    description = "Você resgatou: $rewardDescription por $points pontos."
                )
            )
            return@withContext true
        }
        return@withContext false
    }

    suspend fun toggleVipSubscription() = withContext(Dispatchers.IO) {
        val currentProfile = db.userDao().getUserProfile().firstOrNull() ?: UserProfile()
        val nextVipState = !currentProfile.isVip
        db.userDao().insertUserProfile(currentProfile.copy(isVip = nextVipState))
        
        db.notificationDao().insertNotification(
            NotificationItem(
                title = if (nextVipState) "👑 Você agora é VIP!" else "⚠️ Assinatura VIP Cancelada",
                description = if (nextVipState) "Acesso ilimitado a ofertas secretas, suporte 24h e cashback extra ativado!"
                              else "Sentiremos sua falta! Seus benefícios VIP foram encerrados."
            )
        )
    }

    suspend fun addClickToAffiliate() = withContext(Dispatchers.IO) {
        val currentAff = db.affiliateDao().getDashboard().firstOrNull() ?: AffiliateDashboard()
        db.affiliateDao().insertDashboard(currentAff.copy(clicks = currentAff.clicks + 1))
    }

    suspend fun sendChatMessage(text: String, sender: String = "user") = withContext(Dispatchers.IO) {
        db.chatDao().insertMessage(ChatMessage(sender = sender, text = text))
        
        // Auto-reply simulation from Personal Shopper after 1 second (handled at ViewModel or Composable level, but we can do a simple trigger)
        if (sender == "user") {
            // Simulated response from Personal Shopper Support
            val responseText = when {
                text.contains("olá", ignoreCase = true) || text.contains("oi", ignoreCase = true) -> 
                    "Olá! Seja muito bem-vindo ao VIP Shopping Support. Como posso te ajudar com suas compras premium hoje?"
                text.contains("vip", ignoreCase = true) -> 
                    "Nossos assinantes VIP têm cupom exclusivo de 20% OFF extra, atendimento no WhatsApp prioritário e cashback em dobro! Deseja assinar na aba VIP?"
                text.contains("frete", ignoreCase = true) -> 
                    "O frete é grátis para todo o Brasil para compras acima de R$ 250, ou para qualquer valor se você for VIP!"
                text.contains("rastreio", ignoreCase = true) || text.contains("pedido", ignoreCase = true) -> 
                    "Seu último pedido já está em rota de entrega! Você receberá o código de rastreamento por e-mail e SMS em breve."
                else -> "Entendi! Vou analisar sua solicitação agora mesmo. Um de nossos Personal Shoppers humanos já está olhando para a sua mensagem. Aguarde um instante!"
            }
            db.chatDao().insertMessage(ChatMessage(sender = "support", text = responseText))
        }
    }

    suspend fun addNotification(title: String, desc: String) = withContext(Dispatchers.IO) {
        db.notificationDao().insertNotification(NotificationItem(title = title, description = desc))
    }

    suspend fun markNotificationRead(id: Int) = withContext(Dispatchers.IO) {
        db.notificationDao().markAsRead(id)
    }

    suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existingProducts = db.productDao().getAllProducts().firstOrNull()
        if (existingProducts.isNullOrEmpty()) {
            // 1. Seed User Profile & Affiliate
            db.userDao().insertUserProfile(UserProfile(name = "Ana Silva", email = "ana.silva@vipmail.com", points = 350, totalSpent = 450.0))
            db.affiliateDao().insertDashboard(AffiliateDashboard(clicks = 124, conversions = 18, salesAmount = 5600.0, earnings = 560.0))

            // 2. Seed Channels
            val seedChannels = listOf(
                Channel(id = 1, name = "Nike Outlet", description = "As melhores ofertas de ponta de estoque Nike.", logoUrl = "nike", followersCount = 14500),
                Channel(id = 2, name = "Adidas Outlet", description = "Descontos de até 60% em tênis e roupas Adidas.", logoUrl = "adidas", followersCount = 9800),
                Channel(id = 3, name = "Centauro Esportes", description = "Sua loja de esportes com promoções relâmpago diárias.", logoUrl = "centauro", followersCount = 12200),
                Channel(id = 4, name = "Amazon VIP", description = "Eletrônicos, livros e utilidades domésticas com menor preço.", logoUrl = "amazon", followersCount = 28900),
                Channel(id = 5, name = "Mercado Livre Prime", description = "Ofertas selecionadas com Frete Full e Entrega Rápida.", logoUrl = "mercadolivre", followersCount = 35000),
                Channel(id = 6, name = "Shopee Oficial", description = "Os cupons de frete grátis e descontos da Shopee mais quentes.", logoUrl = "shopee", followersCount = 42000)
            )
            db.channelDao().insertChannels(seedChannels)

            // 3. Seed Products
            val seedProducts = listOf(
                Product(
                    id = 1,
                    name = "Tênis Nike Air Max 90 Premium",
                    description = "O Nike Air Max 90 permanece fiel às suas raízes esportivas clássicas com a icônica sola Waffle, sobreposições costuradas duráveis e detalhes em TPU. Um clássico moderno de alta durabilidade e estilo impecável.",
                    brand = "Nike",
                    category = "Calçados",
                    sku = "NK-AM90-PRM",
                    stock = 15,
                    price = 899.90,
                    promoPrice = 599.90,
                    imageUrl = "nike_airmax",
                    isFeatured = true,
                    sizeOptions = "38, 39, 40, 41, 42, 43",
                    colorOptions = "Preto/Dourado, Branco/Cinza, All Black",
                    rating = 4.8f,
                    storeName = "Nike Outlet"
                ),
                Product(
                    id = 2,
                    name = "Tênis Adidas Ultraboost 22",
                    description = "Conforto e responsividade máximos. A entressola BOOST proporciona amortecimento infinito, enquanto o cabedal Primeknit oferece ajuste confortável que envolve seus pés com perfeição.",
                    brand = "Adidas",
                    category = "Calçados",
                    sku = "AD-UB22-M",
                    stock = 8,
                    price = 1199.90,
                    promoPrice = 799.90,
                    imageUrl = "adidas_ub",
                    isFeatured = true,
                    sizeOptions = "39, 40, 41, 42, 43, 44",
                    colorOptions = "Azul Marinho, Core Black, Cloud White",
                    rating = 4.9f,
                    storeName = "Adidas Outlet"
                ),
                Product(
                    id = 3,
                    name = "Tênis Puma Slipstream Retro",
                    description = "Nascido nas quadras de basquete nos anos 80, o Puma Slipstream é um ícone clássico do streetwear que mantém o design original com toque premium de couro macio e estilo vintage incomparável.",
                    brand = "Puma",
                    category = "Calçados",
                    sku = "PM-SLIP-RET",
                    stock = 25,
                    price = 599.90,
                    promoPrice = 399.90,
                    imageUrl = "puma_slipstream",
                    isFeatured = false,
                    sizeOptions = "37, 38, 39, 40, 41, 42",
                    colorOptions = "Branco/Verde, Branco/Preto",
                    rating = 4.7f,
                    storeName = "Centauro Esportes"
                ),
                Product(
                    id = 4,
                    name = "Kindle Paperwhite 16GB Wi-Fi",
                    description = "Agora com tela de 6,8” e bordas mais finas, temperatura de luz ajustável, bateria com duração de até 10 semanas e passagens de página 20% mais rápidas. À prova d'água.",
                    brand = "Amazon",
                    category = "Eletrônicos",
                    sku = "AMZ-KND-PW16",
                    stock = 50,
                    price = 799.00,
                    promoPrice = 699.00,
                    imageUrl = "kindle_paperwhite",
                    isFeatured = true,
                    sizeOptions = "16 GB",
                    colorOptions = "Preto",
                    rating = 4.9f,
                    storeName = "Amazon VIP"
                ),
                Product(
                    id = 5,
                    name = "Apple AirPods Pro (2ª Geração)",
                    description = "Cancelamento Ativo de Ruído até duas vezes melhor. Modo de Transparência Adaptativa reduz ruídos externos altos. Áudio Espacial Personalizado com rastreamento dinâmico da cabeça.",
                    brand = "Apple",
                    category = "Eletrônicos",
                    sku = "APL-APP2-GEN",
                    stock = 12,
                    price = 2299.00,
                    promoPrice = 1599.00,
                    imageUrl = "airpods_pro",
                    isFeatured = true,
                    sizeOptions = "Único",
                    colorOptions = "Branco",
                    rating = 4.9f,
                    storeName = "Mercado Livre Prime"
                ),
                Product(
                    id = 6,
                    name = "Caixa de Som Portátil JBL Charge 5",
                    description = "Leve a festa com você sob qualquer clima. A JBL Charge 5 oferece o som profissional ousado da JBL, com driver de longa excursão otimizado, tweeter separado e radiadores de graves duplos da JBL de alta potência.",
                    brand = "JBL",
                    category = "Eletrônicos",
                    sku = "JBL-CHG5-BLK",
                    stock = 20,
                    price = 999.00,
                    promoPrice = 749.00,
                    imageUrl = "jbl_charge5",
                    isFeatured = false,
                    sizeOptions = "Único",
                    colorOptions = "Preto Matte, Vermelho Esportivo, Camuflado",
                    rating = 4.7f,
                    storeName = "Shopee Oficial"
                ),
                Product(
                    id = 7,
                    name = "Jaqueta Corta Vento Nike Essential",
                    description = "Mantenha o ritmo sob vento ou chuva leve. Esta jaqueta impermeável clássica é feita com tecido leve de nylon reciclado, trazendo bolsos de segurança com zíper e capuz ajustável.",
                    brand = "Nike",
                    category = "Vestuário",
                    sku = "NK-CV-ESS",
                    stock = 18,
                    price = 399.90,
                    promoPrice = 279.90,
                    imageUrl = "nike_windbreaker",
                    isFeatured = false,
                    sizeOptions = "P, M, G, GG",
                    colorOptions = "Preto, Branco, Cinza",
                    rating = 4.6f,
                    storeName = "Nike Outlet"
                ),
                Product(
                    id = 8,
                    name = "Moletom Adidas Trefoil Classic",
                    description = "O autêntico estilo esportivo Adidas. Este moletom com capuz é confeccionado em moletinho de algodão ultra macio e traz o logo icônico do trevo estampado no peito com visual vintage.",
                    brand = "Adidas",
                    category = "Vestuário",
                    sku = "AD-ML-TRE",
                    stock = 30,
                    price = 349.90,
                    promoPrice = 229.90,
                    imageUrl = "adidas_hoodie",
                    isFeatured = false,
                    sizeOptions = "P, M, G, GG, XG",
                    colorOptions = "Cinza Mescla, All Black, Verde Floresta",
                    rating = 4.8f,
                    storeName = "Adidas Outlet"
                )
            )
            db.productDao().insertProducts(seedProducts)

            // 4. Seed Deal Posts
            val seedPosts = listOf(
                DealPost(
                    id = 1,
                    channelId = 1,
                    title = "🔥 CUPOM DE 30% OFF EXTRA NA OUTLET NIKE!",
                    description = "Cupom exclusivo funcionando em todo o site e produtos de outlet! Use o código: NIKE30EXTRA e garanta o Air Max 90 Premium por R$599,90 ou Jaqueta Corta Vento por apenas R$279,90! Estoque renovado agora de madrugada.",
                    imageUrl = "post_nike",
                    couponCode = "NIKE30EXTRA",
                    externalLink = "https://www.nike.com.br",
                    likesCount = 342,
                    commentsCount = 2,
                    timestamp = System.currentTimeMillis() - 3600000 // 1 hour ago
                ),
                DealPost(
                    id = 2,
                    channelId = 2,
                    title = "⚡ OFERTA RELÂMPAGO: Ultraboost 22 pela METADE DO PREÇO!",
                    description = "Inacreditável! O Adidas Ultraboost 22, o tênis mais confortável do mundo, está saindo de R$1199,90 por apenas R$799,90 na Outlet Oficial. Ative o link e o desconto de R$400 será aplicado direto no carrinho!",
                    imageUrl = "post_adidas",
                    couponCode = "AUTOMATICO",
                    externalLink = "https://www.adidas.com.br",
                    likesCount = 512,
                    commentsCount = 1,
                    timestamp = System.currentTimeMillis() - 7200000 // 2 hours ago
                ),
                DealPost(
                    id = 3,
                    channelId = 5,
                    title = "🎁 AirPods Pro Geração 2 com Menor Preço em 12 meses!",
                    description = "De R$ 2.299 por R$ 1.599 em até 10x sem juros com frete grátis full para todo o país! Link promocional ativo na Shopee e no Mercado Livre Prime. Se você for VIP, ganha R$ 80 de cashback de volta!",
                    imageUrl = "post_airpods",
                    couponCode = "MLPRIME100",
                    externalLink = "https://www.mercadolivre.com.br",
                    likesCount = 1205,
                    commentsCount = 0,
                    timestamp = System.currentTimeMillis() - 14400000 // 4 hours ago
                ),
                DealPost(
                    id = 4,
                    channelId = 4,
                    title = "📚 Kindle Paperwhite 16GB em Promoção na Amazon VIP!",
                    description = "Aproveite! O Kindle Paperwhite 16GB está com R$100 de desconto direto para assinantes Prime/VIP. Perfeito para leitura noturna e à prova d'água. Compre e ganhe 699 pontos de fidelidade no ato!",
                    imageUrl = "post_kindle",
                    couponCode = "KINDLE100",
                    externalLink = "https://www.amazon.com.br",
                    likesCount = 210,
                    commentsCount = 0,
                    timestamp = System.currentTimeMillis() - 28800000 // 8 hours ago
                )
            )
            db.dealPostDao().insertPosts(seedPosts)

            // 5. Seed Comments
            db.commentDao().insertComment(Comment(id = 1, postId = 1, userName = "Carlos Eduardo", userAvatar = "avatar1", content = "Acabei de comprar o Air Max 90! Funcionou perfeitamente o cupom, valeu demais Personal Shopper!"))
            db.commentDao().insertComment(Comment(id = 2, postId = 1, userName = "Julia Mendonça", userAvatar = "avatar2", content = "Cupom maravilhoso! Consegui a jaqueta corta vento preta tamanho M. Frete deu grátis."))
            db.commentDao().insertComment(Comment(id = 3, postId = 2, userName = "Rodrigo Alencar", userAvatar = "avatar3", content = "Ultraboost é surreal. Esse preço está mais barato que nos EUA."))

            // 6. Seed Notifications
            db.notificationDao().insertNotification(
                NotificationItem(
                    title = "🔥 Bem-vindo ao SHOPPING VIP!",
                    description = "Explore os canais de ofertas exclusivas das suas lojas favoritas e acumule pontos a cada compra realizada dentro do app!"
                )
            )
            
            // 7. Seed Initial Support Message
            db.chatDao().insertMessage(ChatMessage(sender = "support", text = "Olá, Ana! Sou o seu Personal Shopper exclusivo. Estou aqui para ajudar você a encontrar os melhores preços, cupons e produtos importados. O que você está buscando hoje?"))
        }
    }
}
