package com.example.restarant_mobile_app.favourite

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.restarant_mobile_app.RestaurantDatabase
import com.example.restarant_mobile_app.products.Entities.Product
import com.example.restarant_mobile_app.products.pages.MenuActivity
import com.example.restarant_mobile_app.ui.theme.Restarant_mobile_appTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FavouritePage : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
        setContent {
            Restarant_mobile_appTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FavouritesScreen(
                        userId = userId,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_USER_ID = "user_id"
    }
}

@Composable
fun FavouritesScreen(userId: Long, modifier: Modifier = Modifier) {
    var loading by remember { mutableStateOf(true) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    suspend fun reload(): List<Product> =
        RestaurantDatabase.getInstance(context.applicationContext)
            .favouritesDao()
            .getFavouriteProducts(userId)

    LaunchedEffect(userId) {
        products = withContext(Dispatchers.IO) { reload() }
        loading = false
    }

    Column(modifier = modifier.fillMaxSize()) {
        FavouritesTopBar {
            navigateToProducts(context, userId)
        }

        when {
            loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            userId == -1L -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("See Your Favourites") }

            products.isEmpty() -> EmptyFavourites()

            else -> Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "My Favourites (${products.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        FavouriteCard(
                            product = product,
                            onRemove = {
                                scope.launch(Dispatchers.IO) {
                                    RestaurantDatabase
                                        .getInstance(context.applicationContext)
                                        .favouritesDao()
                                        .removeFavourite(userId, product.id)
                                    products = reload()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/** Top row with a button that returns to the Products page. */
@Composable
private fun FavouritesTopBar(
    modifier: Modifier = Modifier,
    onBackToProducts: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = onBackToProducts) {
            Text("← Products")
        }
    }
}

private fun navigateToProducts(context: Context, userId: Long) {
    context.startActivity(
        Intent(context, MenuActivity::class.java).apply {
            putExtra(FavouritePage.EXTRA_USER_ID, userId)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
    )
}

@Composable
private fun FavouriteCard(
    product: Product,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "♡",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Product #${product.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Saved in your favourites",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(onClick = onRemove) { Text("Remove") }
        }
    }
}

@Composable
private fun EmptyFavourites(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No favourites yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FavouritesScreenPreview() {
    Restarant_mobile_appTheme {
        FavouritesScreenPreviewContent()
    }
}

@Composable
private fun FavouritesScreenPreviewContent() {
    Column {
        Text(
            text = "My Favourites (2)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp)
        )
        FavouriteCard(product = Product(id = 1), onRemove = {})
        FavouriteCard(product = Product(id = 2), onRemove = {})
    }
}

@Preview(showBackground = true)
@Composable
fun EmptyFavouritesPreview() {
    Restarant_mobile_appTheme {
        EmptyFavourites()
    }
}
