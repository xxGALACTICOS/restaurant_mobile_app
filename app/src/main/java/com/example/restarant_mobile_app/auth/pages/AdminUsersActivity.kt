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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.restarant_mobile_app.RestaurantDatabase
import com.example.restarant_mobile_app.auth.Entities.User
import com.example.restarant_mobile_app.auth.pages.ui.theme.Restarant_mobile_appTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminUsersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Restarant_mobile_appTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AdminUsersScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AdminUsersScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = remember {
        RestaurantDatabase.getInstance(context.applicationContext).userDao()
    }

    val users = remember { mutableStateListOf<User>() }

    // Loads the current list straight from the DB
    suspend fun reload() {
        val list = withContext(Dispatchers.IO) { dao.getAllUsers() }
        users.clear()
        users.addAll(list)
    }

    LaunchedEffect(Unit) { reload() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF2B2B2B))
            .padding(16.dp)
    ) {
        Text(
            text = "Users",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(users, key = { it.id }) { user ->
                UserCard(
                    user = user,
                    onBlockClick = {
                        scope.launch {
                            withContext(Dispatchers.IO) {
                                dao.updateUser(user.copy(blocked = !user.blocked))
                            }
                            reload()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onBlockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF3A3A3A), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = user.email,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFBDBDBD)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Button(
            onClick = onBlockClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (user.blocked) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
        ) {
            Text(
                text = if (user.blocked) "Unblock" else "Block",
                fontWeight = FontWeight.Bold
            )
        }
    }
}
