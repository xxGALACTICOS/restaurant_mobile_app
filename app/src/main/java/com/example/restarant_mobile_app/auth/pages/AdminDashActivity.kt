package com.example.restarant_mobile_app.auth.pages

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.restarant_mobile_app.auth.pages.ui.theme.Restarant_mobile_appTheme

class AdminDashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Restarant_mobile_appTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AdminDashScreen(
                        onProductsClick = {
                            startActivity(Intent(this, AdminProductsActivity::class.java))
                        },
                        onUsersClick = {
                            startActivity(Intent(this, AdminUsersActivity::class.java))
                        },
                        onOrdersClick = {
                            startActivity(Intent(this, AdminOrdersActivity::class.java))
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminDashScreen(
    onProductsClick: () -> Unit,
    onUsersClick: () -> Unit,
    onOrdersClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "Hello, Admin!",
            fontSize = 28.sp,
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "What would you like to manage?",
            fontSize = 14.sp,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onProductsClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Products")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onUsersClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Users")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = onOrdersClick, modifier = Modifier.fillMaxWidth()) {
            Text(text = "Orders")
        }
    }
}
