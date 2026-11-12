package com.example.restarant_mobile_app.products.Entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "products")
data class Product(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,

    val price: Double,

    // Drawable resource id (image stored on the device).
    // Can be changed to a URL later if images come from the internet.
    val imageRes: Int
)
