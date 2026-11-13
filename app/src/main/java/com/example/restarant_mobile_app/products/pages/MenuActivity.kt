
package com.example.restarant_mobile_app.products.pages

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.restarant_mobile_app.R
import com.example.restarant_mobile_app.ui.theme.Restarant_mobile_appTheme

data class Product(
    val name: String,
    val description: String,
    val price: Int,
    val image: Int
)

class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Restarant_mobile_appTheme {
                MenuScreen()
            }
        }
    }
}


@Composable
fun MenuScreen() {

    val products = listOf(
        Product("Burger", "Delicious beef burger with cheese", 120, R.drawable.burger),
        Product("Pizza", "Fresh pizza with mozzarella cheese", 180, R.drawable.pizza),
        Product("Cola", "Cold refreshing drink", 30, R.drawable.cola),
        Product("Cake", "Chocolate cake with cream", 90, R.drawable.cola)
    )

    var currentTab by remember { mutableStateOf("Home") }
    var selectedProduct by remember { mutableStateOf<Product?>(null) }

    val cart = remember {
        mutableStateMapOf<Product, Int>()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),

        bottomBar = {
            if (selectedProduct == null) {

                NavigationBar(
                    containerColor = Color.White
                ) {

                    val tabs = listOf(
                        "Home",
                        "Menu",
                        "Cart",
                        "Profile"
                    )

                    val icons = listOf("⌂", "☰", "🛒", "♙")

                    tabs.forEachIndexed { index, tab ->

                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            icon = {
                                Text(
                                    text = icons[index],
                                    style = MaterialTheme.typography.titleLarge
                                )
                            },
                            label = {
                                Text(tab)
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            if (selectedProduct != null) {

                ProductDetailsScreen(
                    product = selectedProduct!!,
                    onBack = {
                        selectedProduct = null
                    },
                    onAddToCart = { quantity ->

                        val product = selectedProduct!!

                        cart[product] =
                            (cart[product] ?: 0) + quantity

                        selectedProduct = null
                        currentTab = "Cart"
                    }
                )

            } else {

                when (currentTab) {

                    "Home" -> HomeScreen(
                        onOpenMenu = {
                            currentTab = "Menu"
                        }
                    )

                    "Menu" -> MenuContent(
                        products = products,
                        onProductClick = {
                            selectedProduct = it
                        }
                    )

                    "Cart" -> CartScreen(
                        cart = cart,
                        onRemove = {
                            cart.remove(it)
                        }
                    )

                    "Profile" -> ProfileScreen()
                }
            }
        }
    }
}
