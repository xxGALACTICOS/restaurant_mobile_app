package com.example.restarant_mobile_app.products.pages

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.restarant_mobile_app.products.Entities.Product


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
    val options = product.extraOptions()

    var selectedExtra by remember(product.id) {
        mutableStateOf(options.firstOrNull() ?: "")
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {

        Button(
            onClick = onBack,
            modifier = Modifier.padding(12.dp)
        ) {
            Text("← Back")
        }

        FavoriteButton(
            isFavorite = isFavorite,
            onClick = onToggleFavorite,
            modifier = Modifier
                .align(Alignment.End)
                .padding(end = 16.dp)
        )

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

@SuppressLint("DiscouragedApi")
@Composable
fun ProductImage(
    imageName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (imageName.startsWith("content://") || imageName.startsWith("file://")) {
        val bitmap = remember(imageName) {
            try {
                val uri = android.net.Uri.parse(imageName)
                val inputStream = context.contentResolver.openInputStream(uri)
                BitmapFactory.decodeStream(inputStream)
            } catch (_: Exception) {
                null
            }
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = modifier,
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = modifier,
                contentAlignment = Alignment.Center
            ) {
                Text("No image", color = Color.Gray)
            }
        }
    } else {
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
}
