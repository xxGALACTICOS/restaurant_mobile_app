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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.restarant_mobile_app.RestaurantDatabase
import com.example.restarant_mobile_app.auth.pages.ui.theme.Restarant_mobile_appTheme
import com.example.restarant_mobile_app.products.Entities.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

@Composable
fun AdminProductsScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember {
        RestaurantDatabase.getInstance(context.applicationContext).productsDao()
    }

    val products = remember { mutableStateListOf<Product>() }
    var productToEdit by remember { mutableStateOf<Product?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }

    // Loads the current list straight from the DB
    suspend fun reload() {
        val list = withContext(Dispatchers.IO) { dao.getAllProducts() }
        products.clear()
        products.addAll(list)
    }

    LaunchedEffect(Unit) { reload() }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Products",
                fontSize = 24.sp,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            Button(onClick = { showCreateDialog = true }) {
                Text(text = "Create product")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(products, key = { it.id }) { product ->
                ProductRow(
                    product = product,
                    onEditClick = { productToEdit = product },
                    onDeleteClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) { dao.deleteProduct(product) }
                            reload()
                        }
                    }
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateProductDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { created ->
                showCreateDialog = false
                scope.launch {
                    withContext(Dispatchers.IO) { dao.insertProduct(created) }
                    reload()
                }
            }
        )
    }

    productToEdit?.let { current ->
        EditProductDialog(
            product = current,
            onDismiss = { productToEdit = null },
            onSave = { updated ->
                productToEdit = null
                scope.launch {
                    withContext(Dispatchers.IO) { dao.updateProduct(updated) }
                    reload()
                }
            }
        )
    }
}

@Composable
fun ProductRow(
    product: Product,
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
fun CreateProductDialog(
    onDismiss: () -> Unit,
    onCreate: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var imageText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Create product") },
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
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text = "Description") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text(text = "Price") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = imageText,
                    onValueChange = { imageText = it },
                    label = { Text(text = "Image name (e.g. burger)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: return@Button
                    if (name.isBlank()) return@Button
                    onCreate(
                        Product(
                            name = name.trim(),
                            description = description.trim(),
                            price = price,
                            imageName = imageText.trim().lowercase(Locale.US)
                        )
                    )
                }
            ) {
                Text(text = "Create")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}

@Composable
fun EditProductDialog(
    product: Product,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    var name by remember { mutableStateOf(product.name) }
    var description by remember { mutableStateOf(product.description) }
    var priceText by remember { mutableStateOf(product.price.toString()) }
    var imageText by remember { mutableStateOf(product.imageName) }

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
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(text = "Description") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text(text = "Price") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                TextField(
                    value = imageText,
                    onValueChange = { imageText = it },
                    label = { Text(text = "Image name (e.g. burger)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: return@Button
                    onSave(
                        product.copy(
                            name = name.trim(),
                            description = description.trim(),
                            price = price,
                            imageName = imageText.trim().lowercase(Locale.US)
                        )
                    )
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
