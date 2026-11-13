package com.example.restarant_mobile_app.auth.pages

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.restarant_mobile_app.auth.pages.ui.theme.Restarant_mobile_appTheme
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

data class AdminOrderItem(
    val productName: String,
    val quantity: Int,
    val unitPrice: Double
)

data class AdminOrder(
    val id: Long,
    val userName: String,
    val timestamp: Long,
    val items: List<AdminOrderItem>
) {
    val total: Double
        get() = items.sumOf { it.quantity * it.unitPrice }
}

@Composable
fun AdminOrdersScreen(modifier: Modifier = Modifier) {
    val orders = remember {
        mutableStateListOf(
            AdminOrder(
                101, "Hamdi Ola",
                System.currentTimeMillis() - 36_000_000,
                listOf(
                    AdminOrderItem("Margherita Pizza", 1, 8.99),
                    AdminOrderItem("Chicken Burger", 2, 6.49)
                )
            ),
            AdminOrder(
                102, "Sara Ali",
                System.currentTimeMillis() - 7_200_000,
                listOf(AdminOrderItem("Caesar Salad", 1, 5.75))
            ),
            AdminOrder(
                103, "Omar Khaled",
                System.currentTimeMillis() - 1_800_000,
                listOf(
                    AdminOrderItem("Spaghetti Bolognese", 1, 9.25),
                    AdminOrderItem("Margherita Pizza", 2, 8.99),
                    AdminOrderItem("Chicken Burger", 1, 6.49)
                )
            )
        )
        // TODO: load all orders with user names via OrderDAO + UsersDAO (join query)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF2B2B2B))
            .padding(16.dp)
    ) {
        Text(
            text = "Orders",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(orders, key = { it.id }) { order ->
                OrderCard(order = order)
            }
        }
    }
}

@Composable
fun OrderCard(order: AdminOrder, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF3A3A3A), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Order #${order.id}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "By ${order.userName} · ${formatOrderTime(order.timestamp)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFFBDBDBD)
        )
        Spacer(modifier = Modifier.height(12.dp))
        order.items.forEach { item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${item.quantity}x ${item.productName}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "$%.2f".format(Locale.US, item.quantity * item.unitPrice),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Total: $%.2f".format(Locale.US, order.total),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
        // TODO: mark order COMPLETED / CANCELLED via OrderDAO on user action
    }
}

private fun formatOrderTime(timestamp: Long): String {
    return SimpleDateFormat("MMM d, HH:mm", Locale.US).format(Date(timestamp))
}
