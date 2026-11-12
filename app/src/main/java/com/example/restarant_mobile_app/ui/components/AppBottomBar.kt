package com.example.restarant_mobile_app.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import com.example.restarant_mobile_app.MainActivity
import com.example.restarant_mobile_app.auth.pages.ProfileActivity
import com.example.restarant_mobile_app.cart.pages.CartActivity
import com.example.restarant_mobile_app.products.pages.MenuActivity

/** The tabs shown on the bottom bar of every protected (logged-in) page. */
enum class BottomBarTab(val label: String, val icon: String) {
    HOME("Home", "🏠"),
    PRODUCTS("Products", "🍔"),
    CART("Cart", "🛒"),
    PROFILE("Profile", "👤")
}

const val EXTRA_USER_ID = "user_id"

@Composable
fun AppBottomBar(
    modifier: Modifier = Modifier,
    current: BottomBarTab,
    onTabSelected: (BottomBarTab) -> Unit
) {
    NavigationBar(modifier = modifier) {
        BottomBarTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == current,
                onClick = { if (tab != current) onTabSelected(tab) },
                icon = { Text(text = tab.icon, fontSize = 22.sp) },
                label = { Text(tab.label) },
                alwaysShowLabel = true
            )
        }
    }
}

@Composable
fun ProtectedScaffold(
    userId: Long,
    currentTab: BottomBarTab,
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit
) {
    val context = LocalContext.current
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            AppBottomBar(current = currentTab) { tab ->
                navigateToTab(context, userId, tab)
            }
        },
        content = content
    )
}

private fun navigateToTab(context: Context, userId: Long, tab: BottomBarTab) {
    val target = when (tab) {
        BottomBarTab.HOME -> MainActivity::class.java
        BottomBarTab.PRODUCTS -> MenuActivity::class.java
        BottomBarTab.CART -> CartActivity::class.java
        BottomBarTab.PROFILE -> ProfileActivity::class.java
    }
    context.startActivity(
        Intent(context, target).apply {
            putExtra(EXTRA_USER_ID, userId)
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
    )
}
