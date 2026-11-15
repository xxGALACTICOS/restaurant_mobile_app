package com.example.restarant_mobile_app.auth.pages

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.restarant_mobile_app.RestaurantDatabase
import com.example.restarant_mobile_app.auth.Entities.User
import com.example.restarant_mobile_app.auth.pages.ui.theme.Restarant_mobile_appTheme
import com.example.restarant_mobile_app.order.Entities.Order
import com.example.restarant_mobile_app.ui.components.BottomBarTab
import com.example.restarant_mobile_app.ui.components.ProtectedScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
        setContent {
            Restarant_mobile_appTheme {
                ProtectedScaffold(
                    userId = userId,
                    currentTab = BottomBarTab.PROFILE
                ) { innerPadding ->
                    ProfileScreen(
                        userId = userId,
                        modifier = Modifier.padding(innerPadding),
                        onLogout = { logout() }
                    )
                }
            }
        }
    }

    /** Logs out: clears the whole task and returns to the login screen. */
    private fun logout() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
                // Wipe the back stack so Back can't return to protected pages
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        )
        finish()
    }

    companion object {
        const val EXTRA_USER_ID = "user_id"
    }
}

@Composable
fun ProfileScreen(userId: Long, modifier: Modifier = Modifier, onLogout: () -> Unit) {
    var loading by remember { mutableStateOf(true) }
    var user by remember { mutableStateOf<User?>(null) }
    var orders by remember { mutableStateOf<List<Order>>(emptyList()) }

    val context = LocalContext.current
    LaunchedEffect(userId) {
        val result = withContext(Dispatchers.IO) {
            val db = RestaurantDatabase.getInstance(context.applicationContext)
            val found = db.userDao().getUserById(userId)
            if (found == null) null else found to db.userDao().getAllOrders(userId)
        }
        user = result?.first
        orders = result?.second.orEmpty()
        loading = false
    }

    when {
        loading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }

        user == null -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text("User not found") }

        else -> user?.let {
            ProfileContent(user = it, orders = orders, modifier = modifier, onLogout = onLogout)
        }
    }
}

@Composable
fun ProfileContent(
    user: User,
    orders: List<Order>,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { ProfileHeader(user) }

        item {
            Text(
                text = "My Orders (${orders.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (orders.isEmpty()) {
            item { EmptyOrders() }
        } else {
            items(orders, key = { it.id }) { order ->
                OrderCard(order)
            }
        }

        item {
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text("Log out")
            }
        }
    }
}

@Composable
private fun ProfileHeader(user: User, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.onPrimaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.name.trim().take(1).uppercase().ifEmpty { "?" },
                    color = MaterialTheme.colorScheme.primaryContainer,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = user.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun OrderCard(order: Order, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.id}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(order.status)
            }
            Text(
                text = formatDate(order.date),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = String.format(Locale.getDefault(), "$%.2f", order.total),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val (bg, fg) = when (status.lowercase()) {
        "delivered", "completed" ->
            colors.primaryContainer to colors.onPrimaryContainer
        "cancelled" ->
            colors.errorContainer to colors.onErrorContainer
        else ->
            colors.secondaryContainer to colors.onSecondaryContainer
    }
    Surface(modifier = modifier, shape = RoundedCornerShape(50), color = bg) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            color = fg,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyOrders(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No orders yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault()).format(Date(timestamp))

@Preview(showBackground = true)
@Composable
fun ProfileContentPreview() {
    Restarant_mobile_appTheme {
        ProfileContent(
            user = User(
                name = "Ahmed Ashraf",
                address = "Mostord",
                password = "ashraf",
                email = "ahmed@gmail.com"
            ),
            orders = listOf(
                Order(orderId = 1, userId = 1, totalPrice = 250.0, status = "pending"),
                Order(orderId = 2, userId = 1, totalPrice = 99.5, status = "delivered"),
                Order(orderId = 3, userId = 1, totalPrice = 40.0, status = "cancelled")
            ),
            onLogout = {}
        )
    }
}
