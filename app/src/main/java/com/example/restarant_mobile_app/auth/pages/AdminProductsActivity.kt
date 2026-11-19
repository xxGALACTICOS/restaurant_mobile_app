package com.example.restarant_mobile_app.auth.pages

import android.net.Uri
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.restarant_mobile_app.products.pages.ProductImage
import kotlinx.coroutines.launch
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
    val activity = context as ComponentActivity
    val dao = remember {
        RestaurantDatabase.getInstance(context.applicationContext).productsDao()
    }

    var products by remember { mutableStateOf(emptyList<Product>()) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showProductDialog by remember { mutableStateOf(false) }

    suspend fun reload() {
        products = dao.getAllProducts()
    }

    LaunchedEffect(Unit) { reload() }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Products",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    editingProduct = null
                    showProductDialog = true
                }
            ) {
                Text(text = "Create product")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(products, key = { it.id }) { product ->
                ProductRow(
                    product = product,
                    onEditClick = {
                        editingProduct = product
                        showProductDialog = true
                    },
                    onDeleteClick = {
                        activity.lifecycleScope.launch {
                            dao.deleteProduct(product)
                            reload()
                        }
                    }
                )
            }
        }
    }

    if (showProductDialog) {
        ProductFormDialog(
            product = editingProduct,
            onDismiss = {
                showProductDialog = false
                editingProduct = null
            },
            onSave = { saved ->
                showProductDialog = false
                editingProduct = null
                activity.lifecycleScope.launch {
                    if (saved.id == 0) {
                        dao.insertProduct(saved)
                    } else {
                        dao.updateProduct(saved)
                    }
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
        ProductImage(
            imageName = product.imageName,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(12.dp))
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
fun ProductFormDialog(
    product: Product?,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(product?.name ?: "") }
    var description by remember { mutableStateOf(product?.description ?: "") }
    var priceText by remember { mutableStateOf(product?.price?.toString() ?: "") }
    var imageUri by remember { mutableStateOf(product?.imageName ?: "") }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            imageUri = it.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = if (product == null) "Create product" else "Edit product")
        },
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8E8E8)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri.isNotEmpty()) {
                            ProductImage(
                                imageName = imageUri,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text("No Image", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    OutlinedButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = if (imageUri.isEmpty()) "Choose Image" else "Change Image")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: return@Button
                    if (name.isBlank()) return@Button

                    val saved = product?.copy(
                        name = name.trim(),
                        description = description.trim(),
                        price = price,
                        imageName = imageUri
                    ) ?: Product(
                        name = name.trim(),
                        description = description.trim(),
                        price = price,
                        imageName = imageUri
                    )

                    onSave(saved)
                }
            ) {
                Text(text = if (product == null) "Create" else "Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = "Cancel")
            }
        }
    )
}
