package com.example.restarant_mobile_app.auth.pages

import android.os.Bundle
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.restarant_mobile_app.auth.pages.ui.theme.Restarant_mobile_appTheme
import java.util.Locale

class AdminProductsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Restarant_mobile_appTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AdminProductsScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

data class AdminProduct(
    val id: Long,
    val name: String,
    val price: Double
)

@Composable
fun AdminProductsScreen(modifier: Modifier = Modifier) {
    val products = remember {
        mutableStateListOf(
            AdminProduct(1, "Margherita Pizza", 8.99),
            AdminProduct(2, "Chicken Burger", 6.49),
            AdminProduct(3, "Caesar Salad", 5.75),
            AdminProduct(4, "Spaghetti Bolognese", 9.25)
        )
        // TODO: load products from DB via ProductDAO
    }
    var productToEdit by remember { mutableStateOf<AdminProduct?>(null) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Products",
            fontSize = 24.sp,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(products, key = { it.id }) { product ->
                ProductRow(
                    product = product,
                    onEditClick = { productToEdit = product },
                    onDeleteClick = {
                        products.remove(product)
                        // TODO: delete product from DB via ProductDAO
                    }
                )
            }
        }
    }

    productToEdit?.let { current ->
        EditProductDialog(
            product = current,
            onDismiss = { productToEdit = null },
            onSave = { updated ->
                val index = products.indexOfFirst { it.id == updated.id }
                if (index != -1) products[index] = updated
                productToEdit = null
                // TODO: update product in DB via ProductDAO
            }
        )
    }
}

@Composable
fun ProductRow(
    product: AdminProduct,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE8E8E8))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "$%.2f".format(Locale.US, product.price),
                fontSize = 14.sp,
                color = Color(0xFF6B6B6B)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedButton(onClick = onEditClick) {
            Text(text = "Edit")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = onDeleteClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
        ) {
            Text(text = "Delete")
        }
    }
}

@Composable
fun EditProductDialog(
    product: AdminProduct,
    onDismiss: () -> Unit,
    onSave: (AdminProduct) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(product.name) }
    var priceText by remember { mutableStateOf(product.price.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Edit product") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(text = "Name") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text(text = "Price") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: return@Button
                    onSave(product.copy(name = name, price = price))
                }
            ) {
                Text(text = "Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
