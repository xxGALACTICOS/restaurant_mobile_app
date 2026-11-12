package com.example.restarant_mobile_app.products.pages

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.restarant_mobile_app.products.pages.ui.theme.Restarant_mobile_appTheme
import com.example.restarant_mobile_app.ui.components.BottomBarTab
import com.example.restarant_mobile_app.ui.components.EXTRA_USER_ID
import com.example.restarant_mobile_app.ui.components.ProtectedScaffold

class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val userId = intent.getLongExtra(EXTRA_USER_ID, -1L)
        setContent {
            Restarant_mobile_appTheme {
                ProtectedScaffold(userId = userId, currentTab = BottomBarTab.PRODUCTS) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Restarant_mobile_appTheme {
        Greeting("Android")
    }
}