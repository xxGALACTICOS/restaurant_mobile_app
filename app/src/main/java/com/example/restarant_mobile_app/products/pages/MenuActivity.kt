package com.example.restarant_mobile_app.products.pages

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.restarant_mobile_app.RestaurantDatabase
import com.example.restarant_mobile_app.cart.Entities.Cart
import com.example.restarant_mobile_app.cart.Entities.CartItems
import com.example.restarant_mobile_app.favourite.Favourite
import com.example.restarant_mobile_app.products.Entities.Product
import com.example.restarant_mobile_app.products.pages.ui.theme.Restarant_mobile_appTheme
import com.example.restarant_mobile_app.ui.components.BottomBarTab
import com.example.restarant_mobile_app.ui.components.EXTRA_USER_ID
import com.example.restarant_mobile_app.ui.components.ProtectedScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MenuActivity : ComponentActivity() {

    private lateinit var db: RestaurantDatabase
    private var userId: Long = -1L

    private val products = mutableStateListOf<Product>()
    private var favoriteIds by mutableStateOf(setOf<Int>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        db = RestaurantDatabase.getInstance(this)
        userId = intent.getLongExtra(EXTRA_USER_ID, -1L)

        loadProducts()

        setContent {
            Restarant_mobile_appTheme {
                ProtectedScaffold(
                    userId = userId,
                    currentTab = BottomBarTab.HOME
                ) { innerPadding ->
                    ProductFlow(
                        products = products,
                        favoriteIds = favoriteIds,
                        onToggleFavorite = { product -> toggleFavorite(product) },
                        onAddToCart = { product, extra, quantity ->
                            addToCart(product, extra, quantity)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        loadFavourites()
    }


    private fun loadProducts() {
        lifecycleScope.launch(Dispatchers.IO) {
            val loaded = db.productsDao().getAllProducts()

            withContext(Dispatchers.Main) {
                products.clear()
                products.addAll(loaded)
            }
        }
    }

    private fun loadFavourites() {
        lifecycleScope.launch(Dispatchers.IO) {
            val ids = db.favouritesDao()
                .getFavourites(userId)
                .map { it.productId }
                .toSet()

            withContext(Dispatchers.Main) {
                favoriteIds = ids
            }
        }
    }


    private fun toggleFavorite(product: Product) {
        val isAdding = product.id !in favoriteIds

        // update the hearts immediately (main thread)
        favoriteIds = if (isAdding) favoriteIds + product.id else favoriteIds - product.id

        lifecycleScope.launch(Dispatchers.IO) {
            if (isAdding) {
                db.favouritesDao().addFavourite(
                    Favourite(userId = userId, productId = product.id)
                )
            } else {
                db.favouritesDao().removeFavourite(userId, product.id)
            }

            withContext(Dispatchers.Main) {
                Toast.makeText(
                    this@MenuActivity,
                    if (isAdding) "Added to favorites ❤️" else "Removed from favorites",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }



    private fun addToCart(product: Product, extra: String, quantity: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            var cart = db.cartDao().getCartByUserId(userId)

            if (cart == null) {
                db.cartDao().insertCart(Cart(userId = userId))
                cart = db.cartDao().getCartByUserId(userId)
            }

            if (cart != null) {
                val existingItem = db.cartItemDao().getCartItem(cart.id, product.id, extra)

                val message = if (existingItem == null) {
                    db.cartItemDao().insertCartItem(
                        CartItems(
                            productId = product.id,
                            quantity = quantity,
                            cartId = cart.id,
                            selectedExtra = extra
                        )
                    )
                    "Added to cart"
                } else {
                    "This product is already in your cart"
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MenuActivity, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}



@Composable
fun ProductFlow(
    products: List<Product>,
    favoriteIds: Set<Int>,
    onToggleFavorite: (Product) -> Unit,
    onAddToCart: (Product, String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selected by remember { mutableStateOf<Product?>(null) }

    val product = selected

    if (product == null) {
        ProductHomeScreen(
            products = products,
            favoriteIds = favoriteIds,
            onToggleFavorite = { id ->
                products.find { it.id == id }?.let(onToggleFavorite)
            },
            onProductClick = { selected = it },
            modifier = modifier
        )
    } else {
        BackHandler { selected = null }

        ProductDetailsScreen(
            product = product,
            onBack = { selected = null },
            onAddToCart = onAddToCart,
            modifier = modifier,
            formatPrice = { price -> "$price EGP" },
            isFavorite = product.id in favoriteIds,
            onToggleFavorite = { onToggleFavorite(product) }
        )
    }
}


@Composable
fun ProductHomeScreen(
    products: List<Product>,
    favoriteIds: Set<Int>,
    onToggleFavorite: (Int) -> Unit,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxSize()
    ) {
        items(products) { product ->
            ProductCard(
                product = product,
                isFavorite = product.id in favoriteIds,
                onToggleFavorite = { onToggleFavorite(product.id) },
                onClick = { onProductClick(product) }
            )
        }
    }
}