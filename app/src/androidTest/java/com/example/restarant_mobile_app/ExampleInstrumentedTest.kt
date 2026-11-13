import com.example.restarant_mobile_app.ui.theme.Restarant_mobile_appTheme
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
import androidx.compose.foundation.background
import androidx.compose.material3.Button

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
        Product(
            "Burger",
            "Delicious beef burger with cheese",
            120,
            R.drawable.burger
        ),
        Product(
            "Pizza",
            "Fresh pizza with mozzarella cheese",
            180,
            R.drawable.pizza
        ),
        Product(
            "Cola",
            "Cold refreshing drink",
            30,
            R.drawable.cola
        ),
        Product(
            "Cake",
            "Chocolate cake with cream",
            90,
            R.drawable.cola
        )
        ,
        Product(
            "Cake",
            "Chocolate cake with cream",
            90,
            R.drawable.cola
        ),
        Product(
            "Cake",
            "Chocolate cake with cream",
            90,
            R.drawable.cola
        )
    )

    var selectedProduct by remember {
        mutableStateOf<Product?>(null)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            items(products.chunked(2)) { rowProducts ->

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    rowProducts.forEach { product ->

                        ProductCard(
                            product = product,
                            isSelected = selectedProduct == product,
                            onClick = {
                                selectedProduct = product
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (rowProducts.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ProductDetailsScreen(
    product: Product,
    onBack: () -> Unit,
    onAddToCart: (Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }

    var selectedSize by remember {
        mutableStateOf("Medium")
    }

    var extraCheese by remember {
        mutableStateOf(false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        Text(
            text = "← Back to Menu",
            modifier = Modifier
                .clickable { onBack() }
                .padding(16.dp),
            fontWeight = FontWeight.Bold
        )

        Image(
            painter = painterResource(product.image),
            contentDescription = product.name,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.description,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${product.price} EGP",
                color = Color(0xFFE65100),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Choose Size",
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("Small", "Medium", "Large").forEach { size ->

                    val selected = selectedSize == size

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedSize = size },
                        colors = CardDefaults.cardColors(
                            containerColor = if (selected)
                                Color(0xFFFFE0B2)
                            else
                                Color.LightGray.copy(alpha = 0.2f)
                        ),
                        border = if (selected)
                            BorderStroke(2.dp, Color(0xFFFF9800))
                        else null
                    ) {
                        Text(
                            text = size,
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "Extra Cheese",
                    modifier = Modifier.weight(1f)
                )

                androidx.compose.material3.Checkbox(
                    checked = extraCheese,
                    onCheckedChange = { extraCheese = it }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "-",
                    modifier = Modifier
                        .clickable {
                            if (quantity > 1) quantity--
                        }
                        .padding(16.dp),
                    style = MaterialTheme.typography.headlineMedium
                )

                Text(
                    text = quantity.toString(),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "+",
                    modifier = Modifier
                        .clickable { quantity++ }
                        .padding(16.dp),
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onAddToCart(quantity) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF9800)
                )
            ) {
                Text(
                    text = "Add to Cart - ${product.price * quantity} EGP",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                Color(0xFFFFE0B2)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, Color(0xFFFF9800))
        } else {
            null
        }
    ) {

        Column(
            modifier = Modifier.padding(10.dp)
        ) {

            Image(
                painter = painterResource(product.image),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = product.name,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = product.description,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${product.price} EGP",
                color = Color(0xFFE65100),
                fontWeight = FontWeight.Bold
            )
        }
    }
}