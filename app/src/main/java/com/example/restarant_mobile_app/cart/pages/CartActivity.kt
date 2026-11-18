
package com.example.restarant_mobile_app.cart.pages

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.example.restarant_mobile_app.R
import com.example.restarant_mobile_app.RestaurantDatabase
import com.example.restarant_mobile_app.cart.Entities.CartItems
import com.example.restarant_mobile_app.order.Entities.Order
import com.example.restarant_mobile_app.order.Entities.OrderItem
import com.example.restarant_mobile_app.products.Entities.Product
import com.example.restarant_mobile_app.products.pages.ProductImage
import com.example.restarant_mobile_app.products.pages.ui.theme.Restarant_mobile_appTheme
import com.example.restarant_mobile_app.ui.components.BottomBarTab
import com.example.restarant_mobile_app.ui.components.EXTRA_USER_ID
import com.example.restarant_mobile_app.ui.components.ProtectedScaffold
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CartActivity : ComponentActivity() {

    private val cartItems = mutableStateListOf<CartItems>()
    private val products = mutableStateListOf<Product>()

    private var currentCartId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        val userId = intent.getLongExtra(EXTRA_USER_ID, -1L)

        val database = RestaurantDatabase.getInstance(this)
        val cartDAO = database.cartDao()
        val cartItemsDAO = database.cartItemDao()

        lifecycleScope.launch {

            val cart = cartDAO.getCartByUserId(userId)

            cartItems.clear()
            products.clear()

            if (cart != null) {

                currentCartId = cart.id

                val savedItems = cartItemsDAO.getCartItems(cart.id)

                val allProducts = database.productsDao().getAllProducts()

                for (item in savedItems) {

                    val product = allProducts.find {
                        it.id == item.productId
                    }

                    if (product != null) {
                        cartItems.add(item)
                        products.add(product)
                    }
                }
            }
        }

        setContent {
            Restarant_mobile_appTheme {

                ProtectedScaffold(
                    userId = userId,
                    currentTab = BottomBarTab.CART
                ) { innerPadding ->

                    CartScreen(
                        cartItems = cartItems,
                        products = products,
                        modifier = Modifier.padding(innerPadding),

                        onBack = {
                            finish()
                        },

                        onIncrease = { item, index ->

                            lifecycleScope.launch {

                                val cartId = currentCartId

                                if (cartId != null) {

                                    cartItemsDAO.increaseQuantity(
                                        cartId,
                                        item.productId
                                    )

                                    cartItems[index] = item.copy(
                                        quantity = item.quantity + 1
                                    )
                                }
                            }
                        },

                        onDecrease = { item, index ->

                            lifecycleScope.launch {

                                val cartId = currentCartId

                                if (cartId != null && item.quantity > 1) {

                                    cartItemsDAO.decreaseQuantity(
                                        cartId,
                                        item.productId
                                    )

                                    cartItems[index] = item.copy(
                                        quantity = item.quantity - 1
                                    )
                                }
                            }
                        },

                        onDelete = { item, index ->

                            lifecycleScope.launch {

                                cartItemsDAO.deleteCartItem(item)

                                cartItems.removeAt(index)
                                products.removeAt(index)
                            }
                        },

                        onCheckout = {

                            lifecycleScope.launch {

                                val cartId = currentCartId

                                if (cartId != null && cartItems.isNotEmpty()) {

                                    val totalPrice = cartItems.indices.sumOf { index ->
                                        products[index].price * cartItems[index].quantity
                                    }

                                    val order = Order(
                                        userId = userId,
                                        totalPrice = totalPrice,
                                        status = "Pending"
                                    )

                                    val orderItemsList = cartItems.indices.map { index ->
                                        val item = cartItems[index]
                                        val product = products[index]
                                        OrderItem(
                                            foodId = product.id,
                                            foodName = product.name,
                                            price = product.price,
                                            quantity = item.quantity,
                                            selectedExtra = item.selectedExtra
                                        )
                                    }

                                    withContext(Dispatchers.IO) {
                                        database.orderDao().createOrder(order, orderItemsList)
                                        cartItemsDAO.clearCart(cartId)
                                    }

                                    cartItems.clear()
                                    products.clear()

                                    Toast.makeText(
                                        this@CartActivity,
                                        "Order placed successfully!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CartScreen(
    cartItems: List<CartItems>,
    products: List<Product>,
    onBack: () -> Unit,
    onIncrease: (CartItems, Int) -> Unit,
    onDecrease: (CartItems, Int) -> Unit,
    onDelete: (CartItems, Int) -> Unit,
    onCheckout: () -> Unit,
    modifier: Modifier = Modifier
) {

    val total = cartItems.indices.sumOf { index ->
        products[index].price * cartItems[index].quantity
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {

        TextButton(
            onClick = onBack,
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Text("← Back")
        }

        Text(
            text = if (cartItems.isEmpty()) "My Cart" else "My Cart (${cartItems.size})",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (cartItems.isEmpty()) {

            EmptyCart(modifier = Modifier.weight(1f))

        } else {

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {

                cartItems.forEachIndexed { index, item ->

                    CartItemCard(
                        product = products[index],
                        item = item,

                        onIncrease = {
                            onIncrease(item, index)
                        },

                        onDecrease = {
                            onDecrease(item, index)
                        },

                        onDelete = {
                            onDelete(item, index)
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = "Total: $total EGP",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 8.dp)
            )

            Button(
                onClick = onCheckout,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "Checkout",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CartItemCard(
    product: Product,
    item: CartItems,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit
) {

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {

                ProductImage(
                    imageName = product.imageName,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = product.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {

                        Icon(
                            painter = painterResource(id = R.drawable.ic_delete),
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (item.selectedExtra.isNotBlank()) {

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {

                        Text(
                            text = "Extra: ${item.selectedExtra}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {

                        QuantityButton(
                            symbol = "-",
                            enabled = item.quantity > 1,
                            onClick = onDecrease
                        )

                        Text(
                            text = item.quantity.toString(),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.width(28.dp)
                        )

                        QuantityButton(
                            symbol = "+",
                            enabled = true,
                            onClick = onIncrease
                        )
                    }

                    Text(
                        text = "${product.price * item.quantity} EGP",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun QuantityButton(
    symbol: String,
    enabled: Boolean,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .size(34.dp)
            .shadow(
                elevation = 2.dp,
                shape = CircleShape
            )
            .background(
                color = if (enabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {

        IconButton(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxSize()
        ) {

            Text(
                text = symbol,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyCart(
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "🛒",
                fontSize = 56.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Your cart is empty",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
