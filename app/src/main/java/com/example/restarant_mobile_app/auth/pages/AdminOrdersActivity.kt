package com.example.restarant_mobile_app.auth.pages

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.restarant_mobile_app.RestaurantDatabase
import com.example.restarant_mobile_app.auth.pages.ui.theme.Restarant_mobile_appTheme
import com.example.restarant_mobile_app.order.Entities.OrderWithItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminOrdersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Restarant_mobile_appTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AdminOrdersScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

data class AdminOrderData(
    val orderWithItems: OrderWithItems,
    val userName: String,
    val userEmail: String,
    val userAddress: String
)

@Composable
fun AdminOrdersScreen(modifier: Modifier = Modifier) {
    var loading by remember { mutableStateOf(true) }
    var adminOrders by remember { mutableStateOf<List<AdminOrderData>>(emptyList()) }
    var selectedOrder by remember { mutableStateOf<AdminOrderData?>(null) }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    fun loadOrders() {
        scope.launch(Dispatchers.IO) {
            val db = RestaurantDatabase.getInstance(context.applicationContext)
            val ordersWithItems = db.orderDao().getAllOrdersWithItems()
            val usersMap = db.userDao().getAllUsers().associateBy { it.id }

            val list = ordersWithItems.map { orderWithItems ->
                val user = usersMap[orderWithItems.order.userId]
                AdminOrderData(
                    orderWithItems = orderWithItems,
                    userName = user?.name ?: "Unknown User",
                    userEmail = user?.email ?: "",
                    userAddress = user?.address ?: ""
                )
            }
            withContext(Dispatchers.Main) {
                adminOrders = list
                loading = false
            }
        }
    }

    fun updateStatus(orderId: Int, newStatus: String) {
        scope.launch(Dispatchers.IO) {
            val db = RestaurantDatabase.getInstance(context.applicationContext)
            db.orderDao().updateOrderStatus(orderId, newStatus)
            loadOrders()
            withContext(Dispatchers.Main) {
                if (selectedOrder?.orderWithItems?.order?.id == orderId) {
                    val updated = selectedOrder?.orderWithItems?.order?.copy(status = newStatus)
                    if (updated != null && selectedOrder != null) {
                        selectedOrder = selectedOrder!!.copy(
                            orderWithItems = selectedOrder!!.orderWithItems.copy(order = updated)
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        loadOrders()
    }

    when {
        loading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        else -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Orders (${adminOrders.size})",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (adminOrders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No orders found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(adminOrders, key = { it.orderWithItems.order.id }) { adminOrder ->
                            AdminOrderCard(
                                adminOrder = adminOrder,
                                onClick = { selectedOrder = adminOrder },
                                onStatusChange = { newStatus ->
                                    updateStatus(adminOrder.orderWithItems.order.id, newStatus)
                                }
                            )
                        }
                    }
                }
            }

            selectedOrder?.let { selected ->
                AdminOrderDetailsDialog(
                    adminOrder = selected,
                    onStatusChange = { newStatus ->
                        updateStatus(selected.orderWithItems.order.id, newStatus)
                    },
                    onDismiss = { selectedOrder = null }
                )
            }
        }
    }
}

@Composable
private fun AdminOrderCard(
    adminOrder: AdminOrderData,
    onClick: () -> Unit,
    onStatusChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val order = adminOrder.orderWithItems.order
    val items = adminOrder.orderWithItems.items

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                text = "By ${adminOrder.userName} · ${formatDate(order.date)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "${items.sumOf { it.quantity }} item(s) • Tap to view details",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: $${String.format(Locale.getDefault(), "%.2f", order.total)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { onStatusChange("Pending") },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                    enabled = order.status.lowercase() != "pending"
                ) {
                    Text("Pending", fontSize = 11.sp, maxLines = 1, softWrap = false)
                }
                Button(
                    onClick = { onStatusChange("Completed") },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                    enabled = order.status.lowercase() != "completed"
                ) {
                    Text("Completed", fontSize = 11.sp, maxLines = 1, softWrap = false)
                }
                Button(
                    onClick = { onStatusChange("Cancelled") },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    enabled = order.status.lowercase() != "cancelled"
                ) {
                    Text("Cancel", fontSize = 11.sp, maxLines = 1, softWrap = false)
                }
            }
        }
    }
}

@Composable
private fun AdminOrderDetailsDialog(
    adminOrder: AdminOrderData,
    onStatusChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val order = adminOrder.orderWithItems.order
    val items = adminOrder.orderWithItems.items

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order #${order.id}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                StatusChip(order.status)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column {
                    Text(
                        text = "Customer: ${adminOrder.userName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (adminOrder.userEmail.isNotBlank()) {
                        Text(
                            text = "Email: ${adminOrder.userEmail}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (adminOrder.userAddress.isNotBlank()) {
                        Text(
                            text = "Address: ${adminOrder.userAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Date: ${formatDate(order.date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider()

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
                        text = String.format(Locale.getDefault(), "$%.2f", order.total),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                HorizontalDivider()

                Text(
                    text = "Change Status:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { onStatusChange("Pending") },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                        enabled = order.status.lowercase() != "pending"
                    ) {
                        Text("Pending", fontSize = 11.sp, maxLines = 1, softWrap = false)
                    }
                    Button(
                        onClick = { onStatusChange("Completed") },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                        enabled = order.status.lowercase() != "completed"
                    ) {
                        Text("Completed", fontSize = 11.sp, maxLines = 1, softWrap = false)
                    }
                    Button(
                        onClick = { onStatusChange("Cancelled") },
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        enabled = order.status.lowercase() != "cancelled"
                    ) {
                        Text("Cancel", fontSize = 11.sp, maxLines = 1, softWrap = false)
                    }
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

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault()).format(Date(timestamp))
