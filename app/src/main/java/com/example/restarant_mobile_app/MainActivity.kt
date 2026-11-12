package com.example.restarant_mobile_app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.restarant_mobile_app.cart.pages.CartActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Opens the cart screen for now so we can see it while testing.
        // TODO: replace with the real home/menu screen later.
        startActivity(Intent(this, CartActivity::class.java))
        finish()
    }
}
