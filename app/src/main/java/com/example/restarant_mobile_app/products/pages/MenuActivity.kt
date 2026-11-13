package com.example.restarant_mobile_app.products.pages
import android.os.Bundle
import android.widget.Toast
import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.restarant_mobile_app.RestaurantDatabase
import com.example.restarant_mobile_app.cart.Entities.Cart
import com.example.restarant_mobile_app.cart.Entities.CartItems
import com.example.restarant_mobile_app.products.pages.ui.theme.Restarant_mobile_appTheme
import com.example.restarant_mobile_app.products.Entities.Product
import com.example.restarant_mobile_app.ui.components.BottomBarTab
import com.example.restarant_mobile_app.ui.components.EXTRA_USER_ID
import com.example.restarant_mobile_app.ui.components.ProtectedScaffold
import kotlinx.coroutines.launch

class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Restarant_mobile_appTheme {
                val userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
                val database = RestaurantDatabase.getInstance(this)

                val cartDAO = database.cartDao()
                val cartItemsDAO = database.cartItemDao()
                ProtectedScaffold(
                    userId = userId,
                    currentTab = BottomBarTab.HOME
                ) { innerPadding ->

                    ProductFlow(
                        products = sampleProducts,

                        onAddToCart = { product, extra, quantity ->

                            lifecycleScope.launch {

                                var cart =
                                    cartDAO.getCartByUserId(userId)

                                if (cart == null) {

                                    cartDAO.insertCart(
                                        Cart(userId = userId)
                                    )

                                    cart =
                                        cartDAO.getCartByUserId(userId)

                                }

                                if (cart != null) {

                                    val result =
                                        cartItemsDAO.insertCartItem(
                                            CartItems(
                                                productId = product.id,
                                                quantity = quantity,
                                                cartId = cart.id,
                                                selectedExtra = extra
                                            )
                                        )

                                    if (result == -1L) {

                                        Toast.makeText(
                                            this@MenuActivity,
                                            "This product is already in your cart",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                    } else {

                                        Toast.makeText(
                                            this@MenuActivity,
                                            "Added to cart",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                }
                            }
                        },

                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


// =================================
// Sample Products
// =================================

val sampleProducts = listOf(

    Product(
        1,
        "Beef Burger",
        "Juicy beef patty with cheese, lettuce and tomato.",
        120.0,
        "burger"
    ),

    Product(
        2,
        "Chicken Wings",
        "Crispy fried wings served with dipping sauce.",
        95.0,
        "wings"
    ),

    Product(
        3,
        "Pizza Margherita",
        "Tomato sauce, mozzarella and fresh basil.",
        140.0,
        "pizza",
        extras = "Original"
    ),

    Product(
        4,
        "Pasta Alfredo",
        "Creamy white sauce pasta with grilled chicken.",
        110.0,
        "pasta"
    ),

    Product(
        5,
        "French Fries",
        "Golden fries with a pinch of salt.",
        40.0,
        "fries"
    )
)


// =================================
// Product Flow
// =================================

@Composable
fun ProductFlow(
    products: List<Product>,
    onAddToCart: (Product, String, Int) -> Unit,
    modifier: Modifier
) {

    val context = LocalContext.current

    var selected by remember {
        mutableStateOf<Product?>(null)
    }

    var favoriteIds by remember {
        mutableStateOf(setOf<Int>())
    }

    val product = selected

    if (product == null) {
        ProductHomeScreen(
            products = products,

            favoriteIds = favoriteIds,

            onToggleFavorite = { id ->

                val isAdding = id !in favoriteIds

                favoriteIds = if (isAdding) {
                    favoriteIds + id
                } else {
                    favoriteIds - id
                }

                Toast.makeText(
                    context,
                    if (isAdding)
                        "Added to favorites ❤️"
                    else
                        "Removed from favorites",
                    Toast.LENGTH_SHORT
                ).show()
            },

            onProductClick = {
                selected = it
            },

            modifier = modifier
        )
    } else {

        BackHandler {
            selected = null
        }

        ProductDetailsScreen(
            product = product,

            onBack = {
                selected = null
            },

            onAddToCart = onAddToCart,
            modifier = modifier,

            formatPrice = { price ->
                "$price EGP"
            },

            isFavorite = product.id in favoriteIds,

            onToggleFavorite = {

                val isAdding = product.id !in favoriteIds

                favoriteIds = if (isAdding) {
                    favoriteIds + product.id
                } else {
                    favoriteIds - product.id
                }

                Toast.makeText(
                    context,
                    if (isAdding)
                        "Added to favorites ❤️"
                    else
                        "Removed from favorites",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}


// =================================
// Product Home Screen
// =================================

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

                onToggleFavorite = {
                    onToggleFavorite(product.id)
                },

                onClick = {
                    onProductClick(product)
                })
        }
    }
}

// =================================
// Product Card
// =================================

@Composable
fun ProductCard(
    product: Product,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick()
            }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box {

            ProductImage(
                imageName = product.imageName,

                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            )

            // Improved Favorite Button

            FavoriteButton(
                isFavorite = isFavorite,
                onClick = onToggleFavorite,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = product.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = product.description,
            fontSize = 12.sp,
            color = Color.Gray,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "${product.price} EGP",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}


// =================================
// Favorite Button Component
// =================================

@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .size(42.dp)
            .shadow(
                elevation = 4.dp,
                shape = CircleShape
            )
            .background(
                color = Color.White,
                shape = CircleShape
            ),

        contentAlignment = Alignment.Center
    ) {

        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize()
        ) {

            Text(
                text = if (isFavorite) "♥" else "♡",

                fontSize = 25.sp,

                fontWeight = FontWeight.Bold,

                color = if (isFavorite)
                    Color(0xFFE53935)
                else
                    Color(0xFF757575)
            )
        }
    }
}
// =================================
// Product Details Screen
// =================================

@Composable
fun ProductDetailsScreen(
    product: Product,
    onBack: () -> Unit,
    onAddToCart: (Product, String, Int) -> Unit,
    modifier: Modifier = Modifier,
    formatPrice: (Double) -> String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit
) {

    val context = LocalContext.current

    val options = product.extraOptions()

    var selectedExtra by remember(product.id) {
        mutableStateOf(options.firstOrNull() ?: "")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        // Back Button

        Button(
            onClick = onBack,
            modifier = Modifier.padding(12.dp)
        ) {
            Text("← Back")
        }

        // Favorite Button

        FavoriteButton(
            isFavorite = isFavorite,
            onClick = onToggleFavorite,
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 16.dp)
        )

        // Product Image

        ProductImage(
            imageName = product.imageName,

            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )

        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = product.name,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = formatPrice(product.price),
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(12.dp))

            Text(text = product.description)

            // Extras

            if (options.isNotEmpty()) {

                Spacer(Modifier.height(16.dp))

                Text(
                    "Choose your type",
                    fontWeight = FontWeight.Bold
                )

                options.forEach { option ->

                    Row(
                        verticalAlignment = Alignment.CenterVertically,

                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedExtra = option
                            }
                    ) {

                        RadioButton(
                            selected = selectedExtra == option,
                            onClick = {
                                selectedExtra = option
                            }
                        )

                        Text(option)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Add To Cart Button

            Button(
                onClick = {

                    onAddToCart(
                        product,
                        selectedExtra,
                        1
                    )
                },

                modifier = Modifier.fillMaxWidth()
            ) {

                Text(
                    "Add to Cart • ${formatPrice(product.price)}"
                )
            }
        }
    }
}


// =================================
// Product Image
// =================================

@SuppressLint("DiscouragedApi")
@Composable
fun ProductImage(
    imageName: String,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    val imageId = remember(imageName) {

        context.resources.getIdentifier(
            imageName,
            "drawable",
            context.packageName
        )
    }

    if (imageId != 0) {

        Image(
            painter = painterResource(id = imageId),

            contentDescription = imageName,

            modifier = modifier,

            contentScale = ContentScale.Crop
        )

    } else {

        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {

            Text(
                "No image",
                color = Color.Gray
            )
        }
    }
}
