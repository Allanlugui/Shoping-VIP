package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = DatabaseRepository(db)

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filter states
    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedBrand = MutableStateFlow<String?>(null)
    val selectedBrand: StateFlow<String?> = _selectedBrand.asStateFlow()

    private val _priceRange = MutableStateFlow<ClosedFloatingPointRange<Float>>(0f..3000f)
    val priceRange: StateFlow<ClosedFloatingPointRange<Float>> = _priceRange.asStateFlow()

    // Core Data Streams
    val products: StateFlow<List<Product>> = _searchQuery
        .combine(_selectedCategory) { query, category -> Pair(query, category) }
        .combine(_selectedBrand) { pair, brand -> Triple(pair.first, pair.second, brand) }
        .flatMapLatest { (query, category, brand) ->
            if (query.isNotEmpty()) {
                repository.searchProducts(query)
            } else {
                repository.products
            }.map { list ->
                list.filter { prod ->
                    (category == null || prod.category.equals(category, ignoreCase = true)) &&
                    (brand == null || prod.brand.equals(brand, ignoreCase = true)) &&
                    (prod.promoPrice ?: prod.price).toFloat() in _priceRange.value
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val channels: StateFlow<List<Channel>> = repository.channels
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val userProfile: StateFlow<UserProfile> = repository.userProfile
        .map { it ?: UserProfile() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserProfile()
        )

    val affiliateDashboard: StateFlow<AffiliateDashboard> = repository.affiliateDashboard
        .map { it ?: AffiliateDashboard() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AffiliateDashboard()
        )

    val chatMessages: StateFlow<List<ChatMessage>> = repository.chatMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val notifications: StateFlow<List<NotificationItem>> = repository.notifications
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dealPosts: StateFlow<List<DealPost>> = repository.getAllDealPosts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savedDealPosts: StateFlow<List<DealPost>> = repository.getSavedDealPosts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Categories helper stream
    val categories: StateFlow<List<String>> = repository.products
        .map { prods -> prods.map { it.category }.distinct() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Brands helper stream
    val brands: StateFlow<List<String>> = repository.products
        .map { prods -> prods.map { it.brand }.distinct() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    // Action Methods
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: String?) {
        _selectedCategory.value = category
    }

    fun setBrandFilter(brand: String?) {
        _selectedBrand.value = brand
    }

    fun setPriceRangeFilter(range: ClosedFloatingPointRange<Float>) {
        _priceRange.value = range
    }

    fun resetFilters() {
        _selectedCategory.value = null
        _selectedBrand.value = null
        _priceRange.value = 0f..3000f
        _searchQuery.value = ""
    }

    // Cart Operations
    fun addToCart(product: Product, quantity: Int, size: String, color: String) {
        viewModelScope.launch {
            repository.addToCart(product.id, quantity, size, color)
            repository.addNotification(
                "🛒 Item adicionado ao carrinho!",
                "${product.name} (${size}, ${color}) adicionado com sucesso."
            )
        }
    }

    fun updateCartQuantity(cartItemId: Int, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItemId, quantity)
        }
    }

    fun removeFromCart(cartItemId: Int) {
        viewModelScope.launch {
            repository.removeFromCart(cartItemId)
        }
    }

    // Checkout
    fun checkoutCart(paymentMethod: String, couponCode: String, discountAmount: Double) {
        viewModelScope.launch {
            val items = cartItems.value
            if (items.isEmpty()) return@launch

            // Retrieve products to calculate total
            val allProds = products.value
            var subTotal = 0.0
            for (item in items) {
                val prod = allProds.find { it.id == item.productId }
                if (prod != null) {
                    val priceToUse = prod.promoPrice ?: prod.price
                    subTotal += priceToUse * item.quantity
                }
            }

            val finalAmount = maxOf(0.0, subTotal - discountAmount)
            repository.addPurchase(finalAmount)
            repository.clearCart()
        }
    }

    // Channel/Offer Operations
    fun toggleFollowChannel(channelId: Int, isFollowing: Boolean) {
        viewModelScope.launch {
            repository.toggleFollowChannel(channelId, isFollowing)
            val channelName = channels.value.find { it.id == channelId }?.name ?: "Canal"
            repository.addNotification(
                if (isFollowing) "🔔 Seguindo $channelName!" else "🔕 Deixou de seguir $channelName",
                if (isFollowing) "Você receberá atualizações em tempo real deste canal."
                else "Você não receberá mais notificações deste canal."
            )
        }
    }

    fun toggleLikePost(postId: Int, isLiked: Boolean) {
        viewModelScope.launch {
            repository.toggleLikePost(postId, isLiked)
        }
    }

    fun toggleSavePost(postId: Int, isSaved: Boolean) {
        viewModelScope.launch {
            repository.toggleSavePost(postId, isSaved)
            repository.addNotification(
                if (isSaved) "📌 Promoção salva!" else "🗑️ Promoção removida dos salvos",
                if (isSaved) "Você pode acessar esta promoção na aba Comunidade a qualquer momento."
                else "Promoção removida do seu painel de favoritos."
            )
        }
    }

    fun getCommentsForPost(postId: Int): Flow<List<Comment>> {
        return repository.getCommentsForPost(postId)
    }

    fun addComment(postId: Int, content: String) {
        viewModelScope.launch {
            repository.addComment(
                Comment(
                    postId = postId,
                    userName = userProfile.value.name,
                    userAvatar = "user_avatar",
                    content = content
                )
            )
        }
    }

    // Chat
    fun sendChatMessage(text: String) {
        viewModelScope.launch {
            repository.sendChatMessage(text, "user")
        }
    }

    // VIP Subscription
    fun toggleVipSubscription() {
        viewModelScope.launch {
            repository.toggleVipSubscription()
        }
    }

    // Loyalty Rewards
    fun redeemReward(points: Int, rewardDescription: String): Flow<Boolean> {
        val resultFlow = MutableStateFlow(false)
        viewModelScope.launch {
            val success = repository.redeemPoints(points, rewardDescription)
            resultFlow.value = success
        }
        return resultFlow.asStateFlow()
    }

    // Affiliate
    fun addClickToAffiliate() {
        viewModelScope.launch {
            repository.addClickToAffiliate()
        }
    }

    // Notification Read
    fun markNotificationRead(id: Int) {
        viewModelScope.launch {
            repository.markNotificationRead(id)
        }
    }

    // Push Notification Simulation
    fun triggerSimulatedPushNotification(title: String, desc: String) {
        viewModelScope.launch {
            repository.addNotification(title, desc)
        }
    }

    // Bulk Import of Products Simulation (CSV/Excel/XML)
    fun importProductsFromText(text: String, format: String): String {
        try {
            val lines = text.split("\n").filter { it.isNotBlank() }
            if (lines.size <= 1) return "Formato inválido ou sem dados suficientes."

            var importedCount = 0
            val newProductsList = mutableListOf<Product>()

            when (format.uppercase()) {
                "CSV", "TXT" -> {
                    // Expect format: Name, Brand, Category, SKU, Stock, Price, StoreName
                    // Skip header
                    for (i in 1 until lines.size) {
                        val parts = lines[i].split(",").map { it.trim() }
                        if (parts.size >= 7) {
                            val name = parts[0]
                            val brand = parts[1]
                            val category = parts[2]
                            val sku = parts[3]
                            val stock = parts[4].toIntOrNull() ?: 5
                            val price = parts[5].toDoubleOrNull() ?: 99.90
                            val storeName = parts[6]
                            
                            newProductsList.add(
                                Product(
                                    name = name,
                                    brand = brand,
                                    category = category,
                                    sku = sku,
                                    stock = stock,
                                    price = price,
                                    imageUrl = "imported_placeholder",
                                    storeName = storeName,
                                    description = "Produto importado via painel administrativo em massa ($format)."
                                )
                            )
                            importedCount++
                        }
                    }
                }
                "XML" -> {
                    // Simpler parse just finding nodes: <product><name>X</name>...</product>
                    // Because it's a simulation, we detect simple XML keywords
                    val content = text
                    val pattern = Regex("<product>([\\s\\S]*?)</product>")
                    val matches = pattern.findAll(content)
                    for (match in matches) {
                        val inner = match.groupValues[1]
                        val name = Regex("<name>(.*?)</name>").find(inner)?.groupValues?.get(1) ?: "Importado XML"
                        val brand = Regex("<brand>(.*?)</brand>").find(inner)?.groupValues?.get(1) ?: "Generico"
                        val category = Regex("<category>(.*?)</category>").find(inner)?.groupValues?.get(1) ?: "Outros"
                        val sku = Regex("<sku>(.*?)</sku>").find(inner)?.groupValues?.get(1) ?: "XML-SKU"
                        val stock = Regex("<stock>(.*?)</stock>").find(inner)?.groupValues?.get(1)?.toIntOrNull() ?: 10
                        val price = Regex("<price>(.*?)</price>").find(inner)?.groupValues?.get(1)?.toDoubleOrNull() ?: 149.90
                        val store = Regex("<store>(.*?)</store>").find(inner)?.groupValues?.get(1) ?: "Canal VIP"

                        newProductsList.add(
                            Product(
                                name = name,
                                brand = brand,
                                category = category,
                                sku = sku,
                                stock = stock,
                                price = price,
                                imageUrl = "imported_placeholder",
                                storeName = store,
                                description = "Produto importado via XML em lote."
                            )
                        )
                        importedCount++
                    }
                }
            }

            if (newProductsList.isNotEmpty()) {
                viewModelScope.launch {
                    repository.insertProducts(newProductsList)
                    repository.addNotification(
                        "📥 Importação concluída!",
                        "Foram cadastrados com sucesso $importedCount produtos via arquivo $format."
                    )
                }
                return "Sucesso: $importedCount produtos importados!"
            } else {
                return "Erro: Nenhum produto pôde ser lido. Verifique o padrão de colunas."
            }
        } catch (e: Exception) {
            return "Erro na importação: ${e.localizedMessage}"
        }
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
