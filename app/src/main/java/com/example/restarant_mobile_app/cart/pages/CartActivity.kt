package com.example.restarant_mobile_app.cart.pages

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.restarant_mobile_app.AppDatabase
import com.example.restarant_mobile_app.R
import com.example.restarant_mobile_app.cart.DAOs.CartItemsDAO
import com.example.restarant_mobile_app.cart.Entities.Cart
import com.example.restarant_mobile_app.cart.Entities.CartItems
import com.example.restarant_mobile_app.cart.pages.ui.theme.Restarant_mobile_appTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * One row shown on the cart screen.
 * Built by joining `CartItems` (rowId, quantity) with `Product` (name, price, image).
 *
 * rowId = cart_items.id, needed by deleteCartItem().
 * cartId = the cart this row belongs to, needed by the quantity/clear queries.
 */
data class CartItemUi(
    val rowId: Int,
    val cartId: Int,
    val productId: Int,
    val name: String,
    val price: Double,
    val imageRes: Int,
    val quantity: Int
)

class CartActivity : ComponentActivity() {

    // TODO: use the real logged-in user id once the login flow is ready.
    private val currentUserId = 1

    private val db by lazy { AppDatabase.get(applicationContext) }

    private val cartRows = mutableStateListOf<CartItemUi>()
    private var isLoading by mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        loadCart()
        setContent {
            Restarant_mobile_appTheme {
                CartScreen(
                    cartItems = cartRows,
                    isLoading = isLoading,
                    onBack = { finish() },
                    onIncrease = { row -> updateQuantity(row) { it.increaseQuantity(row.cartId, row.productId) } },
                    onDecrease = { row -> updateQuantity(row) { it.decreaseQuantity(row.cartId, row.productId) } },
                    onDelete = { row -> deleteRow(row) },
                    onCheckout = { checkout() }
                )
            }
        }
    }

    /** Loads (and creates if missing) the user's cart, then reads its rows. */
    private fun loadCart() {
        lifecycleScope.launch(Dispatchers.IO) {
            val cartDao = db.cartDao()
            val itemsDao = db.cartItemsDao()
            val productDao = db.productDao()

            // Seed the sample products the first time the app runs.
            val products = db.seedSampleProductsIfEmpty().associateBy { it.id }

            // Make sure this user has a cart. insertCart uses IGNORE on conflict,
            // so it returns -1 when the cart already exists.
            val newRowId = cartDao.insertCart(Cart(userId = currentUserId))
            val cart = cartDao.getCartByUserId(currentUserId) ?: return@launch

            // The first time this cart is created, put a few items in it so the
            // screen isn't empty. Uses insertCartItem + increaseQuantity.
            if (newRowId != -1L) {
                listOf("Classic Burger" to 2, "Pepperoni Pizza" to 1, "Iced Coffee" to 1)
                    .forEach { (productName, quantity) ->
                        val product = products.values.firstOrNull { it.name == productName }
                            ?: return@forEach
                        itemsDao.insertCartItem(
                            CartItems(productId = product.id, quantity = 1, cartId = cart.id)
                        )
                        repeat(quantity - 1) { itemsDao.increaseQuantity(cart.id, product.id) }
                    }
            }

            // Join cart items with their products to build the rows.
            val rows = itemsDao.getCartItems(cart.id).mapNotNull { item ->
                products[item.productId]?.let { product ->
                    CartItemUi(
                        rowId = item.id,
                        cartId = item.cartId,
                        productId = item.productId,
                        name = product.name,
                        price = product.price,
                        imageRes = product.imageRes,
                        quantity = item.quantity
                    )
                }
            }

            withContext(Dispatchers.Main) {
                cartRows.clear()
                cartRows.addAll(rows)
                isLoading = false
            }
        }
    }

    /** + / − : calls the matching DAO query, then reloads the rows. */
    private fun updateQuantity(
        row: CartItemUi,
        update: suspend (CartItemsDAO) -> Unit
    ) {
        lifecycleScope.launch(Dispatchers.IO) {
            update(db.cartItemsDao())
            loadCart()
        }
    }

    /** Delete button: removes the cart_items row by its entity. */
    private fun deleteRow(row: CartItemUi) {
        lifecycleScope.launch(Dispatchers.IO) {
            db.cartItemsDao().deleteCartItem(
                CartItems(
                    id = row.rowId,
                    productId = row.productId,
                    quantity = row.quantity,
                    cartId = row.cartId
                )
            )
            loadCart()
        }
    }

    /** Checkout: clears all products from the cart. */
    private fun checkout() {
        lifecycleScope.launch(Dispatchers.IO) {
            val cartId = cartRows.firstOrNull()?.cartId ?: return@launch
            db.cartItemsDao().clearCart(cartId)
            loadCart()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@CartActivity, "Order placed! Cart cleared.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    cartItems: List<CartItemUi>,
    isLoading: Boolean,
    onBack: () -> Unit = {},
    onIncrease: (CartItemUi) -> Unit = {},
    onDecrease: (CartItemUi) -> Unit = {},
    onDelete: (CartItemUi) -> Unit = {},
    onCheckout: () -> Unit = {}
) {
    val totalPrice = cartItems.sumOf { it.price * it.quantity }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "My Cart") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (!isLoading && cartItems.isNotEmpty()) {
                CartSummary(
                    totalPrice = totalPrice,
                    onCheckout = onCheckout
                )
            }
        }
    ) { innerPadding ->
        when {
            isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            cartItems.isEmpty() -> EmptyCart(modifier = Modifier.padding(innerPadding))

            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { Spacer(modifier = Modifier.height(4.dp)) }

                items(cartItems, key = { it.rowId }) { item ->
                    CartItemRow(
                        item = item,
                        onIncrease = { onIncrease(item) },
                        onDecrease = { onDecrease(item) },
                        onDelete = { onDelete(item) }
                    )
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItemUi,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = item.imageRes),
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "$%.2f".format(item.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                QuantityStepper(
                    quantity = item.quantity,
                    onIncrease = onIncrease,
                    onDecrease = onDecrease
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_delete),
                    contentDescription = "Delete ${item.name}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun QuantityStepper(
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // The "-" button is disabled at quantity 1, same rule as decreaseQuantity() in CartItemsDAO.
        QuantityButton(text = "−", enabled = quantity > 1, onClick = onDecrease)

        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        QuantityButton(text = "+", onClick = onIncrease)
    }
}

@Composable
fun QuantityButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.size(28.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun CartSummary(
    totalPrice: Double,
    onCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "$%.2f".format(totalPrice),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onCheckout,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Checkout",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
fun EmptyCart(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_cart),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Your cart is empty",
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CartScreenPreview() {
    Restarant_mobile_appTheme {
        CartScreen(
            cartItems = listOf(
                CartItemUi(1, 1, 1, "Classic Burger", 8.99, R.drawable.img_burger, 2),
                CartItemUi(2, 1, 2, "Pepperoni Pizza", 12.50, R.drawable.img_pizza, 1)
            ),
            isLoading = false
        )
    }
}
