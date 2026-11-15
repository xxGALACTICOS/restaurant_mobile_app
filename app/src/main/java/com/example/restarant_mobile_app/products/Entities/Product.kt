package com.example.restarant_mobile_app.products.Entities

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val price: Double,
     val imageName: String,
     val categoryId: Int = 0,
     val extras: String = "Original,Spicy"
) {
     fun extraOptions(): List<String> =
        extras.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}