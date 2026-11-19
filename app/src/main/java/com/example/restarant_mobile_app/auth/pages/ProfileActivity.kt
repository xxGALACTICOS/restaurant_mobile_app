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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import com.example.restarant_mobile_app.RestaurantDatabase
import com.example.restarant_mobile_app.auth.Entities.User
import com.example.restarant_mobile_app.auth.pages.ui.theme.Restarant_mobile_appTheme
import com.example.restarant_mobile_app.order.Entities.OrderWithItems
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

    private fun logout() {
        startActivity(
            Intent(this, LoginActivity::class.java).apply {
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
    var ordersWithItems by remember { mutableStateOf<List<OrderWithItems>>(emptyList()) }
    var selectedOrderWithItems by remember { mutableStateOf<OrderWithItems?>(null) }

    val context = LocalContext.current
    LaunchedEffect(userId) {
        val result = withContext(Dispatchers.IO) {
            val db = RestaurantDatabase.getInstance(context.applicationContext)
            val found = db.userDao().getUserById(userId)
            if (found == null) null else found to db.orderDao().getOrdersWithItemsByUserId(userId)
        }
        user = result?.first
        ordersWithItems = result?.second.orEmpty()
        loading = false
    }

    if (loading) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { CircularProgressIndicator() }
    } else if (user == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) { Text("User not found") }
    } else {
        val profileUser = user!!
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { ProfileHeader(profileUser) }

            item {
                Text(
                    text = "My Orders (${ordersWithItems.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (ordersWithItems.isEmpty()) {
                item { EmptyOrders() }
            } else {
                items(ordersWithItems, key = { it.order.orderId }) { orderWithItems ->
                    OrderCard(
                        orderWithItems = orderWithItems,
                        onClick = { selectedOrderWithItems = orderWithItems }
                    )
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

        val selected = selectedOrderWithItems
        if (selected != null) {
            OrderDetailsDialog(
                orderWithItems = selected,
                onDismiss = { selectedOrderWithItems = null }
            )
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
                    text = "Address: ${user.address}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun OrderCard(
    orderWithItems: OrderWithItems,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val order = orderWithItems.order
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
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
                    text = "Order #${order.orderId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(order.status)
            }
            Text(
                text = formatDate(order.orderDate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${orderWithItems.items.sumOf { it.quantity }} item(s) • Tap to view items",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
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
                    text = String.format(Locale.getDefault(), "$%.2f", order.totalPrice),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun OrderDetailsDialog(
    orderWithItems: OrderWithItems,
    onDismiss: () -> Unit
) {
    val order = orderWithItems.order
    val items = orderWithItems.items

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Order #${order.orderId}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Items (${items.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                if (items.isEmpty()) {
                    Text(
                        text = "No items in this order",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items) { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.foodName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (item.selectedExtra.isNotBlank()) {
                                        Text(
                                            text = "Extra: ${item.selectedExtra}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = "${item.quantity} x $${String.format(Locale.getDefault(), "%.2f", item.price)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = String.format(Locale.getDefault(), "$%.2f", item.price * item.quantity),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Total Price",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "$%.2f", order.totalPrice),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun StatusChip(status: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val lower = status.lowercase()
    val (bg, fg) = if (lower == "delivered" || lower == "completed") {
        colors.primaryContainer to colors.onPrimaryContainer
    } else if (lower == "cancelled") {
        colors.errorContainer to colors.onErrorContainer
    } else {
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
