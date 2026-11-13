package com.example.restarant_mobile_app.products.pages

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.restarant_mobile_app.products.pages.ui.theme.Restarant_mobile_appTheme
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import com.example.restarant_mobile_app.products.Entities.Product
import com.example.restarant_mobile_app.ui.components.BottomBarTab
import com.example.restarant_mobile_app.ui.components.EXTRA_USER_ID
import com.example.restarant_mobile_app.ui.components.ProtectedScaffold

class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Restarant_mobile_appTheme {
                val userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
                ProtectedScaffold(userId = userId, currentTab = BottomBarTab.HOME) { innerPadding ->
                    ProductFlow(
                        products = sampleProducts,
                        onAddToCart = { product, extra, quantity ->
                            Toast.makeText(
                                this,
                                "${product.name} added to cart. Extra: $extra, Quantity: $quantity",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun ProductFlow(
    products: List<Product>,
    onAddToCart: (Product, String, Int) -> Unit,
    modifier: Modifier
) {
    var selected by remember { mutableStateOf<Product?>(null) }
    val product = selected

    if (product == null) {
        ProductHomeScreen(
            products = products,
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
            formatPrice = { price -> "${price} EGP" }
        )
    }
}

val sampleProducts = listOf(
    Product(1, "Beef Burger", "Juicy beef patty with cheese, lettuce and tomato.", 120.0, "burger"),
    Product(2, "Chicken Wings", "Crispy fried wings served with dipping sauce.", 95.0, "wings"),
    Product(3, "Pizza Margherita", "Tomato sauce, mozzarella and fresh basil.", 140.0, "pizza", extras = "Original"),
    Product(4, "Pasta Alfredo", "Creamy white sauce pasta with grilled chicken.", 110.0, "pasta"),
    Product(5, "French Fries", "Golden fries with a pinch of salt.", 40.0, "fries")
)


@Composable
fun ProductHomeScreen(
    products: List<Product>,
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
            ProductCard(product = product, onClick = { onProductClick(product) })
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        ProductImage(
            imageName = product.imageName,
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        )

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
@Composable
fun ProductDetailsScreen(
    product: Product,
    onBack: () -> Unit,
    onAddToCart: (Product, String, Int) -> Unit,
    modifier: Modifier = Modifier,
    formatPrice: (Double) -> String
) {
    val context = LocalContext.current
    val options = product.extraOptions()

    var selectedExtra by remember { mutableStateOf(options.firstOrNull() ?: "") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Button(onClick = onBack, modifier = Modifier.padding(12.dp)) {
            Text("← Back")
        }

        ProductImage(
            imageName = product.imageName,
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = product.name, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                text = formatPrice(product.price),
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(12.dp))
            Text(text = product.description)

            // Extras (Original / Spicy / ...)
            if (options.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text("Choose your type", fontWeight = FontWeight.Bold)
                options.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedExtra = option }
                    ) {
                        RadioButton(
                            selected = selectedExtra == option,
                            onClick = { selectedExtra = option }
                        )
                        Text(option)
                    }
                }
            }



            Spacer(Modifier.height(24.dp))

            Button(
                onClick = {
                    onAddToCart(product, selectedExtra, 1)

                    Toast.makeText(
                        context,
                        "Added to cart",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add to Cart • ${formatPrice(product.price)}")
            }
        }
    }
}

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
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = imageId),
            contentDescription = imageName,
            modifier = modifier,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier,
            contentAlignment = Alignment.Center
        ) {
            Text("No image", color = Color.Gray)
        }
    }
}
